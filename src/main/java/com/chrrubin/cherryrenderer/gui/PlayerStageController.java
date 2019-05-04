package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.MediaObject;
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
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerStageController implements IController {
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
    private Slider volumeSlider;
    @FXML
    private VBox bottomBarVBox;
    @FXML
    private MenuBar menuBar;
    @FXML
    private VBox timeTooltipVBox;
    @FXML
    private Label timeTooltipLabel;
    @FXML
    private ImageView playPauseImageView;
    @FXML
    private ImageView volumeImageView;
    @FXML
    private VBox volumeTooltipVBox;
    @FXML
    private Label volumeTooltipLabel;
    @FXML
    private ImageView rewindImageView;
    @FXML
    private ImageView stopImageView;
    @FXML
    private ImageView fastForwardImageView;
    @FXML
    private MenuItem preferencesMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private MenuItem mediaInfoMenuItem;
    @FXML
    private MenuItem snapshotMenuItem;
    @FXML
    private MenuItem playPauseMenuItem;
    @FXML
    private MenuItem stopMenuItem;
    @FXML
    private MenuItem rewindMenuItem;
    @FXML
    private MenuItem forwardMenuItem;
    @FXML
    private MenuItem volUpMenuItem;
    @FXML
    private MenuItem volDownMenuItem;
    @FXML
    private MenuItem muteMenuItem;
    @FXML
    private MenuItem fullscreenMenuItem;
    @FXML
    private MenuItem helpMenuItem;
    @FXML
    private MenuItem updateMenuItem;
    @FXML
    private Menu playbackMenu;
    @FXML
    private VBox waitingVBox;
    @FXML
    private Label waitingLabel;

    private final Logger LOGGER = Logger.getLogger(PlayerStageController.class.getName());

    private URI currentUri = null;
    private AVTransportHandler avTransportHandler = AVTransportHandler.getInstance();
    private RenderingControlHandler renderingControlHandler = RenderingControlHandler.getInstance();
    private PauseTransition mouseIdleTimer = new PauseTransition(Duration.seconds(1));
    private Image playImage;
    private Image pauseImage;
    private Image volumeFullImage;
    private Image volumeMuteImage;
    private ScheduledService<Void> updateTimeService;
    private final String WAITING_CONNECTION = "Awaiting connection from control point app...";
    private final String WAITING_VIDEO = "Video is loading...";

    /*
    Start of property listeners and event handlers
     */
    /**
     * Listener for statusProperty of videoMediaView.getMediaPlayer()
     */
    private ChangeListener<Status> playerStatusListener = (observable, oldStatus, newStatus) -> {
        if(oldStatus == null){
            return;
        }

        LOGGER.finer("Media player exited "+ oldStatus.name() + " status, going into " + newStatus.name() + " status.");

        if(newStatus == Status.STALLED){
            LOGGER.fine("(Caught by listener) Media player in entered STALLED status. Video is currently buffering (?)");
        }
    };

    /**
     * Listener for currentTimeProperty of videoMediaView.getMediaPlayer()
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
     * Listener for MuteProperty of videoMediaView.getMediaPlayer()
     * Changes the volume image based on whether player is muted.
     * Also notifies RenderingControlService of mute changes.
     */
    private InvalidationListener playerMuteListener = ((observable) -> {
        boolean isMute = videoMediaView.getMediaPlayer().muteProperty().get();
        changeVolumeImage(isMute);
        renderingControlHandler.setRendererMute(isMute);
    });

    /**
     * Listener for VolumeProperty of videoMediaView.getMediaPlayer()
     * Updates volumeSlider based on volume changes.
     * Also notifies RenderingControlService of volume changes.
     */
    private ChangeListener<Number> playerVolumeListener = ((observable, oldVolume, newVolume) -> {
        if(!volumeSlider.isValueChanging()){
            volumeSlider.setValue(newVolume.doubleValue() * 100);
        }
        renderingControlHandler.setRendererVolume(newVolume.doubleValue() * 100);
    });

    /**
     * Listener for isChangingProperty of timeSlider
     * Seeks video based on timeSlider value.
     */
    private ChangeListener<Boolean> timeIsChangingListener = (observable, wasChanging, isChanging) -> {
        if(!isChanging){
            videoMediaView.getMediaPlayer().seek(videoMediaView.getMediaPlayer().getTotalDuration().multiply(timeSlider.getValue() / 100.0));
            updateCurrentTime();
        }
    };

    /**
     * Listener for valueProperty of timeSlider
     * Seeks video based on timeSlider value.
     */
    private ChangeListener<Number> timeChangeListener = (observable, oldValue, newValue) -> {
        if(!timeSlider.isValueChanging() && Math.abs(newValue.doubleValue() - oldValue.doubleValue()) > 0.5){
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
            videoMediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100.0);
        }
    };

    /**
     * Listener for valueProperty of volumeSlider
     * Changes volume of video based on volumeSlider value.
     */
    private InvalidationListener volumeInvalidationListener = observable -> {
        if(!volumeSlider.isValueChanging()){
            videoMediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100.0);
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

        double sliderValue = timeSlider.getValue();
        Duration sliderDragTime = videoMediaView.getMediaPlayer().getTotalDuration().multiply(sliderValue / 100.0);
        Duration timeDifference = sliderDragTime.subtract(currentTimeCalcProperty.get());
        String output = CherryUtil.durationToString(sliderDragTime) + System.lineSeparator() +
                "[" + CherryUtil.durationToString(timeDifference) + "]";

        timeTooltipLabel.setText(output);

        timeTooltipVBox.setVisible(true);

        Bounds timeSliderBounds = timeSlider.localToScene(timeSlider.getBoundsInLocal());
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
        int sliderValue = (int)Math.round(volumeSlider.getValue());

        volumeTooltipLabel.setText(sliderValue + "%");

        volumeTooltipVBox.setVisible(true);

        Bounds volumeSliderBounds = volumeSlider.localToScene(volumeSlider.getBoundsInLocal());
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
                onPlay();
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
                volumeSlider.increment();
                break;
            case DOWN:
                volumeSlider.decrement();
                break;
        }
    };
    /*
    End of property listeners and event handlers
     */


    @Override
    public AbstractStage getStage() {
        return (AbstractStage) rootStackPane.getScene().getWindow();
    }

    @FXML
    private void initialize(){
        String friendlyName = CherryPrefs.FriendlyName.LOADED_VALUE;
        LOGGER.info("Current device friendly name is " + friendlyName);
        RendererService handler = new RendererService(friendlyName);
        handler.startService();

        avTransportHandler.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);
        renderingControlHandler.getVideoVolumeEvent().addListener(this::onRendererVolumeChange);
        renderingControlHandler.getVideoMuteEvent().addListener(this::onRendererMuteChange);

        Image rewindImage;
        Image stopImage;
        Image fastForwardImage;
        Image preferencesImage;
        Image infoImage;
        Image exitImage;
        Image snapshotImage;
        Image fullscreenImage;
        Image helpImage;
        Image updateImage;
        Image volumeDownImage;

        if (CherryPrefs.Theme.LOADED_VALUE.equals("DARK")) {
            playImage = new Image("icons/grey/play.png");
            pauseImage = new Image("icons/grey/pause.png");
            rewindImage = new Image("icons/grey/rewind.png");
            stopImage = new Image("icons/grey/stop.png");
            fastForwardImage = new Image("icons/grey/forward.png");
            volumeFullImage = new Image("icons/grey/volume-full.png");
            volumeMuteImage = new Image("icons/grey/volume-mute.png");

            preferencesImage = new Image("icons/grey/pref.png");
            infoImage = new Image("icons/grey/info.png");
            exitImage = new Image("icons/grey/exit.png");
            snapshotImage = new Image("icons/grey/snapshot.png");
            fullscreenImage = new Image("icons/grey/fullscreen.png");
            helpImage = new Image("icons/grey/help.png");
            updateImage = new Image("icons/grey/update.png");
            volumeDownImage = new Image("icons/grey/volume-down.png");
        }
        else {
            playImage = new Image("icons/play.png");
            pauseImage = new Image("icons/pause.png");
            rewindImage = new Image("icons/rewind.png");
            stopImage = new Image("icons/stop.png");
            fastForwardImage = new Image("icons/forward.png");
            volumeFullImage = new Image("icons/volume-full.png");
            volumeMuteImage = new Image("icons/volume-mute.png");

            preferencesImage = new Image("icons/pref.png");
            infoImage = new Image("icons/info.png");
            exitImage = new Image("icons/exit.png");
            snapshotImage = new Image("icons/snapshot.png");
            fullscreenImage = new Image("icons/fullscreen.png");
            helpImage = new Image("icons/help.png");
            updateImage = new Image("icons/update.png");
            volumeDownImage = new Image("icons/volume-down.png");
        }

        playPauseImageView.setImage(playImage);
        rewindImageView.setImage(rewindImage);
        stopImageView.setImage(stopImage);
        fastForwardImageView.setImage(fastForwardImage);
        volumeImageView.setImage(volumeFullImage);

        preferencesMenuItem.setGraphic(createMenuImageView(preferencesImage));
        exitMenuItem.setGraphic(createMenuImageView(exitImage));
        mediaInfoMenuItem.setGraphic(createMenuImageView(infoImage));
        snapshotMenuItem.setGraphic(createMenuImageView(snapshotImage));
        playPauseMenuItem.setGraphic(createMenuImageView(playImage));
        stopMenuItem.setGraphic(createMenuImageView(stopImage));
        rewindMenuItem.setGraphic(createMenuImageView(rewindImage));
        forwardMenuItem.setGraphic(createMenuImageView(fastForwardImage));
        volUpMenuItem.setGraphic(createMenuImageView(volumeFullImage));
        volDownMenuItem.setGraphic(createMenuImageView(volumeDownImage));
        muteMenuItem.setGraphic(createMenuImageView(volumeMuteImage));
        fullscreenMenuItem.setGraphic(createMenuImageView(fullscreenImage));
        helpMenuItem.setGraphic(createMenuImageView(helpImage));
        updateMenuItem.setGraphic(createMenuImageView(updateImage));
        aboutMenuItem.setGraphic(createMenuImageView(infoImage));
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
            playPauseImageView.setImage(pauseImage);
            playPauseMenuItem.setGraphic(createMenuImageView(pauseImage));
            playPauseMenuItem.setText("Pause");
        });

        player.setOnPaused(() -> {
            playPauseImageView.setImage(playImage);
            playPauseMenuItem.setGraphic(createMenuImageView(playImage));
            playPauseMenuItem.setText("Play");
        });

        player.setOnReady(() -> {
            videoMediaView.setVisible(true);
            Duration totalDuration = player.getTotalDuration();

            totalTimeLabel.setText(CherryUtil.durationToString(totalDuration));
            avTransportHandler.setMediaInfoWithTotalTime(totalDuration);
            avTransportHandler.setPositionInfoWithTimes(totalDuration, Duration.ZERO);
            avTransportHandler.sendLastChangeMediaDuration(totalDuration);
            avTransportHandler.setTransportInfo(TransportState.PLAYING);

            try {
                String title = avTransportHandler.getMediaObject().getTitle();
                if (title != null && !title.equals("")) {
                    LOGGER.finer("Video title is " + title);

                    getStage().setTitle("CherryRenderer - " + title);
                } else {
                    LOGGER.finer("Video title was not detected.");
                }
            }
            catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e){
                LOGGER.log(Level.SEVERE, e.toString(), e);
                Alert alert = getStage().createErrorAlert(e.toString());
                alert.showAndWait();
            }

            bottomBarVBox.setDisable(false);
            for (MenuItem item : playbackMenu.getItems()) {
                item.setDisable(false);
            }
            waitingVBox.setVisible(false);
            volumeImageView.setOpacity(1);
        });

        player.setOnError(() -> {
            String error = player.getError().toString();

            LOGGER.log(Level.SEVERE, error, player.getError());
            Alert alert = getStage().createErrorAlert(error);
            alert.showAndWait();

            waitingLabel.setText(WAITING_CONNECTION);
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
        timeTooltipVBox.setManaged(false);

        timeSlider.setOnMouseDragged(timeMouseDraggedEvent);

        timeSlider.setOnMouseReleased(timeMouseReleasedEvent);

        /*
        Shows tooltip when dragging the thumb of volumeSlider
         */
        volumeTooltipVBox.setManaged(false);

        volumeSlider.setOnMouseDragged(volumeMouseDraggedEvent);

        volumeSlider.setOnMouseReleased(volumeMouseReleasedEvent);

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
                        if(videoMediaView.getMediaPlayer() != null && videoMediaView.getMediaPlayer().getStatus() == Status.PLAYING) {
                            updateCurrentTime(videoMediaView.getMediaPlayer().getCurrentTime(), videoMediaView.getMediaPlayer().getTotalDuration());
                        }
                        return null;
                    }
                };
            }
        };

        updateTimeService.setPeriod(Duration.seconds(1));
        updateTimeService.start();

        /*
        Seeks video when VideoSeekEvent is triggered by control point
         */
        avTransportHandler.getVideoSeekEvent().addListener(seekDuration -> {
            if(seekDuration != null) {
                player.seek(seekDuration);
                updateCurrentTime();
            }
        });

        videoMediaView.requestFocus();

    }

    /**
     * Runs the following cleanup tasks after video playback is done:
     * Properly removes property listeners and event handlers that should only be applied during playback.
     * Stops the service that auto updates PositionInfo
     * Disposes media player.
     * Disables and resets UI elements.
     */
    private void endOfMedia(){
        LOGGER.fine("Running end of media function");

        avTransportHandler.clearInfo();
        avTransportHandler.setTransportInfo(TransportState.STOPPED);

        getStage().setTitle("CherryRenderer");

        LOGGER.finer("Clearing property listeners & event handlers");
        timeSlider.valueChangingProperty().removeListener(timeIsChangingListener);
        timeSlider.valueProperty().removeListener(timeChangeListener);
        volumeSlider.valueChangingProperty().removeListener(volumeIsChangingListener);
        volumeSlider.valueProperty().removeListener(volumeInvalidationListener);
        timeSlider.setOnMouseDragged(null);
        timeSlider.setOnMouseReleased(null);
        volumeSlider.setOnMouseDragged(null);
        volumeSlider.setOnMouseReleased(null);
        rootStackPane.setOnKeyReleased(null);
        getStage().fullScreenProperty().removeListener(isFullScreenListener);

        if(updateTimeService != null && updateTimeService.isRunning()){
            LOGGER.finer("Stopping auto update of PositionInfo service");
            updateTimeService.cancel();
        }

        if(videoMediaView.getMediaPlayer() != null){
            LOGGER.finer("Disposing MediaPlayer");
            videoMediaView.getMediaPlayer().dispose();
        }

        currentTimeLabel.setText("--:--:--");
        totalTimeLabel.setText("--:--:--");
        timeSlider.setValue(0);
        volumeSlider.setValue(100);

        bottomBarVBox.setDisable(true);
        for (MenuItem item : playbackMenu.getItems()) {
            item.setDisable(true);
        }
        waitingLabel.setText(WAITING_CONNECTION);
        waitingVBox.setVisible(true);
        volumeImageView.setOpacity(0.4);

        currentUri = null;

        videoMediaView.setOnMouseClicked(null);
        if(getStage().isFullScreen()){
            getStage().setFullScreen(false);
        }

        videoMediaView.setVisible(false);
    }

    @FXML
    private void onPlay(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null){
            LOGGER.warning("MediaPlayer is null when tried to play/pause.");
            return;
        }

        Status status = player.getStatus();

        if(status == Status.UNKNOWN){
            LOGGER.warning("Attempted to pause/play video while player is still initializing. Ignoring.");
            return;
        }

        if(status == Status.PAUSED || status == Status.STOPPED || status == Status.READY){
            player.play();
            updateCurrentTime();
            avTransportHandler.setTransportInfo(TransportState.PLAYING);
        }
        else{
            player.pause();
            updateCurrentTime();
            avTransportHandler.setTransportInfo(TransportState.PAUSED_PLAYBACK);
        }
    }

    @FXML
    private void onRewind(){
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
    private void onStop(){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null){
            LOGGER.warning("MediaPlayer is null when tried to stop.");
            return;
        }

        Status status = player.getStatus();

        if(status == Status.PAUSED || status == Status.PLAYING || status == Status.STALLED){
            LOGGER.finer("Stopping playback");
            player.stop();
        }
        else if(status == Status.UNKNOWN){
            LOGGER.warning("Attempted to stop while player is still initializing. Will stop after finish initializing");
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
    private void onMenuPreferences(){
        AbstractStage preferencesStage = new PreferencesStage(getStage());
        try{
            preferencesStage.prepareStage();
            preferencesStage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }

    }

    @FXML
    private void onMenuHelp(){
        AbstractStage helpStage = new HelpStage(getStage());
        try{
            helpStage.prepareStage();
            helpStage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    @FXML
    private void onMenuUpdate(){
        AbstractStage updaterStage = new UpdaterStage(getStage());
        try{
            updaterStage.prepareStage();
            updaterStage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    @FXML
    private void onMenuAbout(){
        AbstractStage aboutStage = new AboutStage(getStage());
        try{
            aboutStage.prepareStage();
            aboutStage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }

    }

    @FXML
    private void onMenuExit(){
        System.exit(0);
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
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player == null || !Arrays.asList(Status.PAUSED, Status.PLAYING, Status.READY).contains(player.getStatus())){
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
    private void onIncreaseVolume(){
        volumeSlider.increment();
    }

    @FXML
    private void onDecreaseVolume(){
        volumeSlider.decrement();
    }

    @FXML
    private void onMediaInfo(){
        AbstractStage mediaInfoStage = new MediaInfoStage(getStage());
        try{
            mediaInfoStage.prepareStage();
            mediaInfoStage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    @FXML
    private void onSnapshot(){

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
                if(player != null && player.getStatus() != Status.DISPOSED){
                    onStop();
                }
                break;
            case PLAYING:
                MediaObject mediaObject = avTransportHandler.getMediaObject();

                if(!mediaObject.getUri().equals(currentUri)) {
                    if(player != null && player.getStatus() != Status.DISPOSED){
                        LOGGER.warning("Tried to create new player while existing player still running. Stopping current player.");
                        onStop();
                    }

                    LOGGER.finer("Creating new player for URI " + mediaObject.getUriString());
                    currentUri = mediaObject.getUri();

                    Platform.runLater(() -> waitingLabel.setText(WAITING_VIDEO));

                    Media media = mediaObject.toJFXMedia();
                    videoMediaView.setMediaPlayer(new MediaPlayer(media));

                    avTransportHandler.setTransportInfo(TransportState.TRANSITIONING);

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
     * Switches the volume image between volume-full.png and volume-mute.png based on the mute boolean.
     * @param mute Whether player is muted
     */
    private void changeVolumeImage(boolean mute){
        if(mute && !volumeImageView.getImage().equals(volumeMuteImage)){
            volumeImageView.setImage(volumeMuteImage);
        }
        else if(!volumeImageView.getImage().equals(volumeFullImage)){
            volumeImageView.setImage(volumeFullImage);
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

            videoMediaView.fitHeightProperty().bind(getStage().getScene().heightProperty().subtract(bottomBarHeight + menuBarHeight));
            videoMediaView.fitWidthProperty().bind(getStage().getScene().widthProperty());

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

        avTransportHandler.setPositionInfoWithTimes(player.getTotalDuration(), player.getCurrentTime());
    }

    private void updateCurrentTime(Duration currentTime, Duration totalTime){
        avTransportHandler.setPositionInfoWithTimes(totalTime, currentTime);
    }

    /**
     * Handles RendererVolumeChanged events, triggered by RenderingControlService via eventing thru RenderingControlHandler
     * @param volume volume set by control point
     */
    private void onRendererVolumeChange(double volume){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player != null && Arrays.asList(Status.PAUSED, Status.PLAYING, Status.READY).contains(player.getStatus())){
            player.setVolume(volume / 100.0);
        }
    }

    /**
     * Handles RendererMuteChanged events, triggered by RenderingControlService via eventing thru RenderingControlHandler
     * @param isMute whether control point enabled/disabled mute
     */
    private void onRendererMuteChange(boolean isMute){
        MediaPlayer player = videoMediaView.getMediaPlayer();
        if(player != null && Arrays.asList(Status.PAUSED, Status.PLAYING, Status.READY).contains(player.getStatus())){
            player.setMute(isMute);
        }
    }

    private ImageView createMenuImageView(Image image){
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(15);
        imageView.setFitWidth(15);

        return imageView;
    }
}
