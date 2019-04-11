package com.chrrubin.cherryrenderer.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class AboutStageController implements BaseController {
    @FXML
    private GridPane rootGridPane;

    @Override
    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }
}
