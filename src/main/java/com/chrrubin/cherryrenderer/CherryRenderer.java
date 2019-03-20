package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.gui.PlayerStage;
import javafx.application.Application;
import javafx.stage.Stage;

public class CherryRenderer extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        PlayerStage stage = new PlayerStage();
        stage.prepareStage();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
