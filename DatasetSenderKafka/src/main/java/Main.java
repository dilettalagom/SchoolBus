
public class Main {

    public static void main(String[] args) {

        final String filePath = args[0];
        final float speed = Float.parseFloat(args[1]);
        final String topic = args[2];

        DatasetSenderKafka datasetSender = new DatasetSenderKafka(filePath,speed,topic);
        datasetSender.startSendingData();

    }

}
