#!/usr/bin/env bash

usage() {
  cat <<EOF >&1
    Usage: $0 [options] [command]
    Commands:
      execute-query

    Options:
      -h help               Consuming dataQuery2 topic data

    Example:
    sh submit-query.sh
EOF
  exit 1
}


execute_query() {
    echo "--------------------< consuming dataQuery2 >--------------------"

    java -cp KafkaStreams-1.0-SNAPSHOT.jar query.SecondQuery

}


execute_query