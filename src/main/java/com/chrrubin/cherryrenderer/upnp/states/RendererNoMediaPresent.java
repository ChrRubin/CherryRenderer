package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.RendererEventBus;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import java.net.URI;

public class RendererNoMediaPresent extends NoMediaPresent {

    private RendererEventBus rendererEventBus = RendererEventBus.getInstance();

    public RendererNoMediaPresent(AVTransport avTransport){
        super(avTransport);
        System.out.println("Entered NoMediaPresent state");

        rendererEventBus.setRendererState(RendererState.NOMEDIAPRESENT);
    }

    public void onExit(){
        System.out.println("Exited NoMediaPresent state");
    }


    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {

        System.out.println("RendererNoMediaPresent.SetTransportURI triggered");

        if(uri != rendererEventBus.getUri()) {
            rendererEventBus.setUri(uri);
            rendererEventBus.setMetadata(metaData);

            getTransport().setMediaInfo(
                    new MediaInfo(uri.toString(), metaData)
            );

//             If you can, you should find and set the duration of the track here!
//            getTransport().setPositionInfo(
//                    new PositionInfo(1, metaData, uri.toString())
//            );

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

    public Class<? extends AbstractState> stop() {
        System.out.println("RendererNoMediaPresent.stop triggered");
        return RendererStopped.class;
    }
}
