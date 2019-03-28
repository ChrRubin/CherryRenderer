package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.RendererService;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.net.URI;

public class PlayerStageController extends BaseController {
    @FXML
    private GridPane rootGridPane;
    @FXML
    private MediaView videoMediaView;
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Slider timeSlider;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private Button playButton;
    @FXML
    private Button rewindButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Slider volumeSlider;
    @FXML
    private HBox seekHbox;
    @FXML
    private HBox controlHbox;

    private URI currentUri;
    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();
    private ScheduledService<Void> eventService = null;

    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    public void initialize(){
        RendererService handler = new RendererService("CherryRenderer");
        handler.startService();

        rendererHandler.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);
    }

    private void prepareMediaPlayer(){
        bindMediaView();

        MediaPlayer player = videoMediaView.getMediaPlayer();

        player.setAutoPlay(true);

        player.currentTimeProperty().addListener(observable -> {
            currentTimeLabel.setText(CherryUtil.durationToString(player.getCurrentTime()));
            if(!timeSlider.isValueChanging()) {
                timeSlider.setValue(player.getCurrentTime().divide(player.getTotalDuration().toMillis()).toMillis() * 100.0);
            }
        });

        player.setOnPlaying(() -> playButton.setText("||"));

        player.setOnPaused(() -> playButton.setText(">"));

        player.setOnReady(() -> {
            totalTimeLabel.setText(CherryUtil.durationToString(player.getTotalDuration()));
            rendererHandler.setVideoTotalTime(player.getTotalDuration());
            transportHandler.setMediaInfo(
                    rendererHandler.getUri(),
                    rendererHandler.getMetadata(),
                    player.getTotalDuration()
            );
            transportHandler.setPositionInfo(
                    rendererHandler.getUri(),
                    rendererHandler.getMetadata(),
                    player.getTotalDuration(),
                    new Duration(0)
            );
            startOfMedia();
        });

        ChangeListener<Boolean> timeChangingListener = (observable, wasChanging, isChanging) -> {
            if(!isChanging){
                player.seek(player.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
            }
        };
        timeSlider.valueChangingProperty().addListener(timeChangingListener);

        ChangeListener<Number> timeChangeListener = (observable, oldValue, newValue) -> {
            if(!timeSlider.isValueChanging()){
                double currentTime = oldValue.doubleValue();
                double newTime = newValue.doubleValue();
                if(Math.abs(newTime - currentTime) > 0.5) {

                    player.seek(player.getTotalDuration().multiply(newTime / 100.0));
                }
            }
        };
        timeSlider.valueProperty().addListener(timeChangeListener);

        ChangeListener<Boolean> volumeChangingListener = (observable, wasChanging, isChanging) -> {
            if(!isChanging){
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        };
        volumeSlider.valueChangingProperty().addListener(volumeChangingListener);

        InvalidationListener volumeInvalidationListener = observable -> {
            if(!volumeSlider.isValueChanging()){
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        };
        volumeSlider.valueProperty().addListener(volumeInvalidationListener);

        player.setOnEndOfMedia(() -> {
            clearSliderListeners(
                    timeSlider, timeChangingListener, timeChangeListener,
                    volumeSlider, volumeChangingListener, volumeInvalidationListener
            );
            endOfMedia();
        });

        player.setOnStopped(() -> {
            clearSliderListeners(
                    timeSlider, timeChangingListener, timeChangeListener,
                    volumeSlider, volumeChangingListener, volumeInvalidationListener
            );
            endOfMedia();
        });

        eventService = new ScheduledService<Void>(){
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if(player.getStatus() == Status.PLAYING) {
//                            rendererHandler.setVideoCurrentTime(player.getCurrentTime());
                            transportHandler.setPositionInfo(
                                    rendererHandler.getUri(),
                                    rendererHandler.getMetadata(),
                                    player.getTotalDuration(),
                                    player.getCurrentTime()
                            );
                        }
                        return null;
                    }
                };
            }
        };
        eventService.setPeriod(Duration.seconds(1));
        eventService.start();

        rendererHandler.getVideoSeekEvent().addListener(seekDuration -> {
            if(seekDuration != null) {
                player.seek(seekDuration);
            }
        });

    }

    private void endOfMedia(){
        if(eventService != null){
            eventService.cancel();
        }

        if(videoMediaView.getMediaPlayer() != null){
            videoMediaView.getMediaPlayer().dispose();
        }

        playButton.setText(">");
        currentTimeLabel.setText("--:--");
        totalTimeLabel.setText("--:--");
        timeSlider.setValue(0);
        volumeSlider.setValue(1);

        timeSlider.setDisable(true);
        playButton.setDisable(true);
        rewindButton.setDisable(true);
        stopButton.setDisable(true);
        forwardButton.setDisable(true);
        volumeSlider.setDisable(true);

        currentUri = null;
        rendererHandler.setUri(null);
    }

    private void startOfMedia(){
        timeSlider.setDisable(false);
        playButton.setDisable(false);
        rewindButton.setDisable(false);
        stopButton.setDisable(false);
        forwardButton.setDisable(false);
        volumeSlider.setDisable(false);
    }

    @FXML
    private void onPlay(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        Status status = player.getStatus();

        if(status == Status.UNKNOWN || status == Status.HALTED){
            return;
        }

        if(status == Status.PAUSED || status == Status.STOPPED || status == Status.READY){
            player.play();
        }
        else{
            player.pause();
        }
    }

    @FXML
    private void onRewind(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        Duration currentTime = player.getCurrentTime();

        if(currentTime.greaterThanOrEqualTo(Duration.seconds(10))){
            player.seek(currentTime.subtract(Duration.seconds(10)));
        }
        else{
            player.seek(Duration.ZERO);
        }
    }

    @FXML
    private void onStop(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        Status status = player.getStatus();

        if(status == Status.PAUSED || status == Status.PLAYING || status == Status.HALTED){
            player.stop();
        }
    }

    @FXML
    private void onForward(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        Duration currentTime = player.getCurrentTime();

        if(currentTime.add(Duration.seconds(10)).lessThanOrEqualTo(player.getTotalDuration())){
            player.seek(currentTime.add(Duration.seconds(10)));
        }
        else{
            player.seek(player.getTotalDuration());
        }
    }

    private void onRendererStateChanged(RendererState rendererState){
        MediaPlayer player = videoMediaView.getMediaPlayer();

        switch (rendererState){
            case NOMEDIAPRESENT:
                break;
            case STOPPED:
                if(player != null){
                    player.stop();
                }
                break;
            case PLAYING:
                if(rendererHandler.getUri() != currentUri) {
                    currentUri = rendererHandler.getUri();

                    Media media = new Media(rendererHandler.getUri().toString());
                    videoMediaView.setMediaPlayer(new MediaPlayer(media));

                    rendererHandler.setVideoTotalTime(videoMediaView.getMediaPlayer().getTotalDuration());
                    prepareMediaPlayer();
                }
                else{
                    if(player != null){
                        player.play();
                    }
                }
                break;
            case PAUSED:
                if(player != null){
                    rendererHandler.setVideoCurrentTime(player.getCurrentTime());
                    player.pause();
                }
                break;
        }
    }

    private void bindMediaView(){
        double bottomBarHeight = (seekHbox.getHeight() + controlHbox.getHeight()) * 2.0; // I don't understand why * 2.0 but it works /shrug
        videoMediaView.fitHeightProperty().bind(getStage().heightProperty().subtract(bottomBarHeight));
        videoMediaView.fitWidthProperty().bind(getStage().widthProperty());
    }

    private void clearSliderListeners(Slider timeSlider, ChangeListener<Boolean> timeChangingListener, ChangeListener<Number> timeChangeListener,
                                      Slider volumeSlider, ChangeListener<Boolean> volumeChangingListener, InvalidationListener volumeInvalidationListener){
        timeSlider.valueChangingProperty().removeListener(timeChangingListener);
        timeSlider.valueProperty().removeListener(timeChangeListener);
        volumeSlider.valueChangingProperty().removeListener(volumeChangingListener);
        volumeSlider.valueProperty().removeListener(volumeInvalidationListener);
    }
}
