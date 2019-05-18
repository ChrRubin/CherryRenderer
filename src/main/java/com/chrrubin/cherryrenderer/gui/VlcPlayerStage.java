package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;

public class VlcPlayerStage extends AbstractStage {
    public VlcPlayerStage(){
        super("CherryRenderer " + CherryUtil.VERSION + " [VLC]", "VlcPlayerStage.fxml", 640, 450);
    }
}
