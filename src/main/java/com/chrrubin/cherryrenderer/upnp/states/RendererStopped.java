package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Stopped;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;
import java.util.logging.Logger;

public class RendererStopped extends Stopped {
    final private Logger LOGGER = Logger.getLogger(RendererStopped.class.getName());

    private TransportHandler transportHandler = TransportHandler.getInstance();

    public RendererStopped(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        LOGGER.fine("Entered Stopped state");

        transportHandler.setTransport(getTransport());
        transportHandler.setRendererState(RendererState.STOPPED);
    }

    public void onExit(){
        LOGGER.fine("Exited Stopped state");
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        LOGGER.finer("Setting transport URI...");
        transportHandler.setTransportURI(uri, metaData);
        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        LOGGER.finer("Stop invoked");

        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        LOGGER.finer("Play invoked");

        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        LOGGER.finer("Next invoked");
        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        LOGGER.finer("Previous invoked");
        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        LOGGER.finer("Seek invoked");
        return RendererStopped.class;
    }
}
