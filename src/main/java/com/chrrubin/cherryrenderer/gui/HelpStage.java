package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.stage.Window;

public class HelpStage extends AbstractStage {
    public HelpStage(Window windowParent){
        super("Help", "HelpStage.fxml", true, windowParent);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }
        });
    }
}
