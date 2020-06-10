public class Main {


    public static void main(String[] args) {
        final String filePath = args[0];
        final float speed = Float.parseFloat(args[1]);
        DatasetSender datasetSender = new DatasetSender(filePath,speed);
        datasetSender.startSendingData();

    }

}
