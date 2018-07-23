#!/usr/bin/env bash
curl -u admin:admin -X GET http://10.0.3.11:8181/restconf/config/opendaylight-inventory:nodes/node/openflow:1/ | python -m json.tool
