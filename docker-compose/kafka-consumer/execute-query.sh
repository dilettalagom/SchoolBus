#!/usr/bin/env bash

usage() {
  cat <<EOF >&1
    Usage: $0 [options] [command]
    Builds or pushes the built-in Spark Docker image.
    Commands:
      execute-query

    Options:
      -q num_query          Execute the specific queries number (2)
      -h help               Print all the SchoolBus informations

    Example:
    sh consume-query.sh -q 2
EOF
  exit 1
}

getopts_get_optional_argument() {
  eval next_token=\${$OPTIND}
  if [[ -n $next_token && $next_token != -* ]]; then
    OPTIND=$((OPTIND + 1))
    OPTARG=$next_token
  else
    OPTARG=""
  fi
}

wrong_query_name() {
  echo "ERROR: Select the right query_number (1,2,3)";
  usage
}

execute_query() {

  if [ $q = 1 ]
  then
    echo "--------------------< consuming dataQuery1 >--------------------"


  elif [ $q = 2 ]
  then
    echo "--------------------< consuming dataQuery2 >--------------------"

    java -cp KafkaStreams-1.0-SNAPSHOT.jar query.SecondQuery


  elif [ $q = 3 ]
  then
    echo "--------------------< consuming dataQuery3 >--------------------"


  else
    wrong_query_name
  fi

}


while getopts "q:" o;do
	case $o in
	  q) q=$OPTARG;;
	  h) usage
	esac
done
shift "$((OPTIND - 1))"



execute_query $q