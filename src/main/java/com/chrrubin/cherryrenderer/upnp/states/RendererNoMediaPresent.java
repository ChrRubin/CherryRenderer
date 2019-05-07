package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.upnp.AVTransportHandler;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;
import java.util.logging.Logger;

public class RendererNoMediaPresent extends NoMediaPresent {
    final private Logger LOGGER = Logger.getLogger(RendererNoMediaPresent.class.getName());

    private AVTransportHandler avTransportHandler = AVTransportHandler.getInstance();

    public RendererNoMediaPresent(AVTransport avTransport){
        super(avTransport);
    }

    @Override
    public void onEntry(){
        super.onEntry();
        LOGGER.fine("Entered NoMediaPresent state");

        avTransportHandler.setTransport(getTransport());
        avTransportHandler.setRendererState(RendererState.NOMEDIAPRESENT);
    }

    public void onExit(){
        LOGGER.fine("Exited NoMediaPresent state");
    }


    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        LOGGER.finer("Setting transport URI...");
        avTransportHandler.setTransportURI(uri, metaData);
        return RendererStopped.class;
    }

    public Class<? extends AbstractState> stop() {
        LOGGER.finer("Stop invoked");
        return RendererStopped.class;
    }

    public Class<? extends AbstractState> play(String speed) {
        LOGGER.finer("Play invoked");
        return RendererPlaying.class;
    }

    public Class<? extends AbstractState> pause() {
        LOGGER.finer("Pause invoked");
        return RendererPausedPlay.class;
    }

    public Class<? extends AbstractState> seek(SeekMode unit, String target){
        LOGGER.finer("Seek invoked");
        avTransportHandler.seek(unit, target);
        return RendererPausedPlay.class;
    }
}
