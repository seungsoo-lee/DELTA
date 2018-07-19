#!/bin/bash

curl -X DELETE --header 'Accept: application/json' 'http://192.168.11.102:8181/onos/v1/flows/application/DELTA' -u karaf:karaf

curl -X DELETE --header 'Accept: application/json' 'http://192.168.11.102:8181/onos/v1/flows/application/ATTACK' -u karaf:karaf
