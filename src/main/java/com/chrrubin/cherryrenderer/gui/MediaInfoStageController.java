package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.MediaObject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaInfoStageController implements IController {
    private final Logger LOGGER = Logger.getLogger(MediaInfoStageController.class.getName());
    @FXML
    private GridPane rootGridPane;
    @FXML
    private TextField uriTextField;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextArea metadataTextArea;

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootGridPane.getScene().getWindow();
    }

    @FXML
    private void onClose(){
        getStage().close();
    }

    public void loadMediaInfo(MediaObject mediaObject){
        try {
            uriTextField.setText(mediaObject.getUriString());
            titleTextField.setText(mediaObject.getTitle());
            if(!mediaObject.getXmlMetadata().isEmpty()) {
                metadataTextArea.setText(mediaObject.getPrettyXmlMetadata());
            }
            else {
                metadataTextArea.setText("Not available");
            }
        }
        catch (Exception e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = getStage().createErrorAlert(e.toString());
            alert.showAndWait();
        }
    }
}
