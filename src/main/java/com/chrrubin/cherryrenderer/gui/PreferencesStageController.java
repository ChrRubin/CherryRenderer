package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferencesStageController implements IController {
    private final Logger LOGGER = Logger.getLogger(PreferencesStageController.class.getName());

    @FXML
    private GridPane rootGridPane;
    @FXML
    private TextField nameTextField;
    @FXML
    private ComboBox<String> logLevelComboBox;
    @FXML
    private CheckBox hardwareCheckBox;
    @FXML
    private ComboBox<String> themeComboBox;

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    @Override
    public void prepareControls(){
        nameTextField.setText(CherryPrefs.FriendlyName.get());

        hardwareCheckBox.setSelected(CherryPrefs.HardwareAcceleration.get());

        logLevelComboBox.getItems().add("DEBUG");
        logLevelComboBox.getItems().add("DEBUG+");
        logLevelComboBox.getItems().add("ALL");
        logLevelComboBox.setValue(CherryPrefs.LogLevel.get());
        logLevelComboBox.setOnAction(event -> onLogLevelSelect());

        themeComboBox.getItems().add("DEFAULT");
        themeComboBox.getItems().add("DARK");
        themeComboBox.setValue(CherryPrefs.Theme.get());
    }

    private void onLogLevelSelect(){
        Alert alert;
        switch(logLevelComboBox.getValue()){
            case "DEBUG+":
                alert = getStage().createInfoAlert("DEBUG+ generates a more detailed debug log that includes UPnP's SOAP protocol contents." +
                        System.lineSeparator() + System.lineSeparator() +
                        "You should only enable this if DEBUG does not provide the necessary logging required.");
                alert.showAndWait();
                break;
            case "ALL":
                alert = getStage().createWarningAlert(
                        "ALL is an extremely verbose debug level that will fill up the debug logs in a matter of minutes." +
                        System.lineSeparator() + System.lineSeparator() +
                        "The only reason this exists is as a LAST RESORT ONLY." + System.lineSeparator() + System.lineSeparator() +
                        "Unless I specifically tell you to enable this logging level I will almost always ignore any bug requests accompanied by logs with this logging level.");
                alert.showAndWait();
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
                    Alert alert = getStage().createErrorAlert(e.toString());
                    alert.showAndWait();
                }
            }
            else{
                LOGGER.warning("This desktop does not support opening file manager.");
                Alert alert = getStage().createWarningAlert("This desktop does not support opening file manager.");
                alert.showAndWait();
            }
        }).start();
    }

    @FXML
    private void onResetDefault(){
        Alert alert = getStage().createConfirmAlert("Are you sure you want to reset to default preferences? This cannot be undone!");
        alert.showAndWait();

        if(alert.getResult() == ButtonType.YES){
            CherryPrefs.FriendlyName.reset();
            CherryPrefs.LogLevel.reset();
            CherryPrefs.HardwareAcceleration.reset();
            CherryPrefs.Theme.reset();

            LOGGER.fine("User preferences have been reset to their default values");

            Alert alertOk = getStage().createInfoAlert("Preferences have been reset to their default values.");
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
        String theme = themeComboBox.getValue();
        String logLevel = logLevelComboBox.getValue();

        if(!friendlyName.equals("") && friendlyName.length() < 64) {
            CherryPrefs.FriendlyName.put(friendlyName);
            CherryPrefs.Theme.put(theme);
            CherryPrefs.LogLevel.put(logLevel);
            CherryPrefs.HardwareAcceleration.put(hardwareCheckBox.isSelected());

            LOGGER.fine("User preferences have been saved.");
            LOGGER.finer(CherryPrefs.FriendlyName.KEY + " has been set to " + friendlyName);
            LOGGER.finer(CherryPrefs.Theme.KEY + " has been set to " + theme);
            LOGGER.finer(CherryPrefs.LogLevel.KEY + " has been set to " + logLevel);
            LOGGER.finer(CherryPrefs.HardwareAcceleration.KEY + " has been set to " + hardwareCheckBox.isSelected());

            Alert alert = getStage().createInfoAlert("Preferences have been saved.");
            alert.showAndWait();

            getStage().close();
        }
        else{
            Alert alert = getStage().createWarningAlert("Invalid device name entered! Please ensure it is not empty and has a max of 64 characters.");
            alert.showAndWait();
        }
    }
}
