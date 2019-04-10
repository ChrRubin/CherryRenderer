package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.gui.PlayerStage;
import javafx.application.Application;
import javafx.stage.Stage;

public class CherryRenderer extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        String path = CherryRenderer.class.getClassLoader().getResource("log-debug.properties").getFile();
//        String path = CherryRenderer.class.getClassLoader().getResource("log-normal.properties").getFile();

        System.setProperty("java.util.logging.config.file", path);

        PlayerStage stage = new PlayerStage();
        stage.prepareStage();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
