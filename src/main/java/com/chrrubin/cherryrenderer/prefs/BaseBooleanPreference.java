package com.chrrubin.cherryrenderer.prefs;

public abstract class BaseBooleanPreference extends AbstractPreference<Boolean> {
    private final boolean DEFAULT;

    BaseBooleanPreference(String KEY, boolean DEFAULT){
        super(KEY);
        this.DEFAULT = DEFAULT;
    }

    @Override
    public Boolean get() {
        return getPreferencesNode().getBoolean(getKey(), DEFAULT);
    }

    @Override
    public void put(Boolean value) {
        getPreferencesNode().putBoolean(getKey(), value);
    }

    @Override
    public void reset() {
        getPreferencesNode().putBoolean(getKey(), DEFAULT);
    }
}
