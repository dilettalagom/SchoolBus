###SENDER

####Kafka
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery1 -c spark
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery1 -c flink
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery1 -c kafkaS

####Pulsar
cd ..
cd pulsar/jar
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery1  [SOLO CON FLINK]
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery2
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery3

###CONSUMER

####Flink

sh submit-query.sh -c pulsar -q 1
sh submit-query.sh -c pulsar -q 2 -v split
sh submit-query.sh -c pulsar -q 2 -v aggregate
sh submit-query.sh -c pulsar -q 3

sh submit-query.sh -c kafka -q 1
sh submit-query.sh -c kafka -q 2 -v split
sh submit-query.sh -c kafka -q 2 -v aggregate
sh submit-query.sh -c kafka -q 3

####SparkStreaming

sh execute-query.sh -c kafka -q 2

####KafkaStreams

#Consumer
sh submit-query.sh

#Producer
sh submit-dataset.sh -f bus.csv -s 100000 -t dataQuery2 -c kafkaS