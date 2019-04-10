package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import javafx.util.Duration;

import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class RendererHandler {
    private final Logger LOGGER = Logger.getLogger(RendererHandler.class.getName());

    private static RendererHandler instance = new RendererHandler();

    private URI uri = null;
    private String metadata = null;
    private Duration videoCurrentTime = null;
    private Duration videoTotalTime = null;

    private ReadWriteLock currentTimeLock = new ReentrantReadWriteLock();
    private Lock readCurrentTimeLock = currentTimeLock.readLock();
    private Lock writeCurrentTimeLock = currentTimeLock.writeLock();

    private final Event<RendererState> rendererStateChangedEvent = new SimpleEvent<>();
    private final Event<Duration> videoSeekEvent = new SimpleEvent<>();

    private RendererHandler(){}

    public static RendererHandler getInstance() {
        return instance;
    }

    public Event<RendererState> getRendererStateChangedEvent() {
        return rendererStateChangedEvent;
    }

    public Event<Duration> getVideoSeekEvent() {
        return videoSeekEvent;
    }

    public void setRendererState(RendererState rendererState){
        LOGGER.info("RendererState set to " + rendererState.name());
        rendererStateChangedEvent.trigger(rendererState);
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri){
        this.uri = uri;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata){
        this.metadata = metadata;
    }

    public Duration getVideoCurrentTime() {
        try {
            readCurrentTimeLock.lock();
            return videoCurrentTime;
        }
        finally {
            readCurrentTimeLock.unlock();
        }
    }

    public void setVideoCurrentTime(Duration duration){
        try {
            writeCurrentTimeLock.lock();
            this.videoCurrentTime = duration;
        }
        finally {
            writeCurrentTimeLock.unlock();
        }
    }

    public Duration getVideoTotalTime() {
        return videoTotalTime;
    }

    public void setVideoTotalTime(Duration duration){
        this.videoTotalTime = duration;
    }

    public void setVideoSeek(Duration duration){
        videoSeekEvent.trigger(duration);
    }
}
