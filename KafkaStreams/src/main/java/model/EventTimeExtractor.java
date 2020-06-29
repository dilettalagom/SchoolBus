package model;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import java.time.Instant;
import java.time.format.DateTimeParseException;


public class EventTimeExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long l) {

        String line = (String) record.value();
        String[] split = line.split(";", -1);

        if (split[1] != null) {
            return convertToEpochMilli(split[1]);
        }

        return 0L;
    }


    private long convertToEpochMilli (String timestampString){
        try {
            return Instant.parse(timestampString + 'Z').toEpochMilli();
        } catch (DateTimeParseException e) {
            return System.currentTimeMillis();
        }
    }

}

