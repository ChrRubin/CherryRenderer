package com.chrrubin.cherryrenderer.gui.custom;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class LoadingVBox extends VBox {
    @FXML
    private Label waitingLabel;

    public LoadingVBox(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/custom/LoadingVBox.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.setClassLoader(getClass().getClassLoader());
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setWaitingConnection(){
        waitingLabel.setText("Awaiting connection from control point app...");
    }

    public void setWaitingVideo(){
        waitingLabel.setText("Video is loading...");
    }
}
