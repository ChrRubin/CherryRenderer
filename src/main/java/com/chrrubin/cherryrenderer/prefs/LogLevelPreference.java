package com.chrrubin.cherryrenderer.prefs;

public class LogLevelPreference extends BaseEnumPreference<LogLevelPreferenceValue> {
    public LogLevelPreference(){
        super("logLevel", LogLevelPreferenceValue.DEBUG );
    }
}
