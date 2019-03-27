package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.RendererEventBus;
import com.chrrubin.cherryrenderer.upnp.UpnpHandler;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
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
    // TODO: handle eventBus events

    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    public void initialize(){
        UpnpHandler handler = new UpnpHandler("CherryRenderer");
        handler.startService();

        rendererEventBus.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);

//        ScheduledService<Void> helperService = new ScheduledService<Void>() {
//            @Override
//            protected Task<Void> createTask() {
//                return new Task<Void>() {
//                    @Override
//                    protected Void call() throws Exception {
//                        checkApplicationHelper();
//                        return null;
//                    }
//                };
//            }
//        };
//        helperService.setPeriod(Duration.seconds(0.5));
//        helperService.start();
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

        player.setOnEndOfMedia(() -> {
            playButton.setText(">");
            currentTimeLabel.setText("--:--");
            totalTimeLabel.setText("--:--");
            timeSlider.setValue(0);
            volumeSlider.setValue(1);
            this.endOfMedia();
        });

        timeSlider.valueChangingProperty().addListener((observable, wasChanging, isChanging) -> {
            if(!isChanging){
                player.seek(player.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
            }
        });

        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(!timeSlider.isValueChanging()){
                double currentTime = oldValue.doubleValue();
                double newTime = newValue.doubleValue();
                if(Math.abs(newTime - currentTime) > 0.5) {

                    player.seek(player.getTotalDuration().multiply(newTime / 100.0));
                }
            }
        });

        volumeSlider.valueChangingProperty().addListener((observable, wasChanging, isChanging) -> {
            if(!isChanging){
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        });

        volumeSlider.valueProperty().addListener(observable -> {
            if(!volumeSlider.isValueChanging()){
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        });
    }

    private void endOfMedia(){
        timeSlider.setDisable(true);
        playButton.setDisable(true);
        rewindButton.setDisable(true);
        stopButton.setDisable(true);
        forwardButton.setDisable(true);
        volumeSlider.setDisable(true);

        if(videoMediaView.getMediaPlayer() != null){
            videoMediaView.getMediaPlayer().stop();
            videoMediaView.getMediaPlayer().dispose();
        }

        currentUri = null;
    }

    private void startOfMedia(){
        // TODO: Only trigger when media is done initial loading?
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
            endOfMedia();
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
                if(player != null){
                    endOfMedia();
                }
                break;
            case STOPPED:
                if(player != null){
                    endOfMedia();
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
            case SEEKING:
                rendererEventBus.setRendererState(RendererState.PLAYING);
                break;
        }
    }

    private void bindMediaView(){
        double bottomBarHeight = (seekHbox.getHeight() + controlHbox.getHeight()) * 2.0; // I don't understand why * 2.0 but it works /shrug
        videoMediaView.fitHeightProperty().bind(getStage().heightProperty().subtract(bottomBarHeight));
        videoMediaView.fitWidthProperty().bind(getStage().widthProperty());
    }
}
