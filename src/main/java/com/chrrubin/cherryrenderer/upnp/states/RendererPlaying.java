package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.upnp.AVTransportHandler;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;
import java.util.logging.Logger;

public class RendererPlaying extends Playing {
    final private Logger LOGGER = Logger.getLogger(RendererPlaying.class.getName());

    private AVTransportHandler avTransportHandler = AVTransportHandler.getInstance();

    public RendererPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        LOGGER.fine("Entered Playing state");

        avTransportHandler.setTransport(getTransport());
        avTransportHandler.setRendererState(RendererState.PLAYING);

    }

    public void onExit(){
        LOGGER.fine("Exited Playing state");
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        LOGGER.finer("Setting transport URI...");
        avTransportHandler.setTransportURI(uri, metaData);
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        LOGGER.finer("Play invoked");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> pause() {
        LOGGER.finer("Pause invoked");
        return RendererPausedPlay.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        LOGGER.finer("Next invoked");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        LOGGER.finer("Previous invoked");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        LOGGER.finer("Seek invoked");
        avTransportHandler.seek(unit, target);
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        LOGGER.finer("Stop invoked");

        return RendererStopped.class;
    }
}
