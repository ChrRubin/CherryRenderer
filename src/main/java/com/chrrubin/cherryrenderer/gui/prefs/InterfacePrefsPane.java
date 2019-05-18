package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.prefs.ThemePreference;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.logging.Logger;

public class InterfacePrefsPane extends GridPane implements IPrefsPane {
    private final Logger LOGGER = Logger.getLogger(InterfacePrefsPane.class.getName());
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

    private ThemePreference themePreference = new ThemePreference();

    public InterfacePrefsPane(){
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
        themeComboBox.setValue(themePreference.get());

        resizeOriginalRadioButton.setSelected(true);
    }

    @Override
    public void resetToDefaults() {
        themePreference.reset();
    }

    @Override
    public void savePreferences() {
        String theme = themeComboBox.getValue();

        themePreference.put(theme);

        LOGGER.finer(themePreference.getKey() + " has been set to " + theme);
    }
}
