package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.RendererService;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.fourthline.cling.support.model.TransportState;

import java.net.URI;
import java.util.logging.Logger;

public class PlayerStageController extends BaseController {
    @FXML
    private StackPane rootStackPane;
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
    private VBox bottomBarVBox;
    @FXML
    private ProgressIndicator videoProgressIndicator;

    private final Logger LOGGER = Logger.getLogger(PlayerStageController.class.getName());

    private URI currentUri = null;
    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();
    private ScheduledService<Void> eventService = null;

    private PauseTransition mouseIdle = new PauseTransition(Duration.seconds(1));

    public BaseStage getStage() {
        return (BaseStage) rootStackPane.getScene().getWindow();
    }

    public void initialize(){
        RendererService handler = new RendererService("CherryRenderer");
        handler.startService();

        rendererHandler.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);
    }

    /**
     * Prepares MediaPlayer for video playing.
     * Creates required property listeners and event handlers that are used during video playback.
     */
    private void prepareMediaPlayer(){
        prepareFullScreen(false);

        MediaPlayer player = videoMediaView.getMediaPlayer();

        player.setAutoPlay(true);

        player.currentTimeProperty().addListener(observable -> {
            currentTimeLabel.setText(CherryUtil.durationToString(player.getCurrentTime()));
            if(!timeSlider.isValueChanging()) {
                timeSlider.setValue(player.getCurrentTime().divide(player.getTotalDuration().toMillis()).toMillis() * 100.0);
            }
        });

        player.setOnPlaying(() -> {
            playButton.setText("||");
        });

        player.setOnPaused(() -> {
            playButton.setText(">");
        });

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

        // TODO: Implement buffer handling
        //  Soooooo this doesn't work for some reason???????
        ChangeListener<Status> playerStatusListener = (observable, oldStatus, newStatus) -> {
            if(newStatus == Status.STALLED){
                videoProgressIndicator.setOpacity(1);
            }
            else if(oldStatus == Status.STALLED){
                videoProgressIndicator.setOpacity(0);
            }
        };
        player.statusProperty().addListener(playerStatusListener);

        // FIXME: Seeking from renderer side doesn't update control point
        /*
        timeSlider and volumeSlider property listeners.
        Allows video time and volume to be manipulated via the respective sliders.
         */
        ChangeListener<Boolean> timeChangingListener = (observable, wasChanging, isChanging) -> {
            if(!isChanging){
                player.seek(player.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
                updateCurrentTime();
            }
        };
        timeSlider.valueChangingProperty().addListener(timeChangingListener);

        ChangeListener<Number> timeChangeListener = (observable, oldValue, newValue) -> {
            if(!timeSlider.isValueChanging()){
                double currentTime = oldValue.doubleValue();
                double newTime = newValue.doubleValue();
                if(Math.abs(newTime - currentTime) > 0.5) {

                    player.seek(player.getTotalDuration().multiply(newTime / 100.0));
                    updateCurrentTime();
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

        /*
        Toggle fullscreen via double click
         */
        videoMediaView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    if(!getStage().isFullScreen()){
                        getStage().setFullScreen(true);
                    }
                    else{
                        getStage().setFullScreen(false);
                    }
                }
            }
        });

        ChangeListener<Boolean> isFullScreenListener =
                (observable, wasFullScreen, isFullScreen) -> prepareFullScreen(isFullScreen);
        getStage().fullScreenProperty().addListener(isFullScreenListener);

        /*
        Properly removes property listeners during onEndOfMedia and onStopped
         */
        player.setOnEndOfMedia(() -> {
            clearSliderListeners(
                    timeSlider, timeChangingListener, timeChangeListener,
                    volumeSlider, volumeChangingListener, volumeInvalidationListener
            );

            endOfMedia();
            getStage().fullScreenProperty().removeListener(isFullScreenListener);
            player.statusProperty().removeListener(playerStatusListener);
        });

        player.setOnStopped(() -> {
            clearSliderListeners(
                    timeSlider, timeChangingListener, timeChangeListener,
                    volumeSlider, volumeChangingListener, volumeInvalidationListener
            );

            endOfMedia();
            getStage().fullScreenProperty().removeListener(isFullScreenListener);
            player.statusProperty().removeListener(playerStatusListener);
        });

        eventService = new ScheduledService<Void>(){
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if(videoMediaView.getMediaPlayer() != null && videoMediaView.getMediaPlayer().getStatus() == Status.PLAYING) {
                            updateCurrentTime();
                        }
                        return null;
                    }
                };
            }
        };
        eventService.setPeriod(Duration.seconds(1));
        eventService.start();

        /*
        Seeks video when VideoSeekEvent is triggered by control point
         */
        rendererHandler.getVideoSeekEvent().addListener(seekDuration -> {
            if(seekDuration != null) {
                player.seek(seekDuration);
                updateCurrentTime();
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
        volumeSlider.setValue(100);

        timeSlider.setDisable(true);
        playButton.setDisable(true);
        rewindButton.setDisable(true);
        stopButton.setDisable(true);
        forwardButton.setDisable(true);
        volumeSlider.setDisable(true);

        currentUri = null;

        videoMediaView.setOnMouseClicked(null);
        if(getStage().isFullScreen()){
            getStage().setFullScreen(false);
        }

        transportHandler.setTransportInfo(TransportState.STOPPED);
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
            updateCurrentTime();
            transportHandler.setTransportInfo(TransportState.PLAYING);
        }
        else{
            player.pause();
            updateCurrentTime();
            transportHandler.setTransportInfo(TransportState.PAUSED_PLAYBACK);
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

        updateCurrentTime();
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

        updateCurrentTime();
    }

    /**
     * Handles RendererStateChanged events, triggered by uPnP state classes.
     * @param rendererState Current RendererState of MediaRenderer
     */
    private void onRendererStateChanged(RendererState rendererState){
        MediaPlayer player = videoMediaView.getMediaPlayer();

        switch (rendererState){
            case NOMEDIAPRESENT:
                if(player != null){
                    player.stop();
                }
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
                    if(player != null && player.getStatus() == Status.PAUSED){
                        player.play();
                        updateCurrentTime();
                    }
                }
                break;
            case PAUSED:
                if(player != null){
                    player.pause();
                    updateCurrentTime();
                }
                break;
        }
    }

    /**
     * Prepares UI elements based on whether fullscreen is triggered.
     * During fullscreen, cursor and bottom bar will be hidden when mouse is idle for 1 second.
     * @param isFullScreen Whether the application is in fullscreen mode.
     */
    private void prepareFullScreen(boolean isFullScreen){
        videoMediaView.fitHeightProperty().unbind();
        videoMediaView.fitWidthProperty().unbind();

        if(isFullScreen){
            StackPane.setMargin(videoMediaView, new Insets(0,0,0,0));
//            StackPane.setMargin(videoProgressIndicator, new Insets(0, 0, 0, 0));

            videoMediaView.fitHeightProperty().bind(getStage().heightProperty());
            videoMediaView.fitWidthProperty().bind(getStage().widthProperty());

            bottomBarVBox.setMaxWidth(Region.USE_PREF_SIZE);
            bottomBarVBox.setOpacity(0);
            getStage().getScene().setCursor(Cursor.NONE);

            mouseIdle.setOnFinished(event -> {
                getStage().getScene().setCursor(Cursor.NONE);
                bottomBarVBox.setOpacity(0);
            });

            videoMediaView.setOnMouseMoved(event -> {
                getStage().getScene().setCursor(Cursor.DEFAULT);
                bottomBarVBox.setOpacity(1);
                mouseIdle.playFromStart();
            });

            bottomBarVBox.setOnMouseEntered(event -> mouseIdle.pause());
        }
        else {
            mouseIdle.setOnFinished(null);
            videoMediaView.setOnMouseMoved(null);
            bottomBarVBox.setOnMouseEntered(null);

            double bottomBarHeight = bottomBarVBox.getHeight();
            StackPane.setMargin(videoMediaView, new Insets(0,0, bottomBarHeight,0));
//            StackPane.setMargin(videoProgressIndicator, new Insets(0,0, bottomBarHeight,0));

            videoMediaView.fitHeightProperty().bind(getStage().heightProperty().subtract(bottomBarHeight * 2.0));
            // I don't understand why multiplying by 2 works but it works. There's still a top/bottom black bar but I don't feel like fidgeting with the math /shrug
            videoMediaView.fitWidthProperty().bind(getStage().widthProperty());

            bottomBarVBox.setMaxWidth(Region.USE_COMPUTED_SIZE);

            getStage().getScene().setCursor(Cursor.DEFAULT);
            bottomBarVBox.setOpacity(1);
        }
    }

    private void clearSliderListeners(Slider timeSlider, ChangeListener<Boolean> timeChangingListener, ChangeListener<Number> timeChangeListener,
                                      Slider volumeSlider, ChangeListener<Boolean> volumeChangingListener, InvalidationListener volumeInvalidationListener){
        timeSlider.valueChangingProperty().removeListener(timeChangingListener);
        timeSlider.valueProperty().removeListener(timeChangeListener);
        volumeSlider.valueChangingProperty().removeListener(volumeChangingListener);
        volumeSlider.valueProperty().removeListener(volumeInvalidationListener);
    }

    /**
     * Notifies control point of current time changes via transportHandler
     */
    private void updateCurrentTime(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null){
            return;
        }

        rendererHandler.setVideoCurrentTime(player.getCurrentTime());

        transportHandler.setPositionInfo(
                rendererHandler.getUri(),
                rendererHandler.getMetadata(),
                player.getTotalDuration(),
                player.getCurrentTime()
        );
    }
}
