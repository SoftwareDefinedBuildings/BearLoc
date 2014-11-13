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
  "strconv"
)

var fakelocation string =   `{ "type": "estimated semloc", "id": "3f2cd8d0-9831-11e3-a5e2-0800200c9a66", "target id": "1d352410-4a5e-11e3-8f96-0800200c9a66", "epoch": 1387670483532, "country": "US", "state": "CA", "city": "Berkeley", "street": "Leroy Ave", "building": "Soda Hall", "locale": "494"}`

type LocRequest struct {
    Msgtype     string      `json:"msgtype"`
    Uuid        string      `json:"uuid"`
    Epoch       int64         `json:"epoch"`
    Backtopic   string      `json:"backtopic"`
}

type LocResult struct {
    Msgtype     string      `json:"msgtype"`
    Uuid        string      `json:"uuid"`
    Epoch       int64       `json:"epoch"`
    Result      string      `json:"result"`
}

type DataPoint struct {
    Msgtype     string      `json:"msgtype"`
    Uuid        string      `json:"uuid"`
    Epoch       int64         `json:"epoch"`
    Data        string      `json:"data"`
}

type DataRequest struct {
    Msgtype     string      `json:"msgtype"`
    Uuid        string      `json:"uuid"`
    Epoch       int64       `json:"epoch"`
    Backtopic   string      `json:"backtopic"`
}

var jobs = make(chan LocRequest , 100)

var dataReceiveHandler MQTT.MessageHandler = func(client *MQTT.MqttClient, message MQTT.Message) {
    msg := message.Payload()
    data := &DataPoint{}
    if err := json.Unmarshal(msg, &data); err != nil {
        panic(err)
    }
    fmt.Println(data)

    for job, ok := <- jobs; ok; job, ok := <- jobs {
        if (job.Epoch < data.Epoch) {
            location := fakelocation//localize(data.data)
            result := LocResult {
                "locResult",
                "abcdefg",
                time.Now().UnixNano() / 1000000,
                location}
            resultjson, _ := json.Marshal(result)
            mqttPublish(client, string(job.Backtopic), string(resultjson))
            break
        }
    }
    fmt.Println("Handled")
}

var locRequestHandler MQTT.MessageHandler = func(client *MQTT.MqttClient, message MQTT.Message) {
    msg := string(message.Payload())
    fmt.Println(string(msg));
    req := &LocRequest{}
    if err := json.Unmarshal([]byte(msg), &req); err != nil {
        panic(err)
    }
    fmt.Println(req)

    jobs <- *req

    backtopic := string(req.Uuid) + "-" + strconv.FormatInt(req.Epoch, 10) + "-data"
    epoch := time.Now().UnixNano() / 1000000

    datareq := DataRequest{
        "dataRequest",
        "abcdefg",
        epoch,
        backtopic}

    reqjson, _ := json.Marshal(datareq)

    mqttSubscribe(client, dataReceiveHandler, string(backtopic))
    mqttPublish(client, "algorithm001-sensors", string(reqjson))

}

func mqttPublish(c *MQTT.MqttClient, topic string, message string) {
    receipt := c.Publish(MQTT.QOS_ONE, topic, message)
    <-receipt
}

//subscribe to the topic /go-mqtt/sample and request messages to be delivered
//at a maximum qos of zero, wait for the receipt to confirm the subscription
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

    //subscribe to the topic /go-mqtt/sample and request messages to be delivered
    //at a maximum qos of zero, wait for the receipt to confirm the subscription
    mqttSubscribe(c, locRequestHandler, "algorithm001-request")

    mqttSubscribe(c, f, "test")

    for {
        time.Sleep(1 * time.Second)
    }

    c.Disconnect(250)
}