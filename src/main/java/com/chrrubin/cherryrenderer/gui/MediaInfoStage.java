package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.stage.Window;

public class MediaInfoStage extends AbstractStage {
    public MediaInfoStage(Window windowParent){
        super("Media Information", "MediaInfoStage.fxml", true, windowParent);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }
        });
    }
}
