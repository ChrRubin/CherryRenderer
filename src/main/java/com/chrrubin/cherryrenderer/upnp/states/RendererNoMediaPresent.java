package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.ApplicationHelper;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import java.net.URI;

public class RendererNoMediaPresent extends NoMediaPresent {

    public RendererNoMediaPresent(AVTransport avTransport){
        super(avTransport);
        System.out.println("Entered NoMediaPresent state");
        ApplicationHelper.setInstanceID(avTransport.getInstanceId());
        ApplicationHelper.setRendererState(RendererState.NOMEDIAPRESENT);
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        if(uri != ApplicationHelper.getUri()) {
            ApplicationHelper.setUri(uri);
            ApplicationHelper.setMetadata(metaData);

            getTransport().setMediaInfo(
                    new MediaInfo(uri.toString(), metaData)
            );

            // If you can, you should find and set the duration of the track here!
            getTransport().setPositionInfo(
                    new PositionInfo(1, metaData, uri.toString())
            );

            // It's up to you what "last changes" you want to announce to event listeners
            getTransport().getLastChange().setEventedValue(
                    getTransport().getInstanceId(),
                    new AVTransportVariable.AVTransportURI(uri),
                    new AVTransportVariable.CurrentTrackURI(uri)
            );

            return RendererStopped.class;
        }
        else{
            return RendererNoMediaPresent.class;
        }
    }
}
