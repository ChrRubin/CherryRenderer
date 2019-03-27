package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.CherryUtil;
import javafx.util.Duration;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;

import java.net.URI;

public class TransportHandler {
    private static TransportHandler instance = new TransportHandler();

    private AVTransport transport = null;

    private TransportHandler(){}

    public static TransportHandler getInstance(){
        return instance;
    }

    public AVTransport getTransport() {
        return transport;
    }

    public void setTransport(AVTransport transport) {
        this.transport = transport;
    }

    public void setPositionInfo(URI uri, String metadata){
        if(transport != null) {
            transport.setPositionInfo(new PositionInfo(1, metadata, uri.toString()));
        }
    }

    public void setPositionInfo(URI uri, String metadata, Duration totalTime, Duration currentTime){
        if(transport != null) {
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

    public void setMediaInfo(URI uri, String metadata){
        if(transport != null) {
            transport.setMediaInfo(new MediaInfo(uri.toString(), metadata));

            transport.getLastChange().setEventedValue(
                    transport.getInstanceId(),
                    new AVTransportVariable.AVTransportURI(uri),
                    new AVTransportVariable.CurrentTrackURI(uri)
            );
        }
    }

    public void setMediaInfo(URI uri, String metadata, Duration totalTime){
        if(transport != null) {
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
}
