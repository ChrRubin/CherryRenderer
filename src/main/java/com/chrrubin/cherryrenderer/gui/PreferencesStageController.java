package com.chrrubin.cherryrenderer.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;


public class PreferencesStageController implements BaseController {
    @FXML
    private GridPane rootGridPane;
    @FXML
    private TextField nameTextField;

    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    @FXML
    private void onResetDefault(){

    }

    @FXML
    private void onCancel(){

    }

    @FXML
    private void onSave(){

    }
}
