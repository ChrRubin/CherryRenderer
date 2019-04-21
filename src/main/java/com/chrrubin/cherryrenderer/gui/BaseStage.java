package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
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

    public BaseStage(String title, String fxml, double minWidth, double minHeight){
        this.title = title;
        this.fxml = fxml;
        this.minHeight = minHeight;
        this.minWidth = minWidth;
    }
    public BaseStage(String title, String fxml, boolean modal, Window parent){
        this.title = title;
        this.fxml = fxml;
        this.modal = modal;
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

    public Alert createInfoAlert(String text){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.OK);
        prepareAlertDialog(alert.getDialogPane());

        return alert;
    }

    public Alert createWarningAlert(String text){
        Alert alert = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK);
        prepareAlertDialog(alert.getDialogPane());

        return alert;
    }

    public Alert createErrorAlert(String errorName){
        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                "An error has occurred: " + System.lineSeparator() + errorName + System.lineSeparator() +
                        "Please refer to logs for more detail.",
                ButtonType.OK);
        prepareAlertDialog(alert.getDialogPane());

        return alert;
    }

    public Alert createConfirmAlert(String text){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.NO, ButtonType.YES);
        prepareAlertDialog(alert.getDialogPane());

        return alert;
    }

    public void loadCss(String cssName){
        getScene().getStylesheets().add(getClass().getClassLoader().getResource("fxml/" + cssName).toExternalForm());
    }

    private void prepareAlertDialog(DialogPane dialogPane){
        dialogPane.setMinWidth(500);
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        if(CherryPrefs.Theme.LOADED_VALUE.equals("DARK")){
            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("fxml/DarkBase.css").toExternalForm());
        }
    }
}
