package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.gui.PlayerStage;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.logging.LogManager;

public class CherryRenderer extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        InputStream inputStream = CherryRenderer.class.getClassLoader().getResourceAsStream("log-debug.properties");
        LogManager.getLogManager().readConfiguration(inputStream);

        PlayerStage stage = new PlayerStage();
        stage.prepareStage();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
