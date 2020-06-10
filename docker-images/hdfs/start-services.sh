#!/usr/bin/env bash
sh $HADOOP_HOME/etc/hadoop/hadoop-env.sh

rm /tmp/*.pid
service ssh start

echo "----------------------- just doing " $HOSTNAME " stuff, don't mind me -----------------------"
hdfs namenode -format
$HADOOP_HOME/sbin/start-dfs.sh

hdfs dfs -chmod -R 777 /
hdfs dfs -mkdir /results

echo "----------------------- end -----------------------"


/bin/bash