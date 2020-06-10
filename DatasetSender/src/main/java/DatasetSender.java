import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DatasetSender {

    private String csvFilePath;
    private BufferedReader bufferedReader;
    private static final String pulsarUrl = "pulsar://localhost:6650";
    private static final String topicName = "query";
    private PulsarClient pulsarClient;
    private Producer<String> producer;
    private float servingSpeed;


    public DatasetSender(String aCsvFilePath, float aServingSpeed) {
        csvFilePath = aCsvFilePath;
        servingSpeed = aServingSpeed;
        initiCSVReader();
        initPulsarClient();
    }

    private void initiCSVReader() {
        try {
            bufferedReader = new BufferedReader(new FileReader(csvFilePath));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    private void initPulsarClient() {
        try {
            pulsarClient = PulsarClient.builder()
                    .serviceUrl(pulsarUrl)
                    .build();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }


    private void sendToTopic(String value){

        try {producer = pulsarClient.newProducer(Schema.STRING)
                    .topic(topicName)
                    .create();
            producer.send(value);
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }

    }

    public void startSendingData(){

        String firstLine = readLineFromCSV();
        long firstTimestamp = extractTimeStamp(firstLine);
        sendToTopic(firstLine);
        String line;

        while ((line = readLineFromCSV())!=null) {

            long curTimestamp = extractTimeStamp(line);
            long deltaTimeStamp = computeDelta(firstTimestamp,curTimestamp);

            if (deltaTimeStamp > 0)
                addDelay(deltaTimeStamp);

            sendToTopic(line);

            firstTimestamp = curTimestamp;
        }

        String poisonedTuple = "1546300799,ffffffffffffffffffffffff,9999,9999,comment,1546300799,1,False,0,,0,Unknown,Unknown,9999,\"-\",,,,,,,,,,,,,,,,,,,";
        sendToTopic(poisonedTuple);

    }

    private void addDelay(long deltaTimeStamp) {
        try {
            Thread.sleep(deltaTimeStamp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long computeDelta(long firstTimestamp, long curTimestamp) {
        long milliSecsDelta = (curTimestamp - firstTimestamp) * 1000L; // delta in millisecs
        return (long) (milliSecsDelta / servingSpeed);
    }


    private String readLineFromCSV() {
        String line = "";
        try {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    private long extractTimeStamp(String line) {
        try {
            String[] tokens = line.split(",",-1);
            return Long.parseLong(tokens[5]);

        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
