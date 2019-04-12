package com.chrrubin.cherryrenderer.gui;

import javafx.stage.Window;

public class PreferencesStage extends BaseStage {
    public PreferencesStage(Window windowParent){
        super("Preferences", "PreferencesStage.fxml", true, windowParent);

        setOnShown(event -> ((PreferencesStageController)getController()).prepareControls());
    }
}
