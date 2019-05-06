package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.CherryUtil;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdaterStageController implements IController {
    private final Logger LOGGER = Logger.getLogger(UpdaterStageController.class.getName());

    @FXML
    private GridPane rootGridPane;
    @FXML
    private Label currentLabel;
    @FXML
    private Label latestLabel;
    @FXML
    private Label statusLabel;

    private final String UP_TO_DATE = "Your current version is up to date!" + System.lineSeparator() +
            "Thank you for using CherryRenderer.";
    private final String OUTDATED = "Your current version is outdated!" + System.lineSeparator() +
            "Consider updating for new features and bug fixes.";
    private final String ERROR = "Could not find latest version!" + System.lineSeparator() +
            "Please check your internet connection.";
    private Service<String> getLatestVersionService = CherryUtil.getLatestVersionJFXService();

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    @FXML
    private void initialize(){
        currentLabel.setText(CherryPrefs.VERSION);
    }

    @FXML
    private void onViewLatest(){
        new Thread(() -> getStage().openBrowser("https://github.com/ChrRubin/CherryRenderer/releases/latest", LOGGER)).start();
    }

    @FXML
    private void onClose(){
        getStage().close();
    }

    public void checkForUpdate(){
        getLatestVersionService.setOnSucceeded(event -> {
            String latestVersion = getLatestVersionService.getValue();

            latestLabel.setText(latestVersion);
            if(CherryUtil.isOutdated(latestVersion)){
                currentLabel.setStyle("-fx-text-fill: red");
                statusLabel.setText(OUTDATED);
            }
            else{
                currentLabel.setStyle("-fx-text-fill: green");
                statusLabel.setText(UP_TO_DATE);
            }
        });

        getLatestVersionService.setOnFailed(event -> {
            latestLabel.setText("???");
            statusLabel.setText(ERROR);
            Throwable e = getLatestVersionService.getException();

            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = getStage().createErrorAlert(e.toString());
            alert.showAndWait();
        });

        getLatestVersionService.start();
    }

    public void skipUpdateCheck(String latestVersion){
        latestLabel.setText(latestVersion);
        currentLabel.setStyle("-fx-text-fill: red");
        statusLabel.setText(OUTDATED);
    }
}
