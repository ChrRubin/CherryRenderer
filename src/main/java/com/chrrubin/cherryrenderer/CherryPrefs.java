package com.chrrubin.cherryrenderer;

public class CherryPrefs {
    public static final String VERSION = "1.1";

    public class LogLevel{
        public static final String KEY = "logLevel";
        public static final String DEFAULT = "DEBUG";
    }

    public class FriendlyName{
        public static final String KEY = "friendlyName";
        public static final String DEFAULT = "CherryRenderer";
    }

    public class HardwareAcceleration{
        public static final String KEY = "hardwareAcceleration";
        public static final boolean DEFAULT = true;
    }
}
