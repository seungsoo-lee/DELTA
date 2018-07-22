#!/bin/bash

curl -X POST http://192.168.11.102:8080/wm/staticflowpusher/json -d '{"switch":"5e:3e:c4:54:44:90:2a:7a", "name":"A1", "priority":100, "eth_src":"e8:11:32:38:71:aa", "eth_dst":"e8:11:32:38:71:9a", "in_port":1, "actions":"output=2"}'

curl -X POST http://192.168.11.102:8080/wm/staticflowpusher/json -d '{"switch":"5e:3e:c4:54:44:90:2a:7a", "name":"A2", "priority":100, "eth_src":"e8:11:32:38:71:9a", "eth_dst":"e8:11:32:38:71:aa", "in_port":2, "actions":"output=1"}'

curl -X POST http://192.168.11.102:8080/wm/staticflowpusher/json -d '{"switch":"5e:3e:c4:54:44:90:2a:3a", "name":"B1", "priority":100, "eth_src":"e8:11:32:38:71:aa", "eth_dst":"e8:11:32:38:71:9a", "in_port":2}'

curl -X POST http://192.168.11.102:8080/wm/staticflowpusher/json -d '{"switch":"5e:3e:c4:54:44:90:2a:3a", "name":"B2", "priority":100, "eth_src":"e8:11:32:38:71:9a", "eth_dst":"e8:11:32:38:71:aa", "in_port":1, "actions":"output=2"}'



