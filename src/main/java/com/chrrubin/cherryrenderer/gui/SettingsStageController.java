package com.chrrubin.cherryrenderer.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class SettingsStageController extends BaseController {

    @FXML
    private GridPane rootGridPane;

    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }
}
