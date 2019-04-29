package com.chrrubin.cherryrenderer.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class HelpStageController implements IController {
    @FXML
    private GridPane rootGridPane;
    @FXML
    private TableView<HotKey> hotkeyTableView;
    @FXML
    private TableColumn<HotKey, String> functionTableColumn;
    @FXML
    private TableColumn<HotKey, String> keyTableColumn;
    @FXML
    private VBox scrollVBox;
    @FXML
    private ScrollPane helpScrollPane;

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    public void initialize(){
//        scrollVBox.maxWidthProperty().bind(helpScrollPane.widthProperty());

        // FIXME: this is not working???
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
    }

    @FXML
    private void onClose(){
        getStage().close();
    }

    class HotKey{
        private final SimpleStringProperty function;
        private final SimpleStringProperty key;

        HotKey(String function, String key) {
            this.function = new SimpleStringProperty(function);
            this.key = new SimpleStringProperty(key);
        }

        SimpleStringProperty functionProperty() {
            return function;
        }

        SimpleStringProperty keyProperty(){
            return key;
        }
    }
}
