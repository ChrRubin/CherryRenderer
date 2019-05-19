package com.chrrubin.cherryrenderer.prefs;

import com.chrrubin.cherryrenderer.CherryRenderer;

import java.util.prefs.Preferences;

abstract class AbstractPreference<T> {
    private final Preferences NODE = Preferences.userNodeForPackage(CherryRenderer.class);
    private final String KEY;
    private final T DEFAULT;

    AbstractPreference(String KEY, T DEFAULT){
        this.KEY = KEY;
        this.DEFAULT = DEFAULT;
    }

    public abstract T get();
    public abstract void put(T value);
    public abstract void reset();

    Preferences getPreferencesNode(){
        return NODE;
    }

    public String getKey(){
        return KEY;
    }

    public T getDefault(){
        return DEFAULT;
    }
}
