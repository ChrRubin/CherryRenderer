package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.MediaObject;
import javafx.stage.Window;

public class MediaInfoStage extends AbstractStage {
    public MediaInfoStage(Window windowParent, MediaObject mediaObject){
        super("Media Information", "MediaInfoStage.fxml", true, windowParent);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }

            ((MediaInfoStageController)getController()).loadMediaInfo(mediaObject);
        });
    }
}
