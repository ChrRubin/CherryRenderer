package com.chrrubin.cherryrenderer.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class LicenseStageController implements IController {
    @FXML
    private GridPane rootGridPane;

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    @Override
    public void prepareControls(){}

    @FXML
    private void onClose(){
        getStage().close();
    }
}
