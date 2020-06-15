package time;

import model.ReasonDelayPojo;
import java.time.LocalTime;

public class TimeSlotFilter {

    private final LocalTime slotAMstart = LocalTime.parse("05:00:00.000");
    private final LocalTime slotAMend = LocalTime.parse("11:59:00.000");
    private final LocalTime slotPMstart = LocalTime.parse("12:00:00.000");
    private final LocalTime slotPMend = LocalTime.parse("19:00:00.000");

    private static TimeSlotFilter instance = null;

    private TimeSlotFilter(){ }

    public static TimeSlotFilter getInstance(){
        if (instance == null){
            instance = new TimeSlotFilter();
        }
        return instance;
    }

    public boolean ckeckAM(ReasonDelayPojo pojo){

        LocalTime target = LocalTime.parse(pojo.getTimestamp().split("T", -1)[1]);
        boolean isAM = target.isAfter(slotAMstart) && target.isBefore(slotAMend);
        if(isAM)
            pojo.setTimeSlot("AM");
        return isAM;
    }

    public boolean ckeckPM(ReasonDelayPojo pojo){

        LocalTime target = LocalTime.parse(pojo.getTimestamp().split("T", -1)[1]);

        boolean isPM = target.isAfter(slotPMstart) && target.isBefore(slotPMend);
        if(isPM)
            pojo.setTimeSlot("PM");
        return isPM;
    }

}
