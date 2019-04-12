package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryRenderer;
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
import javafx.scene.control.MenuBar;
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

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class PlayerStageController implements BaseController {
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
    private MenuBar menuBar;

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
        Preferences preferences = Preferences.userNodeForPackage(CherryRenderer.class);

        String friendlyName = preferences.get("friendlyName", "CherryRenderer");
        LOGGER.info("Current device friendly name is " + friendlyName);
        RendererService handler = new RendererService(friendlyName);
        handler.startService();

        rendererHandler.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);
    }

    /**
     * Prepares MediaPlayer for video playing.
     * Creates required property listeners and event handlers that are used during video playback.
     */
    private void prepareMediaPlayer(){
        LOGGER.finer("Preparing Media Player for playback");

        prepareFullScreen(false);

        MediaPlayer player = videoMediaView.getMediaPlayer();

        player.setAutoPlay(true);

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
            transportHandler.sendLastChangeMediaDuration(player.getTotalDuration());
            startOfMedia();
        });

        // TODO: Handle player errors better (media not supported, player halted etc)
        player.setOnError(() ->
                LOGGER.log(Level.SEVERE, player.getError().toString(), player.getError()));

        player.setOnHalted(() -> {
            LOGGER.warning("Media player reached HALTED status. Disposing media player...");
            endOfMedia();
        });

        // TODO: Implement buffering handling
        //  For some reason Status.STALLED can't be used to determine whether player is buffering...
        player.setOnStalled(() ->
                LOGGER.fine("(Caught by setOnStalled) Media player in STALLED status. Video is currently buffering."));

        player.statusProperty().addListener((observable, oldStatus, newStatus) -> {
            if(oldStatus == null){
                return;
            }

            LOGGER.finer("Media player exited "+ oldStatus.name() + " status, going into " + newStatus.name() + " status.");

            if(newStatus == Status.STALLED){
                LOGGER.fine("(Caught by listener) Media player in STALLED status. Video is currently buffering.");
            }
        });

        // TODO: Previous InvalidationListener was causing deadlocks on edge cases. Continue testing this for deadlocks
        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            currentTimeLabel.setText(CherryUtil.durationToString(newValue));
            if(!timeSlider.isValueChanging()){
                timeSlider.setValue(newValue.divide(player.getTotalDuration().toMillis()).toMillis() * 100.0);
            }
        });

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
            LOGGER.finest("player.setOnEndOfMedia triggered");

            clearSliderListeners(
                    timeSlider, timeChangingListener, timeChangeListener,
                    volumeSlider, volumeChangingListener, volumeInvalidationListener
            );

            endOfMedia();
            getStage().fullScreenProperty().removeListener(isFullScreenListener);
        });

        player.setOnStopped(() -> {
            LOGGER.finest("player.setOnStopped triggered");

            clearSliderListeners(
                    timeSlider, timeChangingListener, timeChangeListener,
                    volumeSlider, volumeChangingListener, volumeInvalidationListener
            );

            endOfMedia();
            getStage().fullScreenProperty().removeListener(isFullScreenListener);
        });

        eventService = new ScheduledService<Void>(){
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if(videoMediaView.getMediaPlayer() != null && videoMediaView.getMediaPlayer().getStatus() == Status.PLAYING) {
                            updateCurrentTime(videoMediaView.getMediaPlayer().getCurrentTime(), videoMediaView.getMediaPlayer().getTotalDuration());
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
        LOGGER.finer("Running end of media function");

        if(eventService != null){
            eventService.cancel();
        }

        if(videoMediaView.getMediaPlayer() != null){
            LOGGER.finer("Disposing MediaPlayer");
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

        if(status == Status.UNKNOWN){
            LOGGER.warning("Attempted to pause/play video while player is still initializing. Ignoring.");
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

        if(status == Status.PAUSED || status == Status.PLAYING || status == Status.STALLED){
            LOGGER.finer("Stopping playback");
            player.stop();
        }
        else if(status == Status.UNKNOWN){
            LOGGER.warning("Attempted to stop while player is still initializing. Will run stop() after player is done initializing");
            player.stop();
        }
    }

    @FXML
    private void onForward(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        Duration currentTime = player.getCurrentTime();

        if(currentTime.add(Duration.seconds(10)).lessThanOrEqualTo(player.getTotalDuration())){
            LOGGER.finer("Seeking 10 seconds forward");
            player.seek(currentTime.add(Duration.seconds(10)));
        }
        else{
            LOGGER.finer("Seeking to end of media");
            player.seek(player.getTotalDuration());
        }

        updateCurrentTime();
    }

    @FXML
    private void onMenuPreferences(){
        PreferencesStage preferencesStage = new PreferencesStage(getStage());
        try{
            preferencesStage.prepareStage();
            preferencesStage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }

    }

    @FXML
    private void onMenuAbout(){

    }

    @FXML
    private void onMenuExit(){
        System.exit(0);
    }

    /**
     * Handles RendererStateChanged events, triggered by uPnP state classes.
     * @param rendererState Current RendererState of MediaRenderer
     */
    private void onRendererStateChanged(RendererState rendererState){
        LOGGER.fine("Detected RendererState change to " + rendererState.name());
        MediaPlayer player = videoMediaView.getMediaPlayer();

        switch (rendererState){
            case NOMEDIAPRESENT:
                if(player != null){
                    onStop();
                }
                break;
            case STOPPED:
                if(player != null){
                    onStop();
                }
                break;
            case PLAYING:
                if(!rendererHandler.getUri().equals(currentUri)) {
                    LOGGER.finer("Creating new player for URI " + rendererHandler.getUri().toString());
                    currentUri = rendererHandler.getUri();

                    Media media = new Media(rendererHandler.getUri().toString());
                    videoMediaView.setMediaPlayer(new MediaPlayer(media));

                    rendererHandler.setVideoTotalTime(videoMediaView.getMediaPlayer().getTotalDuration());
                    prepareMediaPlayer();
                }
                else{
                    if(player != null && player.getStatus() == Status.PAUSED){
                        LOGGER.finer("Resuming playback");
                        player.play();
                        updateCurrentTime();
                    }
                }
                break;
            case PAUSED:
                if(player != null){
                    LOGGER.finer("Pausing playback");
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

            videoMediaView.fitHeightProperty().bind(getStage().heightProperty());
            videoMediaView.fitWidthProperty().bind(getStage().widthProperty());

            bottomBarVBox.setMaxWidth(Region.USE_PREF_SIZE);
            bottomBarVBox.setOpacity(0);
            getStage().getScene().setCursor(Cursor.NONE);
            menuBar.setOpacity(0);

            mouseIdle.setOnFinished(event -> {
                getStage().getScene().setCursor(Cursor.NONE);
                bottomBarVBox.setOpacity(0);
                menuBar.setOpacity(0);
            });

            videoMediaView.setOnMouseMoved(event -> {
                getStage().getScene().setCursor(Cursor.DEFAULT);
                bottomBarVBox.setOpacity(1);
                mouseIdle.playFromStart();
            });

            bottomBarVBox.setOnMouseEntered(event -> mouseIdle.pause());

            menuBar.setOnMouseEntered(event -> {
                mouseIdle.pause();
                menuBar.setOpacity(1);
            });
        }
        else {
            mouseIdle.setOnFinished(null);
            videoMediaView.setOnMouseMoved(null);
            bottomBarVBox.setOnMouseEntered(null);
            menuBar.setOnMouseEntered(null);

            double bottomBarHeight = bottomBarVBox.getHeight();
            double menuBarHeight = menuBar.getHeight();
            StackPane.setMargin(videoMediaView, new Insets(menuBarHeight,0, bottomBarHeight,0));

            // TBF I don't understand how this math works /shrug
            videoMediaView.fitHeightProperty().bind(getStage().heightProperty().subtract((bottomBarHeight + menuBarHeight) * 1.45));
            videoMediaView.fitWidthProperty().bind(getStage().widthProperty().subtract(10));

            bottomBarVBox.setMaxWidth(Region.USE_COMPUTED_SIZE);

            getStage().getScene().setCursor(Cursor.DEFAULT);
            bottomBarVBox.setOpacity(1);
            menuBar.setOpacity(1);
        }
    }

    private void clearSliderListeners(Slider timeSlider, ChangeListener<Boolean> timeChangingListener, ChangeListener<Number> timeChangeListener,
                                      Slider volumeSlider, ChangeListener<Boolean> volumeChangingListener, InvalidationListener volumeInvalidationListener){
        LOGGER.finest("Clearing media player slider listeners");

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

    private void updateCurrentTime(Duration currentTime, Duration totalTime){
        rendererHandler.setVideoCurrentTime(currentTime);

        transportHandler.setPositionInfo(
                rendererHandler.getUri(),
                rendererHandler.getMetadata(),
                totalTime,
                currentTime
        );
    }
}
