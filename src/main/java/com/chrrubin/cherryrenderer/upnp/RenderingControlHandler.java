package com.chrrubin.cherryrenderer.upnp;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;

import java.util.logging.Logger;

public class RenderingControlHandler {
    private final Logger LOGGER = Logger.getLogger(RenderingControlHandler.class.getName());
    private static RenderingControlHandler instance = new RenderingControlHandler();

    private double volume = 100.0;
    private boolean mute = false;
    private Event<Boolean> videoMuteEvent = new SimpleEvent<>();
    private Event<Double> videoVolumeEvent = new SimpleEvent<>();
    private Event<Boolean> rendererMuteEvent = new SimpleEvent<>();
    private Event<Double> rendererVolumeEvent = new SimpleEvent<>();

    private RenderingControlHandler(){}

    public static RenderingControlHandler getInstance() {
        return instance;
    }

    public synchronized double getVolume() {
        return volume;
    }

    public synchronized void setVideoVolume(double volume){
        this.volume = volume;
        videoVolumeEvent.trigger(volume);
    }

    public synchronized void setRendererVolume(double volume){
        this.volume = volume;
        rendererVolumeEvent.trigger(volume);
    }

    public synchronized boolean isMute() {
        return mute;
    }

    public synchronized void setVideoMute(boolean mute){
        this.mute = mute;
        videoMuteEvent.trigger(mute);
    }

    public synchronized void setRendererMute(boolean mute){
        this.mute = mute;
        rendererMuteEvent.trigger(mute);
    }

    public Event<Boolean> getVideoMuteEvent() {
        return videoMuteEvent;
    }

    public Event<Double> getVideoVolumeEvent() {
        return videoVolumeEvent;
    }

    public Event<Boolean> getRendererMuteEvent() {
        return rendererMuteEvent;
    }

    public Event<Double> getRendererVolumeEvent() {
        return rendererVolumeEvent;
    }
}
