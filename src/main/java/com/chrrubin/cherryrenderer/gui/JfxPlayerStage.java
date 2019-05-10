package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;

public class JfxPlayerStage extends AbstractStage {
    public JfxPlayerStage(){
        super("CherryRenderer " + CherryPrefs.VERSION, "JfxPlayerStage.fxml", 640, 450);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }
        });
    }
}
