package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.gui.prefs.AdvancedPrefs;
import com.chrrubin.cherryrenderer.gui.prefs.GeneralPrefs;
import com.chrrubin.cherryrenderer.gui.prefs.InterfacePrefs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.logging.Level;
import java.util.logging.Logger;


public class PreferencesStageController implements IController {
    private final Logger LOGGER = Logger.getLogger(PreferencesStageController.class.getName());

    @FXML
    private GridPane rootGridPane;
    @FXML
    private ListView<String> prefListView;
    @FXML
    private TitledPane prefTitledPane;
    @FXML
    private ScrollPane prefScrollPane;

    private GeneralPrefs generalPrefs = new GeneralPrefs();
    private InterfacePrefs interfacePrefs = new InterfacePrefs();
    private AdvancedPrefs advancedPrefs;

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    @FXML
    private void initialize(){
        Platform.runLater(() -> advancedPrefs = new AdvancedPrefs(getStage()));

        prefListView.getItems().add("General");
        prefListView.getItems().add("Interface");
        prefListView.getItems().add("Advanced");

        prefListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            switch (newValue){
                case "General":
                    prefTitledPane.setText("General Preferences");
                    prefScrollPane.setContent(generalPrefs);
                    break;
                case "Interface":
                    prefTitledPane.setText("Interface Preferences");
                    prefScrollPane.setContent(interfacePrefs);
                    break;
                case "Advanced":
                    prefTitledPane.setText("Advanced Preferences");
                    prefScrollPane.setContent(advancedPrefs);
                    break;
            }
        }));

        prefListView.getSelectionModel().select("General");
    }

    @FXML
    private void onResetDefault(){
        Alert alert = getStage().createConfirmAlert("Are you sure you want to reset to default preferences? This cannot be undone!");
        alert.showAndWait();

        if(alert.getResult() == ButtonType.YES){
            generalPrefs.resetToDefaults();
            interfacePrefs.resetToDefaults();
            advancedPrefs.resetToDefaults();

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
        try{
            generalPrefs.savePreferences();
            interfacePrefs.savePreferences();
            advancedPrefs.savePreferences();

            Alert alert = getStage().createInfoAlert("Preferences have been saved. It will be applied on the next program restart.");
            alert.showAndWait();
            getStage().close();
        }
        catch (RuntimeException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = getStage().createErrorAlert(e.toString());
            alert.showAndWait();
        }
    }
}
