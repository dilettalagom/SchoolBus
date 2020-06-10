import org.apache.log4j.BasicConfigurator;
import org.apache.pulsar.client.impl.*;
public class Main {


    public static void main(String[] args) {

        //BasicConfigurator.configure();

        final String filePath = args[0];
        final float speed = Float.parseFloat(args[1]);

        DatasetSender datasetSender = new DatasetSender(filePath,speed);
        datasetSender.startSendingData();

    }

}
