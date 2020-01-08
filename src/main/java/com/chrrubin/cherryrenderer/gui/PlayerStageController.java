package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.MediaObject;
import com.chrrubin.cherryrenderer.api.ApiService;
import com.chrrubin.cherryrenderer.gui.custom.*;
import com.chrrubin.cherryrenderer.prefs.*;
import com.chrrubin.cherryrenderer.upnp.AVTransportHandler;
import com.chrrubin.cherryrenderer.upnp.RendererService;
import com.chrrubin.cherryrenderer.upnp.RenderingControlHandler;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.fourthline.cling.support.model.TransportState;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

public class PlayerStageController implements IController {
    private final Logger LOGGER = Logger.getLogger(PlayerStageController.class.getName());
    @FXML
    private StackPane rootStackPane;
    @FXML
    private IPlayer player;
    @FXML
    private CustomMenuBar menuBar;
    @FXML
    private MediaToolbar mediaToolbar;
    @FXML
    private TooltipVBox timeTooltipVBox;
    @FXML
    private TooltipVBox volumeTooltipVBox;
    @FXML
    private LoadingVBox loadingVBox;

    private MediaObject currentMediaObject = null;
    private AVTransportHandler avTransportHandler = AVTransportHandler.getInstance();
    private RenderingControlHandler renderingControlHandler = RenderingControlHandler.getInstance();
    private PauseTransition mouseIdleTimer = new PauseTransition(Duration.seconds(1));
    private ApiService apiService = null;
    private ScheduledService<Void> updateTimeService;
    private String playerName;
    private double currentResizeRatio;

    /*
    Start of property listeners and event handlers
     */
    /**
     * Listener for currentTimeProperty of media player
     * Updates currentTimeLabel and timeSlider based on the current time of the media player.
     */
    private ChangeListener<Duration> playerCurrentTimeListener = (observable, oldValue, newValue) -> {
        Platform.runLater(() -> {
            mediaToolbar.setCurrentTimeText(CherryUtil.durationToString(newValue));
            if(!mediaToolbar.isTimeSliderValueChanging()){
                mediaToolbar.setTimeSliderValue(newValue.divide(player.getTotalTime().toMillis()).toMillis() * 100.0);
            }
        });
    };

    /**
     * Listener for MuteProperty of media player
     * Changes the volume image based on whether player is muted.
     * Also notifies RenderingControlService of mute changes.
     */
    private InvalidationListener playerMuteListener = ((observable) -> {
        Platform.runLater(() -> {
            boolean isMute = player.isMute();
            mediaToolbar.changeVolumeImage(isMute);
            renderingControlHandler.setRendererMute(isMute);
        });
    });

    /**
     * Listener for VolumeProperty of media player
     * Updates volumeSlider based on volume changes.
     * Also notifies RenderingControlService of volume changes.
     */
    private ChangeListener<Number> playerVolumeListener = ((observable, oldVolume, newVolume) -> {
        Platform.runLater(() -> {
            if(!mediaToolbar.isVolumeSliderValueChanging()){
                mediaToolbar.setVolumeSliderValue(newVolume.doubleValue() * 100);
            }
            renderingControlHandler.setRendererVolume(newVolume.doubleValue() * 100);
        });
    });

    /**
     * Listener for isChangingProperty of timeSlider
     * Seeks video based on timeSlider value.
     */
    private ChangeListener<Boolean> timeIsChangingListener = (observable, wasChanging, isChanging) -> {
        if(!isChanging){
            player.seek(player.getTotalTime().multiply(mediaToolbar.getTimeSliderValue() / 100.0));
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
            player.seek(player.getTotalTime().multiply(newTime / 100.0));
            updateCurrentTime();
        }
    };

    /**
     * Listener for isChangingProperty of volumeSlider
     * Changes volume of video based on volumeSlider value.
     */
    private ChangeListener<Boolean> volumeIsChangingListener = (observable, wasChanging, isChanging) -> {
        if(!isChanging){
            player.setVolume(mediaToolbar.getVolumeSliderValue());
        }
    };

