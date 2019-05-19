package com.chrrubin.cherryrenderer.prefs;

public abstract class BaseDoublePreference extends AbstractPreference<Double>{
    BaseDoublePreference(String KEY, double DEFAULT){
        super(KEY, DEFAULT);
    }

    @Override
    public Double get() {
        return getPreferencesNode().getDouble(getKey(), getDefault());
    }

    @Override
    public void put(Double value) {
        getPreferencesNode().putDouble(getKey(), value);
    }

    @Override
    public void reset() {
        getPreferencesNode().putDouble(getKey(), getDefault());
    }
}
