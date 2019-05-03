package com.chrrubin.cherryrenderer;

import java.util.prefs.Preferences;

public class CherryPrefs {
    public static final String VERSION = "1.2";
    private static final Preferences NODE = Preferences.userNodeForPackage(CherryRenderer.class);

    public static class LogLevel{
        public static final String KEY = "logLevel";
        public static final String DEFAULT = "DEBUG";
        public static final String LOADED_VALUE = get();

        public static String get(){
            return NODE.get(KEY, DEFAULT);
        }
        public static void put(String value){
            NODE.put(KEY, value);
        }
        public static void reset(){
            NODE.put(KEY, DEFAULT);
        }
    }

    public static class FriendlyName{
        public static final String KEY = "friendlyName";
        public static final String DEFAULT = "CherryRenderer";
        public static final String LOADED_VALUE = get();

        public static String get(){
            return NODE.get(KEY, DEFAULT);
        }
        public static void put(String value){
            NODE.put(KEY, value);
        }
        public static void reset(){
            NODE.put(KEY, DEFAULT);
        }
    }

    public static class HardwareAcceleration{
        public static final String KEY = "hardwareAcceleration";
        public static final boolean DEFAULT = true;
        public static final boolean LOADED_VALUE = get();

        public static boolean get(){
            return NODE.getBoolean(KEY, DEFAULT);
        }
        public static void put(boolean value){
            NODE.putBoolean(KEY, value);
        }
        public static void reset(){
            NODE.putBoolean(KEY, DEFAULT);
        }
    }

    public static class Theme{
        public static final String KEY = "theme";
        public static final String DEFAULT = "DEFAULT";
        public static final String LOADED_VALUE = get();

        public static String get(){
            return NODE.get(KEY, DEFAULT);
        }
        public static void put(String value){
            NODE.put(KEY, value);
        }
        public static void reset(){
            NODE.put(KEY, DEFAULT);
        }
    }
}
