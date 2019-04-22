package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.stage.Window;

public class AboutStage extends AbstractStage {
    public AboutStage(Window windowParent){
        super("About", "AboutStage.fxml", true, windowParent);
        
        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }
            getController().prepareControls();
        });
    }
}
