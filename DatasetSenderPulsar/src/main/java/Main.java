

public class Main {

    public static void main(String[] args) {

        //BasicConfigurator.configure();

        String filePath = args[0];
        float speed = Float.parseFloat(args[1]);
        String topic = args[2];

        DatasetSenderPulsar datasetSender = new DatasetSenderPulsar(filePath,speed, topic);
        datasetSender.startSendingData();
        //readerLocal();

    }



    public static void readerLocal(){
        String path = "./docker-compose/pulsar-jar/bus-breakdown-and-delays_cp.csv";

        DatasetSenderPulsar data = new DatasetSenderPulsar(path, 100000000000L, "dataQuery1");
        data.startSendingData();


    }


}
