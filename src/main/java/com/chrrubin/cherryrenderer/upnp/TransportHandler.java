package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.CherryUtil;
import javafx.util.Duration;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.*;

import java.net.URI;
import java.util.logging.Logger;

public class TransportHandler {
    private final Logger LOGGER = Logger.getLogger(TransportHandler.class.getName());

    private static TransportHandler instance = new TransportHandler();

    private AVTransport transport = null;

    private TransportHandler(){}

    public static TransportHandler getInstance(){
        return instance;
    }

    public synchronized void setTransport(AVTransport transport) {
        this.transport = transport;
    }

    public synchronized void setPositionInfo(URI uri, String metadata){
        if(transport == null){
            return;
        }

        transport.setPositionInfo(new PositionInfo(1, metadata, uri.toString()));
    }

    public synchronized void setPositionInfo(URI uri, String metadata, Duration totalTime, Duration currentTime){
        if(transport == null){
            return;
        }

        transport.setPositionInfo(new PositionInfo(1,
                CherryUtil.durationToString(totalTime),
                metadata,
                uri.toString(),
                CherryUtil.durationToString(currentTime),
                CherryUtil.durationToString(currentTime),
                2147483647,
                2147483647
        ));

        transport.getLastChange().setEventedValue(
                transport.getInstanceId(),
                new AVTransportVariable.RelativeTimePosition(CherryUtil.durationToString(currentTime)),
                new AVTransportVariable.AbsoluteTimePosition(CherryUtil.durationToString(currentTime)),
                new AVTransportVariable.CurrentMediaDuration(CherryUtil.durationToString(totalTime))
        );
    }

    public synchronized void setMediaInfo(URI uri, String metadata){
        if(transport == null){
            return;
        }

        transport.setMediaInfo(new MediaInfo(uri.toString(), metadata));

        transport.getLastChange().setEventedValue(
                transport.getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );
    }

    public synchronized void setMediaInfo(URI uri, String metadata, Duration totalTime){
        if(transport == null){
            return;
        }

        transport.setMediaInfo(new MediaInfo(
                uri.toString(),
                metadata,
                new UnsignedIntegerFourBytes(0L),
                CherryUtil.durationToString(totalTime),
                StorageMedium.NOT_IMPLEMENTED
        ));

        transport.getLastChange().setEventedValue(
                transport.getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );
    }

    /**
     * Notifies control point of the MediaRenderer's state changes when state is changed
     * from the renderer side.
     * @param transportState Current TransportState of the MediaRenderer.
     */
    public synchronized void setTransportInfo(TransportState transportState){
        // FIXME: Control point doesn't get notified after the first video???
        //  As with a lot of other uPnP stuff I'm not sure if it's cling or me or the control point causing this
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
}
