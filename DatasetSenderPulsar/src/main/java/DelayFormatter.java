import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DelayFormatter {

    private static DelayFormatter instance = null;
    private DelayFormatter(){}
    public static DelayFormatter getInstance(){
        if (instance == null){
            instance = new DelayFormatter();
        }
        return instance;
    }

    public String formatDelay(String inputString)
    {
        ArrayList<String> numericValues = replaceAndSplitNumbers(inputString);
        ArrayList<String> literal = splitLetters(inputString);
        String delayToSend = "";

        if(inputString.equals(""))
            delayToSend= "";

            //minutes and hours
        else if(literal.size() == 2 && inputString.contains("m") && inputString.contains("h")){
            if (numericValues.size() == 2)
                delayToSend = computeHourMinutesDelay(numericValues, literal);
            else
                delayToSend = "";
        }

        //if contains h more than once
        else if(numericValues.size() == 2 && Collections.frequency(literal, "h") == 2){
            if (numericValues.size() == 2)
                delayToSend = computeMean(convertHoursToMinutes(literal.get(0)),convertHoursToMinutes(literal.get(1)));
            else
                delayToSend = "";
        }

        //only minutes
        else if(inputString.contains("m")){
            delayToSend = computeMinutesDelay(numericValues);
        }

        //only hours
        else if(inputString.contains("h")){

            delayToSend = computeHoursDelay(numericValues, literal);

        }
        else{
            if(numericValues.size() == 2)
                delayToSend = computeMean(numericValues.get(0),numericValues.get(1));
            else if(isNumeric(inputString))
                delayToSend = inputString.replaceAll("[-/]","");
        }
        return delayToSend;
    }

    private String computeMixedMean(ArrayList<String> numericValues, ArrayList<String> literal) {
        String delayToSend;
        if(literal.get(0).contains("h"))
            delayToSend = computeMean(convertHoursToMinutes(numericValues.get(0)),numericValues.get(1));
        else
            delayToSend = computeMean(numericValues.get(0),convertHoursToMinutes(numericValues.get(1)));
        return delayToSend;
    }

    private String computeHourMinutesDelay(ArrayList<String> numericValues, ArrayList<String> literal) {
        String delayToSend;
        if(literal.get(0).contains("h"))
            delayToSend = sum(convertHoursToMinutes(numericValues.get(0)), numericValues.get(1));
        else
            delayToSend = computeMean(numericValues.get(0),convertHoursToMinutes(numericValues.get(1)));
        return delayToSend;
    }


    private String sum(String a, String b){
        long sum = Long.parseLong(a) + Long.parseLong(b);
        return String.valueOf(sum);
    }

    private ArrayList<String> replaceAndSplitNumbers(String inputString)
    {
        String pattern = "[^A-Za-z0-9]";

        inputString = inputString.replaceAll(pattern, " ");
        ArrayList<String> res = new ArrayList<>(Arrays.asList(inputString.split("[ a-zA-Z]")));
        res.removeAll(Collections.singleton(""));
        return res;
    }


    private ArrayList<String> splitLetters(String inputString)
    {
        String pattern = "[0-9]";
        ArrayList<String> res = new ArrayList<>(Arrays.asList(inputString.split(pattern)));
        res.removeAll(Collections.singleton(""));
        return res;
    }


    private boolean isNumeric(String strNum) {

        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    private String convertHoursToMinutes(String s){
        if(!isNumeric(s)) {
            return "";
        }
        return String.valueOf(Integer.parseInt(s)*60);
    }

    private String computeMean(String a, String b){
        if(!isNumeric(a) && isNumeric(b)) {
            return b;
        }
        else if(isNumeric(a) && !isNumeric(b)) {
            return a;
        }
        else if(!isNumeric(a) && !isNumeric(b))
            return "";
        else{
            return String.valueOf( (int)(Math.ceil((Double.parseDouble(a) + Double.parseDouble(b)) / 2)));
        }
    }

    private String computeMinutesDelay(List<String> numbers){
        String resultDelay;
        if(numbers.size() == 2){
            resultDelay = computeMean(numbers.get(0),numbers.get(1));
        }
        else if(numbers.size() == 1){
            resultDelay = numbers.get(0);
        }else{
            resultDelay = "error";
        }
        return resultDelay;
    }

    private String computeHoursDelay(ArrayList<String> numbers, ArrayList<String> literal){

        String resultDelay;

        if(numbers.size() == 1){
            resultDelay = convertHoursToMinutes(numbers.get(0));
        }
        else if(numbers.size() == 2 && numbers.get(0).equals("1") && numbers.get(1).equals("2")){
            resultDelay = "30";
        }
        else if(numbers.size() == 2 && numbers.get(0).equals("1") && numbers.get(1).equals("5")){
            resultDelay = "90";
        }
        else if(numbers.size() == 2){
            resultDelay = computeMixedMean(numbers, literal);
        }
        else{
            resultDelay = "error";
        }
        return resultDelay;
    }
}
