#!/usr/bin/env bash
CONTROLLER_IP=10.0.3.11
PORT=8181

curl -s -u admin:admin -X GET http://$CONTROLLER_IP:$PORT/restconf/config/opendaylight-inventory:nodes/node/openflow:1/ | python -m json.tool
