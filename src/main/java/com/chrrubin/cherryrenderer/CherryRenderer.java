package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.gui.PlayerStage;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class CherryRenderer extends Application {
    private Preferences preferences = Preferences.userNodeForPackage(CherryRenderer.class);
    private Logger LOGGER = Logger.getLogger(CherryRenderer.class.getName());

    @Override
    public void start(Stage primaryStage){
        try {
            String propertiesFileName;
            switch (preferences.get(CherryPrefs.LogLevel.KEY, CherryPrefs.LogLevel.DEFAULT)) {
                case "DEBUG":
                    propertiesFileName = "log-debug.properties";
                    break;
                case "DEBUG+":
                    propertiesFileName = "log-debugplus.properties";
                    break;
                case "ALL":
                    propertiesFileName = "log-all.properties";
                    break;
                default:
                    propertiesFileName = "log-info.properties";
                    break;
            }
            InputStream inputStream = CherryRenderer.class.getClassLoader().getResourceAsStream(propertiesFileName);
            LogManager.getLogManager().readConfiguration(inputStream);
            LOGGER.fine("Loaded logger properties from " + propertiesFileName);

            if(!preferences.getBoolean(CherryPrefs.HardwareAcceleration.KEY, CherryPrefs.HardwareAcceleration.DEFAULT)){
                LOGGER.fine("Using software acceleration.");
                System.setProperty("prism.order", "sw");
            }

            PlayerStage stage = new PlayerStage();
            stage.prepareStage();
            stage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
        launch(args);
    }

}
