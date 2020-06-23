#!/usr/bin/env bash

rm /tmp/*.pid
service ssh start

if [ $HOSTNAME = "master" ]
then
    echo "----------------------- just doing " $HOSTNAME " stuff, don't mind me -----------------------"

    $SPARK_HOME/sbin/start-all.sh
    $SPARK_HOME/sbin/start-history-server.sh
    echo "----------------------- end -----------------------"
fi

/bin/bash