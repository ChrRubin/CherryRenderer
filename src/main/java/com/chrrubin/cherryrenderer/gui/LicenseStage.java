package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.stage.Window;

public class LicenseStage extends BaseStage {
    public LicenseStage(Window parent){
        super("License", "LicenseStage.fxml", true, parent);

        this.setOnShown(event -> {
            if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
                loadCss("DarkBase.css");
            }
        });
    }
}
