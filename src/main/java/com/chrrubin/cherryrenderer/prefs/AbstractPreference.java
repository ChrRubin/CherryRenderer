package com.chrrubin.cherryrenderer.prefs;

import com.chrrubin.cherryrenderer.CherryRenderer;

import java.util.prefs.Preferences;

abstract class AbstractPreference<T> {
    private final Preferences NODE = Preferences.userNodeForPackage(CherryRenderer.class);
    private final String KEY;

    AbstractPreference(String KEY){
        this.KEY = KEY;
    }

    abstract T get();
    abstract void put(T value);
    abstract void reset();

    Preferences getPreferencesNode(){
        return NODE;
    }

    public String getKey(){
        return KEY;
    }
}
