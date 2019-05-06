package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;

public class PlayerStage extends AbstractStage {

    public PlayerStage(){
        super("CherryRenderer", "PlayerStage.fxml", 640, 450);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
                loadCss("PlayerStage-Dark.css");
            }
            else{
                loadCss("PlayerStage.css");
            }

            if(CherryPrefs.AutoCheckUpdate.LOADED_VALUE) {
                ((PlayerStageController)getController()).checkUpdate();
            }
        });
    }
}
