package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.CherryUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
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
    private Service<Map<String, Boolean>> latestVersionService = new Service<Map<String, Boolean>>() {
        @Override
        protected Task<Map<String, Boolean>> createTask() {
            return new Task<Map<String, Boolean>>() {
                @Override
                protected Map<String, Boolean> call() throws Exception {
                    HashMap<String, Boolean> map = new HashMap<>(1);
                    map.put(CherryUtil.getLatestVersion(), CherryUtil.isOutdated());
                    return map;
                }
            };
        }
    };

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    @FXML
    private void initialize(){
        currentLabel.setText(CherryPrefs.VERSION);

        latestVersionService.setOnSucceeded(event -> {
            Map<String, Boolean> map = latestVersionService.getValue();
            Map.Entry<String, Boolean> firstPair = map.entrySet().iterator().next();
            String latest = firstPair.getKey();
            boolean isOutdated = firstPair.getValue();

            latestLabel.setText(latest);
            if(isOutdated){
                currentLabel.setStyle("-fx-text-fill: red");
                statusLabel.setText(OUTDATED);
            }
            else{
                currentLabel.setStyle("-fx-text-fill: green");
                statusLabel.setText(UP_TO_DATE);
            }
        });

        latestVersionService.setOnFailed(event -> {
            latestLabel.setText("???");
            statusLabel.setText(ERROR);
            Throwable e = latestVersionService.getException();

            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = getStage().createErrorAlert(e.toString());
            alert.showAndWait();
        });

        latestVersionService.start();
    }

    @FXML
    private void onViewLatest(){
        if(Desktop.isDesktopSupported()){
            try{
                Desktop.getDesktop().browse(new URI("https://github.com/ChrRubin/CherryRenderer/releases/latest"));
            }
            catch (URISyntaxException | IOException e){
                LOGGER.log(Level.SEVERE, e.toString(), e);
                Alert alert = getStage().createErrorAlert(e.toString());
                alert.showAndWait();
            }
        }
        else{
            LOGGER.warning("This desktop does not support opening web browser");
            Alert alert = getStage().createWarningAlert("This desktop does not support opening web browser");
            alert.showAndWait();
        }
    }

    @FXML
    private void onClose(){
        getStage().close();
    }

}
