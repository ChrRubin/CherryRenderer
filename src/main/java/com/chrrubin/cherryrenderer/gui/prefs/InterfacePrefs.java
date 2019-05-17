package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.logging.Logger;

public class InterfacePrefs extends GridPane implements IPrefs {
    private final Logger LOGGER = Logger.getLogger(InterfacePrefs.class.getName());
    @FXML
    private ComboBox<String> themeComboBox;
    @FXML
    private ToggleGroup resizeToggleGroup;
    @FXML
    private RadioButton resizeDisabledRadioButton;
    @FXML
    private RadioButton resizeQuarterRadioButton;
    @FXML
    private RadioButton resizeHalfRadioButton;
    @FXML
    private RadioButton resizeOriginalRadioButton;
    @FXML
    private RadioButton resizeDoubleRadioButton;

    public InterfacePrefs(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prefs/InterfacePrefs.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.setClassLoader(getClass().getClassLoader());
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        themeComboBox.getItems().add("DEFAULT");
        themeComboBox.getItems().add("DARK");
        themeComboBox.setValue(CherryPrefs.Theme.get());

        resizeOriginalRadioButton.setSelected(true);
    }

    @Override
    public void resetToDefaults() {
        CherryPrefs.Theme.reset();
    }

    @Override
    public void savePreferences() {
        String theme = themeComboBox.getValue();

        CherryPrefs.Theme.put(theme);

        LOGGER.finer(CherryPrefs.Theme.KEY + " has been set to " + theme);
    }
}
