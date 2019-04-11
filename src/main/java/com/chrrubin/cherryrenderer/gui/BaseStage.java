package com.chrrubin.cherryrenderer.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class BaseStage extends Stage {
    private String title;
    private String fxml;
    private double minWidth;
    private double minHeight;
    private boolean modal = false;
    private Window parent = null;
    private BaseController controller;

    public BaseStage(String title, String fxml){
        this.title = title;
        this.fxml = fxml;
    }
    public BaseStage(String title, String fxml, double minWidth, double minHeight){
        this.title = title;
        this.fxml = fxml;
        this.minHeight = minHeight;
        this.minWidth = minWidth;
    }
    public BaseStage(String title, String fxml, boolean modal, Window parent){
        this.title = title;
        this.fxml = fxml;
        this.modal = true;
        this.parent = parent;
    }

    public void prepareStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
        Parent root = loader.load();
        controller = loader.getController();

        this.setTitle(title);
        this.setScene(new Scene(root));

        if(modal){
            this.initModality(Modality.APPLICATION_MODAL);
            this.initOwner(parent);
            this.setResizable(false);
        }
        else{
            this.setMinWidth(minWidth);
            this.setMinHeight(minHeight);
            this.setOnCloseRequest(event -> System.exit(0));
        }
    }

    public BaseController getController(){
        return controller;
    }
}
