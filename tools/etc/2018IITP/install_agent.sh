#!/usr/bin/env bash
cd ~/DELTA/agents/apps/onos/onos-1.13.1/
mvn clean install
onos-app localhost reinstall org.deltaproject.appagent ~/DELTA/agents/apps/onos/onos-1.13.1/target/delta-agent-app-onos-1.13.1-1.0-SNAPSHOT.oar
