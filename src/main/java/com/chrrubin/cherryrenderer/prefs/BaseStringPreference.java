package com.chrrubin.cherryrenderer.prefs;

public abstract class BaseStringPreference extends AbstractPreference<String> {
    BaseStringPreference(String KEY, String DEFAULT){
        super(KEY, DEFAULT);
    }

    @Override
    public String get() {
        return getPreferencesNode().get(getKey(), getDefault());
    }

    @Override
    public void put(String value) {
        getPreferencesNode().put(getKey(), value);
    }

    @Override
    public void reset() {
        getPreferencesNode().put(getKey(), getDefault());
    }
}
