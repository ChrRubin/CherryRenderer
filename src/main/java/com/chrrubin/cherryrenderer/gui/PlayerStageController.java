package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.RendererEventBus;
import com.chrrubin.cherryrenderer.upnp.RendererService;
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
    private RendererEventBus rendererEventBus = RendererEventBus.getInstance();
    private ScheduledService<Void> eventService = null;
    // TODO: handle eventBus events

    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    public void initialize(){
        RendererService handler = new RendererService("CherryRenderer");
        handler.startService();

        rendererEventBus.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);
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
            timeSlider.valueChangingProperty().removeListener(timeChangingListener);
            timeSlider.valueProperty().removeListener(timeChangeListener);
            volumeSlider.valueChangingProperty().removeListener(volumeChangingListener);
            volumeSlider.valueProperty().removeListener(volumeInvalidationListener);
            endOfMedia();
        });

        player.setOnStopped(() -> {
            timeSlider.valueChangingProperty().removeListener(timeChangingListener);
            timeSlider.valueProperty().removeListener(timeChangeListener);
            volumeSlider.valueChangingProperty().removeListener(volumeChangingListener);
            volumeSlider.valueProperty().removeListener(volumeInvalidationListener);
            endOfMedia();
        });

        eventService = new ScheduledService<Void>(){
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if(player.getStatus() == Status.PLAYING) {
                            rendererEventBus.setVideoCurrentTime(player.getCurrentTime());
                        }
                        return null;
                    }
                };
            }
        };
        eventService.setPeriod(Duration.seconds(1));
        eventService.start();

        rendererEventBus.getVideoSeekEvent().addListener(seekDuration -> {
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
        rendererEventBus.setUri(null);
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
                if(rendererEventBus.getUri() != currentUri) {
                    currentUri = rendererEventBus.getUri();

                    Media media = new Media(rendererEventBus.getUri().toString());
                    videoMediaView.setMediaPlayer(new MediaPlayer(media));

                    rendererEventBus.setVideoTotalTime(videoMediaView.getMediaPlayer().getTotalDuration());
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
                    rendererEventBus.setVideoCurrentTime(player.getCurrentTime());
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
}
