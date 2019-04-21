package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.scene.image.Image;

public class PlayerStage extends BaseStage {

    public PlayerStage(){
        super("CherryRenderer", "PlayerStage.fxml", 640, 450);
        getIcons().add(new Image(PlayerStage.class.getClassLoader().getResourceAsStream("icons/cherry64.png")));

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
                loadCss("PlayerStage-Dark.css");
            }
            else{
                loadCss("PlayerStage.css");
            }

            ((PlayerStageController)getController()).prepareControls();
        });
    }
}
