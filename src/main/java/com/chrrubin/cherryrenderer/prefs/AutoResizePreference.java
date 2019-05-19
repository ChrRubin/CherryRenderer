package com.chrrubin.cherryrenderer.prefs;

public class AutoResizePreference extends BaseEnumPreference<AutoResizePreferenceValue> {
    public AutoResizePreference(){
        super("autoResize", AutoResizePreferenceValue.ORIGINAL);
    }
}
