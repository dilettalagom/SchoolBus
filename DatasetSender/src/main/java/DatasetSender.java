import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class DatasetSender {

    private String csvFilePath;
    private BufferedReader bufferedReader;
    private static final String pulsarUrl = "pulsar://localhost:6650";
    private static final String[] topicNames = new String[]{"dataQuery1", "dataQuery2", "dataQuery3"};
    private PulsarClient pulsarClient;
    private Producer<String> producer1, producer2, producer3;
    private float servingSpeed;
    private DelayFormatter delayFormatter;


    public DatasetSender(String csvFilePath, float servingSpeed) {
        this.csvFilePath = csvFilePath;
        this.servingSpeed = servingSpeed;
        this.delayFormatter = new DelayFormatter();
        initCSVReader();
        initPulsarClient();
    }

    private void initCSVReader() {
        try {
            this.bufferedReader = new BufferedReader(new FileReader(csvFilePath));
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

    public void startSendingData(){

        String header = readLineFromCSV();
        sendToTopic(header.split(";",-1));

        long firstTimestamp=0;
        int i=1;
        //Validating first line
        String[] firstLine = (readLineFromCSV()).split(";",-1);
        firstLine[11] = delayFormatter.createDelayFormat(firstLine[11].toLowerCase());
        if(firstLine[11]!= null) {
            firstTimestamp = extractTimeStamp(firstLine[7]);
            sendToTopic(firstLine);
            i++;
        }

        String line;
        while ((line = readLineFromCSV())!=null) {

            String[] tokens = line.split(";",-1);

            //ckeck if is a valid line  --> total row: 379412, validated row: 332571
            String validatedDelay = delayFormatter.createDelayFormat(tokens[11].toLowerCase());

            //publishing on topic only if is a valid line
            if(validatedDelay != null) {
                i++;
                tokens[11] = validatedDelay;

                long curTimestamp = extractTimeStamp(tokens[7]);
                long deltaTimeStamp = computeDelta(firstTimestamp, curTimestamp);

                if (deltaTimeStamp > 0) {

                    addDelay(deltaTimeStamp);
                    firstTimestamp = curTimestamp;
                }

                sendToTopic( tokens );
            }
        }

        System.out.println("poisonedTuple" + "total: " + i);
        String poisonedTuple = "2015-2016;1212751;Special Ed AM Run;201;W685;Poison;75420;3020-09-30T07:42:00.000;2015-09-03T08:06:00.000;Unknown;Unknown;??min??hr,ins;2;Yes;Yes;No;2015-09-03T08:06:00.000;;2015-09-03T08:06:11.000;Running Late;School-Age\n";
        sendToTopic(poisonedTuple.split(";",-1));

        try {

            producer1.close();
            producer2.close();
            producer3.close();
            pulsarClient.close();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<String> prepareStringToPublish(String[] value){
        ArrayList<String> dataToSend = new ArrayList<>();

        dataToSend.add(String.join(";", value[7],value[9],value[11]));
        dataToSend.add(String.join(";", value[5],value[7]));
        dataToSend.add(String.join(";", value[5],value[10]));

        return dataToSend;
    }

    private void sendToTopic(String[] value){

        ArrayList<String> dataToSend = prepareStringToPublish(value);

        try {
            producer1 = pulsarClient.newProducer(Schema.STRING)
                    .topic(topicNames[0])
                    .create();
            //System.out.println(dataToSend.get(0));
            producer1.send(dataToSend.get(0));
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
        try {
            producer2 = pulsarClient.newProducer(Schema.STRING)
                    .topic(topicNames[1])
                    .create();
            //System.out.println(dataToSend.get(1));
            producer2.send(dataToSend.get(1));
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
        try {
            producer3 = pulsarClient.newProducer(Schema.STRING)
                    .topic(topicNames[2])
                    .create();
            //System.out.println(dataToSend.get(2));
            producer3.send(dataToSend.get(2));
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }

    }




    private void addDelay(long deltaTimeStamp) {
        try {
            Thread.sleep(deltaTimeStamp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long convertToEpochMilli(String timestamp ){
        try {
            return  Instant.parse(timestamp+'Z').toEpochMilli();
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }

    private long computeDelta(long firstTimestamp, long curTimestamp) {
        long milliSecsDelta = (curTimestamp - firstTimestamp); // delta in millisecs
        return (long) (milliSecsDelta / servingSpeed);
    }

    private long extractTimeStamp(String timestampString) {

        return convertToEpochMilli(timestampString);
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


}
