package com.chrrubin.cherryrenderer.gui;

import javafx.stage.Window;

public class UpdaterStage extends AbstractStage {
    public UpdaterStage(Window windowParent){
        super("Updater", "UpdaterStage.fxml", true, windowParent);

        this.setOnShowing(event -> ((UpdaterStageController)getController()).checkForUpdate());
    }

    public UpdaterStage(Window windowParent, String latestVersion){
        super("Updater", "UpdaterStage.fxml", true, windowParent);

        this.setOnShowing(event -> ((UpdaterStageController)getController()).skipUpdateCheck(latestVersion));
    }
}
