import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Properties;

public class DatasetSenderKafka {

    private String csvFilePath;
    private BufferedReader bufferedReader;
    private static final String[] topicNames = new String[]{"dataQuery1", "dataQuery2", "dataQuery3"};
    private static final String KafkaUri = "kafka:9092";
    private KafkaProducer producer;
    private float servingSpeed;
    private DelayFormatter delayFormatter;

    //BufferedWriter out;


    public DatasetSenderKafka(String csvFilePath, float servingSpeed) {
        this.csvFilePath = csvFilePath;
        this.servingSpeed = servingSpeed;
        this.delayFormatter = DelayFormatter.getInstance();
        initCSVReader();
        initKafkaProducer();

    }

    private void initCSVReader() {
        try {
            //this.out = new BufferedWriter(new FileWriter("dataQ1.txt", true));
            this.bufferedReader = new BufferedReader(new FileReader(csvFilePath));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaUri);
        props.put("group.id", "test");
        props.put("key.serializer", StringSerializer.class);
        props.put("value.serializer", StringSerializer.class);
        producer = new KafkaProducer(props);
    }

    public void startSendingData(){

        String header = readLineFromCSV();
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
        String poisonedTuple = "2015-2016;1212751;Special Ed AM Run;201;W685;Poison;75420;3020-09-30T07:42:00.000;2015-09-03T08:06:00.000;Unknown;Unknown;30;2;Yes;Yes;No;2015-09-03T08:06:00.000;;2015-09-03T08:06:11.000;Running Late;School-Age\n";
        sendToTopic(poisonedTuple.split(";",-1));

        try {
            bufferedReader.close();
            //out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);

        }
        //producer.close();

    }

    private ArrayList<String> prepareStringToPublish(String[] value){
        ArrayList<String> dataToSend = new ArrayList<>();

        //value[7] = String.valueOf(extractTimeStamp(value[7]));
        dataToSend.add(String.join(";", value[7],value[9],value[11]));
        dataToSend.add(String.join(";", value[5],value[7]));
        dataToSend.add(String.join(";", value[5],value[7], value[10], value[11]));

        return dataToSend;
    }


    private void sendToTopic(String[] value){

        String s = String.join(";",value);
        ProducerRecord dataToSend = new ProducerRecord(topicNames[0],null,s);
        producer.send(dataToSend);

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
