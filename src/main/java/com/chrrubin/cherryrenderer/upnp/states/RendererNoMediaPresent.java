package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.model.AVTransport;

import java.net.URI;
import java.util.logging.Logger;

public class RendererNoMediaPresent extends NoMediaPresent {
    final private Logger LOGGER = Logger.getLogger(RendererNoMediaPresent.class.getName());

    private TransportHandler transportHandler = TransportHandler.getInstance();

    public RendererNoMediaPresent(AVTransport avTransport){
        super(avTransport);
    }

    @Override
    public void onEntry(){
        super.onEntry();
        LOGGER.fine("Entered NoMediaPresent state");

        transportHandler.setTransport(getTransport());
        transportHandler.setRendererState(RendererState.NOMEDIAPRESENT);
    }

    public void onExit(){
        LOGGER.fine("Exited NoMediaPresent state");
    }


    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        LOGGER.finer("Setting transport URI...");
        transportHandler.setTransportURI(uri, metaData);
        return RendererStopped.class;
    }

    public Class<? extends AbstractState> stop() {
        LOGGER.finer("Stop invoked");
        return RendererStopped.class;
    }
}
