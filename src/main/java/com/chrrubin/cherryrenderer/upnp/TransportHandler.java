package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.CherryUtil;
import javafx.util.Duration;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.*;

import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransportHandler {
    private static TransportHandler instance = new TransportHandler();

    private AVTransport transport = null;
    private ReadWriteLock transportLock = new ReentrantReadWriteLock();
    private Lock readTransportLock = transportLock.readLock();
    private Lock writeTransportLock = transportLock.writeLock();

    private TransportHandler(){}

    public static TransportHandler getInstance(){
        return instance;
    }

    public AVTransport getTransport() {
        try {
            readTransportLock.lock();
            return transport;
        }
        finally {
            readTransportLock.unlock();
        }
    }

    public void setTransport(AVTransport transport) {
        try {
            writeTransportLock.lock();
            this.transport = transport;
        }
        finally {
            writeTransportLock.unlock();
        }
    }

    public void setPositionInfo(URI uri, String metadata){
        try {
            writeTransportLock.lock();
            if (transport != null) {
                transport.setPositionInfo(new PositionInfo(1, metadata, uri.toString()));
            }
        }
        finally {
            writeTransportLock.unlock();
        }
    }

    public void setPositionInfo(URI uri, String metadata, Duration totalTime, Duration currentTime){
        try {
            writeTransportLock.lock();
            if (transport != null) {
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
        }
        finally {
            writeTransportLock.unlock();
        }
    }

    public void setMediaInfo(URI uri, String metadata){
        try {
            writeTransportLock.lock();
            if (transport != null) {
                transport.setMediaInfo(new MediaInfo(uri.toString(), metadata));

                transport.getLastChange().setEventedValue(
                        transport.getInstanceId(),
                        new AVTransportVariable.AVTransportURI(uri),
                        new AVTransportVariable.CurrentTrackURI(uri)
                );
            }
        }
        finally {
            writeTransportLock.unlock();
        }
    }

    public void setMediaInfo(URI uri, String metadata, Duration totalTime){
        try {
            writeTransportLock.lock();
            if (transport != null) {
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
        }
        finally {
            writeTransportLock.unlock();
        }
    }

    public void setTransportInfo(TransportState transportState){
        // TODO: is this really the best way of doing this? I can notify the control point of changes but I'm worried the MediaRenderer itself wouldn't be notified
        try{
            writeTransportLock.lock();
            if(transport != null){
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
        finally {
            writeTransportLock.unlock();
        }
    }
}
