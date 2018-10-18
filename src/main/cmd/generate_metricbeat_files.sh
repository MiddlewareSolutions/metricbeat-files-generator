#!/bin/sh
URL=service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-trun
USER=tadmin
PWD=tadmin
TARGET=/etc/metricbeat/modules.d/

java -jar metricbeat-files-generator-0.0.1-SNAPSHOT.jar $URL $USER $PWD $TARGET
