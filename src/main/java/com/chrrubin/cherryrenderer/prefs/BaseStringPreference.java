package com.chrrubin.cherryrenderer.prefs;

public abstract class BaseStringPreference extends AbstractPreference<String> {
    private final String DEFAULT;

    BaseStringPreference(String KEY, String DEFAULT){
        super(KEY);
        this.DEFAULT = DEFAULT;
    }

    @Override
    public String get() {
        return getPreferencesNode().get(getKey(), DEFAULT);
    }

    @Override
    public void put(String value) {
        getPreferencesNode().put(getKey(), value);
    }

    @Override
    public void reset() {
        getPreferencesNode().put(getKey(), DEFAULT);
    }
}
