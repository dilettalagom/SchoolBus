#!/usr/bin/env bash

usage() {
  cat <<EOF >&1
    Usage: $0 [options] [command]
    Builds or pushes the built-in Spark Docker image.
    Commands:
      execute-query

    Options:
      -q num_query          Execute the specific querys number (1,2,3)
      -t kmeans_type        Specify which k-means implementation will be used (naive, mllib, ml)
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

    #show results in hdfs
    #hdfs dfs -ls /results/firstQuery




  elif [ $q = 2 ]
  then
    echo "\n--------------------< submitting QUERY 2 >--------------------"

    #query submit
    $FLINK_HOME/bin/flink run\
    -c query.SecondQuery \
    $FLINK_HOME/flink-jar/FlinkAnalyzer-1.0-SNAPSHOT.jar

    #show results in hdfs
    #hdfs dfs -ls /results/secondQuery





  elif [ $q = 3 ]
  then
    echo "\n--------------------< submitting QUERY 3 >--------------------"

    $FLINK_HOME/bin/flink run\
    -c query.ThirdQuery \
    $FLINK_HOME/flink-jar/FlinkAnalyzer-1.0-SNAPSHOT.jar

    #hdfs dfs -ls /results/thirdQuery

  else
    wrong_query_name
  fi

}


while getopts "q:t:h:" o;do
	case $o in
	  q) q=$OPTARG;;
	  t) t=$OPTARG;;
	  h) usage
	esac
done
shift "$((OPTIND - 1))"

kmeans_type=""
if [ $q -eq 3 ] && [ -n $t ]
then
    case $t in
        ("naive") kmeans_type="naive";;
        ("mllib") kmeans_type="mllib";;
        ("ml") kmeans_type="ml";;
        (*) wrong_query_name
    esac
fi

execute_query $q $kmeans_type