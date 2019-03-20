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

    private URI currentUri;

    public BaseStage getStage() {
        return (BaseStage)rootGridPane.getScene().getWindow();
    }

    public void initialize(){

        ScheduledService<Void> helperService = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
//                        System.out.println("Test");
                        checkApplicationHelper();
                        return null;
                    }
                };
            }
        };
        helperService.setPeriod(Duration.seconds(1));
        helperService.start();

        UpnpHandler handler = new UpnpHandler("RMediaRenderer");
        handler.startService();


//        ScheduledExecutorService helperService = Executors.newSingleThreadScheduledExecutor();
//        helperService.scheduleWithFixedDelay(new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                checkApplicationHelper();
//                return null;
//            }
//        }, 0, 1, TimeUnit.SECONDS);
    }

    public void createListeners(){
        MediaPlayer player = videoMediaView.getMediaPlayer();

        player.setAutoPlay(true);

        player.currentTimeProperty().addListener(observable -> {
            currentTimeLabel.setText(durationToString(player.getCurrentTime()));
            timeSlider.setValue(player.getCurrentTime().divide(player.getTotalDuration().toMillis()).toMillis() * 100.0);
        });

        player.setOnPlaying(() -> playButton.setText("||"));

        player.setOnPaused(() -> playButton.setText(">"));

        player.setOnReady(() -> totalTimeLabel.setText(durationToString(player.getTotalDuration())));

        player.setOnEndOfMedia(this::endOfMedia);

        timeSlider.valueProperty().addListener(observable -> {
            if(timeSlider.isValueChanging()){
                player.seek(player.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
            }
        });

        volumeSlider.valueProperty().addListener(observable -> {
            if(volumeSlider.isValueChanging()){
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        });
    }

    private String durationToString(Duration duration){
        int intSeconds = (int)Math.floor(duration.toSeconds());
        int hours = intSeconds / 60 / 60;
        if(hours > 0){
            intSeconds -= (hours * 60 * 60);
        }
        int minutes = intSeconds / 60;
        int seconds = intSeconds - (hours * 60 * 60) - (minutes * 60);

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    private void endOfMedia(){
        timeSlider.setDisable(true);
        playButton.setDisable(true);
        rewindButton.setDisable(true);
        stopButton.setDisable(true);
        forwardButton.setDisable(true);
        volumeSlider.setDisable(true);

        timeSlider.setValue(0);
        playButton.setText(">");
    }

    @FXML
    private void onMenuSettings(){}

    @FXML
    private void onMenuClose(){
        System.exit(0);
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

    private void checkApplicationHelper(){
//        System.out.println("Checking helper");
//        System.out.println(ApplicationHelper.getRendererState().name());

        switch(ApplicationHelper.getRendererState()){
            case PLAYING:
                if(ApplicationHelper.getUri() != currentUri) {
                    currentUri = ApplicationHelper.getUri();

                    Media media = new Media(ApplicationHelper.getUri().toString());
                    videoMediaView.setMediaPlayer(new MediaPlayer(media));
                    videoMediaView.getMediaPlayer().play();
                }
                break;
            case PAUSED:
                break;
            case STOPPED:
                break;
            case NOMEDIAPRESENT:
                endOfMedia();
                break;
        }
    }

}
