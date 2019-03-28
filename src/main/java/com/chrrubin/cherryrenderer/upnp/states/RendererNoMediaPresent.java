package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.model.AVTransport;

import java.net.URI;

public class RendererNoMediaPresent extends NoMediaPresent {

    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();

    public RendererNoMediaPresent(AVTransport avTransport){
        super(avTransport);
    }

    @Override
    public void onEntry(){
        super.onEntry();
        System.out.println("Entered NoMediaPresent state");

        transportHandler.setTransport(getTransport());
        rendererHandler.setRendererState(RendererState.NOMEDIAPRESENT);
    }

    public void onExit(){
        System.out.println("Exited NoMediaPresent state");
    }


    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {

        System.out.println("RendererNoMediaPresent.SetTransportURI triggered");

        if(uri != rendererHandler.getUri()) {
            rendererHandler.setUri(uri);
            rendererHandler.setMetadata(metaData);

            transportHandler.setMediaInfo(uri, metaData);

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
