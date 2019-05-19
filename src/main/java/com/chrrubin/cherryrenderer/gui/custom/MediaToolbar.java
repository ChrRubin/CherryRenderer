package com.chrrubin.cherryrenderer.gui.custom;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.prefs.ThemePreferenceValue;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MediaToolbar extends VBox {
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Slider timeSlider;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ImageView rewindImageView;
    @FXML
    private ImageView stopImageView;
    @FXML
    private ImageView fastForwardImageView;
    @FXML
    private ImageView playPauseImageView;
    @FXML
    private ImageView volumeImageView;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button rewindButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button forwardButton;

    private Image playImage;
    private Image pauseImage;
    private Image volumeFullImage;
    private Image volumeMuteImage;

    public MediaToolbar(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/custom/MediaToolbar.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.setClassLoader(getClass().getClassLoader());
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        Image rewindImage;
        Image stopImage;
        Image fastForwardImage;

        if (CherryUtil.LOADED_THEME == ThemePreferenceValue.DARK) {
            playImage = new Image("icons/grey/play.png");
            pauseImage = new Image("icons/grey/pause.png");
            rewindImage = new Image("icons/grey/rewind.png");
            stopImage = new Image("icons/grey/stop.png");
            fastForwardImage = new Image("icons/grey/forward.png");
            volumeFullImage = new Image("icons/grey/volume-full.png");
            volumeMuteImage = new Image("icons/grey/volume-mute.png");

            getStylesheets().add(getClass().getResource("/fxml/custom/MediaToolbar-Dark.css").toExternalForm());
        }
        else {
            playImage = new Image("icons/play.png");
            pauseImage = new Image("icons/pause.png");
            rewindImage = new Image("icons/rewind.png");
            stopImage = new Image("icons/stop.png");
            fastForwardImage = new Image("icons/forward.png");
            volumeFullImage = new Image("icons/volume-full.png");
            volumeMuteImage = new Image("icons/volume-mute.png");

            getStylesheets().add(getClass().getResource("/fxml/custom/MediaToolbar-Default.css").toExternalForm());
        }

        playPauseImageView.setImage(playImage);
        rewindImageView.setImage(rewindImage);
        stopImageView.setImage(stopImage);
        fastForwardImageView.setImage(fastForwardImage);
        volumeImageView.setImage(volumeFullImage);
    }

    /*
    Start properties
     */
    public final ObjectProperty<EventHandler<ActionEvent>> onPlayPauseProperty(){
        return playPauseButton.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnPlayPause(){
        return onPlayPauseProperty().get();
    }

    public void setOnPlayPause(EventHandler<ActionEvent> eventHandler){
        onPlayPauseProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onRewindProperty(){
        return rewindButton.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnRewind(){
        return onRewindProperty().get();
    }

    public void setOnRewind(EventHandler<ActionEvent> eventHandler){
        onRewindProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onStopProerty(){
        return stopButton.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnStop(){
        return onStopProerty().get();
    }

    public void setOnStop(EventHandler<ActionEvent> eventHandler){
        onStopProerty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onForwardProperty(){
        return forwardButton.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnForward(){
        return onForwardProperty().get();
    }

    public void setOnForward(EventHandler<ActionEvent> eventHandler){
        onForwardProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onVolumeImageMouseReleasedProperty(){
        return volumeImageView.onMouseReleasedProperty();
    }

    public EventHandler<? super MouseEvent> getOnVolumeImageMouseReleased(){
        return onVolumeImageMouseReleasedProperty().get();
    }

    public void setOnVolumeImageMouseReleased(EventHandler<? super MouseEvent> eventHandler){
        onVolumeImageMouseReleasedProperty().set(eventHandler);
    }
    /*
    End properties
     */

    public void disableToolbar(){
        setDisable(true);
        volumeImageView.setOpacity(0.4);
        setCurrentTimeText("--:--:--");
        setTotalTimeText("--:--:--");
        setTimeSliderValue(0);
        setVolumeSliderValue(100);
    }

    public void enableToolbar(){
        setDisable(false);
        volumeImageView.setOpacity(1);
    }

    public void setCurrentTimeText(String text){
        currentTimeLabel.setText(text);
    }

    public void setTotalTimeText(String text){
        totalTimeLabel.setText(text);
    }

    public Slider getTimeSlider(){
        return timeSlider;
    }

    public double getTimeSliderValue(){
        return timeSlider.getValue();
    }

    public void setTimeSliderValue(double value){
        timeSlider.setValue(value);
    }

    public boolean isTimeSliderValueChanging(){
        return timeSlider.isValueChanging();
    }

    public Slider getVolumeSlider(){
        return volumeSlider;
    }

    public double getVolumeSliderValue(){
        return volumeSlider.getValue();
    }

    public void setVolumeSliderValue(double value){
        volumeSlider.setValue(value);
    }

    public boolean isVolumeSliderValueChanging(){
        return volumeSlider.isValueChanging();
    }

    public void setPlayPauseToPlay(){
        playPauseImageView.setImage(playImage);
    }

    public void setPlayPauseToPause(){
        playPauseImageView.setImage(pauseImage);
    }

    public void changeVolumeImage(boolean mute){
        if(mute && !volumeImageView.getImage().equals(volumeMuteImage)){
            volumeImageView.setImage(volumeMuteImage);
        }
        else if(!volumeImageView.getImage().equals(volumeFullImage)){
            volumeImageView.setImage(volumeFullImage);
        }
    }

    public void setTimeSliderListenersHandlers(ChangeListener<Number> valueChangeListener,
                                                  ChangeListener<Boolean> valueIsChangingListener,
                                                  EventHandler<MouseEvent> mouseDraggedEventHandler,
                                                  EventHandler<MouseEvent> mouseReleasedEventHandler){
        timeSlider.valueProperty().addListener(valueChangeListener);
        timeSlider.valueChangingProperty().addListener(valueIsChangingListener);
        timeSlider.setOnMouseDragged(mouseDraggedEventHandler);
        timeSlider.setOnMouseReleased(mouseReleasedEventHandler);

    }

    public void clearTimeSliderListenersHandlers(ChangeListener<Number> valueChangeListener,
                                                 ChangeListener<Boolean> valueIsChangingListener){
        timeSlider.valueProperty().removeListener(valueChangeListener);
        timeSlider.valueChangingProperty().removeListener(valueIsChangingListener);
        timeSlider.setOnMouseDragged(null);
        timeSlider.setOnMouseReleased(null);
    }

    public void setVolumeSliderListenersHandlers(InvalidationListener valueInvalidationListener,
                                                 ChangeListener<Boolean> valueIsChangingListener,
                                                 EventHandler<MouseEvent> mouseDraggedEventHandler,
                                                 EventHandler<MouseEvent> mouseReleasedEventHandler){
        volumeSlider.valueProperty().addListener(valueInvalidationListener);
        volumeSlider.valueChangingProperty().addListener(valueIsChangingListener);
        volumeSlider.setOnMouseDragged(mouseDraggedEventHandler);
        volumeSlider.setOnMouseReleased(mouseReleasedEventHandler);
    }

    public void clearVolumeSliderListenersHandlers(InvalidationListener valueInvalidationListener,
                                                   ChangeListener<Boolean> valueIsChangingListener){
        volumeSlider.valueProperty().removeListener(valueInvalidationListener);
        volumeSlider.valueChangingProperty().removeListener(valueIsChangingListener);
        volumeSlider.setOnMouseDragged(null);
        volumeSlider.setOnMouseReleased(null);
    }
}
