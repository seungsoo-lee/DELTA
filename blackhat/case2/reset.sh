#!/bin/bash

curl -X DELETE -d '{"name":"R1"}' http://192.168.11.102:8080/wm/staticflowpusher/json

curl -X DELETE -d '{"name":"R2"}' http://192.168.11.102:8080/wm/staticflowpusher/json

curl -X DELETE -d '{"name":"R3"}' http://192.168.11.102:8080/wm/staticflowpusher/json

curl -X DELETE -d '{"name":"R4"}' http://192.168.11.102:8080/wm/staticflowpusher/json
