package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutStageController implements BaseController {
    private final Logger LOGGER = Logger.getLogger(AboutStageController.class.getName());
    @FXML
    private GridPane rootGridPane;
    @FXML
    private Label versionLabel;

    @Override
    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    public void loadVersion(){
        versionLabel.setText(CherryPrefs.VERSION);
    }

    @FXML
    private void onClickSite(){
        new Thread(() -> {
            if(Desktop.isDesktopSupported()){
                try{
                    Desktop.getDesktop().browse(new URI("https://github.com/ChrRubin"));
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
        }).start();
    }
}
