#!/bin/bash

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://192.168.11.102:8181/onos/v1/flows/of:5e3ec45444902a7a?appId=DELTA' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":2}]}, "selector":{"criteria":[{"type":"IN_PORT", "port":1}, {"type":"ETH_SRC", "mac":"e8:11:32:38:71:aa"}, {"type":"ETH_DST", "mac":"e8:11:32:38:71:9a"}]}}'

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://192.168.11.102:8181/onos/v1/flows/of:5e3ec45444902a7a?appId=DELTA' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":1}]}, "selector":{"criteria":[{"type":"IN_PORT", "port":2}, {"type":"ETH_SRC", "mac":"e8:11:32:38:71:9a"}, {"type":"ETH_DST", "mac":"e8:11:32:38:71:aa"}]}}'

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://192.168.11.102:8181/onos/v1/flows/of:5e3ec45444902a3a?appId=DELTA' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":2}]}, "selector":{"criteria":[{"type":"IN_PORT", "port":1}, {"type":"ETH_SRC", "mac":"e8:11:32:38:71:9a"}, {"type":"ETH_DST", "mac":"e8:11:32:38:71:aa"}]}}'

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://192.168.11.102:8181/onos/v1/flows/of:5e3ec45444902a3a?appId=DELTA' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":1}]}, "selector":{"criteria":[{"type":"IN_PORT", "port":2}, {"type":"ETH_SRC", "mac":"e8:11:32:38:71:aa"}, {"type":"ETH_DST", "mac":"e8:11:32:38:71:9a"}]}}'
