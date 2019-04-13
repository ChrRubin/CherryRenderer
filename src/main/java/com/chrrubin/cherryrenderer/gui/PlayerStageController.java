package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.CherryRenderer;
import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.RendererService;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.fourthline.cling.support.model.TransportState;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
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
    @FXML
    private VBox tooltipVBox;
    @FXML
    private Label tooltipLabel;

    private final Logger LOGGER = Logger.getLogger(PlayerStageController.class.getName());

    private URI currentUri = null;
    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();
    private PauseTransition mouseIdleTimer = new PauseTransition(Duration.seconds(1));

    /**
     * ScheduledService that updates PositionInfo every X seconds.
     */
    private ScheduledService<Void> updateTimeService = new ScheduledService<Void>(){
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

    /*
    Start of property listeners and event handlers
     */
    /**
     * Listener for statusProperty of videoMediaView.getPlayer()
     */
    private ChangeListener<Status> playerStatusListener = (observable, oldStatus, newStatus) -> {
        if(oldStatus == null){
            return;
        }

        LOGGER.finer("Media player exited "+ oldStatus.name() + " status, going into " + newStatus.name() + " status.");

        if(newStatus == Status.STALLED){
            LOGGER.fine("(Caught by listener) Media player in STALLED status. Video is currently buffering.");
        }
    };

    /**
     * Listener for currentTimeProperty of videoMediaView.getPlayer()
     * Updates currentTimeLabel and timeSlider based on the current time of the media player.
     */
    // TODO: Previous InvalidationListener was causing deadlocks on edge cases. Continue testing this for deadlocks
    private ChangeListener<Duration> playerCurrentTimeListener = (observable, oldValue, newValue) -> {
        currentTimeLabel.setText(CherryUtil.durationToString(newValue));
        if(!timeSlider.isValueChanging()){
            timeSlider.setValue(newValue.divide(videoMediaView.getMediaPlayer().getTotalDuration().toMillis()).toMillis() * 100.0);
        }
    };

    /**
     * Listener for isChangingProperty of timeSlider.
     */
    private ChangeListener<Boolean> timeIsChangingListener = (observable, wasChanging, isChanging) -> {
        if(!isChanging){
            videoMediaView.getMediaPlayer().seek(videoMediaView.getMediaPlayer().getTotalDuration().multiply(timeSlider.getValue() / 100.0));
            updateCurrentTime();
        }
    };

    /**
     * Listener for valueProperty of timeSlider.
     */
    private ChangeListener<Number> timeChangeListener = (observable, oldValue, newValue) -> {
        if(!timeSlider.isValueChanging()){
            double currentTime = oldValue.doubleValue();
            double newTime = newValue.doubleValue();
            if(Math.abs(newTime - currentTime) > 0.5) {

                videoMediaView.getMediaPlayer().seek(videoMediaView.getMediaPlayer().getTotalDuration().multiply(newTime / 100.0));
                updateCurrentTime();
            }
        }
    };

    /**
     * Listener for isChangingProperty of volumeSlider,
     */
    private ChangeListener<Boolean> volumeIsChangingListener = (observable, wasChanging, isChanging) -> {
        if(!isChanging){
            videoMediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100.0);
        }
    };

    /**
     * Listener for valueProperty of volumeSlider
     */
    private InvalidationListener volumeInvalidationListener = observable -> {
        if(!volumeSlider.isValueChanging()){
            videoMediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100.0);
        }
    };

    /**
     * EventHandler for onMouseClicked of videoMediaView
     * Toggles fullscreen when user double clicks on videoMediaView
     */
    private EventHandler<MouseEvent> mediaViewClickEvent = event -> {
        if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2){
                if(!getStage().isFullScreen()){
                    getStage().setFullScreen(true);
                }
                else{
                    getStage().setFullScreen(false);
                }
            }
        }
    };

    /**
     * Listener for fullScreenProperty of getStage()
     */
    private ChangeListener<Boolean> isFullScreenListener = (observable, wasFullScreen, isFullScreen) ->
            prepareFullScreen(isFullScreen);

    private final ObjectProperty<Duration> currentTimeCalcProperty = new SimpleObjectProperty<>();

    /**
     * EventHandler for onMouseDragged of timeSlider
     * Shows a tooltip containing seek info when user drags on timeSlider.
     */
    private EventHandler<MouseEvent> timeMouseDraggedEvent = event -> {
        if(currentTimeCalcProperty.get() == null){
            currentTimeCalcProperty.set(videoMediaView.getMediaPlayer().getCurrentTime());
        }

        double sliderValue = timeSlider.getValue();
        Duration sliderDragTime = videoMediaView.getMediaPlayer().getTotalDuration().multiply(sliderValue / 100.0);
        Duration timeDifference = sliderDragTime.subtract(currentTimeCalcProperty.get());
        String output = CherryUtil.durationToString(sliderDragTime) + System.lineSeparator() +
                "[" + CherryUtil.durationToString(timeDifference) + "]";

        tooltipLabel.setText(output);

        tooltipVBox.setVisible(true);

        Bounds timeSliderBounds = timeSlider.localToScene(timeSlider.getBoundsInLocal());
        if(event.getSceneX() < timeSliderBounds.getMinX()){
            tooltipVBox.setLayoutX(timeSliderBounds.getMinX() - (tooltipVBox.getWidth() / 2));
        }
        else if(event.getSceneX() > timeSliderBounds.getMaxX()){
            tooltipVBox.setLayoutX(timeSliderBounds.getMaxX() - (tooltipVBox.getWidth() / 2));
        }
        else{
            tooltipVBox.setLayoutX(event.getSceneX() - (tooltipVBox.getWidth() / 2));
        }
        tooltipVBox.setLayoutY(timeSliderBounds.getMinY() - 40);
    };

    /**
     * EventHandler for onMouseReleased of timeSlider
     * Hides the tooltip when user releases mouse click.
     */
    private EventHandler<MouseEvent> timeMouseReleasedEvent = event -> {
        tooltipVBox.setVisible(false);
        currentTimeCalcProperty.set(null);
    };
    /*
    End of property listeners and event handlers
     */


    public BaseStage getStage() {
        return (BaseStage) rootStackPane.getScene().getWindow();
    }

    public void initialize(){
        Preferences preferences = Preferences.userNodeForPackage(CherryRenderer.class);

        String friendlyName = preferences.get(CherryPrefs.FriendlyName.KEY, CherryPrefs.FriendlyName.DEFAULT);
        LOGGER.info("Current device friendly name is " + friendlyName);
        RendererService handler = new RendererService(friendlyName);
        handler.startService();

        rendererHandler.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);
    }

    /**
     * Prepares MediaPlayer for video playing.
     * Attaches required property listeners and event handlers that are used during video playback.
     */
    private void prepareMediaPlayback(){
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
            Duration totalDuration = player.getTotalDuration();

            totalTimeLabel.setText(CherryUtil.durationToString(totalDuration));
            rendererHandler.setVideoTotalTime(totalDuration);
            transportHandler.setMediaInfo(
                    rendererHandler.getUri(),
                    rendererHandler.getMetadata(),
                    totalDuration
            );
            transportHandler.setPositionInfo(
                    rendererHandler.getUri(),
                    rendererHandler.getMetadata(),
                    totalDuration,
                    new Duration(0)
            );
            transportHandler.sendLastChangeMediaDuration(player.getTotalDuration());
            startOfMedia();
        });

        // TODO: Handle player errors better (media not supported, player halted etc)
        player.setOnError(() -> {
            String error = player.getError().toString();

            LOGGER.log(Level.SEVERE, error, player.getError());
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "An error has occured: " + System.lineSeparator() + error + System.lineSeparator() +
                    "Please refer to logs for more detail.",
                    ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
        });

        player.setOnHalted(() -> {
            LOGGER.warning("Media player reached HALTED status. Disposing media player...");
            endOfMedia();
        });

        // TODO: Implement buffering handling
        //  For some reason Status.STALLED can't be used to determine whether player is buffering...
        player.setOnStalled(() ->
                LOGGER.fine("(Caught by setOnStalled) Media player in STALLED status. Video is currently buffering."));

        player.statusProperty().addListener(playerStatusListener);

        player.currentTimeProperty().addListener(playerCurrentTimeListener);

        /*
        Allow manipulation of video via timeSlider & volumeSlider
         */
        timeSlider.valueChangingProperty().addListener(timeIsChangingListener);

        timeSlider.valueProperty().addListener(timeChangeListener);

        volumeSlider.valueChangingProperty().addListener(volumeIsChangingListener);

        volumeSlider.valueProperty().addListener(volumeInvalidationListener);

        /*
        Toggle fullscreen via double click
         */
        videoMediaView.setOnMouseClicked(mediaViewClickEvent);

        getStage().fullScreenProperty().addListener(isFullScreenListener);

        /*
        Shows tooltip when dragging the thumb of timeSlider
         */
        tooltipVBox.setManaged(false);

        timeSlider.setOnMouseDragged(timeMouseDraggedEvent);

        timeSlider.setOnMouseReleased(timeMouseReleasedEvent);

        player.setOnEndOfMedia(() -> {
            LOGGER.finest("player.setOnEndOfMedia triggered");
            endOfMedia();
        });

        player.setOnStopped(() -> {
            LOGGER.finest("player.setOnStopped triggered");
            endOfMedia();
        });

        updateTimeService.setPeriod(Duration.seconds(1));
        updateTimeService.start();

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

    /**
     * Runs the following cleanup tasks after video playback is done:
     * Properly removes property listeners and event handlers that should only be applied during playback.
     * Stops the service that auto updates PositionInfo
     * Disposes media player.
     * Disables and resets UI elements.
     */
    private void endOfMedia(){
        LOGGER.finer("Running end of media function");

        LOGGER.finer("Clearing property listeners & event handlers");
        timeSlider.valueChangingProperty().removeListener(timeIsChangingListener);
        timeSlider.valueProperty().removeListener(timeChangeListener);
        volumeSlider.valueChangingProperty().removeListener(volumeIsChangingListener);
        volumeSlider.valueProperty().removeListener(volumeInvalidationListener);
        timeSlider.setOnMouseDragged(null);
        timeSlider.setOnMouseReleased(null);
        getStage().fullScreenProperty().removeListener(isFullScreenListener);

        if(updateTimeService != null){
            LOGGER.finer("Stopping auto update of PositionInfo service");
            updateTimeService.cancel();
        }

        if(videoMediaView.getMediaPlayer() != null){
            LOGGER.finer("Disposing MediaPlayer");
            videoMediaView.getMediaPlayer().dispose();
        }

        playButton.setText(">");
        currentTimeLabel.setText("--:--:--");
        totalTimeLabel.setText("--:--:--");
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
        transportHandler.clearInfo();
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
        else{
            LOGGER.warning("Attempted to stop while player has no status. Yes apparently this is a thing even though THERE IS A STATUS CALLED UNKNOWN THAT YOU'RE SUPPOSED TO USE");
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
     * Handles RendererStateChanged events, triggered by uPnP state classes via eventing thru RendererHandler.
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
                    prepareMediaPlayback();
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

            mouseIdleTimer.setOnFinished(event -> {
                getStage().getScene().setCursor(Cursor.NONE);
                bottomBarVBox.setOpacity(0);
                menuBar.setOpacity(0);
            });

            videoMediaView.setOnMouseMoved(event -> {
                getStage().getScene().setCursor(Cursor.DEFAULT);
                bottomBarVBox.setOpacity(1);
                mouseIdleTimer.playFromStart();
            });

            bottomBarVBox.setOnMouseEntered(event -> mouseIdleTimer.pause());

            menuBar.setOnMouseEntered(event -> {
                mouseIdleTimer.pause();
                menuBar.setOpacity(1);
            });
        }
        else {
            mouseIdleTimer.setOnFinished(null);
            videoMediaView.setOnMouseMoved(null);
            bottomBarVBox.setOnMouseEntered(null);
            menuBar.setOnMouseEntered(null);

            double bottomBarHeight = bottomBarVBox.getHeight();
            double menuBarHeight = menuBar.getHeight();
            StackPane.setMargin(videoMediaView, new Insets(menuBarHeight,0, bottomBarHeight,0));

            // Frankly I don't understand how this math works /shrug
            videoMediaView.fitHeightProperty().bind(getStage().heightProperty().subtract((bottomBarHeight + menuBarHeight) * 1.45));
            videoMediaView.fitWidthProperty().bind(getStage().widthProperty().subtract(10));

            bottomBarVBox.setMaxWidth(Region.USE_COMPUTED_SIZE);

            getStage().getScene().setCursor(Cursor.DEFAULT);
            bottomBarVBox.setOpacity(1);
            menuBar.setOpacity(1);
        }
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
