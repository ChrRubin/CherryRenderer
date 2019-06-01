package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.gui.AbstractStage;
import com.chrrubin.cherryrenderer.gui.JfxPlayerStage;
import com.chrrubin.cherryrenderer.gui.VlcPlayerStage;
import com.chrrubin.cherryrenderer.prefs.ForceJfxPreference;
import com.chrrubin.cherryrenderer.prefs.HardwareAccelerationPreference;
import com.chrrubin.cherryrenderer.prefs.LogLevelPreference;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class CherryRenderer extends Application {
    private final Logger LOGGER = Logger.getLogger(CherryRenderer.class.getName());

    @Override
    public void start(Stage primaryStage){
        Thread.currentThread().setName("JavaFX Thread");
        System.setProperty("http.agent", CherryUtil.USER_AGENT);
        try {
            String propertiesFileName;
            switch (new LogLevelPreference().get()) {
                case DEBUG:
                    propertiesFileName = "log-debug.properties";
                    break;
                case DEBUG_PLUS:
                    propertiesFileName = "log-debugplus.properties";
                    break;
                case ALL:
                    propertiesFileName = "log-all.properties";
                    break;
                default:
                    propertiesFileName = "log-info.properties";
                    break;
            }
            InputStream inputStream = CherryRenderer.class.getClassLoader().getResourceAsStream(propertiesFileName);
            LogManager.getLogManager().readConfiguration(inputStream);
            LOGGER.info("Loaded logger properties from " + propertiesFileName);

            LOGGER.info("CherryRenderer version is " + CherryUtil.VERSION);

            if(!new HardwareAccelerationPreference().get()){
                LOGGER.info("Using software acceleration.");
                System.setProperty("prism.order", "sw"); // Fix occasional visual glitches as https://stackoverflow.com/questions/37750553/javafx-graphic-glitch-white-boxes
            }


            AbstractStage stage;
            if (CherryUtil.FOUND_VLC && !(new ForceJfxPreference().get())) {
                LOGGER.info("VLC installation detected. Using embedded VLC player.");
                System.setProperty("VLCJ_INITX", "no"); // Fix JVM crashing when opening FileChooser on X11 as https://github.com/caprica/vlcj/issues/353
                stage = new VlcPlayerStage();
            }
            else{
                LOGGER.info("Using default JavaFX media player.");
                stage = new JfxPlayerStage();
            }
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
        launch(args);
    }

}
