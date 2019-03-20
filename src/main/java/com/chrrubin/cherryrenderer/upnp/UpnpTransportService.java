package com.chrrubin.cherryrenderer.upnp;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.model.*;

public class UpnpTransportService extends AbstractAVTransportService {
    private String uriString;
    private String metaData;

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId, String currentURI, String currentURIMetaData) throws AVTransportException {
        this.uriString = currentURI;
        this.metaData = metaData;
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId, String nextURI, String nextURIMetaData) throws AVTransportException {

    }

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return null;
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return null;
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return null;
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return null;
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return null;
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
        System.out.println("URI: " + uriString);
        System.out.println("Metadata: " + metaData);
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {

    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {

    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {

    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return new TransportAction[0];
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return new UnsignedIntegerFourBytes[0];
    }
}
