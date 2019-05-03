package com.chrrubin.cherryrenderer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public static String getLatestVersion() throws IOException, RuntimeException{
        URL url = new URL("https://api.github.com/repos/chrrubin/cherryrenderer/releases/latest");

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        InputStream input = connection.getInputStream();

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(new InputStreamReader(input), JsonObject.class);
        String latestVersion = jsonObject.get("tag_name").getAsString();
        if(latestVersion.isEmpty()){
            throw new RuntimeException("Could not get latest version.");
        }
        else{
            return latestVersion;
        }
    }

    public static boolean isOutdated() throws IOException, RuntimeException {
        int[] currentIntArray = semanticToIntArray(CherryPrefs.VERSION);
        int[] latestIntArray = semanticToIntArray(getLatestVersion());

        int maxLength = Math.max(currentIntArray.length, latestIntArray.length);
        for (int i = 0; i < maxLength; i++) {
            int current = i < currentIntArray.length ? currentIntArray[i] : 0;
            int latest = i < latestIntArray.length ? latestIntArray[i] : 0;
            if(latest > current){
                return true;
            }
        }
        return false;
    }

    private static int[] semanticToIntArray(String semantic){
        String[] split = semantic.split("\\.");
        int[] numbers = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            numbers[i] = Integer.valueOf(split[i]);
        }

        return numbers;
    }
}
