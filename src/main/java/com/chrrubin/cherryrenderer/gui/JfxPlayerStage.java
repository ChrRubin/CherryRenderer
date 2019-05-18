package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;

public class JfxPlayerStage extends AbstractStage {
    public JfxPlayerStage(){
        super("CherryRenderer " + CherryUtil.VERSION + " [JFX]", "JfxPlayerStage.fxml", 640, 450);
    }
}
