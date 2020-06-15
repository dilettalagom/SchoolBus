package time;

import model.BoroDelayPojo;
import model.ReasonDelayPojo;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;

public class DateTimeAscendingAssignerQuery2 extends AscendingTimestampExtractor<ReasonDelayPojo> {

    public DateTimeAscendingAssignerQuery2() {
    }

    @Override
    public long extractAscendingTimestamp(ReasonDelayPojo reasonDelayPojo) {
        return TimeConverter.getInstance().convertToEpochMilli(reasonDelayPojo.getTimestamp());
    }


}
