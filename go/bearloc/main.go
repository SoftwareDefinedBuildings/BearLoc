package main

import (
  "fmt"
  //import the Paho Go MQTT library
  MQTT "git.eclipse.org/gitroot/paho/org.eclipse.paho.mqtt.golang.git"
  "os"
  "time"
  "encoding/json"
  "syscall"
  "os/signal"
)

var fakeLocation = Location{"US", "CA", "Berkeley", "Leroy Ave", "Soda Hall", "494"}

type Location struct {
    Country     string      `json:country`
    State       string      `json:state`
    City        string      `json:city`
    Street      string      `json:street`
    Building    string      `json:building`
    Locale      string      `json:locale`
}

type LocRequest struct {
    Msgtype     string      `json:"msgtype"`
    Uuid        string      `json:"uuid"`
    Epoch       int64       `json:"epoch"`
    WifiTopic   string      `json:"wifitopic"`
    Backtopic   string      `json:"backtopic"`
}

type LocResult struct {
    Msgtype     string      `json:"msgtype"`
    Uuid        string      `json:"uuid"`
    Epoch       int64       `json:"epoch"`
    Result      Location    `json:"result"`
}

type DataPoint struct {
    Msgtype     string      `json:"msgtype"`
    Uuid        string      `json:"uuid"`
    Epoch       int64       `json:"epoch"`
    Data        string      `json:"data"`
}

var jobs = make(chan LocRequest , 100)

var WifiDataHandler MQTT.MessageHandler = func(client *MQTT.MqttClient, message MQTT.Message) {
    topic := message.Topic()
    msg := message.Payload()
    data := DataPoint{}
    if err := json.Unmarshal(msg, &data); err != nil {
        panic(err)
    }
    fmt.Println(data)

    select {
    case job := <-jobs:
        fmt.Println(job.Epoch)
        fmt.Println(data.Epoch)
        fmt.Println(job.WifiTopic)
        fmt.Println(topic)
        if (job.Epoch < data.Epoch && job.WifiTopic == topic) {
            location := fakeLocation//localize(data.data)
            fmt.Println(location)
            result := LocResult {
                "locResult",
                "abcdefg",
                time.Now().UnixNano() / 1000000,
                location}
            resultjson, _ := json.Marshal(result)
            fmt.Println(job.Backtopic)
            fmt.Println(string(resultjson))
            mqttPublish(client, string(job.Backtopic), string(resultjson))
        }
        fmt.Println("Handled")
    default:
        fmt.Println("Ignored")
    }
}

var locRequestHandler MQTT.MessageHandler = func(client *MQTT.MqttClient, message MQTT.Message) {
    msg := string(message.Payload())
    fmt.Println(string(msg));
    req := LocRequest{}
    if err := json.Unmarshal([]byte(msg), &req); err != nil {
        panic(err)
    }
    fmt.Println(req)

    jobs <- req

    mqttSubscribe(client, WifiDataHandler, req.WifiTopic)
}

func mqttPublish(c *MQTT.MqttClient, topic string, message string) {
    receipt := c.Publish(MQTT.QOS_ONE, topic, message)
    <-receipt
}

func mqttSubscribe(c *MQTT.MqttClient, handler MQTT.MessageHandler, topic string) {
    filter, e := MQTT.NewTopicFilter(topic, byte(MQTT.QOS_ZERO))
    if e != nil {
        fmt.Println(e)
        os.Exit(1)
    }

    if receipt, err := c.StartSubscription(handler, filter); err != nil {
        fmt.Println(err)
        os.Exit(1)
    } else {
        <-receipt
    }
}

func mqttUnSubscribe(c *MQTT.MqttClient, topic string) {
    if receipt, err := c.EndSubscription(topic); err != nil {
        fmt.Println(err)
        os.Exit(1)
    } else {
        <-receipt
    }
}

//define a function for the default message handler
var f MQTT.MessageHandler = func(client *MQTT.MqttClient, msg MQTT.Message) {
    fmt.Printf("TOPIC: %s\n", msg.Topic())
    fmt.Printf("MSG: %s\n", msg.Payload())
}

func main() {
    fmt.Println("start running")

    s := make(chan os.Signal, 1)
    signal.Notify(s, os.Interrupt, syscall.SIGTERM)
    go func() {
        <-s
        fmt.Println("signal received, exiting")
        os.Exit(0)
    }()

    //create a ClientOptions struct setting the broker address, clientid, turn
    //off trace output and set the default message handler
    opts := MQTT.NewClientOptions().AddBroker("tcp://bearloc.cal-sdb.org:52411")
    opts.SetClientId("algorithm001-server")
    // opts.SetTraceLevel(MQTT.Off)
    opts.SetDefaultPublishHandler(f)

    //create and start a client using the above ClientOptions
    c := MQTT.NewClient(opts)
    _, err := c.Start()
    if err != nil {
        panic(err)
    } else {
        fmt.Printf("Connected to %s\n", "tcp://bearloc.cal-sdb.org:52411")
    }

    mqttSubscribe(c, locRequestHandler, "algorithm001-request")

    for {
        time.Sleep(1 * time.Second)
    }

    c.Disconnect(250)
}
