#!/usr/bin/env bash
file_name=`find $ONOS_ROOT -name "onos-prep-karaf"`
sed -i.bak 's/ONOS_SECURITY_MODE=\"false\"/ONOS_SECURITY_MODE=\"true\"/' $file_name
sed -i.bak 's/featuresBoot=/featuresBoot =/' $file_name
