//import org.apache.flink.runtime.state.heap.HeapPriorityQueue;



public class Main {

    public static void main(String[] args) {

        //BasicConfigurator.configure();

        final String filePath = args[0];
        final float speed = Float.parseFloat(args[1]);

        DatasetSender datasetSender = new DatasetSender(filePath,speed);
        datasetSender.startSendingData();
        readerLocal();

        //HeapPriorityQueue

    }



    public static void readerLocal(){
        String path = "./docker-compose/pulsar-jar/bus-breakdown-and-delays.csv";

        DatasetSender data = new DatasetSender(path, 100000000000L);
        data.startSendingData();


    }


}
