#!/bin/bash
echo "link appagent to target floodlight.."
ln -s $DELTA_ROOT/app-agent/floodlight/0.91/nss $FL_ROOT/src/main/java/nss


echo "modify floodlight properties file.."
FILE_BK=$FL_ROOT/src/main/resources/floodlightdefault_bk.properties
FILE=$FL_ROOT/src/main/resources/floodlightdefault.properties

k=1

cp $FILE $FILE_BK

while read line;do
	if test $k -eq 1; then
        	echo "$line,nss.delta.appagent.AppAgent" > $FILE
	else
		echo "$line" >> $FILE
	fi
        ((k++))
done < $FILE_BK

echo "modify floodlight module file.."
MODULE_FILE_BK=$FL_ROOT/src/main/resources/META-INF/services/net.floodlightcontroller.core.module.IFloodlightModule_bk
MODULE_FILE=$FL_ROOT/src/main/resources/META-INF/services/net.floodlightcontroller.core.module.IFloodlightModule

cp $MODULE_FILE $MODULE_FILE_BK

echo "nss.delta.appagent.AppAgent" > $MODULE_FILE
while read line;do
	echo "$line" >> $MODULE_FILE
done < $MODULE_FILE_BK


echo "compile floodlight controller.."
cd $FL_ROOT
ant

