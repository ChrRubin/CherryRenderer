package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.gui.prefs.AdvancedPrefsPane;
import com.chrrubin.cherryrenderer.gui.prefs.GeneralPrefsPane;
import com.chrrubin.cherryrenderer.gui.prefs.InterfacePrefsPane;
import com.chrrubin.cherryrenderer.prefs.ThemePreferenceValue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    private GeneralPrefsPane generalPrefsPane = new GeneralPrefsPane();
    private InterfacePrefsPane interfacePrefsPane = new InterfacePrefsPane();
    private AdvancedPrefsPane advancedPrefsPane;

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    @FXML
    private void initialize(){
        Platform.runLater(() -> advancedPrefsPane = new AdvancedPrefsPane(getStage()));

        prefListView.getItems().add("General");
        prefListView.getItems().add("Interface");
        prefListView.getItems().add("Advanced");

        prefListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            switch (newValue){
                case "General":
                    prefTitledPane.setText("General Preferences");
                    prefScrollPane.setContent(generalPrefsPane);
                    break;
                case "Interface":
                    prefTitledPane.setText("Interface Preferences");
                    prefScrollPane.setContent(interfacePrefsPane);
                    break;
                case "Advanced":
                    prefTitledPane.setText("Advanced Preferences");
                    prefScrollPane.setContent(advancedPrefsPane);
                    break;
            }
        }));

        Image generalImage;
        Image interfaceImage;
        Image advancedImage;
        if (CherryUtil.LOADED_THEME == ThemePreferenceValue.DARK) {
            generalImage = new Image("icons/grey/pref.png");
            interfaceImage = new Image("icons/grey/interface.png");
            advancedImage = new Image("icons/grey/advanced.png");
        }
        else {
            generalImage = new Image("icons/pref.png");
            interfaceImage = new Image("icons/interface.png");
            advancedImage = new Image("icons/advanced.png");
        }

        prefListView.setCellFactory(param -> new ListCell<String>(){
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                    return;
                }

                switch (name){
                    case "General":
                        imageView.setImage(generalImage);
                        break;
                    case "Interface":
                        imageView.setImage(interfaceImage);
                        break;
                    case "Advanced":
                        imageView.setImage(advancedImage);
                        break;
                }
                setText(name);
                setGraphic(imageView);
                setGraphicTextGap(5.0);
            }
        });

        prefListView.getSelectionModel().select("General");
    }

    @FXML
    private void onResetDefault(){
        Alert alert = getStage().createConfirmAlert("Are you sure you want to reset to default preferences? This cannot be undone!");
        alert.showAndWait();

        if(alert.getResult() == ButtonType.YES){
            generalPrefsPane.resetToDefaults();
            interfacePrefsPane.resetToDefaults();
            advancedPrefsPane.resetToDefaults();

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
            generalPrefsPane.savePreferences();
            interfacePrefsPane.savePreferences();
            advancedPrefsPane.savePreferences();

            Alert alert = getStage().createInfoAlert("Preferences have been saved.");
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
