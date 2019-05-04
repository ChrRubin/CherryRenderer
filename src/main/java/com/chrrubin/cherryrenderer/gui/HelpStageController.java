package com.chrrubin.cherryrenderer.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.logging.Logger;

public class HelpStageController implements IController {
    private final Logger LOGGER = Logger.getLogger(HelpStageController.class.getName());
    @FXML
    private GridPane rootGridPane;
    @FXML
    private TableView<HotKey> hotkeyTableView;
    @FXML
    private TableColumn<HotKey, String> functionTableColumn;
    @FXML
    private TableColumn<HotKey, String> keyTableColumn;

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    public void initialize(){
        functionTableColumn.setCellValueFactory(new PropertyValueFactory<>("function"));
        keyTableColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

        ObservableList<HotKey> data = FXCollections.observableArrayList(
                new HotKey("Play/Pause", "Space"),
                new HotKey("Toggle Fullscreen", "F"),
                new HotKey("Stop", "S"),
                new HotKey("Rewind", "Left"),
                new HotKey("Fast Forward", "Right"),
                new HotKey("Volume Up", "Up"),
                new HotKey("Volume Down", "Down"),
                new HotKey("Toggle Mute", "M")
        );

        hotkeyTableView.setItems(data);

        hotkeyTableView.setPrefHeight(hotkeyTableView.fixedCellSizeProperty().multiply(Bindings.size(hotkeyTableView.getItems()).add(1.25)).get());
        hotkeyTableView.setMinHeight(Region.USE_PREF_SIZE);
        hotkeyTableView.setMaxHeight(Region.USE_PREF_SIZE);
    }

    @FXML
    private void onClickRepo(){
        new Thread(() -> getStage().openBrowser("https://github.com/ChrRubin/CherryRenderer", LOGGER)).start();
    }

    @FXML
    private void onClickReadme(){
        new Thread(() -> getStage().openBrowser("https://github.com/ChrRubin/CherryRenderer/blob/master/README.md", LOGGER)).start();
    }

    @FXML
    private void onClickMoreInfo(){
        new Thread(() -> getStage().openBrowser("https://github.com/ChrRubin/CherryRenderer/blob/master/MOREINFO.md", LOGGER)).start();
    }

    @FXML
    private void onClose(){
        getStage().close();
    }

    public class HotKey{
        private final SimpleStringProperty function;
        private final SimpleStringProperty key;

        HotKey(String function, String key) {
            this.function = new SimpleStringProperty(function);
            this.key = new SimpleStringProperty(key);
        }

        public SimpleStringProperty functionProperty() {
            return function;
        }

        public SimpleStringProperty keyProperty(){
            return key;
        }
    }
}
