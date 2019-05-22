package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.prefs.AbstractPreference;
import com.chrrubin.cherryrenderer.prefs.AutoCheckUpdatePreference;
import com.chrrubin.cherryrenderer.prefs.AutoSaveSnapshotsPreference;
import com.chrrubin.cherryrenderer.prefs.FriendlyNamePreference;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.logging.Logger;

public class GeneralPrefsPane extends AbstractPrefsPane {
    private final Logger LOGGER = Logger.getLogger(GeneralPrefsPane.class.getName());
    @FXML
    private TextField nameTextField;
    @FXML
    private CheckBox autosaveCheckBox;
    @FXML
    private CheckBox updateCheckBox;

    private AbstractPreference<String> friendlyNamePreference = new FriendlyNamePreference();
    private AbstractPreference<Boolean> autoSaveSnapshotsPreference = new AutoSaveSnapshotsPreference();
    private AbstractPreference<Boolean> autoCheckUpdatePreference = new AutoCheckUpdatePreference();

    public GeneralPrefsPane(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prefs/GeneralPrefsPane.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.setClassLoader(getClass().getClassLoader());
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        nameTextField.setText(friendlyNamePreference.get());
        autosaveCheckBox.setSelected(autoSaveSnapshotsPreference.get());
        updateCheckBox.setSelected(autoCheckUpdatePreference.get());
    }

    @Override
    public void resetToDefaults() {
        friendlyNamePreference.reset();
        autoSaveSnapshotsPreference.reset();
        autoCheckUpdatePreference.reset();
    }

    @Override
    public void savePreferences() {
        String friendlyName = nameTextField.getText().trim();
        if(friendlyName.equals("") || friendlyName.length() > 64){
            throw new RuntimeException("Friendly name must not be empty or more than 64 characters!");
        }

        friendlyNamePreference.put(friendlyName);
        autoSaveSnapshotsPreference.put(autosaveCheckBox.isSelected());
        autoCheckUpdatePreference.put(updateCheckBox.isSelected());

        LOGGER.finer(getSavePrefsLoggingString(friendlyNamePreference, friendlyName));
        LOGGER.finer(getSavePrefsLoggingString(autoSaveSnapshotsPreference, Boolean.toString(autosaveCheckBox.isSelected())));
        LOGGER.finer(getSavePrefsLoggingString(autoCheckUpdatePreference, Boolean.toString(updateCheckBox.isSelected())));
    }
}
