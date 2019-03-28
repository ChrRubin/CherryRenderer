package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import javafx.util.Duration;
import org.fourthline.cling.support.model.AVTransport;

import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RendererHandler {
    private static RendererHandler instance = new RendererHandler();

    private AVTransport avTransport = null;
    private URI uri = null;
    private String metadata = null;
    private Duration videoCurrentTime = null;
    private Duration videoTotalTime = null;

    private ReadWriteLock currentTimeLock = new ReentrantReadWriteLock();
    private Lock readCurrentTimeLock = currentTimeLock.readLock();
    private Lock writeCurrentTimeLock = currentTimeLock.writeLock();

    private final Event<RendererState> rendererStateChangedEvent = new SimpleEvent<>();
    private final Event<URI> uriChangedEvent = new SimpleEvent<>();
    private final Event<String> metadataChangedEvent = new SimpleEvent<>();
    private final Event<Duration> videoCurrentTimeChangedEvent = new SimpleEvent<>();
    private final Event<Duration> videoTotalTimeChangedEvent = new SimpleEvent<>();
    private final Event<Duration> videoSeekEvent = new SimpleEvent<>();

    private RendererHandler(){}

    public static RendererHandler getInstance() {
        return instance;
    }

    public Event<RendererState> getRendererStateChangedEvent() {
        return rendererStateChangedEvent;
    }

    public Event<URI> getUriChangedEvent() {
        return uriChangedEvent;
    }

    public Event<String> getMetadataChangedEvent() {
        return metadataChangedEvent;
    }

    public Event<Duration> getVideoCurrentTimeChangedEvent() {
        return videoCurrentTimeChangedEvent;
    }

    public Event<Duration> getVideoTotalTimeChangedEvent() {
        return videoTotalTimeChangedEvent;
    }

    public Event<Duration> getVideoSeekEvent() {
        return videoSeekEvent;
    }

    public void setRendererState(RendererState rendererState){
        System.out.println("RendererState set to " + rendererState.name());
        rendererStateChangedEvent.trigger(rendererState);
    }

    public AVTransport getAvTransport() {
        return avTransport;
    }

    public void setAvTransport(AVTransport avTransport) {
        this.avTransport = avTransport;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri){
        this.uri = uri;
        uriChangedEvent.trigger(uri);
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata){
        this.metadata = metadata;
        metadataChangedEvent.trigger(metadata);
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
            videoCurrentTimeChangedEvent.trigger(duration);
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
        videoTotalTimeChangedEvent.trigger(duration);
    }

    public void setVideoSeek(Duration duration){
        videoSeekEvent.trigger(duration);
    }
}
