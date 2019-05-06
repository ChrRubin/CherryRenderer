package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.stage.Window;

public class UpdaterStage extends AbstractStage {
    public UpdaterStage(Window windowParent){
        super("Updater", "UpdaterStage.fxml", true, windowParent);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }

            ((UpdaterStageController)getController()).checkForUpdate();
        });
    }

    public UpdaterStage(Window windowParent, String latestVersion){
        super("Updater", "UpdaterStage.fxml", true, windowParent);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }

            ((UpdaterStageController)getController()).skipUpdateCheck(latestVersion);
        });
    }
}
