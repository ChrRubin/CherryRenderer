package com.chrrubin.cherryrenderer.api;

import com.chrrubin.cherryrenderer.MediaObject;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;

import java.net.URI;

public class CurrentlyPlaying implements GetResponse {
    private MediaObject media = new ApiMediaObject(URI.create(""), "");
    private RendererState status = RendererState.STOPPED;
    private int currentTime = 0;
    private int totalTime = 0;
    private boolean mute = false;
    private int volume = 100;

    public CurrentlyPlaying(){}

    public MediaObject getMedia() {
        return media;
    }

    public void setMedia(MediaObject media) {
        this.media = media;
    }

    public RendererState getStatus() {
        return status;
    }

    public void setStatus(RendererState status) {
        this.status = status;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
