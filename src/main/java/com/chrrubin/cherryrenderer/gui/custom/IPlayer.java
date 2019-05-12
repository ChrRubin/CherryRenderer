package com.chrrubin.cherryrenderer.gui.custom;

import com.chrrubin.cherryrenderer.MediaObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.util.Duration;

public interface IPlayer {
    void playNewMedia(MediaObject mediaObject);
    void play();
    void pause();
    void stop();
    void seek(Duration target);
    boolean isMute();
    void setMute(boolean mute);
    double getVolume();
    void setVolume(double volume);
    Duration getCurrentTime();
    Duration getTotalTime();
    PlayerStatus getStatus();
    void setOnBuffering(Runnable runnable);
    void setOnPlaying(Runnable runnable);
    void setOnPaused(Runnable runnable);
    void setOnStopped(Runnable runnable);
    void setOnFinished(Runnable runnable);
    void setOnError(Runnable runnable);
    void setOnReady(Runnable runnable);
    void disposePlayer();
    ReadOnlyObjectProperty<Duration> currentTimeProperty();
    BooleanProperty muteProperty();
    DoubleProperty volumeProperty();
    Node getNode();
    DoubleProperty heightProperty();
    DoubleProperty widthProperty();
    Throwable getError();
    String getErrorMessage();
}
