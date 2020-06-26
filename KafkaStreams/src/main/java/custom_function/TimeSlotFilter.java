package custom_function;


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

    public boolean ckeckAM(ReasonDelayPojo pojo) {

        String[] date = pojo.getTimestamp().split("T", -1);
        LocalTime target = LocalTime.parse(date[1]);
        if (target.isAfter(slotAMstart) && target.isBefore(slotAMend)) {
            pojo.setTimestamp(date[0]);
            pojo.setTimeslot("AM : 5:00-11:59");
            return true;
        }
        return false;
    }

    public boolean ckeckPM(ReasonDelayPojo pojo){

        String[] date = pojo.getTimestamp().split("T", -1);
        LocalTime target = LocalTime.parse(date[1]);
        if(target.isAfter(slotPMstart) && target.isBefore(slotPMend)){
            pojo.setTimestamp(date[0]);
            pojo.setTimeslot("PM : 12:00-19:00");
            return true;
        }
        return false;
    }

}
