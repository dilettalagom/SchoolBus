package time.watermark;

import model.BoroDelayPojo;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import time.TimeConverter;

public class DateTimeAscendingAssignerQuery1 extends AscendingTimestampExtractor<BoroDelayPojo> {

    @Override
    public long extractAscendingTimestamp(BoroDelayPojo boroDelayPojo) {
        // use createdate timestamp in millis as event time
        return Long.parseLong(boroDelayPojo.getTimestamp());
        //return TimeConverter.getInstance().convertToEpochMilli(boroDelayPojo.getTimestamp());
    }

}
