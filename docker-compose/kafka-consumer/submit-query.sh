#!/usr/bin/env bash

usage() {
  cat <<EOF >&1
    Usage: $0 [options] [command]
    Commands:
      execute-query

    Options:
      -q type_query         Execute the specific type of query (1=day.merge, 2=week.merge, 3=day.week.join)
      -h help               Consuming dataQuery2 topic data

    Example:
    sh submit-query.sh -q 2
EOF
  exit 1
}

wrong_query_name() {
  echo "ERROR: Select the right query_number (1,2,3)";
  usage
}

execute_query() {
  if [ $q = 1 ]
  then
    echo "--------------------< consuming dataQuery2 Day>--------------------"
    java -Djava.rmi.server.hostname=kafka-consumer -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=9992 -cp KafkaStreams-1.0-SNAPSHOT.jar query.SecondQueryDay

  elif [ $q = 2 ]
  then
    echo "--------------------< consuming dataQuery2 Week>--------------------"
    java -Djava.rmi.server.hostname=kafka-consumer -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=9992 -cp KafkaStreams-1.0-SNAPSHOT.jar query.SecondQueryWeek

  elif [ $q = 3 ]
  then
    echo "--------------------< consuming dataQuery2 Join>--------------------"
    java -Djava.rmi.server.hostname=kafka-consumer -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=9992 -cp KafkaStreams-1.0-SNAPSHOT.jar query.SecondQueryJoin

  else
    wrong_query_name
  fi
}

while getopts "q:v:" o;do
	case $o in
	  q) q=$OPTARG;;
	  h) usage
	esac
done
shift "$((OPTIND - 1))"

if [ -n $q ]
then
    execute_query $q
else
    wrong_query_name
fi