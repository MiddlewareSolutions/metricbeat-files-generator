#!/bin/sh
export jmx_url="service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-trun"
export jmx_user="tadmin"
export jmx_pwd="tadmin"
export target_path="/etc/metricbeat/modules.d/"
export jolokia_host="localhost:8040"

java -cp metricbeat-files-generator-0.2-SNAPSHOT.jar fr.middlewaresolutions.metricbeat.filesgenerator.talendesb.Client
