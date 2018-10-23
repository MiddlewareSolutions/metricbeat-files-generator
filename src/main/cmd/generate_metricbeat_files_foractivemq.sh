#!/bin/sh
export jmx_url=service:jmx:rmi:///jndi/rmi://localhost:11616/jmxrmi
export jmx_user=admin
export jmx_pwd=admin
export target_path=/etc/metricbeat/modules.d/
export jolokia_host=localhost:8161

java -cp metricbeat-files-generator-0.2-SNAPSHOT.jar fr.middlewaresolutions.metricbeat.filesgenerator.activemq.Client
