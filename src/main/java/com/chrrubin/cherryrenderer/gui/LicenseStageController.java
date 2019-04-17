package com.chrrubin.cherryrenderer.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class LicenseStageController implements BaseController {
    @FXML
    private GridPane rootGridPane;

    @Override
    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    @FXML
    private void onClose(){
        getStage().close();
    }
}
