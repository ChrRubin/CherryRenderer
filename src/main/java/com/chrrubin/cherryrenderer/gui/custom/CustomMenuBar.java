package com.chrrubin.cherryrenderer.gui.custom;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.gui.*;
import com.chrrubin.cherryrenderer.prefs.AutoSaveSnapshotsPreference;
import com.chrrubin.cherryrenderer.prefs.ThemePreferenceValue;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomMenuBar extends MenuBar {
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
    private Menu zoomSubMenu;
    @FXML
    private MenuItem zoomQuarterMenuItem;
    @FXML
    private MenuItem zoomHalfMenuItem;
    @FXML
    private MenuItem zoomOriginalMenuItem;
    @FXML
    private MenuItem zoomDoubleMenuItem;

    private final Logger LOGGER = Logger.getLogger(CustomMenuBar.class.getName());
    private AbstractStage parentStage;
    private Image playImage;
    private Image pauseImage;

    public CustomMenuBar(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/custom/CustomMenuBar.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.setClassLoader(getClass().getClassLoader());
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        Image volumeFullImage;
        Image volumeMuteImage;
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
        Image zoomImage;

        if (CherryUtil.LOADED_THEME == ThemePreferenceValue.DARK) {
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
            zoomImage = new Image("icons/grey/zoom.png");

            getStylesheets().add(getClass().getResource("/fxml/custom/CustomMenuBar-Dark.css").toExternalForm());
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
            zoomImage = new Image("icons/zoom.png");

            getStylesheets().add(getClass().getResource("/fxml/custom/CustomMenuBar-Default.css").toExternalForm());
        }

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
        zoomSubMenu.setGraphic(createMenuImageView(zoomImage));

        preferencesMenuItem.setOnAction(event -> openNewWindow(new PreferencesStage(parentStage)));
        exitMenuItem.setOnAction(event -> Platform.exit());
        helpMenuItem.setOnAction(event -> openNewWindow(new HelpStage(parentStage)));
        updateMenuItem.setOnAction(event -> openNewWindow(new UpdaterStage(parentStage)));
        aboutMenuItem.setOnAction(event -> openNewWindow(new AboutStage(parentStage)));
    }

    private ImageView createMenuImageView(Image image){
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(15);
        imageView.setFitWidth(15);

        return imageView;
    }

    private void openNewWindow(AbstractStage stage){
        try{
            stage.prepareStage();
            stage.show();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = parentStage.createErrorAlert(e.toString());
            alert.showAndWait();
        }
    }

    /*
    Start properties
     */
    public final ObjectProperty<EventHandler<ActionEvent>> onMediaInfoProperty(){
        return mediaInfoMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnMediaInfo(){
        return onMediaInfoProperty().get();
    }

    public void setOnMediaInfo(EventHandler<ActionEvent> eventHandler){
        onMediaInfoProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onPlayPauseProperty(){
        return playPauseMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnPlayPause(){
        return onPlayPauseProperty().get();
    }

    public void setOnPlayPause(EventHandler<ActionEvent> eventHandler){
        onPlayPauseProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onStopProperty(){
        return stopMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnStop(){
        return onStopProperty().get();
    }

    public void setOnStop(EventHandler<ActionEvent> eventHandler){
        onStopProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onRewindProperty(){
        return rewindMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnRewind(){
        return onRewindProperty().get();
    }

    public void setOnRewind(EventHandler<ActionEvent> eventHandler){
        onRewindProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onForwardProperty(){
        return forwardMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnForward(){
        return onForwardProperty().get();
    }

    public void setOnForward(EventHandler<ActionEvent> eventHandler){
        onForwardProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onVolumeUpProperty(){
        return volUpMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnVolumeUp(){
        return onVolumeUpProperty().get();
    }

    public void setOnVolumeUp(EventHandler<ActionEvent> eventHandler){
        onVolumeUpProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onVolumeDownProperty(){
        return volDownMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnVolumeDown(){
        return onVolumeDownProperty().get();
    }

    public void setOnVolumeDown(EventHandler<ActionEvent> eventHandler){
        onVolumeDownProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onToggleMuteProperty(){
        return muteMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnToggleMute(){
        return onToggleMuteProperty().get();
    }

    public void setOnToggleMute(EventHandler<ActionEvent> eventHandler){
        onToggleMuteProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onToggleFullscreenProperty(){
        return fullscreenMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnToggleFullscreen(){
        return onToggleFullscreenProperty().get();
    }

    public void setOnToggleFullscreen(EventHandler<ActionEvent> eventHandler){
        onToggleFullscreenProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onZoomQuarterProperty(){
        return zoomQuarterMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnZoomQuarter(){
        return onZoomQuarterProperty().get();
    }

    public void setOnZoomQuarter(EventHandler<ActionEvent> eventHandler){
        onZoomQuarterProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onZoomHalfProperty(){
        return zoomHalfMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnZoomHalf(){
        return onZoomHalfProperty().get();
    }

    public void setOnZoomHalf(EventHandler<ActionEvent> eventHandler){
        onZoomHalfProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onZoomOriginalProperty(){
        return zoomOriginalMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnZoomOriginal(){
        return onZoomOriginalProperty().get();
    }

    public void setOnZoomOriginal(EventHandler<ActionEvent> eventHandler){
        onZoomOriginalProperty().set(eventHandler);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onZoomDoubleProperty(){
        return zoomDoubleMenuItem.onActionProperty();
    }

    public EventHandler<ActionEvent> getOnZoomDouble(){
        return onZoomDoubleProperty().get();
    }

    public void setOnZoomDouble(EventHandler<ActionEvent> eventHandler){
        onZoomDoubleProperty().set(eventHandler);
    }
    /*
    End properties
     */

    public void setParentStage(AbstractStage parentStage){
        this.parentStage = parentStage;
    }

    public void disablePlaybackMenu(){
        for (MenuItem item : playbackMenu.getItems()) {
            item.setDisable(true);
        }
    }

    public void enablePlaybackMenu(){
        for (MenuItem item : playbackMenu.getItems()) {
            item.setDisable(false);
        }
    }

    public void setPlayPauseToPlay(){
        playPauseMenuItem.setText("Play");
        playPauseMenuItem.setGraphic(createMenuImageView(playImage));
    }

    public void setPlayPauseToPause(){
        playPauseMenuItem.setText("Pause");
        playPauseMenuItem.setGraphic(createMenuImageView(pauseImage));
    }

    public void setSnapshotImageSupplier(Supplier<BufferedImage> imageSupplier){
        snapshotMenuItem.setOnAction(event -> onSnapshot(imageSupplier.get()));
    }

    private void onSnapshot(BufferedImage snapshot){
        try {
            File snapshotFile;
            String defaultFileName = "cherrysnap-" + new SimpleDateFormat("yyyy-MM-dd-HH'h'mm'm'ss's'SSS").format(new Date()) + ".png";

            if (new AutoSaveSnapshotsPreference().get()) {
                snapshotFile = new File(System.getProperty("user.home"), defaultFileName);
            }
            else {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(defaultFileName);
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                fileChooser.setTitle("Save snapshot as..");

                snapshotFile = fileChooser.showSaveDialog(parentStage);
            }

            if (snapshotFile == null) {
                return;
            }
            ImageIO.write(snapshot, "png", snapshotFile);
            Alert alert = parentStage.createInfoAlert("Saved snapshot to " + snapshotFile.getPath());
            alert.showAndWait();
        }
        catch (IOException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            Alert alert = parentStage.createErrorAlert(e.toString());
            alert.showAndWait();
        }
    }
}
