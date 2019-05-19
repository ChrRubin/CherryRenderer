package com.chrrubin.cherryrenderer.prefs;

public abstract class BaseBooleanPreference extends AbstractPreference<Boolean> {
    BaseBooleanPreference(String KEY, boolean DEFAULT){
        super(KEY, DEFAULT);
    }

    @Override
    public Boolean get() {
        return getPreferencesNode().getBoolean(getKey(), getDefault());
    }

    @Override
    public void put(Boolean value) {
        getPreferencesNode().putBoolean(getKey(), value);
    }

    @Override
    public void reset() {
        getPreferencesNode().putBoolean(getKey(), getDefault());
    }
}
