#!/usr/bin/env bash

usage() {
  cat <<EOF >&1
    Usage: $0 [options] [command]
    Commands:
      execute-query

    Options:
      -q type_query         Execute the specific type of query (1=day, 2=week)
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
    java -cp KafkaStreams-1.0-SNAPSHOT.jar query.SecondQueryDay

  elif [ $q = 2 ]
  then
      echo "--------------------< consuming dataQuery2 Week>--------------------"
      java -cp KafkaStreams-1.0-SNAPSHOT.jar query.SecondQueryWeek

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