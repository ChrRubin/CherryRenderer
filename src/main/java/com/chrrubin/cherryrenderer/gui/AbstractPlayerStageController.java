package com.chrrubin.cherryrenderer.gui;

import com.chrrubin.cherryrenderer.CherryPrefs;
import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.MediaObject;
import com.chrrubin.cherryrenderer.api.ApiService;
import com.chrrubin.cherryrenderer.upnp.AVTransportHandler;
import com.chrrubin.cherryrenderer.upnp.RendererService;
import com.chrrubin.cherryrenderer.upnp.RenderingControlHandler;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.util.Duration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractPlayerStageController implements IController {
    private Logger LOGGER;
    private MediaObject currentMediaObject = null;
    private AVTransportHandler avTransportHandler = AVTransportHandler.getInstance();
    private RenderingControlHandler renderingControlHandler = RenderingControlHandler.getInstance();
    private PauseTransition mouseIdleTimer = new PauseTransition(Duration.seconds(1));
    private ApiService apiService = null;

    public AbstractPlayerStageController(Logger logger){
        this.LOGGER = logger;

        try{
            apiService = new ApiService();
            apiService.getMediaObjectEvent().addListener(this::playNewMedia);
            apiService.getTogglePauseEvent().addListener(object -> onPlayPause());
            apiService.getSeekEvent().addListener(this::seekSpecificDuration);
            apiService.getStopEvent().addListener(object -> onStop());
            apiService.getToggleMuteEvent().addListener(object -> onToggleMute());
            apiService.getSetVolumeEvent().addListener(this::onRendererVolumeChanged);
        }
        catch (IOException | RuntimeException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            Platform.runLater(() -> {
                Alert alert = getStage().createErrorAlert(e.toString());
                alert.showAndWait();
            });
        }

        String friendlyName = CherryPrefs.FriendlyName.LOADED_VALUE;
        LOGGER.info("Current device friendly name is " + friendlyName);
        RendererService rendererService = new RendererService(friendlyName);
        rendererService.startService();

        avTransportHandler.getRendererStateChangedEvent().addListener(this::onRendererStateChanged);
        avTransportHandler.getVideoSeekEvent().addListener(this::seekSpecificDuration);
        renderingControlHandler.getVideoVolumeEvent().addListener(this::onRendererVolumeChanged);
        renderingControlHandler.getVideoMuteEvent().addListener(this::onRendererMuteChanged);
    }

    public MediaObject getCurrentMediaObject() {
        return currentMediaObject;
    }

    public void setCurrentMediaObject(MediaObject currentMediaObject) {
        this.currentMediaObject = currentMediaObject;
    }

    public AVTransportHandler getAvTransportHandler() {
        return avTransportHandler;
    }

    public RenderingControlHandler getRenderingControlHandler() {
        return renderingControlHandler;
    }

    public PauseTransition getMouseIdleTimer() {
        return mouseIdleTimer;
    }

    public ApiService getApiService() {
        return apiService;
    }

    public void checkUpdate(){
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

    @FXML
    void onMediaInfo(){
        AbstractStage mediaInfoStage = new MediaInfoStage(getStage(), currentMediaObject);
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

    public abstract AbstractStage getStage();

    abstract void onRendererStateChanged(RendererState rendererState);
    abstract void onRendererVolumeChanged(double volume);
    abstract void onRendererMuteChanged(boolean isMute);
    abstract void updateCurrentTime();

    abstract void onPlayPause();
    abstract void onRewind();
    abstract void onStop();
    abstract void onForward();
    abstract void seekSpecificDuration(Duration target);
    abstract void onToggleFullScreen();
    abstract void onToggleMute();
    abstract void onIncreaseVolume();
    abstract void onDecreaseVolume();

    abstract void playNewMedia(MediaObject mediaObject);
    abstract void prepareMediaPlayback();
    abstract void endOfMedia();

    abstract void prepareFullScreen(boolean isFullScreen);
}
