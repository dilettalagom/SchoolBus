package custom_function.validator;

import model.VendorsDelayPojo;
import org.apache.flink.api.common.functions.FilterFunction;

public class VendorsDelayValidator implements FilterFunction<VendorsDelayPojo> {
    @Override
    public boolean filter(VendorsDelayPojo pojo) throws Exception {
        return pojo != null && !pojo.getVendor().equals("") && !pojo.getReason().equals("");
    }
}
