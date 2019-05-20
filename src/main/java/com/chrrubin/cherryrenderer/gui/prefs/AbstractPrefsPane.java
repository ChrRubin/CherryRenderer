package com.chrrubin.cherryrenderer.gui.prefs;

import javafx.scene.layout.GridPane;

public abstract class AbstractPrefsPane extends GridPane {
    public abstract void resetToDefaults();
    public abstract void savePreferences();
}
