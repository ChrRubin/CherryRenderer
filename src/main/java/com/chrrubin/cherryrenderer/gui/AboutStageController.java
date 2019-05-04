package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutStageController implements IController {
    private final Logger LOGGER = Logger.getLogger(AboutStageController.class.getName());
    @FXML
    private GridPane rootGridPane;
    @FXML
    private Label versionLabel;

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    @FXML
    private void initialize(){
        versionLabel.setText(CherryPrefs.VERSION);
    }

    @FXML
    private void onClickVersion(){
        new Thread(() -> getStage().openBrowser("https://github.com/ChrRubin/CherryRenderer/releases", LOGGER)).start();
    }

    @FXML
    private void onClickAuthor(){
        new Thread(() -> getStage().openBrowser("https://github.com/ChrRubin", LOGGER)).start();
    }

    @FXML
    private void onClickRepo(){
        new Thread(() -> getStage().openBrowser("https://github.com/ChrRubin/CherryRenderer", LOGGER)).start();
    }

    @FXML
    private void onLicense(){
        LicenseStage licenseStage = new LicenseStage(getStage());
        try{
            licenseStage.prepareStage();
            licenseStage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    @FXML
    private void onClose(){
        getStage().close();
    }
}
