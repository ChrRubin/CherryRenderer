package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.MediaObject;
import com.chrrubin.cherryrenderer.api.ApiService;
import com.chrrubin.cherryrenderer.gui.custom.CustomMenuBar;
import com.chrrubin.cherryrenderer.gui.custom.LoadingVBox;
import com.chrrubin.cherryrenderer.gui.custom.MediaToolbar;
import com.chrrubin.cherryrenderer.gui.custom.TooltipVBox;
import com.chrrubin.cherryrenderer.upnp.AVTransportHandler;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.fourthline.cling.support.model.TransportState;

import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JfxPlayerStageController extends AbstractPlayerStageController{
    @FXML
    private StackPane rootStackPane;
    @FXML
    private CustomMenuBar menuBar;
    @FXML
    private MediaView videoMediaView;
    @FXML
    private MediaToolbar mediaToolbar;
    @FXML
    private TooltipVBox timeTooltipVBox;
    @FXML
    private TooltipVBox volumeTooltipVBox;
    @FXML
    private LoadingVBox loadingVBox;

    private final Logger LOGGER = Logger.getLogger(JfxPlayerStageController.class.getName());
    private ScheduledService<Void> updateTimeService;

    public JfxPlayerStageController(){
        super(Logger.getLogger(JfxPlayerStageController.class.getName()));
    }

    /*
    Start of property listeners and event handlers
     */
    /**
     * Listener for statusProperty of videoMediaView.getMediaPlayer()
     */
    private ChangeListener<MediaPlayer.Status> playerStatusListener = (observable, oldStatus, newStatus) -> {
        if(oldStatus == null){
            return;
        }

        LOGGER.finer("Media player exited "+ oldStatus.name() + " status, going into " + newStatus.name() + " status.");

        if(newStatus == MediaPlayer.Status.STALLED){
            LOGGER.fine("(Caught by listener) Media player in entered STALLED status. Video is currently buffering (?)");
        }
    };

    /**
     * Listener for currentTimeProperty of videoMediaView.getMediaPlayer()
     * Updates currentTimeLabel and timeSlider based on the current time of the media player.
     */
    // TODO: Previous InvalidationListener was causing deadlocks on edge cases. Continue testing this for deadlocks
    private ChangeListener<Duration> playerCurrentTimeListener = (observable, oldValue, newValue) -> {
        mediaToolbar.setCurrentTimeText(CherryUtil.durationToString(newValue));
        if(!mediaToolbar.isTimeSliderValueChanging()){
            mediaToolbar.setTimeSliderValue(newValue.divide(videoMediaView.getMediaPlayer().getTotalDuration().toMillis()).toMillis() * 100.0);
        }
    };

    /**
     * Listener for MuteProperty of videoMediaView.getMediaPlayer()
     * Changes the volume image based on whether player is muted.
     * Also notifies RenderingControlService of mute changes.
     */
    private InvalidationListener playerMuteListener = ((observable) -> {
        boolean isMute = videoMediaView.getMediaPlayer().muteProperty().get();
        mediaToolbar.changeVolumeImage(isMute);
        getRenderingControlHandler().setRendererMute(isMute);
    });

    /**
     * Listener for VolumeProperty of videoMediaView.getMediaPlayer()
     * Updates volumeSlider based on volume changes.
     * Also notifies RenderingControlService of volume changes.
     */
    private ChangeListener<Number> playerVolumeListener = ((observable, oldVolume, newVolume) -> {
        if(!mediaToolbar.isVolumeSliderValueChanging()){
            mediaToolbar.setVolumeSliderValue(newVolume.doubleValue() * 100);
        }
        getRenderingControlHandler().setRendererVolume(newVolume.doubleValue() * 100);
    });

    /**
     * Listener for isChangingProperty of timeSlider
     * Seeks video based on timeSlider value.
     */
    private ChangeListener<Boolean> timeIsChangingListener = (observable, wasChanging, isChanging) -> {
        if(!isChanging){
            videoMediaView.getMediaPlayer().seek(videoMediaView.getMediaPlayer().getTotalDuration().multiply(mediaToolbar.getTimeSliderValue() / 100.0));
            updateCurrentTime();
        }
    };

    /**
     * Listener for valueProperty of timeSlider
     * Seeks video based on timeSlider value.
     */
    private ChangeListener<Number> timeChangeListener = (observable, oldValue, newValue) -> {
        if(!mediaToolbar.isTimeSliderValueChanging() && Math.abs(newValue.doubleValue() - oldValue.doubleValue()) > 0.5){
            double newTime = newValue.doubleValue();
            videoMediaView.getMediaPlayer().seek(videoMediaView.getMediaPlayer().getTotalDuration().multiply(newTime / 100.0));
            updateCurrentTime();
        }
    };

    /**
     * Listener for isChangingProperty of volumeSlider
     * Changes volume of video based on volumeSlider value.
     */
    private ChangeListener<Boolean> volumeIsChangingListener = (observable, wasChanging, isChanging) -> {
        if(!isChanging){
            videoMediaView.getMediaPlayer().setVolume(mediaToolbar.getVolumeSliderValue() / 100.0);
        }
    };

    /**
     * Listener for valueProperty of volumeSlider
     * Changes volume of video based on volumeSlider value.
     */
    private InvalidationListener volumeInvalidationListener = observable -> {
        if(!mediaToolbar.isVolumeSliderValueChanging()){
            videoMediaView.getMediaPlayer().setVolume(mediaToolbar.getVolumeSliderValue() / 100.0);
        }
    };

    /**
     * EventHandler for onMouseClicked of videoMediaView
     * Toggles fullscreen when user double clicks on videoMediaView.
     */
    private EventHandler<MouseEvent> mediaViewClickEvent = event -> {
        if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
            onToggleFullScreen();
        }
    };

    /**
     * Listener for fullScreenProperty of getStage()
     * Prepares UI based on whether stage is full screen.
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

        double sliderValue = mediaToolbar.getTimeSliderValue();
        Duration sliderDragTime = videoMediaView.getMediaPlayer().getTotalDuration().multiply(sliderValue / 100.0);
        Duration timeDifference = sliderDragTime.subtract(currentTimeCalcProperty.get());
        String output = CherryUtil.durationToString(sliderDragTime) + System.lineSeparator() +
                "[" + CherryUtil.durationToString(timeDifference) + "]";

        timeTooltipVBox.setText(output);

        timeTooltipVBox.setVisible(true);

        Bounds timeSliderBounds = mediaToolbar.getTimeSlider().localToScene(mediaToolbar.getTimeSlider().getBoundsInLocal());
        if(event.getSceneX() < timeSliderBounds.getMinX()){
            timeTooltipVBox.setLayoutX(timeSliderBounds.getMinX() - (timeTooltipVBox.getWidth() / 2));
        }
        else if(event.getSceneX() > timeSliderBounds.getMaxX()){
            timeTooltipVBox.setLayoutX(timeSliderBounds.getMaxX() - (timeTooltipVBox.getWidth() / 2));
        }
        else{
            timeTooltipVBox.setLayoutX(event.getSceneX() - (timeTooltipVBox.getWidth() / 2));
        }
        timeTooltipVBox.setLayoutY(timeSliderBounds.getMinY() - 40);
    };

    /**
     * EventHandler for onMouseReleased of timeSlider
     * Hides the tooltip when user releases mouse click.
     */
    private EventHandler<MouseEvent> timeMouseReleasedEvent = event -> {
        timeTooltipVBox.setVisible(false);
        currentTimeCalcProperty.set(null);
        videoMediaView.requestFocus();
    };

    /**
     * EventHandler for onMouseDragged of volumeSlider
     * Shows a tooltip containing seek info when user drags on volumeSlider.
     */
    private EventHandler<MouseEvent> volumeMouseDraggedEvent = event -> {
        int sliderValue = (int)Math.round(mediaToolbar.getVolumeSliderValue());

        volumeTooltipVBox.setText(sliderValue + "%");

        volumeTooltipVBox.setVisible(true);

        Bounds volumeSliderBounds = mediaToolbar.getVolumeSlider().localToScene(mediaToolbar.getVolumeSlider().getBoundsInLocal());
        if(event.getSceneX() < volumeSliderBounds.getMinX()){
            volumeTooltipVBox.setLayoutX(volumeSliderBounds.getMinX() - (volumeTooltipVBox.getWidth() / 2));
        }
        else if(event.getSceneX() > volumeSliderBounds.getMaxX()){
            volumeTooltipVBox.setLayoutX(volumeSliderBounds.getMaxX() - (volumeTooltipVBox.getWidth() / 2));
        }
        else{
            volumeTooltipVBox.setLayoutX(event.getSceneX() - (volumeTooltipVBox.getWidth() / 2));
        }
        volumeTooltipVBox.setLayoutY(volumeSliderBounds.getMinY() - 25);
    };

    /**
     * EventHandler for onMouseReleased of volumeSlider
     * Hides the tooltip when user releases mouse click.
     */
    private EventHandler<MouseEvent> volumeMouseReleasedEvent = event -> {
        volumeTooltipVBox.setVisible(false);
        videoMediaView.requestFocus();
    };

    /**
     * EventHandler for onKeyReleased of videoMediaView
     * Allows keyboard interactions with media playback.
     */
    private EventHandler<KeyEvent> videoKeyReleasedEvent = event -> {
        switch (event.getCode()){
            case SPACE:
                onPlayPause();
                break;
            case S:
                onStop();
                break;
            case F:
                onToggleFullScreen();
                break;
            case M:
                onToggleMute();
                break;
            case LEFT:
                onRewind();
                break;
            case RIGHT:
                onForward();
                break;
            case UP:
                onIncreaseVolume();
                break;
            case DOWN:
                onDecreaseVolume();
                break;
        }
    };
    /*
    End of property listeners and event handlers
     */

    @Override
    public AbstractStage getStage() {
        return (AbstractStage)rootStackPane.getScene().getWindow();
    }

    @FXML
    private void initialize(){
        mediaToolbar.disableToolbar();
        menuBar.setSnapshotNode(videoMediaView);
        Platform.runLater(() -> {
            menuBar.setParentStage(getStage());
            if(CherryPrefs.AutoCheckUpdate.LOADED_VALUE){
                checkUpdate();
            }
        });
    }

    /**
     * Prepares MediaPlayer for video playing.
     * Attaches required property listeners and event handlers that are used during video playback.
     */
    void prepareMediaPlayback(){
        LOGGER.finer("Preparing Media Player for playback");

        AVTransportHandler avTransportHandler = getAvTransportHandler();
        ApiService apiService = getApiService();

        Platform.runLater(() -> loadingVBox.setWaitingVideo());

        avTransportHandler.setMediaObject(getCurrentMediaObject());
        if(apiService != null) {
            apiService.setCurrentlyPlayingMedia(getCurrentMediaObject());
        }

        prepareFullScreen(false);

        MediaPlayer player = videoMediaView.getMediaPlayer();

        player.setAutoPlay(true);

        player.setOnPlaying(() -> {
            mediaToolbar.setPlayPauseToPause();
            menuBar.setPlayPauseToPause();

            avTransportHandler.setTransportInfo(TransportState.PLAYING);
            if(apiService != null){
                apiService.setCurrentStatus(RendererState.PLAYING);
            }
        });

        player.setOnPaused(() -> {
            mediaToolbar.setPlayPauseToPlay();
            menuBar.setPlayPauseToPlay();

            avTransportHandler.setTransportInfo(TransportState.PAUSED_PLAYBACK);
            if(apiService != null) {
                apiService.setCurrentStatus(RendererState.PAUSED);
            }
        });

        player.setOnReady(() -> {
            videoMediaView.setVisible(true);
            Duration totalDuration = player.getTotalDuration();

            mediaToolbar.setTotalTimeText(CherryUtil.durationToString(totalDuration));
            avTransportHandler.setMediaInfoWithTotalTime(totalDuration);
            avTransportHandler.setPositionInfoWithTimes(totalDuration, Duration.ZERO);
            avTransportHandler.sendLastChangeMediaDuration(totalDuration);
            avTransportHandler.setTransportInfo(TransportState.PLAYING);

            if(apiService != null) {
                apiService.setCurrentStatus(RendererState.PLAYING);
            }

            String title = getCurrentMediaObject().getTitle();
            if (!title.isEmpty()) {
                LOGGER.finer("Video title is " + title);

                getStage().setTitle("CherryRenderer " + CherryPrefs.VERSION + " - " + title);
            } else {
                LOGGER.finer("Video title was not detected.");
            }

            mediaToolbar.enableToolbar();
            menuBar.enablePlaybackMenu();
            loadingVBox.setVisible(false);
        });

        player.setOnError(() -> {
            String error = player.getError().toString();

            LOGGER.log(Level.SEVERE, error, player.getError());
            Alert alert = getStage().createErrorAlert(error);
            alert.showAndWait();

            setCurrentMediaObject(null);
            loadingVBox.setWaitingConnection();

            avTransportHandler.clearInfo();
            avTransportHandler.setTransportInfo(TransportState.STOPPED);

            if(apiService != null) {
                apiService.setCurrentStatus(RendererState.STOPPED);
            }
        });

        player.setOnHalted(() -> {
            LOGGER.warning("Media player reached HALTED status. Disposing media player...");
            endOfMedia();
        });

        // TODO: Implement buffering handling
        //  For some reason Status.STALLED can't be used to determine whether player is buffering...
        player.setOnStalled(() ->
                LOGGER.fine("(Caught by setOnStalled) Media player in STALLED status. Video is currently buffering (?)"));

        player.statusProperty().addListener(playerStatusListener);

        player.currentTimeProperty().addListener(playerCurrentTimeListener);

        player.muteProperty().addListener(playerMuteListener);

        player.volumeProperty().addListener(playerVolumeListener);

        timeTooltipVBox.setManaged(false);

        volumeTooltipVBox.setManaged(false);

        /*
        Set MediaToolbar listeners and handlers
         */
        mediaToolbar.setTimeSliderListenersHandlers(timeChangeListener, timeIsChangingListener, timeMouseDraggedEvent, timeMouseReleasedEvent);
        mediaToolbar.setVolumeSliderListenersHandlers(volumeInvalidationListener, volumeIsChangingListener, volumeMouseDraggedEvent, volumeMouseReleasedEvent);

        /*
        Toggle fullscreen via double click
         */
        videoMediaView.setOnMouseClicked(mediaViewClickEvent);

        getStage().fullScreenProperty().addListener(isFullScreenListener);

        /*
        Allow key press handling on videoMediaView
         */
        videoMediaView.setOnKeyReleased(videoKeyReleasedEvent);

        player.setOnEndOfMedia(() -> {
            LOGGER.finest("player.setOnEndOfMedia triggered");
            endOfMedia();
        });

        player.setOnStopped(() -> {
            LOGGER.finest("player.setOnStopped triggered");
            endOfMedia();
        });

        /*
        ScheduledService that updates the current video time to control points every second.
         */
        updateTimeService = new ScheduledService<Void>(){
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if(videoMediaView.getMediaPlayer() != null && videoMediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                            updateCurrentTime();
                        }
                        return null;
                    }
                };
            }
        };

        updateTimeService.setPeriod(Duration.seconds(1));
        updateTimeService.start();

        videoMediaView.requestFocus();

    }

    /**
     * Runs the following cleanup tasks after video playback is done:
     * Properly removes property listeners and event handlers that should only be applied during playback.
     * Stops the service that auto updates PositionInfo
     * Disposes media player.
     * Disables and resets UI elements.
     */
    void endOfMedia(){
        LOGGER.fine("Running end of media function");

        AVTransportHandler avTransportHandler = getAvTransportHandler();
        ApiService apiService = getApiService();

        avTransportHandler.clearInfo();
        avTransportHandler.setTransportInfo(TransportState.STOPPED);

        if(apiService != null) {
            apiService.clearInfo();
            apiService.setCurrentStatus(RendererState.STOPPED);
        }

        getStage().setTitle("CherryRenderer " + CherryPrefs.VERSION);

        LOGGER.finer("Clearing property listeners & event handlers");
        mediaToolbar.clearTimeSliderListenersHandlers(timeChangeListener, timeIsChangingListener);
        mediaToolbar.clearVolumeSliderListenersHandlers(volumeInvalidationListener, volumeIsChangingListener);
        rootStackPane.setOnKeyReleased(null);
        getStage().fullScreenProperty().removeListener(isFullScreenListener);

        if(updateTimeService != null && updateTimeService.isRunning()){
            LOGGER.finer("Stopping auto update of PositionInfo service");
            updateTimeService.cancel();
        }

        if(videoMediaView.getMediaPlayer().getStatus() != MediaPlayer.Status.DISPOSED){
            LOGGER.finer("Disposing MediaPlayer");
            videoMediaView.getMediaPlayer().dispose();
        }

        mediaToolbar.setCurrentTimeText("--:--:--");
        mediaToolbar.setTotalTimeText("--:--:--");
        mediaToolbar.setTimeSliderValue(0);
        mediaToolbar.setVolumeSliderValue(100);
        mediaToolbar.disableToolbar();
        menuBar.disablePlaybackMenu();
        loadingVBox.setWaitingConnection();
        loadingVBox.setVisible(true);

        setCurrentMediaObject(null);

        videoMediaView.setOnMouseClicked(null);
        if(getStage().isFullScreen()){
            getStage().setFullScreen(false);
        }

        videoMediaView.setVisible(false);
    }

    @FXML
    void onPlayPause(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null){
            LOGGER.warning("MediaPlayer is null when tried to play/pause.");
            return;
        }

        MediaPlayer.Status status = player.getStatus();

        if(status == MediaPlayer.Status.UNKNOWN){
            LOGGER.warning("Attempted to pause/play video while player is still initializing. Ignoring.");
            return;
        }

        if(status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.STOPPED || status == MediaPlayer.Status.READY){
            player.play();
            updateCurrentTime();
        }
        else{
            player.pause();
            updateCurrentTime();
        }
    }

    @FXML
    void onRewind(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null){
            LOGGER.warning("MediaPlayer is null when tried to rewind.");
            return;
        }

        Duration currentTime = player.getCurrentTime();

        if(currentTime.greaterThanOrEqualTo(Duration.seconds(10))){
            LOGGER.finest("Rewinding 10 seconds backwards");
            player.seek(currentTime.subtract(Duration.seconds(10)));
        }
        else{
            LOGGER.finest("Rewinding to start of media");
            player.seek(Duration.ZERO);
        }

        updateCurrentTime();
    }

    @FXML
    void onStop(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null){
            LOGGER.warning("MediaPlayer is null when tried to stop.");
            return;
        }

        MediaPlayer.Status status = player.getStatus();

        if(status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.PLAYING || status == MediaPlayer.Status.STALLED){
            LOGGER.finer("Stopping playback");
            player.stop();
        }
        else if(status == MediaPlayer.Status.UNKNOWN){
            LOGGER.warning("Attempted to stop while player is still initializing. Will stop after finish initializing");
            player.stop();
        }
        else{
            LOGGER.warning("Attempted to stop while player has no status. Yes apparently this is a thing even though THERE IS A STATUS CALLED UNKNOWN THAT YOU'RE SUPPOSED TO USE");
            player.stop();
        }
    }

    @FXML
    void onForward(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null){
            LOGGER.warning("MediaPlayer is null when tried to fast forward.");
            return;
        }

        Duration currentTime = player.getCurrentTime();

        if(currentTime.add(Duration.seconds(10)).lessThanOrEqualTo(player.getTotalDuration())){
            LOGGER.finest("Seeking 10 seconds forward");
            player.seek(currentTime.add(Duration.seconds(10)));
        }
        else{
            LOGGER.finest("Seeking to end of media");
            player.seek(player.getTotalDuration());
        }

        updateCurrentTime();
    }

    @FXML
    void onToggleFullScreen() {
        if(!getStage().isFullScreen()){
            getStage().setFullScreen(true);
        }
        else{
            getStage().setFullScreen(false);
        }
    }

    @FXML
    void onToggleMute(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null || !Arrays.asList(MediaPlayer.Status.PAUSED, MediaPlayer.Status.PLAYING, MediaPlayer.Status.READY).contains(player.getStatus())){
            return;
        }

        if(player.muteProperty().get()){
            player.setMute(false);
        }
        else{
            player.setMute(true);
        }
    }

    @FXML
    void onIncreaseVolume(){
        mediaToolbar.getVolumeSlider().increment();
    }

    @FXML
    void onDecreaseVolume(){
        mediaToolbar.getVolumeSlider().decrement();
    }

    /**
     * Handles RendererStateChanged events, triggered by uPnP state classes via eventing thru RendererHandler.
     * @param rendererState Current RendererState of MediaRenderer
     */
    void onRendererStateChanged(RendererState rendererState){
        LOGGER.fine("Detected RendererState change to " + rendererState.name());
        MediaPlayer player = videoMediaView.getMediaPlayer();
        MediaObject currentMediaObject = getCurrentMediaObject();
        AVTransportHandler avTransportHandler = getAvTransportHandler();

        switch (rendererState){
            case NOMEDIAPRESENT:
                if(currentMediaObject != null){
                    switch (player.getStatus()){
                        case PLAYING:
                            avTransportHandler.setTransportInfo(TransportState.PLAYING);
                            break;
                        case PAUSED:
                            avTransportHandler.setTransportInfo(TransportState.PAUSED_PLAYBACK);
                            break;
                    }
                }
                break;
            case STOPPED:
                if(currentMediaObject != null){
                    onStop();
                }
                break;
            case PLAYING:
                MediaObject mediaObject = avTransportHandler.getMediaObject();
                URI currentUri = null;
                if(currentMediaObject != null){
                    currentUri = currentMediaObject.getUri();
                }

                if(!mediaObject.getUri().equals(currentUri)) {
                    playNewMedia(mediaObject);
                }
                else if(player.getStatus() == MediaPlayer.Status.PAUSED){
                    LOGGER.finer("Resuming playback");
                    player.play();
                    updateCurrentTime();
                }
                break;
            case PAUSED:
                if(currentMediaObject != null){
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
    void prepareFullScreen(boolean isFullScreen){
        videoMediaView.fitHeightProperty().unbind();
        videoMediaView.fitWidthProperty().unbind();

        PauseTransition mouseIdleTimer = getMouseIdleTimer();

        if(isFullScreen){
            StackPane.setMargin(videoMediaView, new Insets(0,0,0,0));

            videoMediaView.fitHeightProperty().bind(getStage().heightProperty());
            videoMediaView.fitWidthProperty().bind(getStage().widthProperty());

            mediaToolbar.setMaxWidth(Region.USE_PREF_SIZE);
            mediaToolbar.setOpacity(0);
            getStage().getScene().setCursor(Cursor.NONE);
            menuBar.setOpacity(0);

            mouseIdleTimer.setOnFinished(event -> {
                getStage().getScene().setCursor(Cursor.NONE);
                mediaToolbar.setOpacity(0);
                menuBar.setOpacity(0);
            });

            videoMediaView.setOnMouseMoved(event -> {
                getStage().getScene().setCursor(Cursor.DEFAULT);
                mediaToolbar.setOpacity(1);
                mouseIdleTimer.playFromStart();
            });

            mediaToolbar.setOnMouseEntered(event -> mouseIdleTimer.pause());

            menuBar.setOnMouseEntered(event -> {
                mouseIdleTimer.pause();
                menuBar.setOpacity(1);
            });
        }
        else {
            mouseIdleTimer.setOnFinished(null);
            videoMediaView.setOnMouseMoved(null);
            mediaToolbar.setOnMouseEntered(null);
            menuBar.setOnMouseEntered(null);

            double bottomBarHeight = mediaToolbar.getHeight();
            double menuBarHeight = menuBar.getHeight();
            StackPane.setMargin(videoMediaView, new Insets(menuBarHeight,0, bottomBarHeight,0));

            videoMediaView.fitHeightProperty().bind(getStage().getScene().heightProperty().subtract(bottomBarHeight + menuBarHeight));
            videoMediaView.fitWidthProperty().bind(getStage().getScene().widthProperty());

            mediaToolbar.setMaxWidth(Region.USE_COMPUTED_SIZE);

            getStage().getScene().setCursor(Cursor.DEFAULT);
            mediaToolbar.setOpacity(1);
            menuBar.setOpacity(1);
        }
    }

    /**
     * Notifies control point of current time changes via transportHandler
     */
    void updateCurrentTime(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null || !Arrays.asList(MediaPlayer.Status.PAUSED, MediaPlayer.Status.PLAYING, MediaPlayer.Status.READY).contains(player.getStatus())){
            return;
        }

        Duration currentDuration = player.getCurrentTime();
        Duration totalDuration = player.getTotalDuration();

        getAvTransportHandler().setPositionInfoWithTimes(totalDuration, currentDuration);
        if(getApiService() != null) {
            getApiService().updateCurrentlyPlayingInfo(currentDuration, totalDuration, videoMediaView.getMediaPlayer().isMute(), (int) Math.round(mediaToolbar.getVolumeSliderValue()));
        }
    }

    /**
     * Handles RendererVolumeChanged events, triggered by RenderingControlService via eventing thru RenderingControlHandler
     * @param volume volume set by control point
     */
    void onRendererVolumeChanged(double volume){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player != null && Arrays.asList(MediaPlayer.Status.PAUSED, MediaPlayer.Status.PLAYING, MediaPlayer.Status.READY).contains(player.getStatus())){
            player.setVolume(volume / 100.0);
        }
    }

    /**
     * Handles RendererMuteChanged events, triggered by RenderingControlService via eventing thru RenderingControlHandler
     * @param isMute whether control point enabled/disabled mute
     */
    void onRendererMuteChanged(boolean isMute){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player != null && Arrays.asList(MediaPlayer.Status.PAUSED, MediaPlayer.Status.PLAYING, MediaPlayer.Status.READY).contains(player.getStatus())){
            player.setMute(isMute);
        }
    }

    /**
     * Set MediaPlayer to play a new media
     * @param mediaObject media to play
     */
    void playNewMedia(MediaObject mediaObject){
        Platform.runLater(() -> {
            getAvTransportHandler().setTransportInfo(TransportState.TRANSITIONING);
            if(getApiService() != null){
                getApiService().setCurrentStatus(RendererState.TRANSITIONING);
            }

            MediaPlayer player = videoMediaView.getMediaPlayer();
            if(player != null && player.getStatus() != MediaPlayer.Status.DISPOSED){
                LOGGER.warning("Tried to create new player while existing player still running. Forcing endOfMedia.");
                endOfMedia();
            }

            LOGGER.fine("Creating new player for URI " + mediaObject.getUriString());
            setCurrentMediaObject(mediaObject);

            videoMediaView.setMediaPlayer(new MediaPlayer(mediaObject.toJFXMedia()));

            prepareMediaPlayback();
        });
    }

    /**
     * For use when seeking to a specific duration
     * @param target target to seek to
     */
    void seekSpecificDuration(Duration target){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null || !Arrays.asList(MediaPlayer.Status.PAUSED, MediaPlayer.Status.PLAYING, MediaPlayer.Status.READY).contains(player.getStatus())){
            return;
        }

        if(target.lessThanOrEqualTo(player.getTotalDuration())){
            LOGGER.finer("Seeking to " + CherryUtil.durationToString(target));
            player.seek(target);
            updateCurrentTime();
        }
    }
}
