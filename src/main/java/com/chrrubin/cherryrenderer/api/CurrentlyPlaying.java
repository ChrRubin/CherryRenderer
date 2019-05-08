package com.chrrubin.cherryrenderer.api;

import com.chrrubin.cherryrenderer.MediaObject;

public class CurrentlyPlaying implements Response {
    private MediaObject video;
    private int currentTime;
    private int totalTime;
    private boolean mute;
    private int volume;

    public CurrentlyPlaying(){}

    public MediaObject getVideo() {
        return video;
    }

    public void setVideo(MediaObject video) {
        this.video = video;
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
