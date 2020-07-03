###SENDER

####Kafka
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery1 
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery2 
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery3 

####Pulsar
cd ..
cd pulsar/jar
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery1 
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery2
sh submit-dataset.sh -f bus-breakdown-and-delays.csv -s 1000000 -t dataQuery3

####Pulsar-perf test
Pulsar consumer:
bin/pulsar-perf consume non-persistent://public/default/topic1 -r 1000000 -i 5 -n 1 [-u pulsar://pulsar-node:6650]

Pulsar producer: 
bin/pulsar-perf produce non-persistent://public/default/topic1 -r 1000000 -i 5 -n 1

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
sh submit-query.sh -q 1 (day)
sh submit-query.sh -q 2 (week)


#Producer
sh submit-dataset.sh -f bus.csv -s 100000 -t dataQuery2 

