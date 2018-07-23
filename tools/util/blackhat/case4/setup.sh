#!/bin/bash

CONTROLLER_IP=10.0.3.11
PORT=8181
SWITCH1_DPID=0000000000000001
SWITCH2_DPID=0000000000000002
HOST1_MAC=00:00:00:00:00:11
HOST2_MAC=00:00:00:00:00:22

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://'$CONTROLLER_IP':'$PORT'/onos/v1/flows/of:'$SWITCH1_DPID'?appId=DELTA' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":2}]}, "selector":{"criteria":[{"type":"IN_PORT", "port":1}, {"type":"ETH_SRC", "mac":"'$HOST1_MAC'"}, {"type":"ETH_DST", "mac":"'$HOST2_MAC'"}]}}'

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://'$CONTROLLER_IP':'$PORT'/onos/v1/flows/of:'$SWITCH1_DPID'?appId=DELTA' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":1}]}, "selector":{"criteria":[{"type":"IN_PORT", "port":2}, {"type":"ETH_SRC", "mac":"'$HOST2_MAC'"}, {"type":"ETH_DST", "mac":"'$HOST1_MAC'"}]}}'

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://'$CONTROLLER_IP':'$PORT'/onos/v1/flows/of:'$SWITCH2_DPID'?appId=DELTA' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":2}]}, "selector":{"criteria":[{"type":"IN_PORT", "port":1}, {"type":"ETH_SRC", "mac":"'$HOST1_MAC'"}, {"type":"ETH_DST", "mac":"'$HOST2_MAC'"}]}}'

curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://'$CONTROLLER_IP':'$PORT'/onos/v1/flows/of:'$SWITCH2_DPID'?appId=DELTA' -u karaf:karaf -d '{"priority":100, "isPermanent":true, "treatment":{"instructions":[{"type":"OUTPUT","port":1}]}, "selector":{"criteria":[{"type":"IN_PORT", "port":2}, {"type":"ETH_SRC", "mac":"'$HOST2_MAC'"}, {"type":"ETH_DST", "mac":"'$HOST1_MAC'"}]}}'
