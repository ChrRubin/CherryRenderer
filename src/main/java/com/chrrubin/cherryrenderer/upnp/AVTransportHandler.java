package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.MediaObject;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import javafx.util.Duration;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AVTransportHandler {
    private final Logger LOGGER = Logger.getLogger(AVTransportHandler.class.getName());

    private static final AVTransportHandler instance = new AVTransportHandler();

    private AVTransport transport = null;
    private MediaObject mediaObject = null;

    private final Event<RendererState> rendererStateChangedEvent = new SimpleEvent<>();
    private final Event<Duration> videoSeekEvent = new SimpleEvent<>();

    private AVTransportHandler(){}

    public static AVTransportHandler getInstance(){
        return instance;
    }

    public synchronized MediaObject getMediaObject(){
        return mediaObject;
    }

    public synchronized void setMediaObject(MediaObject mediaObject){
        this.mediaObject = mediaObject;
    }

    public synchronized void setTransport(AVTransport transport) {
        this.transport = transport;
    }

    public Event<RendererState> getRendererStateChangedEvent() {
        return rendererStateChangedEvent;
    }

    public void setRendererState(RendererState rendererState){
        LOGGER.info("RendererState set to " + rendererState.name());
        rendererStateChangedEvent.trigger(rendererState);
    }

    public Event<Duration> getVideoSeekEvent() {
        return videoSeekEvent;
    }

    private synchronized void setNewPositionInfo(){
        if(transport == null){
            return;
        }

        transport.setPositionInfo(new PositionInfo(1, mediaObject.getXmlMetadata(), mediaObject.getUriString()));
    }

    public synchronized void setPositionInfoWithTimes(Duration totalTime, Duration currentTime){
        if(transport == null){
            return;
        }

        LOGGER.finest("Updating position info");
        LOGGER.finest("RelTime/AbsTime: " + CherryUtil.durationToString(currentTime));
        LOGGER.finest("CurrentTrackDuration: " + CherryUtil.durationToString(totalTime));

        transport.setPositionInfo(new PositionInfo(1,
                CherryUtil.durationToString(totalTime),
                mediaObject.getXmlMetadata(),
                mediaObject.getUriString(),
                CherryUtil.durationToString(currentTime),
                CherryUtil.durationToString(currentTime),
                2147483647,
                2147483647
        ));
    }

    public synchronized void sendLastChangeMediaDuration(Duration currentTime){
        if(transport == null){
            return;
        }

        transport.getLastChange().setEventedValue(
                transport.getInstanceId(),
                new AVTransportVariable.CurrentMediaDuration(CherryUtil.durationToString(currentTime))
        );
    }

    private synchronized void setNewMediaInfo(){
        if(transport == null){
            return;
        }

        transport.setMediaInfo(new MediaInfo(mediaObject.getUriString(), mediaObject.getXmlMetadata()));

        transport.getLastChange().setEventedValue(
                transport.getInstanceId(),
                new AVTransportVariable.AVTransportURI(mediaObject.getUri()),
                new AVTransportVariable.CurrentTrackURI(mediaObject.getUri())
        );
    }

    public synchronized void setMediaInfoWithTotalTime(Duration totalTime){
        if(transport == null){
            return;
        }

        transport.setMediaInfo(new MediaInfo(
                mediaObject.getUriString(),
                mediaObject.getXmlMetadata(),
                new UnsignedIntegerFourBytes(0L),
                CherryUtil.durationToString(totalTime),
                StorageMedium.NOT_IMPLEMENTED
        ));

        transport.getLastChange().setEventedValue(
                transport.getInstanceId(),
                new AVTransportVariable.AVTransportURI(mediaObject.getUri()),
                new AVTransportVariable.CurrentTrackURI(mediaObject.getUri())
        );
    }

    /**
     * Notifies control point of the MediaRenderer's state changes when state is changed
     * from the renderer side.
     * @param transportState Current TransportState of the MediaRenderer.
     */
    public synchronized void setTransportInfo(TransportState transportState){
        if(transport == null){
            return;
        }

        LOGGER.fine("Notifying control point of transport state change to " + transportState.name());
        transport.setTransportInfo(new TransportInfo(
                transportState,
                transport.getTransportInfo().getCurrentTransportStatus(),
                transport.getTransportInfo().getCurrentSpeed()
        ));

        TransportAction[] transportActions;
        switch (transportState){
            case NO_MEDIA_PRESENT:
                transportActions = new TransportAction[]{TransportAction.Stop};
                break;
            case STOPPED:
                transportActions = new TransportAction[]{TransportAction.Stop, TransportAction.Play, TransportAction.Next, TransportAction.Previous, TransportAction.Seek};
                break;
            case PLAYING:
            case TRANSITIONING:
                transportActions = new TransportAction[]{TransportAction.Stop, TransportAction.Play, TransportAction.Pause, TransportAction.Next, TransportAction.Previous, TransportAction.Seek};
                break;
            case PAUSED_PLAYBACK:
                transportActions = new TransportAction[]{TransportAction.Stop, TransportAction.Play};
                break;
            default:
                transportActions = new TransportAction[]{};
                break;
        }

        transport.getLastChange().setEventedValue(
                transport.getInstanceId(),
                new AVTransportVariable.TransportState(transportState),
                new AVTransportVariable.CurrentTransportActions(transportActions)
        );
    }

    public synchronized void clearInfo(){
        LOGGER.fine("Clearing MediaInfo and PositionInfo");
        mediaObject = null;

        if(transport == null){
            return;
        }
        transport.setMediaInfo(new MediaInfo());
        transport.setPositionInfo(new PositionInfo());
    }

    public synchronized void setTransportURI(URI uri, String metadata){
        LOGGER.finest("URI: " + uri.toString());
        LOGGER.finest("Metadata: " + metadata);

        try {
            mediaObject = new UpnpMediaObject(uri, metadata);
            setNewMediaInfo();
            setNewPositionInfo();
        }
        catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    public void seek(SeekMode unit, String target){
        if(unit == SeekMode.ABS_TIME || unit == SeekMode.REL_TIME){
            LOGGER.finer("Seeking to " + target + " with unit " + unit.name());
            videoSeekEvent.trigger(CherryUtil.stringToDuration(target));
        }
    }
}
