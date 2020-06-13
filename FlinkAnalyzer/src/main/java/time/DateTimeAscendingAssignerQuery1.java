package time;

import model.BoroDelayPojo;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;

public class DateTimeAscendingAssignerQuery1 extends AscendingTimestampExtractor<BoroDelayPojo> {

    public DateTimeAscendingAssignerQuery1() {
    }

    @Override
    public long extractAscendingTimestamp(BoroDelayPojo boroDelayPojo) {
        // use createdate timestamp in millis as event time
        return TimeConverter.getInstance().convertToEpochMilli(boroDelayPojo.getTimestamp());
    }

}
