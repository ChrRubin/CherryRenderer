package com.chrrubin.cherryrenderer;

import javafx.util.Duration;

public class CherryUtil {
    public static String durationToString(Duration duration){
        int intSeconds = (int)Math.floor(duration.toSeconds());
        int hours = intSeconds / 60 / 60;
        if(hours > 0){
            intSeconds -= (hours * 60 * 60);
        }
        int minutes = intSeconds / 60;
        int seconds = intSeconds - (hours * 60 * 60) - (minutes * 60);

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
