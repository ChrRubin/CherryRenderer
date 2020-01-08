package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.gui.AbstractStage;
import com.chrrubin.cherryrenderer.prefs.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

public class AdvancedPrefsPane extends AbstractPrefsPane {
    private final Logger LOGGER = Logger.getLogger(AdvancedPrefsPane.class.getName());
    @FXML
    private CheckBox hardwareCheckBox;
    @FXML
    private ComboBox<LogLevelPreferenceValue> logLevelComboBox;
    @FXML
    private CheckBox forceJfxCheckBox;
    @FXML
    private TextField libVlcTextField;
    @FXML
    private CheckBox enableApiCheckBox;

    private AbstractStage windowParent;
    private AbstractPreference<Boolean> hardwareAccelerationPreference = new HardwareAccelerationPreference();
    private AbstractPreference<LogLevelPreferenceValue> logLevelPreference = new LogLevelPreference();
    private AbstractPreference<Boolean> forceJfxPreference = new ForceJfxPreference();
    private AbstractPreference<String> libVlcDirectoryPreference = new LibVlcDirectoryPreference();
    private AbstractPreference<Boolean> enableApiPreference = new EnableApiPreference();

    public AdvancedPrefsPane(AbstractStage windowParent){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prefs/AdvancedPrefsPane.fxml"));
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

        if(CherryUtil.FOUND_VLC){
            forceJfxCheckBox.setSelected(forceJfxPreference.get());
            libVlcTextField.setText(CherryUtil.VLC_NATIVE_DISCOVERY.discoveredPath());
        }
        else{
            forceJfxCheckBox.setDisable(true);
        }

        enableApiCheckBox.setSelected(enableApiPreference.get());

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
        forceJfxPreference.reset();
        libVlcDirectoryPreference.reset();
    }

    @Override
    public void savePreferences() {
        LogLevelPreferenceValue logLevel = logLevelComboBox.getValue();
        String libVlcDirectory = libVlcTextField.getText();

        logLevelPreference.put(logLevel);
        hardwareAccelerationPreference.put(hardwareCheckBox.isSelected());
        forceJfxPreference.put(forceJfxCheckBox.isSelected());
        if(libVlcDirectory != null && !libVlcDirectory.isEmpty()){
            libVlcDirectoryPreference.put(libVlcDirectory);
            LOGGER.finer(getSavePrefsLoggingString(libVlcDirectoryPreference, libVlcDirectory));
        }
        enableApiPreference.put(enableApiCheckBox.isSelected());

        LOGGER.finer(getSavePrefsLoggingString(logLevelPreference, logLevel.name()));
        LOGGER.finer(getSavePrefsLoggingString(hardwareAccelerationPreference, Boolean.toString(hardwareCheckBox.isSelected())));
        LOGGER.finer(getSavePrefsLoggingString(forceJfxPreference, Boolean.toString(forceJfxCheckBox.isSelected())));
        LOGGER.finer(getSavePrefsLoggingString(enableApiPreference, Boolean.toString(enableApiCheckBox.isSelected())));
    }

    @FXML
    private void onOpenUserHome(){
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

    @FXML
    private void onBrowseLibVlc(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select libVLC directory");

        File directory = directoryChooser.showDialog(windowParent);

        if(directory == null){
            return;
        }

        try {
            // AFAIK there's no easy way to ask vlcj to check a directory, so this is my workaround
            String oldPreference = libVlcDirectoryPreference.get();
            String directoryPath = directory.getPath();
            libVlcDirectoryPreference.put(directoryPath);
            libVlcDirectoryPreference.forceFlush();

            LOGGER.fine("Testing libVLC directory on " + directoryPath);

            NativeDiscovery testingDiscovery = new NativeDiscovery();
            boolean discovered = testingDiscovery.discover();
            Alert alert;

            if(!discovered){
                LOGGER.warning("Specified libVLC directory not found");
                alert = windowParent.createWarningAlert("libVLC directory not found! Please ensure you have selected the correct directory.");
            }
            else if(testingDiscovery.discoveredPath().equals(directoryPath)){
                LOGGER.fine("Specified libVLC directory was found");
                alert = windowParent.createInfoAlert("libVLC directory has been found and set.");
                libVlcTextField.setText(directoryPath);
            }
            else{
                String discoveredPath = testingDiscovery.discoveredPath();
                LOGGER.warning("A valid libVLC directory was found, but it is not the specified directory. The discovered directory is " + discoveredPath);
                alert = windowParent.createWarningAlert("A valid libVLC directory was found, but it was not the directory you specified." +
                        System.lineSeparator() + "The discovered libVLC directory is \"" + discoveredPath + "\"." +
                        System.lineSeparator() + "This directory will be used instead of the specified directory.");
                libVlcTextField.setText(discoveredPath);
            }
            libVlcDirectoryPreference.put(oldPreference);
            libVlcDirectoryPreference.forceFlush();
            alert.showAndWait();
        }
        catch (BackingStoreException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert errorAlert = windowParent.createErrorAlert(e.toString());
            errorAlert.showAndWait();
        }
    }
}
