package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.stage.Window;

public class PreferencesStage extends BaseStage {
    public PreferencesStage(Window windowParent){
        super("Preferences", "PreferencesStage.fxml", true, windowParent);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }

            ((PreferencesStageController)getController()).prepareControls();
        });
    }
}
