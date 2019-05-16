package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.MediaObject;
import javafx.stage.Window;

public class MediaInfoStage extends AbstractStage {
    public MediaInfoStage(Window windowParent, MediaObject mediaObject, int width, int height){
        super("Media Information", "MediaInfoStage.fxml", true, windowParent);

        this.setOnShowing(event -> ((MediaInfoStageController)getController()).loadMediaInfo(mediaObject, width, height));
    }
}
