package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;

public class JfxPlayerStage extends AbstractStage {
    public JfxPlayerStage(){
        super("CherryRenderer " + CherryPrefs.VERSION + " [JFX]", "JfxPlayerStage.fxml", 640, 450);
    }
}
