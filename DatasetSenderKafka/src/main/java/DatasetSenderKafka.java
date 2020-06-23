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
    //private static final String[] topicNames = new String[]{"dataQuery1", "dataQuery2", "dataQuery3"};
    private String topic;
    private String consumerType;
    private static final String KafkaUri = "kafka:9092";
    private KafkaProducer producer;
    private float servingSpeed;
    private DelayFormatter delayFormatter;


    public DatasetSenderKafka(String csvFilePath, float servingSpeed, String topic, String consumerType) {
        this.csvFilePath = csvFilePath;
        this.servingSpeed = servingSpeed;
        this.topic = topic;
        this.consumerType = consumerType;
        this.delayFormatter = DelayFormatter.getInstance();
        initCSVReader();
        initKafkaProducer();

    }

    private void initCSVReader() {
        try {
            this.bufferedReader = new BufferedReader(new FileReader(csvFilePath));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    private void initKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaUri);
        props.put("group.id", "SchoolBus");
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
            sendToTopic(firstLine, consumerType);
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

                sendToTopic(tokens, consumerType);
            }
        }

        System.out.println("poisonedTuple" + "total: " + i);
        String poisonedTuple = "2015-2016;1212751;Special Ed AM Run;201;W685;Poison;75420;3020-09-30T07:42:00.000;2015-09-03T08:06:00.000;Unknown;Unknown;30;2;Yes;Yes;No;2015-09-03T08:06:00.000;;2015-09-03T08:06:11.000;Running Late;School-Age\n";
        sendToTopic(poisonedTuple.split(";",-1), consumerType);

        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);

        }
    }

    private String prepareStringToPublish(String[] value, String topic){

        String dataToSend = "";

        switch (topic){
            case "dataQuery1":
                return String.join(";", value[7],value[9],value[11]);
            case "dataQuery2":
                return String.join(";", value[5],value[7]);
            case "dataQuery3":
                return String.join(";", value[5],value[7], value[10], value[11]);
        }

        return dataToSend;
    }



    private void sendToTopic(String[] value, String consumerType){

        String dataToSend = prepareStringToPublish(value, topic);
        ProducerRecord record=null;
        if (consumerType.equals("spark")) {
            record = new ProducerRecord(topic, 0, extractTimeStamp(value[7]), topic, dataToSend);

        }else if(consumerType.equals("flink")){
            record = new ProducerRecord(topic, null, dataToSend);
        }
        producer.send(record);

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
