package com.chrrubin.cherryrenderer.gui;

import javafx.scene.image.Image;

public class PlayerStage extends BaseStage {

    public PlayerStage(){
        super("CherryRenderer", "PlayerStage.fxml", 640, 450);
        getIcons().add(new Image(PlayerStage.class.getClassLoader().getResourceAsStream("icons/cherry64.png")));
    }
}
