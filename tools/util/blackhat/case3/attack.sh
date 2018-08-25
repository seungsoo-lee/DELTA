#!/bin/bash

CONTROLLER_IP=${1}
PORT=8080
SWITCH1_DPID=00:00:00:00:00:00:00:01
SWITCH2_DPID=00:00:00:00:00:00:00:02
HOST1_MAC=00:00:00:00:00:11
HOST2_MAC=00:00:00:00:00:22

curl -X POST http://$CONTROLLER_IP:$PORT/wm/staticflowpusher/json -d '{"switch":"'$SWITCH2_DPID'", "name":"A1", "priority":100, "eth_src":"'$HOST1_MAC'", "eth_dst":"'$HOST2_MAC'", "in_port":2, "actions":"output=1"}'
