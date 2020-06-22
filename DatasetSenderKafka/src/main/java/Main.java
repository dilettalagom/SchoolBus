
public class Main {

    public static void main(String[] args) {

        final String filePath = args[0];
        final float speed = Float.parseFloat(args[1]);

        DatasetSenderKafka datasetSender = new DatasetSenderKafka(filePath,speed);
        datasetSender.startSendingData();

    }



    /*public static void readerLocal(){
        String path = "./docker-compose/pulsar-jar/bus-breakdown-and-delays_cp.csv";

        DatasetSender data = new DatasetSender(path, 100000000000L);
        data.startSendingData();


    }*/


}
