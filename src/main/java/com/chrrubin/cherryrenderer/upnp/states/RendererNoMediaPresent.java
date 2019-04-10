package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.model.AVTransport;

import java.net.URI;
import java.util.logging.Logger;

public class RendererNoMediaPresent extends NoMediaPresent {
    final private Logger LOGGER = Logger.getLogger(RendererNoMediaPresent.class.getName());

    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();

    public RendererNoMediaPresent(AVTransport avTransport){
        super(avTransport);
    }

    @Override
    public void onEntry(){
        super.onEntry();
        LOGGER.info("Entered NoMediaPresent state");

        transportHandler.setTransport(getTransport());
        rendererHandler.setRendererState(RendererState.NOMEDIAPRESENT);
    }

    public void onExit(){
        LOGGER.info("Exited NoMediaPresent state");
    }


    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {

        LOGGER.fine("Setting transport URI...");
        LOGGER.finer("URI: " + uri.toString());
        LOGGER.finer("Metadata: " + metaData);

        rendererHandler.setUri(uri);
        rendererHandler.setMetadata(metaData);

        transportHandler.setMediaInfo(uri, metaData);
        transportHandler.setPositionInfo(uri, metaData);

        return RendererStopped.class;
    }

    public Class<? extends AbstractState> stop() {
        LOGGER.fine("Stop invoked");
        return RendererStopped.class;
    }
}
