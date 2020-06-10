#!/bin/bash
PROGNAME=$0

usage() {
  cat << EOF >&2
Usage: $PROGNAME -f <filename> -s <speedfactor>
-f : filename of dataset to inject
-s : speed factor for accelerate the ingestion
     1:   real time
     10:  10 times faster
     100: 100 times faster
EOF
  exit 1
}

while getopts f:s:r o; do
  case $o in
    (f) filename=$OPTARG;;
    (s) speedfactor=$OPTARG;;
    (*) usage
  esac
done
shift "$((OPTIND - 1))"
echo Remaining arguments: "$@"

echo "filename: "$filename;
echo "speedfactor: "$speedfactor;

start_ingestion() {
  java -cp DatasetSender-1.0-SNAPSHOT.jar Main $filename $speedfactor
}
start_ingestion