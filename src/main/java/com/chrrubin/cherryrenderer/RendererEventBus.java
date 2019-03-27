package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import javafx.util.Duration;

import java.net.URI;

public class RendererEventBus {
    // Singleton to handle events
    private static RendererEventBus instance = new RendererEventBus();

    private URI uri = null;
    private String metadata = null;

    private final Event<RendererState> rendererStateChangedEvent = new SimpleEvent<>();
    private final Event<URI> uriChangedEvent = new SimpleEvent<>();
    private final Event<String> metadataChangedEvent = new SimpleEvent<>();
    private final Event<Duration> videoCurrentTimeChangedEvent = new SimpleEvent<>();
    private final Event<Duration> videoTotalTimeChangedEvent = new SimpleEvent<>();
    private final Event<Duration> videoSeekEvent = new SimpleEvent<>();

    private RendererEventBus(){}

    public static RendererEventBus getInstance() {
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

    public void setVideoCurrentTime(Duration duration){
        videoCurrentTimeChangedEvent.trigger(duration);
    }

    public void setVideoTotalTime(Duration duration){
        videoTotalTimeChangedEvent.trigger(duration);
    }

    public void setVideoSeek(Duration duration){
        videoSeekEvent.trigger(duration);
    }
}
