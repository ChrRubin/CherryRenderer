package com.chrrubin.cherryrenderer.gui.custom;

import com.chrrubin.cherryrenderer.MediaObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.logging.Logger;

public class JfxMediaView extends MediaView implements IPlayer {
    private final Logger LOGGER = Logger.getLogger(JfxMediaView.class.getName());

    @Override
    public void playNewMedia(MediaObject mediaObject) {
        MediaPlayer mediaPlayer = getMediaPlayer();
        if(mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED){
            disposePlayer();
        }
        mediaPlayer = new MediaPlayer(mediaObject.toJFXMedia());
        mediaPlayer.setAutoPlay(true);
        setMediaPlayer(mediaPlayer);
    }

    @Override
    public void play() {
        MediaPlayer mediaPlayer = getMediaPlayer();
        if(mediaPlayer == null){
            LOGGER.warning("MediaPlayer is null when tried to play.");
            return;
        }
        MediaPlayer.Status status = mediaPlayer.getStatus();
        if(status == MediaPlayer.Status.UNKNOWN){
            LOGGER.warning("Attempted to play video while player is still initializing. Ignoring.");
            return;
        }
        if(Arrays.asList(MediaPlayer.Status.PAUSED, MediaPlayer.Status.STOPPED, MediaPlayer.Status.READY).contains(status)) {
            mediaPlayer.play();
        }
    }

    @Override
    public void pause() {
        MediaPlayer mediaPlayer = getMediaPlayer();
        if(mediaPlayer == null){
            LOGGER.warning("MediaPlayer is null when tried to pause.");
            return;
        }
        MediaPlayer.Status status = mediaPlayer.getStatus();
        if(status == MediaPlayer.Status.UNKNOWN){
            LOGGER.warning("Attempted to pause video while player is still initializing. Ignoring.");
            return;
        }
        if(status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        MediaPlayer player = getMediaPlayer();
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

    @Override
    public void seek(Duration target) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to seek.");
            return;
        }
        getMediaPlayer().seek(target);
    }

    @Override
    public boolean isMute() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to get mute.");
            return false;
        }
        return getMediaPlayer().isMute();
    }

    @Override
    public void setMute(boolean mute) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set mute.");
            return;
        }
        getMediaPlayer().setMute(mute);
    }

    @Override
    public double getVolume() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to get volume.");
            return 0;
        }
        return getMediaPlayer().getVolume() * 100;
    }

    @Override
    public void setVolume(double volume) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set volume.");
            return;
        }
        getMediaPlayer().setVolume(volume / 100.0);
    }

    @Override
    public Duration getCurrentTime() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to get current time.");
            return Duration.ZERO;
        }
        return getMediaPlayer().getCurrentTime();
    }

    @Override
    public Duration getTotalTime() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to get total time.");
            return Duration.ZERO;
        }
        return getMediaPlayer().getTotalDuration();
    }

    @Override
    public PlayerStatus getStatus() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to get status.");
            return PlayerStatus.UNKNOWN;
        }
        switch(getMediaPlayer().getStatus()){
            case PAUSED:
                return PlayerStatus.PAUSED;
            case PLAYING:
                return PlayerStatus.PLAYING;
            case READY:
                return PlayerStatus.READY;
            case STOPPED:
                return PlayerStatus.STOPPED;
            case STALLED:
                return PlayerStatus.BUFFERING;
            case HALTED:
                return PlayerStatus.ERROR;
            case DISPOSED:
                return PlayerStatus.DISPOSED;
            default:
                return PlayerStatus.UNKNOWN;
        }
    }

    @Override
    public void setOnBuffering(Runnable runnable) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set onBuffering.");
            return;
        }
        getMediaPlayer().setOnStalled(runnable);
    }

    @Override
    public void setOnPlaying(Runnable runnable) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set onPlaying.");
            return;
        }
        getMediaPlayer().setOnPlaying(runnable);
    }

    @Override
    public void setOnPaused(Runnable runnable) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set onPaused.");
            return;
        }
        getMediaPlayer().setOnPaused(runnable);
    }

    @Override
    public void setOnStopped(Runnable runnable) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set onStopped.");
            return;
        }
        getMediaPlayer().setOnStopped(runnable);
    }

    @Override
    public void setOnFinished(Runnable runnable) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set onFinished.");
            return;
        }
        getMediaPlayer().setOnEndOfMedia(runnable);
    }

    @Override
    public void setOnError(Runnable runnable) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set onError.");
            return;
        }
        getMediaPlayer().setOnError(runnable);
    }

    @Override
    public void setOnReady(Runnable runnable) {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to set onReady.");
            return;
        }
        getMediaPlayer().setOnReady(runnable);
    }

    @Override
    public void disposePlayer() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to dispose.");
            return;
        }
        if(getMediaPlayer().getStatus() != MediaPlayer.Status.DISPOSED) {
            getMediaPlayer().dispose();
        }
    }

    @Override
    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to get currentTimeProperty.");
            return null;
        }
        return getMediaPlayer().currentTimeProperty();
    }

    @Override
    public BooleanProperty muteProperty() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to get muteProperty.");
            return null;
        }
        return getMediaPlayer().muteProperty();
    }

    @Override
    public DoubleProperty volumeProperty() {
        if(getMediaPlayer() == null){
            LOGGER.warning("MediaPlayer is null when tried to get volumeProperty.");
            return null;
        }
        return getMediaPlayer().volumeProperty();
    }

    @Override
    public Node getNode(){
        return this;
    }

    @Override
    public DoubleProperty heightProperty() {
        return fitHeightProperty();
    }

    @Override
    public DoubleProperty widthProperty() {
        return fitWidthProperty();
    }

    @Override
    public Throwable getError() {
        return getMediaPlayer().getError();
    }

    @Override
    public String getErrorMessage() {
        return getMediaPlayer().getError().toString();
    }

    @Override
    public void releaseResources() {

    }
}
