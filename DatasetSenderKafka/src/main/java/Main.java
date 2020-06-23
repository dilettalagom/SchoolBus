
public class Main {

    public static void main(String[] args) {

        String filePath = args[0];
        float speed = Float.parseFloat(args[1]);
        String topic = args[2];
        String consumerType = args[3];

        DatasetSenderKafka datasetSender = new DatasetSenderKafka(filePath, speed, topic, consumerType);
        datasetSender.startSendingData();

    }

}
