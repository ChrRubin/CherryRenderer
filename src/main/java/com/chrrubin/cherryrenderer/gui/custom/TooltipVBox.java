package com.chrrubin.cherryrenderer.gui.custom;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TooltipVBox extends VBox {
    @FXML
    private Label tooltipLabel;

    public TooltipVBox(){
        try {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/custom/TooltipVBox.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setClassLoader(getClass().getClassLoader());
        fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        getStylesheets().add(getClass().getResource("/fxml/custom/TooltipVBox.css").toExternalForm());
    }

    public String getText() {
        return textProperty().get();
    }

    public void setText(String value) {
        textProperty().set(value);
    }

    public StringProperty textProperty() {
        return tooltipLabel.textProperty();
    }
}
