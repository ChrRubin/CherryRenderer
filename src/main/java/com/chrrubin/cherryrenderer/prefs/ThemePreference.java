package com.chrrubin.cherryrenderer.prefs;

public class ThemePreference extends BaseEnumPreference<ThemePreferenceValue> {
    public ThemePreference(){
        super("theme", ThemePreferenceValue.DEFAULT);
    }
}
