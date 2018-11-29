#!/usr/bin/env bash
#"buck clean && buck build onos" is required after executing this script

file_name=`find $ONOS_ROOT -name "onos-prep-karaf"`
opt=$1
if [[ $opt == *"enable"* ]]; then
    sed -i.bak 's/ONOS_SECURITY_MODE=\"false\"/ONOS_SECURITY_MODE=\"true\"/' $file_name
    sed -i.bak 's/featuresBoot=/featuresBoot =/' $file_name
elif [[ $opt == *"disable"* ]]; then
    sed -i.bak 's/ONOS_SECURITY_MODE=\"true\"/ONOS_SECURITY_MODE=\"false\"/' $file_name 
fi