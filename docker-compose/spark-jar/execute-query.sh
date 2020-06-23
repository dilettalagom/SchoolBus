#!/usr/bin/env bash

usage() {
  cat <<EOF >&1
    Usage: $0 [options] [command]
    Builds or pushes the built-in Spark Docker image.
    Commands:
      execute-query

    Options:
      -q num_query          Execute the specific querys number (1,2)
      -h help               Print all the COVID19sabd informations
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
  echo "ERROR: Select the right query_number (1,2)";
  usage
}

execute_query() {

  if [ $q = 1 ]
  then
    echo "\n--------------------< submitting QUERY 1 >--------------------"

    #query submit
    $SPARK_HOME/bin/spark-submit \
    --class query.FirstQuery \
    --master "local" \
    /target/SparkStreaming-1.0-SNAPSHOT.jar


  elif [ $q = 2 ]
  then
    echo "\n--------------------< submitting QUERY 2 >--------------------"
    
    #query submit
    $SPARK_HOME/bin/spark-submit \
    --class query.SecondQuery \
    --master "local" \
    /target/SparkStreaming-1.0-SNAPSHOT.jar


  else
    wrong_query_name
  fi

}


while getopts "q:h:" o;do
	case $o in
	  q) q=$OPTARG;;
	  h) usage
	esac
done
shift "$((OPTIND - 1))"


execute_query $q