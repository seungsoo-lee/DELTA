#!/bin/bash

CONTROLLER_IP=${1}
PORT=8181

curl -X DELETE --header 'Accept: application/json' 'http://'$CONTROLLER_IP':'$PORT'/onos/v1/flows/application/DELTA' -u karaf:karaf

curl -X DELETE --header 'Accept: application/json' 'http://'$CONTROLLER_IP':'$PORT'/onos/v1/flows/application/ATTACK' -u karaf:karaf