    /**
     * Listener for valueProperty of volumeSlider
     * Changes volume of video based on volumeSlider value.
     */
    private InvalidationListener volumeInvalidationListener = observable -> {
        if(!mediaToolbar.isVolumeSliderValueChanging()){
            player.setVolume(mediaToolbar.getVolumeSliderValue());
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
            currentTimeCalcProperty.set(player.getCurrentTime());
        }

        double sliderValue = mediaToolbar.getTimeSliderValue();
        Duration sliderDragTime = player.getTotalTime().multiply(sliderValue / 100.0);
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
        player.getNode().requestFocus();
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
        player.getNode().requestFocus();
    };

    /**
     * EventHandler for onKeyReleased of player
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
            case Z:
                cycleResizeWindow();
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
        if(new EnableApiPreference().get()){
            try{
                apiService = new ApiService();
                apiService.getMediaObjectEvent().addListener(this::playNewMedia);
                apiService.getTogglePauseEvent().addListener(object -> onPlayPause());
                apiService.getSeekEvent().addListener(this::seekSpecificDuration);
                apiService.getStopEvent().addListener(object -> onStop());
                apiService.getToggleMuteEvent().addListener(object -> onToggleMute());
                apiService.getSetVolumeEvent().addListener(this::onRendererVolumeChanged);
                LOGGER.info("API is enabled and running");
            }
            catch (IOException | RuntimeException e){
                LOGGER.log(Level.SEVERE, e.toString(), e);
                Platform.runLater(() -> {
                    Alert alert = getStage().createErrorAlert(e.toString());
                    alert.showAndWait();
                });
            }
        }

        String friendlyName = new FriendlyNamePreference().get();
        LOGGER.info("Current device friendly name is " + friendlyName);
        RendererService rendererService = new RendererService(friendlyName);
        rendererService.startService();

        avTransportHandler.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);
        avTransportHandler.getVideoSeekEvent().addListener(this::seekSpecificDuration);
        renderingControlHandler.getVideoVolumeEvent().addListener(this::onRendererVolumeChanged);
        renderingControlHandler.getVideoMuteEvent().addListener(this::onRendererMuteChanged);

        mediaToolbar.disableToolbar();
        menuBar.setSnapshotImageSupplier(player::getSnapshot);
        Platform.runLater(() -> {
            menuBar.setParentStage(getStage());
            if(new AutoCheckUpdatePreference().get()){
                checkUpdate();
            }

            if(new SaveWindowSizePreference().get()) {
                double savedWidth = new WindowLastWidthPreference().get();
                double savedHeight = new WindowLastHeightPreference().get();
                LOGGER.info("Setting window size to " + savedWidth + " x " + savedHeight);
                getStage().setWidth(savedWidth);
                getStage().setHeight(savedHeight);
            }
        });

        if(player.getClass() == JfxMediaView.class){
            playerName = " [JFX]";
        }
        else if(player.getClass() == VlcPlayerCanvas.class){
            playerName = " [VLC]";
        }
        else {
            playerName = " [???]";
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(!(new SaveWindowSizePreference().get())){
                return;
            }
            double lastWidth = getStage().getWidth();
            double lastHeight = getStage().getHeight();
            if(getStage().isMaximized() || lastHeight == 0.0 || lastWidth == 0.0){
                return;
            }
            LOGGER.info("Saving window size as " + lastWidth + " x " + lastHeight);
            try {
                AbstractPreference<Double> lastHeightPreference = new WindowLastHeightPreference();
                AbstractPreference<Double> lastWidthPreference = new WindowLastWidthPreference();
                lastHeightPreference.put(lastHeight);
                lastWidthPreference.put(lastWidth);

                lastHeightPreference.forceFlush();
            }
            catch (BackingStoreException e){
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }
        }));
    }

    private void checkUpdate(){
        LOGGER.info("Checking for updates...");
        Service<String> getLatestVersionService = CherryUtil.getLatestVersionJFXService();

        getLatestVersionService.setOnSucceeded(event ->{
            String latestVersion = getLatestVersionService.getValue();
            LOGGER.info("Latest version is " + latestVersion);
            if(CherryUtil.isOutdated(latestVersion)){
                LOGGER.info("Current version is outdated!");
                AbstractStage updaterStage = new UpdaterStage(getStage(), latestVersion);
                try{
                    updaterStage.prepareStage();
                    updaterStage.show();
                }
                catch (IOException e){
                    LOGGER.log(Level.SEVERE, e.toString(), e);
                    Alert alert = getStage().createErrorAlert(e.toString());
                    alert.showAndWait();
                }
            }
        });

        getLatestVersionService.setOnFailed(event -> {
            Throwable e = getLatestVersionService.getException();

            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = getStage().createErrorAlert(e.toString());
            alert.showAndWait();
        });

        getLatestVersionService.start();
    }

    /**
     * Prepares media player for video playing.
     * Attaches required property listeners and event handlers that are used during video playback.
     */
    private void prepareMediaPlayback(){
        LOGGER.finer("Preparing Media Player for playback");

        Platform.runLater(() -> loadingVBox.setWaitingVideo());

        avTransportHandler.setMediaObject(currentMediaObject);
        if(apiService != null) {
            apiService.setCurrentlyPlayingMedia(currentMediaObject);
        }

        prepareFullScreen(false);

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
            player.getNode().setVisible(true);
            Duration totalDuration = player.getTotalTime();

            mediaToolbar.setTotalTimeText(CherryUtil.durationToString(totalDuration));
            avTransportHandler.setMediaInfoWithTotalTime(totalDuration);
            avTransportHandler.setPositionInfoWithTimes(totalDuration, Duration.ZERO);
            avTransportHandler.sendLastChangeMediaDuration(totalDuration);
            avTransportHandler.setTransportInfo(TransportState.PLAYING);

            if(apiService != null) {
                apiService.setCurrentStatus(RendererState.PLAYING);
            }

            String title = currentMediaObject.getTitle();

            if (!title.isEmpty()) {
                LOGGER.finer("Video title is " + title);

                getStage().setTitle(title + " - CherryRenderer " + CherryUtil.VERSION + playerName);
            } else {
                LOGGER.finer("Video title was not detected.");
            }

            mediaToolbar.enableToolbar();
            menuBar.enablePlaybackMenu();
            loadingVBox.setVisible(false);

            autoResizeWindow();
        });

        player.setOnError(() -> {
            Throwable error = player.getError();
            String errorMessage = player.getErrorMessage();

            if(error != null) {
                LOGGER.log(Level.SEVERE, errorMessage, error);
            }
            else {
                LOGGER.severe(errorMessage);
            }
            Alert alert = getStage().createErrorAlert(errorMessage);
            alert.showAndWait();

            currentMediaObject = null;
            loadingVBox.setWaitingConnection();

            avTransportHandler.clearInfo();
            avTransportHandler.setTransportInfo(TransportState.STOPPED);

            if(apiService != null) {
                apiService.setCurrentStatus(RendererState.STOPPED);
                apiService.clearInfo();
            }
        });

        player.setOnBuffering(() -> {
            // JFX doesn't handle buffering for some reason, VLC handles buffering but doesn't have a clear way to tell when buffering has ended
            // I'm just keeping this here in case I figure something out to handle buffering
        });

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
        player.getNode().setOnMouseClicked(mediaViewClickEvent);

        getStage().fullScreenProperty().addListener(isFullScreenListener);

        /*
        Allow key press handling on videoMediaView
         */
        player.getNode().setOnKeyReleased(videoKeyReleasedEvent);

        player.setOnFinished(() -> {
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
        if(updateTimeService != null && updateTimeService.isRunning()){
            LOGGER.warning("updateTimeService still running. Cancelling...");
            updateTimeService.cancel();
        }
        updateTimeService = new ScheduledService<Void>(){
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if(player.getStatus() == PlayerStatus.PLAYING) {
                            updateCurrentTime();
                        }
                        return null;
                    }
                };
            }
        };

        updateTimeService.setPeriod(Duration.seconds(1));
        updateTimeService.start();

        player.getNode().requestFocus();
    }

    /**
     * Runs the following cleanup tasks after video playback is done:
     * Properly removes property listeners and event handlers that should only be applied during playback.
     * Stops the service that auto updates PositionInfo
     * Disposes media player (for JFX Media Player).
     * Disables and resets UI elements.
     */
    private void endOfMedia(){
        LOGGER.fine("Running end of media function");

        avTransportHandler.clearInfo();
        avTransportHandler.setTransportInfo(TransportState.STOPPED);

        if(apiService != null) {
            apiService.clearInfo();
            apiService.setCurrentStatus(RendererState.STOPPED);
        }

        getStage().setTitle("CherryRenderer " + CherryUtil.VERSION + playerName);

        if(getStage().isFullScreen()){
            getStage().setFullScreen(false);
        }

        LOGGER.finer("Clearing player property listeners");
        player.currentTimeProperty().removeListener(playerCurrentTimeListener);
        player.muteProperty().removeListener(playerMuteListener);
        player.volumeProperty().removeListener(playerVolumeListener);
        player.getNode().setOnMouseClicked(null);
        player.getNode().setOnKeyReleased(null);
        player.getNode().setVisible(false);
        player.disposePlayer();

        LOGGER.finer("Clearing UI property listeners & event handlers");
        mediaToolbar.clearTimeSliderListenersHandlers(timeChangeListener, timeIsChangingListener);
        mediaToolbar.clearVolumeSliderListenersHandlers(volumeInvalidationListener, volumeIsChangingListener);
        getStage().fullScreenProperty().removeListener(isFullScreenListener);

        if(updateTimeService != null && updateTimeService.isRunning()){
            LOGGER.finer("Stopping auto update of PositionInfo service");
            updateTimeService.cancel();
        }

        mediaToolbar.disableToolbar();
        menuBar.disablePlaybackMenu();
        loadingVBox.setWaitingConnection();
        loadingVBox.setVisible(true);

        currentMediaObject = null;
    }

    @FXML
    private void onMediaInfo(){
        AbstractStage mediaInfoStage = new MediaInfoStage(getStage(), currentMediaObject, player.getVideoWidth(), player.getVideoHeight());
        try{
            mediaInfoStage.prepareStage();
            mediaInfoStage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = getStage().createErrorAlert(e.toString());
            alert.showAndWait();
        }
    }

    @FXML
    private void onPlayPause(){
        if(player.getStatus() == PlayerStatus.PLAYING){
            player.pause();
        }
        else if(player.getStatus() == PlayerStatus.PAUSED){
            player.play();
        }

        updateCurrentTime();
    }

    @FXML
    private void onRewind(){
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
    private void onStop(){
        player.stop();
    }

    @FXML
    private void onForward(){
        Duration currentTime = player.getCurrentTime();

        if(currentTime.add(Duration.seconds(10)).lessThanOrEqualTo(player.getTotalTime())){
            LOGGER.finest("Seeking 10 seconds forward");
            player.seek(currentTime.add(Duration.seconds(10)));
        }
        else{
            LOGGER.finest("Seeking to end of media");
            player.seek(player.getTotalTime());
        }

        updateCurrentTime();
    }

    @FXML
    private void onToggleFullScreen() {
        if(!getStage().isFullScreen()){
            getStage().setFullScreen(true);
        }
        else{
            getStage().setFullScreen(false);
        }
    }

    @FXML
    private void onToggleMute(){
        if(player.isMute()){
            player.setMute(false);
        }
        else{
            player.setMute(true);
        }
    }

    @FXML
    private void onIncreaseVolume(){
        mediaToolbar.getVolumeSlider().increment();
    }

    @FXML
    private void onDecreaseVolume(){
        mediaToolbar.getVolumeSlider().decrement();
    }

    @FXML
    private void onZoomQuarter(){
        resizeWindowToVideo(0.25);
    }

    @FXML
    private void onZoomHalf(){
        resizeWindowToVideo(0.5);
    }

    @FXML
    private void onZoomOriginal(){
        resizeWindowToVideo(1.0);
    }

    @FXML
    private void onZoomDouble(){
        resizeWindowToVideo(2.0);
    }

    /**
     * Handles RendererStateChanged events, triggered by uPnP state classes via eventing thru RendererHandler.
     * @param rendererState Current RendererState of MediaRenderer
     */
    private void onRendererStateChanged(RendererState rendererState){
        LOGGER.fine("Detected RendererState change to " + rendererState.name());

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
                else if(player.getStatus() == PlayerStatus.PAUSED){
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
    private void prepareFullScreen(boolean isFullScreen){
        player.heightProperty().unbind();
        player.widthProperty().unbind();

        if(isFullScreen){
            StackPane.setMargin(player.getNode(), new Insets(0,0,0,0));

            player.heightProperty().bind(getStage().heightProperty());
            player.widthProperty().bind(getStage().widthProperty());

            mediaToolbar.setMaxWidth(Region.USE_PREF_SIZE);
            mediaToolbar.setOpacity(0);
            getStage().getScene().setCursor(Cursor.NONE);
            menuBar.setOpacity(0);

            mouseIdleTimer.setOnFinished(event -> {
                getStage().getScene().setCursor(Cursor.NONE);
                mediaToolbar.setOpacity(0);
                menuBar.setOpacity(0);
            });

            player.getNode().setOnMouseMoved(event -> {
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
            player.getNode().setOnMouseMoved(null);
            mediaToolbar.setOnMouseEntered(null);
            menuBar.setOnMouseEntered(null);

            double bottomBarHeight = mediaToolbar.getHeight();
            double menuBarHeight = menuBar.getHeight();
            StackPane.setMargin(player.getNode(), new Insets(menuBarHeight,0, bottomBarHeight,0));

            player.heightProperty().bind(getStage().getScene().heightProperty().subtract(bottomBarHeight + menuBarHeight));
            player.widthProperty().bind(getStage().getScene().widthProperty());

            mediaToolbar.setMaxWidth(Region.USE_COMPUTED_SIZE);

            getStage().getScene().setCursor(Cursor.DEFAULT);
            mediaToolbar.setOpacity(1);
            menuBar.setOpacity(1);
        }
    }

    /**
     * Notifies control point of current time changes via avTransportHandler
     */
    private void updateCurrentTime(){
        Duration currentDuration = player.getCurrentTime();
        Duration totalDuration = player.getTotalTime();

        avTransportHandler.setPositionInfoWithTimes(totalDuration, currentDuration);
        if(apiService != null) {
            apiService.updateCurrentlyPlayingInfo(currentDuration, totalDuration, player.isMute(), (int)Math.round(player.getVolume()));
        }
    }

    /**
     * Handles RendererVolumeChanged events, triggered by RenderingControlService via eventing thru RenderingControlHandler
     * @param volume volume set by control point
     */
    private void onRendererVolumeChanged(double volume){
        player.setVolume(volume);
    }

    /**
     * Handles RendererMuteChanged events, triggered by RenderingControlService via eventing thru RenderingControlHandler
     * @param isMute whether control point enabled/disabled mute
     */
    private void onRendererMuteChanged(boolean isMute){
        player.setMute(isMute);
    }

    /**
     * Set media player to play a new media
     * @param mediaObject media to play
     */
    private void playNewMedia(MediaObject mediaObject){
        Platform.runLater(() -> {
            avTransportHandler.setTransportInfo(TransportState.TRANSITIONING);
            if(apiService != null){
                apiService.setCurrentStatus(RendererState.TRANSITIONING);
            }

            LOGGER.fine("Creating new player for URI " + mediaObject.getUriString());
            currentMediaObject = mediaObject;
            player.playNewMedia(mediaObject);

            prepareMediaPlayback();
        });
    }

    /**
     * For use when seeking to a specific duration
     * @param target target to seek to
     */
    private void seekSpecificDuration(Duration target){
        if(target.lessThanOrEqualTo(player.getTotalTime())){
            LOGGER.finer("Seeking to " + CherryUtil.durationToString(target));
            player.seek(target);
            updateCurrentTime();
        }
    }

    /**
     * Resize player window based on video size and given ratio.
     * If the calculated window size is larger than the current screen's available visual space, player window will maximize instead.
     * @param ratio Ratio to resize player window to.
     */
    private void resizeWindowToVideo(double ratio){
        if(getStage().isFullScreen()){
            getStage().setFullScreen(false);
        }

        currentResizeRatio = ratio;

        double videoWidth = player.getVideoWidth();
        double videoHeight = player.getVideoHeight();
        double uiHeight = menuBar.getHeight() + mediaToolbar.getHeight();

        Scene scene = getStage().getScene();
        double windowTopInset = scene.getY();
        double windowLeftInset = scene.getX();
        double windowBottomInset = getStage().getHeight() - scene.getHeight() - windowTopInset;
        double windowRightInset = getStage().getWidth() - scene.getWidth() - windowLeftInset;

        double resultHeight = (videoHeight * ratio) + uiHeight + windowTopInset + windowBottomInset;
        double resultWidth = (videoWidth * ratio) + windowLeftInset + windowRightInset;

        // FIXME: This sometimes results in IndexOutOfBoundsException. I'll blame XFCE for now.
        Rectangle2D screenRectangle = Screen.getScreensForRectangle(getStage().getX(), getStage().getY(), getStage().getWidth(), getStage().getHeight()).get(0).getVisualBounds();

        if(resultHeight >= screenRectangle.getHeight() || resultWidth >= screenRectangle.getWidth()){
            getStage().setMaximized(true);
        }
        else{
            if(getStage().isMaximized()){
                getStage().setMaximized(false);
            }
            getStage().setWidth(resultWidth);
            getStage().setHeight(resultHeight);
        }
    }

    /**
     * Only called when video player is ready, player window will automatically resize based on AutoResizePreference.
     */
    private void autoResizeWindow(){
        double ratio;
        switch(new AutoResizePreference().get()){
            case DISABLED:
                return;
            case QUARTER:
                ratio = 0.25;
                break;
            case HALF:
                ratio = 0.5;
                break;
            case DOUBLE:
                ratio = 2.0;
                break;
            default:
                ratio = 1.0;
                break;
        }
        resizeWindowToVideo(ratio);
    }

    /**
     * Only called on key press, player window will cycle between the set resize ratios
     */
    private void cycleResizeWindow(){
        if(!Arrays.asList(0.25, 0.5, 1.0, 2.0).contains(currentResizeRatio)){
            currentResizeRatio = 2.0;
        }

        if(currentResizeRatio == 0.25){
            resizeWindowToVideo(0.5);
        }
        else if(currentResizeRatio == 0.5){
            resizeWindowToVideo(1.0);
        }
        else if(currentResizeRatio == 1.0){
            resizeWindowToVideo(2.0);
        }
        else if(currentResizeRatio == 2.0){
            resizeWindowToVideo(0.25);
        }
    }
}
