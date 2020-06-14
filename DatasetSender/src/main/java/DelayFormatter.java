import org.apache.commons.lang.StringUtils;

public class DelayFormatter {

    public DelayFormatter(){}

    public String createDelayFormat(String stringTime){

        if (!stringTime.matches("[a-zA-Z-? :/]+")  && !stringTime.contains("?") ) {
            //contiene m && h
            if (stringTime.contains("m") && stringTime.contains("h") && !stringTime.contains("[/?:]")) {

                String[] dataArray = stringTime.split("[-+*/= ]");

                if(dataArray.length==2 && !dataArray[0].matches("[a-zA-Z-? :/]+") &&  !dataArray[1].matches("[a-zA-Z-? :/]+")) {
                    String tempMin = "";
                    String tempHou = "";
                    for (String s : dataArray) {
                        if (s.contains("m"))
                            tempMin = getNumericSubstring(s, "m");
                        else if (s.contains("h"))
                            tempHou = convertHoursToMinutes(getNumericSubstring(s, "h"));
                    }
                    return computeMean(tempMin, tempHou);
                }
                return null;

                //contiene solo m  (es: "30 min", "30min", "20-30min")
            }else if(stringTime.contains("m")){

                String[] dataArray = (getNumericSubstring(stringTime, "m")).split("[-,./+a-zA-Z:?* ]");
                if(dataArray.length == 2 && !dataArray[0].equals("") && !dataArray[1].equals("")){

                    return computeMean(dataArray[0],dataArray[1]);

                }else if(dataArray.length == 2 && dataArray[0].equals("")) {
                    return dataArray[1];
                }else
                    return dataArray[0];
                //contiene solo h
            }else if(stringTime.contains("h") && stringTime.contains("r")){

                if(stringTime.contains("hr") && StringUtils.countMatches(stringTime, "hr") == 1) {
                    if(stringTime.contains("1/2"))
                        return "30";
                    else {
                        String actual = stringTime.replace("hr", "").trim();
                        String[] dataArray = (getNumericSubstring(actual, "h")).split("[: -/+a-zA-Z?* ]");
                        if (dataArray.length == 2) {
                            return computeMean(dataArray[0], convertHoursToMinutes(dataArray[1]));
                        }else {
                            return convertHoursToMinutes(dataArray[0]);
                        }
                    }
                }else if ( StringUtils.countMatches(stringTime, "h") == 2) {
                    String[] dataArray = stringTime.split("[-/+a-zA-Z*]");
                    //TODO

                }
            }else{
                if(isNumeric(stringTime))
                    return stringTime;
                else if(stringTime.equals(""))
                    return null;
                else{
                    String[] dataArray = stringTime.split("[-/]");
                    if(!isNumeric(dataArray[0]) || !isNumeric(dataArray[1]))
                        return null;
                    return computeMean(dataArray[0],dataArray[1]);
                }
            }
        }
        return null; //drop

    }

    private String convertHoursToMinutes(String s){
        return String.valueOf(Integer.parseInt(s)*60);
    }

    private String computeMean(String a, String b){
        return String.valueOf( (int)(Math.ceil((Double.parseDouble(a) + Double.parseDouble(b)) / 2)));
    }


    private String getNumericSubstring(String s, String type){
        int pos;
        try {
            if ((pos = s.indexOf(type)) != -1) {

                return s.substring(0, pos);

            } else {
                return "0";
            }
        }catch (NumberFormatException e){
            return "0";
        }


    }


    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
