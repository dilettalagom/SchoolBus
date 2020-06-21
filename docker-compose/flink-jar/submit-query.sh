#!/usr/bin/env bash

usage() {
  cat <<EOF >&1
    Usage: $0 [options] [command]
    Builds or pushes the built-in Spark Docker image.
    Commands:
      execute-query

    Options:
      -q num_query          Execute the specific querys number (1,2,3)
      -v version            Specify which implementation will be used in query2 (split, aggregate)
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
  echo "ERROR: Select the right query_number (1,2,3)";
  usage
}

execute_query() {

  if [ $q = 1 ]
  then
    echo "\n--------------------< submitting QUERY 1 >--------------------"

    #query submit
    $FLINK_HOME/bin/flink run\
    -c query.FirstQuery \
    $FLINK_HOME/flink-jar/FlinkAnalyzer-1.0-SNAPSHOT.jar



  elif [ $q = 2 ]
  then
    echo "\n--------------------< submitting QUERY 2 >--------------------"

    if [ $version = 1 ]
    then
        echo "version with split and coGroup"
        #query submit
        $FLINK_HOME/bin/flink run\
        -c query.SecondQuerySplit \
        $FLINK_HOME/flink-jar/FlinkAnalyzer-1.0-SNAPSHOT.jar

    elif [ $version = 2 ]
    then
        echo "version only with aggregate"
        #query submit
        $FLINK_HOME/bin/flink run\
        -c query.SecondQueryAggregate \
        $FLINK_HOME/flink-jar/FlinkAnalyzer-1.0-SNAPSHOT.jar
    fi



  elif [ $q = 3 ]
  then
    echo "\n--------------------< submitting QUERY 3 >--------------------"

    $FLINK_HOME/bin/flink run\
    -c query.ThirdQuery \
    $FLINK_HOME/flink-jar/FlinkAnalyzer-1.0-SNAPSHOT.jar


  else
    wrong_query_name
  fi

}


while getopts "q:t:h:v:" o;do
	case $o in
	  q) q=$OPTARG;;
	  t) t=$OPTARG;;
	  v) v=$OPTARG;;
	  h) usage
	esac
done
shift "$((OPTIND - 1))"

version=0
if [ $q -eq 2 ] && [ -n $v ]
then
    case $v in
        ("split") version=1;;
        ("aggregate") version=2;;
        (*) wrong_query_name
    esac
fi


execute_query $q $version