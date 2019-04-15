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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
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

    private final Logger LOGGER = Logger.getLogger(PlayerStageController.class.getName());

    private URI currentUri = null;
    private double volumeSavedValue = 0;
    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();
    private PauseTransition mouseIdleTimer = new PauseTransition(Duration.seconds(1));
    private Image volumeFullImage = new Image(this.getClass().getClassLoader().getResourceAsStream("icons/volume.png"));
    private Image volumeMuteImage = new Image(this.getClass().getClassLoader().getResourceAsStream("icons/volume-mute.png"));
    private ScheduledService<Void> updateTimeService;

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
            changeVolumeImage(volumeSlider.getValue());
        }
    };

    /**
     * Listener for valueProperty of volumeSlider
     */
    private InvalidationListener volumeInvalidationListener = observable -> {
        if(!volumeSlider.isValueChanging()){
            videoMediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100.0);
            changeVolumeImage(volumeSlider.getValue());
        }
    };

    /**
     * EventHandler for onMouseClicked of videoMediaView
     * Toggles fullscreen when user double clicks on videoMediaView
     */
    private EventHandler<MouseEvent> mediaViewClickEvent = event -> {
        if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2){
                toggleFullscreen();
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
                toggleFullscreen();
                break;
            case M:
                toggleMute();
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

        player.setOnPlaying(() -> playPauseImageView.setImage(new Image("icons/pause.png")));

        player.setOnPaused(() -> playPauseImageView.setImage(new Image("icons/play.png")));

        player.setOnReady(() -> {
            Duration totalDuration = player.getTotalDuration();

            totalTimeLabel.setText(CherryUtil.durationToString(totalDuration));
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
            transportHandler.sendLastChangeMediaDuration(totalDuration);
            transportHandler.setTransportInfo(TransportState.PLAYING);

            String title = getTitle(rendererHandler.getMetadata());
            if(title != null && !title.equals("")){
                LOGGER.finer("Video title is " + title);

                getStage().setTitle("CherryRenderer - " + title);
            }
            else{
                LOGGER.finer("Video title was not detected.");
            }

            bottomBarVBox.setDisable(false);
            volumeImageView.setOpacity(1);
        });

        // TODO: Handle player errors better (media not supported, player halted etc)
        player.setOnError(() -> {
            String error = player.getError().toString();

            LOGGER.log(Level.SEVERE, error, player.getError());
            Alert alert = getStage().createErrorAlert(error);
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
        timeTooltipVBox.setManaged(false);

        timeSlider.setOnMouseDragged(timeMouseDraggedEvent);

        timeSlider.setOnMouseReleased(timeMouseReleasedEvent);

        /*
        Shows tooltip when dragging the thumb of volumeSlider
         */
        volumeTooltipVBox.setManaged(false);

        volumeSlider.setOnMouseDragged(volumeMouseDraggedEvent);

        volumeSlider.setOnMouseReleased(volumeMouseReleasedEvent);

        videoMediaView.setOnKeyReleased(videoKeyReleasedEvent);

        player.setOnEndOfMedia(() -> {
            LOGGER.finest("player.setOnEndOfMedia triggered");
            endOfMedia();
        });

        player.setOnStopped(() -> {
            LOGGER.finest("player.setOnStopped triggered");
            endOfMedia();
        });

        // FIXME: Sometimes 2 services are running simultaneously?
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
        rendererHandler.getVideoSeekEvent().addListener(seekDuration -> {
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

        transportHandler.clearInfo();
        transportHandler.setTransportInfo(TransportState.STOPPED);

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

        if(updateTimeService != null){
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
        volumeImageView.setOpacity(0.5);

        currentUri = null;

        videoMediaView.setOnMouseClicked(null);
        if(getStage().isFullScreen()){
            getStage().setFullScreen(false);
        }
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
        AboutStage aboutStage = new AboutStage(getStage());
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
    private void toggleMute(){
        if(volumeSavedValue == 0){
            volumeSavedValue = volumeSlider.getValue();
            volumeSlider.setValue(0);
        }
        else{
            volumeSlider.setValue(volumeSavedValue);
            volumeSavedValue = 0;
        }
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
                if(!rendererHandler.getUri().equals(currentUri)) {
                    if(player != null && player.getStatus() != Status.DISPOSED){
                        LOGGER.warning("Tried to create new player while existing player still running. Stopping current player.");
                        onStop();
                    }

                    LOGGER.finer("Creating new player for URI " + rendererHandler.getUri().toString());
                    currentUri = rendererHandler.getUri();

                    Media media = new Media(rendererHandler.getUri().toString());
                    videoMediaView.setMediaPlayer(new MediaPlayer(media));

                    transportHandler.setTransportInfo(TransportState.TRANSITIONING);

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

    private void changeVolumeImage(double volume){
        if(volume == 0){
            volumeImageView.setImage(volumeMuteImage);
        }
        else if(!volumeImageView.getImage().equals(volumeFullImage)){
            volumeImageView.setImage(volumeFullImage);
        }
    }

    private void toggleFullscreen() {
        if(!getStage().isFullScreen()){
            getStage().setFullScreen(true);
        }
        else{
            getStage().setFullScreen(false);
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

        transportHandler.setPositionInfo(
                rendererHandler.getUri(),
                rendererHandler.getMetadata(),
                player.getTotalDuration(),
                player.getCurrentTime()
        );
    }

    private void updateCurrentTime(Duration currentTime, Duration totalTime){

        transportHandler.setPositionInfo(
                rendererHandler.getUri(),
                rendererHandler.getMetadata(),
                totalTime,
                currentTime
        );
    }

    /**
     * Parses the metadata XML to get the title of the video playing
     * @param xml XML metadata string
     * @return Title string of video
     */
    private String getTitle(String xml){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/DIDL-Lite/item/*[local-name() = 'title']");

            return (String)expr.evaluate(document, XPathConstants.STRING);
        }
        catch (Exception e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = getStage().createErrorAlert(e.toString());
            alert.showAndWait();
            return null;
        }
    }
}
