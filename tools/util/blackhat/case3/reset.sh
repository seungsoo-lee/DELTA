#!/bin/bash

CONTROLLER_IP=10.0.3.11
PORT=8080

curl -X DELETE -d '{"name":"A1"}' http://$CONTROLLER_IP:$PORT/wm/staticflowpusher/json

curl -X DELETE -d '{"name":"A2"}' http://$CONTROLLER_IP:$PORT/wm/staticflowpusher/json

curl -X DELETE -d '{"name":"B1"}' http://$CONTROLLER_IP:$PORT/wm/staticflowpusher/json

curl -X DELETE -d '{"name":"B2"}' http://$CONTROLLER_IP:$PORT/wm/staticflowpusher/json
