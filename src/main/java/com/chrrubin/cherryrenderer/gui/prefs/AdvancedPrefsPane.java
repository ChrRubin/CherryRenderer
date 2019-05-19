package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.gui.AbstractStage;
import com.chrrubin.cherryrenderer.prefs.HardwareAccelerationPreference;
import com.chrrubin.cherryrenderer.prefs.LogLevelPreference;
import com.chrrubin.cherryrenderer.prefs.LogLevelPreferenceValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdvancedPrefsPane extends GridPane implements IPrefsPane {
    private final Logger LOGGER = Logger.getLogger(AdvancedPrefsPane.class.getName());
    @FXML
    private CheckBox hardwareCheckBox;
    @FXML
    private ComboBox<LogLevelPreferenceValue> logLevelComboBox;

    private AbstractStage windowParent;
    private HardwareAccelerationPreference hardwareAccelerationPreference = new HardwareAccelerationPreference();
    private LogLevelPreference logLevelPreference = new LogLevelPreference();

    public AdvancedPrefsPane(AbstractStage windowParent){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prefs/AdvancedPrefs.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.setClassLoader(getClass().getClassLoader());
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        hardwareCheckBox.setSelected(hardwareAccelerationPreference.get());

        logLevelComboBox.setItems(FXCollections.observableArrayList(LogLevelPreferenceValue.values()));
        logLevelComboBox.setValue(logLevelPreference.get());
        logLevelComboBox.setOnAction(event -> onLogLevelSelect());

        this.windowParent = windowParent;
    }

    private void onLogLevelSelect(){
        Alert alert;
        switch(logLevelComboBox.getValue()){
            case DEBUG_PLUS:
                alert = windowParent.createInfoAlert("DEBUG_PLUS generates a more detailed debug log that includes UPnP's SOAP protocol contents." +
                        System.lineSeparator() + System.lineSeparator() +
                        "You should only enable this if DEBUG does not provide the necessary logging required.");
                alert.showAndWait();
                break;
            case ALL:
                alert = windowParent.createWarningAlert(
                        "ALL is an extremely verbose debug level that will fill up the debug logs in a matter of minutes." +
                                System.lineSeparator() + System.lineSeparator() +
                                "The only reason this exists is as a LAST RESORT ONLY." + System.lineSeparator() + System.lineSeparator() +
                                "Unless I specifically tell you to enable this logging level I will almost always ignore any bug requests accompanied by logs with this logging level.");
                alert.showAndWait();
                break;
        }
    }

    @Override
    public void resetToDefaults() {
        hardwareAccelerationPreference.reset();
        logLevelPreference.reset();
    }

    @Override
    public void savePreferences() {
        LogLevelPreferenceValue logLevel = logLevelComboBox.getValue();

        logLevelPreference.put(logLevel);
        hardwareAccelerationPreference.put(hardwareCheckBox.isSelected());

        LOGGER.finer(logLevelPreference.getKey() + " has been set to " + logLevel.name());
        LOGGER.finer(hardwareAccelerationPreference.getKey() + " has been set to " + hardwareCheckBox.isSelected());
    }

    @FXML
    private void onOpenLogLocation(){
        new Thread(() -> {
            if(Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(System.getProperty("user.home")));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);
                    Alert alert = windowParent.createErrorAlert(e.toString());
                    alert.showAndWait();
                }
            }
            else{
                LOGGER.warning("This desktop does not support opening file manager.");
                Alert alert = windowParent.createWarningAlert("This desktop does not support opening file manager.");
                alert.showAndWait();
            }
        }).start();
    }
}
