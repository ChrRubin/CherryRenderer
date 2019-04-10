package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;
import java.util.logging.Logger;

public class RendererPlaying extends Playing {
    final private Logger LOGGER = Logger.getLogger(RendererPlaying.class.getName());

    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();

    public RendererPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        LOGGER.info("Entered Playing state");

        transportHandler.setTransport(getTransport());

        if(rendererHandler.getVideoTotalTime() != null && rendererHandler.getVideoCurrentTime() != null){
            transportHandler.setMediaInfo(
                    rendererHandler.getUri(),
                    rendererHandler.getMetadata(),
                    rendererHandler.getVideoTotalTime()
            );
            transportHandler.setPositionInfo(
                    rendererHandler.getUri(),
                    rendererHandler.getMetadata(),
                    rendererHandler.getVideoTotalTime(),
                    rendererHandler.getVideoCurrentTime()
            );
        }

        rendererHandler.setRendererState(RendererState.PLAYING);

    }

    public void onExit(){
        LOGGER.info("Exited Playing state");
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

        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        LOGGER.fine("Play invoked");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> pause() {
        LOGGER.fine("Pause invoked");
        return RendererPausedPlay.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        LOGGER.fine("Next invoked");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        LOGGER.fine("Previous invoked");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        LOGGER.fine("Seek invoked");

        if(unit == SeekMode.ABS_TIME || unit == SeekMode.REL_TIME){
            LOGGER.finer("Seeking to " + target + " with unit " + unit.name());
            rendererHandler.setVideoSeek(CherryUtil.stringToDuration(target));
        }
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        LOGGER.fine("Stop invoked");
        // FIXME: Stopping within 2 seconds after video is ready freezes the program. Deadlock somewhere?
        //  This shit is just inconsistent to reproduce for some reason
        return RendererStopped.class;
    }
}
