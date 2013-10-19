BOSS
===============
# How to run the server
In terminal, go to ./server, and execute

    make run

. To kill the server, run

    make stop

<!--
The -B is just to avoid generating .py[co] files on import.


# How to use server's RPC
The server currently only support HTTP POST.
Denote server's IP as SERVER_IP. Do HTTP POST to 

    http://SERVER_IP:10080/localize

with JSON object:

    {
        'type':'localization',
        'data':{
            'wifi':{
                'timestamp':timestamp,
                'sigstr':signature_string
            }
        }
    }

in which timestamp is an integer, and signature_string is formatted as 

    "MAC Address1:SSID1:RSSI1 MAC Address2:SSID2:RSSI2 ..."

in which MAC Address are in the form of six groups of two hexadecimal digits separated by colons (:), SSID are all "UNKNOWN", and RSSI are integers with unit dBm (normally negative).

The server will return the result location as string with format:

    "[[Building Name, [x, y, z]], Confidence]"

in which Building Name is a string of building name. x, y, and z are corresponding decimal value of the coordinate, and Confidence is the decimal confidence value ranging in [0, 1]. The coordinate is determined as described in [this part](#how-to-get-coordinate)

Examples can be found in 

    ./server/testclient.py

or 

    ./mobile_audit/src/mobile/SFS/HybridLoc.java


# How to get coordinate
Open MATLAB, go to the root folder of this repository, run

    image = imread('./resources/maps/SDH/4th Floor_clean.png');
    imshow(image);

to show the map of SDH 4th floor.
The path can be changed to 

'./resources/maps/Soda/soda-4-map-tabloid.png' 

for Soda Hall 4th floor.

In the opened dialog, Click the icon of "data cursor" in the Toolbar, and click on the map to get the X and Y of the point.

# Which maps are currently used in the server

    './resources/maps/SDH/4th Floor_clean.png'

and 

    './resources/maps/Soda/soda-4-map-tabloid.png'


# How to use the Android app
Compile the mobile_audit, install it on Android phone, and click the "HybridLoc". Click "Locate Me!" to get the location. It may take couple of seconds to return the result, but the server is still being improved.
-->