#!/bin/bash

curl -X DELETE --header 'Accept: application/json' 'http://127.0.0.1:8181/onos/v1/flows/application/DELTA' -u karaf:karaf

curl -X DELETE --header 'Accept: application/json' 'http://127.0.0.1:8181/onos/v1/flows/application/ATTACK' -u karaf:karaf
