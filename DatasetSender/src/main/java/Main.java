import org.apache.log4j.Logger;
import org.apache.pulsar.client.impl.*;



public class Main {

    static Logger log = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {

        //BasicConfigurator.configure();


        final String filePath = args[0];
        final float speed = Float.parseFloat(args[1]);

        DatasetSender datasetSender = new DatasetSender(filePath,speed);
        datasetSender.startSendingData();

    }


}
