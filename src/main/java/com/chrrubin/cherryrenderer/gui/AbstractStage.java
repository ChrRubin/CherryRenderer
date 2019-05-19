package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.prefs.ThemePreferenceValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractStage extends Stage {
    private String title;
    private String fxml;
    private double minWidth;
    private double minHeight;
    private boolean modal = false;
    private Window parent = null;
    private IController controller;
    private final Image icon = new Image("icons/cherry64.png");

    public AbstractStage(String title, String fxml, double minWidth, double minHeight){
        this.title = title;
        this.fxml = fxml;
        this.minHeight = minHeight;
        this.minWidth = minWidth;
    }
    public AbstractStage(String title, String fxml, boolean modal, Window parent){
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
        this.getIcons().add(icon);

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

        this.setOnShown(event -> {
            if(CherryUtil.LOADED_THEME == ThemePreferenceValue.DARK){
                getScene().getStylesheets().add(getClass().getClassLoader().getResource("fxml/DarkBase.css").toExternalForm());
            }
        });
    }

    public IController getController(){
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
                        "Please refer to the debug logs for more details.",
                ButtonType.OK);
        prepareAlertDialog(alert.getDialogPane());

        return alert;
    }

    public Alert createConfirmAlert(String text){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.NO, ButtonType.YES);
        prepareAlertDialog(alert.getDialogPane());

        return alert;
    }

    private void prepareAlertDialog(DialogPane dialogPane){
        dialogPane.setMinWidth(500);
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        if(CherryUtil.LOADED_THEME == ThemePreferenceValue.DARK){
            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("fxml/DarkBase.css").toExternalForm());
        }

        ((Stage)dialogPane.getScene().getWindow()).getIcons().add(icon);
    }

    public void openBrowser(String urlString, Logger logger){
        if(Desktop.isDesktopSupported()){
            try{
                Desktop.getDesktop().browse(new URI(urlString));
            }
            catch (URISyntaxException | IOException e){
                logger.log(Level.SEVERE, e.toString(), e);
                Alert alert = createErrorAlert(e.toString());
                alert.showAndWait();
            }
        }
        else{
            logger.warning("This desktop does not support opening web browser");
            Alert alert = createWarningAlert("This desktop does not support opening web browser");
            alert.showAndWait();
        }
    }
}
