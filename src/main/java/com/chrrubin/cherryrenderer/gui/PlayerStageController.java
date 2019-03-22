package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.ApplicationHelper;
import com.chrrubin.cherryrenderer.upnp.UpnpHandler;
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

    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    public void initialize(){
        UpnpHandler handler = new UpnpHandler("CherryRenderer");
        handler.startService();


        ScheduledService<Void> helperService = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        checkApplicationHelper();
                        return null;
                    }
                };
            }
        };
        helperService.setPeriod(Duration.seconds(0.5));
        helperService.start();
    }

    public void createPlayerListeners(){
        MediaPlayer player = videoMediaView.getMediaPlayer();

        player.setAutoPlay(true);

        player.currentTimeProperty().addListener(observable -> {
            currentTimeLabel.setText(ApplicationHelper.durationToString(player.getCurrentTime()));
            if(!timeSlider.isValueChanging()) {
                timeSlider.setValue(player.getCurrentTime().divide(player.getTotalDuration().toMillis()).toMillis() * 100.0);
            }
        });

        player.setOnPlaying(() -> playButton.setText("||"));

        player.setOnPaused(() -> playButton.setText(">"));

        player.setOnReady(() -> totalTimeLabel.setText(ApplicationHelper.durationToString(player.getTotalDuration())));

        player.setOnEndOfMedia(() -> {
            playButton.setText(">");
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
            videoMediaView.setMediaPlayer(null);
        }

        currentTimeLabel.setText("--:--");
        totalTimeLabel.setText("--:--");
        timeSlider.setValue(0);
        volumeSlider.setValue(1);
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

    private void checkApplicationHelper(){
        switch(ApplicationHelper.getRendererState()){
            case PLAYING:
                if(ApplicationHelper.getUri() != currentUri) {
                    currentUri = ApplicationHelper.getUri();

                    Media media = new Media(ApplicationHelper.getUri().toString());
                    videoMediaView.setMediaPlayer(new MediaPlayer(media));
                    videoMediaView.getMediaPlayer().play();

                    ApplicationHelper.setVideoTotalTime(videoMediaView.getMediaPlayer().getTotalDuration());
                    bindMediaView();
                    createPlayerListeners();
                    startOfMedia();
                }

                ApplicationHelper.setVideoCurrentTime(videoMediaView.getMediaPlayer().getCurrentTime());

                break;
            case PAUSED:
                if(videoMediaView.getMediaPlayer() != null){
                    ApplicationHelper.setVideoCurrentTime(videoMediaView.getMediaPlayer().getCurrentTime());
                    videoMediaView.getMediaPlayer().pause();
                }
                break;
            case STOPPED:
                endOfMedia();
                break;
            case NOMEDIAPRESENT:
                endOfMedia();
                break;
            case SEEKING:
                break;
        }
    }

    public void bindMediaView(){
        double bottomBarHeight = (seekHbox.getHeight() + controlHbox.getHeight()) * 2.0; // I don't understand why * 2.0 but it works /shrug
        videoMediaView.fitHeightProperty().bind(getStage().heightProperty().subtract(bottomBarHeight));
        videoMediaView.fitWidthProperty().bind(getStage().widthProperty());
    }
}
