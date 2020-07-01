import sys
import requests
import json
import time
import datetime



while True:
    QUERY_ID = raw_input("Enter query: ")
    CONNECTOR = raw_input("Enter connector: ")
    JOB_ID = raw_input("Enter the JOB_ID: ")
    #SOURCE_ID = raw_input("Enter the SOURCE_ID: ")
    #SINK_ID = raw_input("Enter the SINK_ID: ")
    break

outputPath = "/opt/flink/flink-jar/throughput/"
SOURCE_ID = "cbc357ccb763df2852fee8c4fc7d55f2"
SINK_ID = "c442e7393058ea674ea088267c325315"
f = open(outputPath+CONNECTOR+'_'+QUERY_ID+'.csv', 'w+', 0)
f.write("time; num_records_out_per_second; num_records_in_per_second\n")

while True:
    #get new value
    resp_out = requests.get('http://localhost:8081/jobs/' + JOB_ID + "/vertices/" + SOURCE_ID + "/metrics?get=0.numRecordsOutPerSecond")
    resp_in = requests.get('http://localhost:8081/jobs/' + JOB_ID + "/vertices/" + SINK_ID + "/metrics?get=0.numRecordsInPerSecond")
    if resp_out.status_code != 200:
        # This means something went wrong.
        raise ApiError('GET /tasks/ {}'.format(resp_out.status_code))
    if resp_in.status_code != 200:
        # This means something went wrong.
        raise ApiError('GET /tasks/ {}'.format(resp_in.status_code))

    json_resp_out = resp_out.json()
    json_resp_in = resp_in.json()

    #metric & value
    currentDT = datetime.datetime.now()
    actual_date = currentDT.strftime("%H:%M:%S")
    str = ('{}; {} ; {}\n'.format(actual_date, json_resp_out[0]['value'],json_resp_in[0]['value']))
    f.write(str)

    #wait
    time.sleep(5)

#f.close();

#QUERY 1
#SOURCE_ID = "cbc357ccb763df2852fee8c4fc7d55f2"
#SINK_ID = "70983ee8852c92f4025146018b6ccdd0"

#QUERY 2
#SOURCE_ID = "cbc357ccb763df2852fee8c4fc7d55f2"
#SINK_ID = "cc8e1c3cccd49040a61320b5b765edeb"

#QUERY 3
#SOURCE_ID = "cbc357ccb763df2852fee8c4fc7d55f2"
#SINK_ID = "c442e7393058ea674ea088267c325315"



