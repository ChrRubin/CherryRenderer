package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.logging.Logger;

public class GeneralPrefs extends GridPane implements IPrefs{
    private final Logger LOGGER = Logger.getLogger(GeneralPrefs.class.getName());

    @FXML
    private TextField nameTextField;
    @FXML
    private CheckBox autosaveCheckBox;
    @FXML
    private CheckBox updateCheckBox;

    public GeneralPrefs(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prefs/GeneralPrefs.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.setClassLoader(getClass().getClassLoader());
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        nameTextField.setText(CherryPrefs.FriendlyName.get());
        autosaveCheckBox.setSelected(CherryPrefs.AutoSaveSnapshots.get());
        updateCheckBox.setSelected(CherryPrefs.AutoCheckUpdate.get());
    }

    @Override
    public void resetToDefaults() {
        CherryPrefs.FriendlyName.reset();
        CherryPrefs.AutoSaveSnapshots.reset();
        CherryPrefs.AutoCheckUpdate.reset();
    }

    @Override
    public void savePreferences() {
        String friendlyName = nameTextField.getText().trim();
        if(friendlyName.equals("") || friendlyName.length() > 64){
            throw new RuntimeException("Friendly name must not be empty or more than 64 characters!");
        }

        CherryPrefs.FriendlyName.put(friendlyName);
        CherryPrefs.AutoSaveSnapshots.put(autosaveCheckBox.isSelected());
        CherryPrefs.AutoCheckUpdate.put(updateCheckBox.isSelected());

        LOGGER.finer(CherryPrefs.FriendlyName.KEY + " has been set to " + friendlyName);
        LOGGER.finer(CherryPrefs.AutoCheckUpdate.KEY + " has been set to " + updateCheckBox.isSelected());
        LOGGER.finer(CherryPrefs.AutoSaveSnapshots.KEY + " has been set to " + autosaveCheckBox.isSelected());
    }
}
