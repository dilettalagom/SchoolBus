package time.watermark;

import model.ReasonDelayPojo;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import time.TimeConverter;

public class DateTimeAscendingAssignerQuery2 extends AscendingTimestampExtractor<ReasonDelayPojo> {


    @Override
    public long extractAscendingTimestamp(ReasonDelayPojo reasonDelayPojo) {
        return TimeConverter.getInstance().convertToEpochMilli(reasonDelayPojo.getTimestamp());
    }


}
