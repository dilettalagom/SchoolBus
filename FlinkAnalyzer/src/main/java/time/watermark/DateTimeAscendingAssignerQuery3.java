package time.watermark;

import model.VendorsDelayPojo;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import time.TimeConverter;

public class DateTimeAscendingAssignerQuery3 extends AscendingTimestampExtractor<VendorsDelayPojo> {

    @Override
    public long extractAscendingTimestamp(VendorsDelayPojo pojo) {
        return TimeConverter.getInstance().convertToEpochMilli(pojo.getTimestamp());
    }
}
