package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.prefs.AbstractPreference;
import javafx.scene.layout.GridPane;

public abstract class AbstractPrefsPane extends GridPane {
    public abstract void resetToDefaults();
    public abstract void savePreferences();

    String getSavePrefsLoggingString(AbstractPreference preference, String value){
        return preference.getKey() + " has been set to " + value;
    }
}
