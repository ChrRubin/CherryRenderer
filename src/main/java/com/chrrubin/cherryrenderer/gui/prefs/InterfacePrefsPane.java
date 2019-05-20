package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.prefs.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.util.logging.Logger;

public class InterfacePrefsPane extends AbstractPrefsPane {
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
    @FXML
    private CheckBox saveWindowSizeCheckBox;

    private AbstractPreference<ThemePreferenceValue> themePreference = new ThemePreference();
    private AbstractPreference<AutoResizePreferenceValue> autoResizePreference = new AutoResizePreference();
    private AbstractPreference<Boolean> saveWindowSizePreference = new SaveWindowSizePreference();

    public InterfacePrefsPane(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prefs/InterfacePrefsPane.fxml"));
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

        saveWindowSizeCheckBox.setSelected(saveWindowSizePreference.get());
    }

    @Override
    public void resetToDefaults() {
        themePreference.reset();
    }

    @Override
    public void savePreferences() {
        ThemePreferenceValue theme = themeComboBox.getValue();
        boolean saveWindowSize = saveWindowSizeCheckBox.isSelected();

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
        saveWindowSizePreference.put(saveWindowSize);

        LOGGER.finer(themePreference.getKey() + " has been set to " + theme.name());
        LOGGER.finer(autoResizePreference.getKey() + " has been set to " + resizeValue.name());
        LOGGER.finer(saveWindowSizePreference.getKey() + " has been set to " + saveWindowSize);
    }
}
