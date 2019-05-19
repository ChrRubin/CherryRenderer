package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.prefs.AutoResizePreference;
import com.chrrubin.cherryrenderer.prefs.AutoResizePreferenceValue;
import com.chrrubin.cherryrenderer.prefs.ThemePreference;
import com.chrrubin.cherryrenderer.prefs.ThemePreferenceValue;
import javafx.collections.FXCollections;
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
    private ComboBox<ThemePreferenceValue> themeComboBox;
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
    private AutoResizePreference autoResizePreference = new AutoResizePreference();

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

        themeComboBox.setItems(FXCollections.observableArrayList(ThemePreferenceValue.values()));
        themeComboBox.setValue(themePreference.get());

        switch (autoResizePreference.get()){
            case DISABLED:
                resizeDisabledRadioButton.setSelected(true);
                break;
            case QUARTER:
                resizeQuarterRadioButton.setSelected(true);
                break;
            case HALF:
                resizeHalfRadioButton.setSelected(true);
                break;
            case ORIGINAL:
                resizeOriginalRadioButton.setSelected(true);
                break;
            case DOUBLE:
                resizeDoubleRadioButton.setSelected(true);
        }
    }

    @Override
    public void resetToDefaults() {
        themePreference.reset();
    }

    @Override
    public void savePreferences() {
        ThemePreferenceValue theme = themeComboBox.getValue();

        RadioButton selectedResize = (RadioButton)resizeToggleGroup.getSelectedToggle();
        AutoResizePreferenceValue resizeValue;
        if(selectedResize == resizeDisabledRadioButton){
            resizeValue = AutoResizePreferenceValue.DISABLED;
        }
        else if(selectedResize == resizeQuarterRadioButton){
            resizeValue = AutoResizePreferenceValue.QUARTER;
        }
        else if(selectedResize == resizeHalfRadioButton){
            resizeValue = AutoResizePreferenceValue.HALF;
        }
        else if(selectedResize == resizeOriginalRadioButton){
            resizeValue = AutoResizePreferenceValue.ORIGINAL;
        }
        else if(selectedResize == resizeDoubleRadioButton){
            resizeValue = AutoResizePreferenceValue.DOUBLE;
        }
        else {
            throw new RuntimeException("None of the Auto Resize Window RadioButtons are selected.");
        }

        themePreference.put(theme);
        autoResizePreference.put(resizeValue);

        LOGGER.finer(themePreference.getKey() + " has been set to " + theme.name());
        LOGGER.finer(autoResizePreference.getKey() + " has been set to " + resizeValue.name());
    }
}
