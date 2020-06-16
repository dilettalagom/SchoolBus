package custom_function.key;

import model.VendorsDelayPojo;
import org.apache.flink.api.java.functions.KeySelector;

public class KeyByVendor implements KeySelector<VendorsDelayPojo,String> {
    @Override
    public String getKey(VendorsDelayPojo pojo) throws Exception {
        return pojo.getVendor();
    }
}
