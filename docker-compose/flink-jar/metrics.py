import sys
import requests
import json
import time



while True:
    JOB_ID = raw_input("Enter the JOB_ID: ")
    SOURCE_ID = raw_input("Enter the SOURCE_ID: ")
    break

f = open('throughput.csv','w+', 0)

while True:
    #get new value
    resp = requests.get('http://localhost:8081/jobs/'+ JOB_ID + "/vertices/"+ SOURCE_ID + "/metrics?get=0.numRecordsOutPerSecond")
    if resp.status_code != 200:
        # This means something went wrong.
        raise ApiError('GET /tasks/ {}'.format(resp.status_code))

    json_resp = resp.json()
    print (json_resp)

    #metric & value
    str = ('{}, {}\n'.format(json_resp[0]['id'], json_resp[0]['value']))
    f.write(str)

    #wait
    time.sleep(5/10000)

#f.close();

