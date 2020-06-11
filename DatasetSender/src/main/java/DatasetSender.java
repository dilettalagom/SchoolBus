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

public class DatasetSender {

    private String csvFilePath;
    private BufferedReader bufferedReader;
    private static final String pulsarUrl = "pulsar://localhost:6650";
    private static final String topicName = "query";
    private PulsarClient pulsarClient;
    private Producer<String> producer;
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
            this.pulsarClient = PulsarClient.builder()
                    .serviceUrl(this.pulsarUrl)
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

        String header = readLineFromCSV();
        sendToTopic(header);
        System.out.println(header);

        String firstLine = readLineFromCSV();
        long firstTimestamp = extractTimeStamp(firstLine);
        String line;

        while ((line = readLineFromCSV())!=null) {

            String[] tokens = line.split(";",-1);

            //ckeck if is a valid line
            String valedatedDelay = delayFormatter.createDelayFormat(tokens[11]);

            //publishing on topic only if is a valid line
            if(valedatedDelay != null) {

                tokens[11] = valedatedDelay;

                long curTimestamp = extractTimeStamp(tokens[7]);
                long deltaTimeStamp = computeDelta(firstTimestamp, curTimestamp);

                if (deltaTimeStamp > 0)
                    addDelay(deltaTimeStamp);

                sendToTopic( String.join(",", tokens) ); //TODO: vogliamo "," o ";" ?

                firstTimestamp = curTimestamp;
            }
        }

        System.out.println("poisonedTuple");
        String poisonedTuple = "1546300799,ffffffffffffffffffffffff,9999,9999,comment,1546300799,1,False,0,,0,Unknown,Unknown,9999,\"-\",,,,,,,,,,,,,,,,,,,";
        sendToTopic(poisonedTuple);

        try {

            producer.close();
            pulsarClient.close();
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
        System.out.println(milliSecsDelta);
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

    private long extractTimeStamp(String timestampString) {

        return convertToEpochMilli(timestampString);

    }
}
