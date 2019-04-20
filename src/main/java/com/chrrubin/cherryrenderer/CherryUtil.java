package com.chrrubin.cherryrenderer;

import javafx.util.Duration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CherryUtil {
    public static String durationToString(Duration duration){
        int intSeconds = (int)Math.floor(duration.toSeconds());
        int hours = intSeconds / 60 / 60;

        if(hours != 0){
            intSeconds -= (hours * 60 * 60);
        }

        int minutes = intSeconds / 60;
        int seconds = intSeconds - (minutes * 60);

        if(duration.greaterThanOrEqualTo(Duration.ZERO)){
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        else{
            return String.format("-%d:%02d:%02d",-hours, -minutes, -seconds);
        }
    }

    public static Duration stringToDuration(String strTime){
        String pattern = "(\\d+:)*\\d{2}:\\d{2}";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(strTime);
        if(!m.matches()){
            return null;
        }

        String[] splitTime = strTime.split(":");

        if(splitTime.length == 3){
            int hour = Integer.parseInt(splitTime[0]);
            int min = Integer.parseInt(splitTime[1]);
            int sec = Integer.parseInt(splitTime[2]);

            if(min <= 59 && sec <= 59){
                return Duration.hours(hour).add(Duration.minutes(min)).add(Duration.seconds(sec));
            }
            else{
                return null;
            }
        }
        else if(splitTime.length == 2){
            int min = Integer.parseInt(splitTime[0]);
            int sec = Integer.parseInt(splitTime[1]);

            if(min <= 59 && sec <= 59){
                return Duration.minutes(min).add(Duration.seconds(sec));
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }
}
