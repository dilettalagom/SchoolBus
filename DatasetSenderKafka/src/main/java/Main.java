
public class Main {

    public static void main(String[] args) {

        String filePath = args[0];
        float speed = Float.parseFloat(args[1]);
        String topic = args[2];

        DatasetSenderKafka datasetSender = new DatasetSenderKafka(filePath, speed, topic);
        datasetSender.startSendingData();

    }

}
