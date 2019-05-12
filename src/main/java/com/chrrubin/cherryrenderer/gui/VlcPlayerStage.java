package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;

public class VlcPlayerStage extends AbstractStage {
    public VlcPlayerStage(){
        super("CherryRenderer " + CherryPrefs.VERSION, "VlcPlayerStage.fxml", 640, 450);
    }
}
