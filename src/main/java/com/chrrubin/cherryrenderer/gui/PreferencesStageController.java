package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.CherryRenderer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class PreferencesStageController implements BaseController {
    private final Logger LOGGER = Logger.getLogger(PreferencesStageController.class.getName());
    private Preferences preferences = Preferences.userNodeForPackage(CherryRenderer.class);

    @FXML
    private GridPane rootGridPane;
    @FXML
    private TextField nameTextField;
    @FXML
    private ComboBox<String> logLevelCombobox;

    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    public void prepareControls(){
        nameTextField.setText(preferences.get(CherryPrefs.FriendlyName.KEY, CherryPrefs.FriendlyName.DEFAULT));

        logLevelCombobox.getItems().add("INFO");
        logLevelCombobox.getItems().add("DEBUG");
        logLevelCombobox.getItems().add("DEBUG+");
        logLevelCombobox.getItems().add("ALL");

        logLevelCombobox.setValue(preferences.get(CherryPrefs.LogLevel.KEY, CherryPrefs.LogLevel.DEFAULT));
    }

    @FXML
    private void onLogLevelSelect(){
        switch(logLevelCombobox.getValue()){
            case "DEBUG+":
                // TODO: Show warning
                break;
            case "ALL":
                // TODO: Show VERY CLEAR WARNINGS that this will completely fill up log files. LAST RESORT ONLY.
                break;
        }
    }

    @FXML
    private void onOpenLogLocation(){
        new Thread(() -> {
            if(Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(System.getProperty("user.home")));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);
                }
            }
            else{
                LOGGER.warning("This desktop does not support opening file manager.");
            }
        }).start();
    }

    // TODO: Refactor alert dialogs

    @FXML
    private void onResetDefault(){
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to reset to default preferences? This cannot be undone!",
                ButtonType.NO, ButtonType.YES);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();

        if(alert.getResult() == ButtonType.YES){
            preferences.put(CherryPrefs.FriendlyName.KEY, CherryPrefs.FriendlyName.DEFAULT);
            preferences.put(CherryPrefs.LogLevel.KEY, CherryPrefs.LogLevel.DEFAULT);

            LOGGER.fine("User preferences have been reset to their default values");

            Alert alertOk = new Alert(Alert.AlertType.INFORMATION, "Preferences have been reset to their default values.", ButtonType.OK);
            alertOk.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alertOk.showAndWait();

            getStage().close();
        }
    }

    @FXML
    private void onCancel(){
        getStage().close();
    }

    @FXML
    private void onSave(){
        String friendlyName = nameTextField.getText().trim();
        String logLevel = logLevelCombobox.getValue();

        if(!friendlyName.equals("") && friendlyName.length() < 64) {
            preferences.put(CherryPrefs.FriendlyName.KEY, friendlyName);
            preferences.put(CherryPrefs.LogLevel.KEY, logLevel);

            LOGGER.fine("User preferences have been saved.");
            LOGGER.finer("friendlyName has been set to " + friendlyName);
            LOGGER.finer("logLevel has been set to " + logLevel);

            Alert alert = new Alert(
                    Alert.AlertType.INFORMATION,
                    "Preferences have been saved.",
                    ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();

            getStage().close();
        }
        else{
            Alert alert = new Alert(
                    Alert.AlertType.WARNING,
                    "Invalid device name entered! Please ensure it is not empty and has a max of 64 characters.",
                    ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
        }
    }
}
