#!/bin/bash
PROGNAME=$0

usage() {
  cat << EOF >&2
Usage: $PROGNAME -f <filename> -s <speedfactor> -t <topicName>
-f : filename of dataset to inject
-s : speed factor for accelerate the ingestion
     1:   real time
     10:  10 times faster
     100: 100 times faster
-t : topic name (dataQuery1, dataQuery2, dataQuery3)
-c : specify the choosen consumer type (flink, spark)

EX: sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 10000 -t dataQuery1 -c spark

EOF
  exit 1
}

while getopts f:s:t:c:r o; do
  case $o in
    (f) filename=$OPTARG;;
    (s) speedfactor=$OPTARG;;
    (t) topic=$OPTARG;;
    (c) consumer=$OPTARG;;
    (*) usage
  esac
done
shift "$((OPTIND - 1))"
echo Remaining arguments: "$@"

echo "filename: "$filename;
echo "speedfactor: "$speedfactor;
echo "topic: "$topic
echo "consumer type: "$consumer


start_ingestion() {
  java -cp DatasetSenderKafka-1.0-SNAPSHOT.jar Main $filename $speedfactor $topic $consumer
}

start_ingestion