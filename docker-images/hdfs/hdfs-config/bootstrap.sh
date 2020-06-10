#!/bin/bash
: ${HADOOP_PREFIX:=/usr/local/hadoop}
sudo $HADOOP_HOME/etc/hadoop/hadoop-env.sh

rm /tmp/*.pid
service ssh start
#$HADOOP_PREFIX/sbin/start-dfs.sh

if [ $HOSTNAME = "master" ]
  then
      chmod 777 /target/start-services.sh
      sh /target/start-services.sh
fi

# Launch bash console
/bin/bash