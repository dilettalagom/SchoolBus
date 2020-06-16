package model;

import lombok.Data;

@Data
public class VendorsDelayPojo {

    private String reason;
    private String timestamp;
    private String vendor;
    private Integer delay;

    public VendorsDelayPojo(String reason, String timestamp, String vendor, Integer delay) {
        this.reason = setReason(reason);
        this.timestamp = timestamp;
        this.vendor = vendor;
        this.delay = delay;
    }

    private String setReason(String reason){
        if(reason.equals("Heavy Traffic") || reason.equals("Mechanical Problem"))
            return reason;
        else if(!reason.equals("") && (!reason.equals("Heavy Traffic") || !reason.equals("Mechanical Problem")))
            return "Other Reason";
        else
            return "";
        }

    }


