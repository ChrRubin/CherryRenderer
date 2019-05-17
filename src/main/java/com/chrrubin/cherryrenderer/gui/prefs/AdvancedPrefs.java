package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.gui.AbstractStage;
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

public class AdvancedPrefs extends GridPane implements IPrefs {
    private final Logger LOGGER = Logger.getLogger(AdvancedPrefs.class.getName());

    @FXML
    private CheckBox hardwareCheckBox;
    @FXML
    private ComboBox<String> logLevelComboBox;

    private AbstractStage windowParent;

    public AdvancedPrefs(AbstractStage windowParent){
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

        hardwareCheckBox.setSelected(CherryPrefs.HardwareAcceleration.get());

        logLevelComboBox.getItems().add("DEBUG");
        logLevelComboBox.getItems().add("DEBUG+");
        logLevelComboBox.getItems().add("ALL");
        logLevelComboBox.setValue(CherryPrefs.LogLevel.get());

        this.windowParent = windowParent;
    }

    @Override
    public void resetToDefaults() {
        CherryPrefs.HardwareAcceleration.reset();
        CherryPrefs.LogLevel.reset();
    }

    @Override
    public void savePreferences() {
        String logLevel = logLevelComboBox.getValue();

        CherryPrefs.LogLevel.put(logLevel);
        CherryPrefs.HardwareAcceleration.put(hardwareCheckBox.isSelected());

        LOGGER.finer(CherryPrefs.LogLevel.KEY + " has been set to " + logLevel);
        LOGGER.finer(CherryPrefs.HardwareAcceleration.KEY + " has been set to " + hardwareCheckBox.isSelected());
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
