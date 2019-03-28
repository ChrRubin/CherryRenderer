package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import javafx.util.Duration;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class RendererPlaying extends Playing {
    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();

    public RendererPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        // Start playing now!
        System.out.println("Entered Playing state");

        rendererHandler.setRendererState(RendererState.PLAYING);
        transportHandler.setTransport(getTransport());

        if(rendererHandler.getVideoTotalTime() != null && rendererHandler.getVideoTotalTime() != null){
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

    }

    public void onExit(){
        System.out.println("Exited Playing state");
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        // Your choice of action here, and what the next state is going to be!

        System.out.println("RendererPlaying.setTransportURI triggered");

        if(uri != rendererHandler.getUri()) {
            rendererHandler.setUri(uri);
            rendererHandler.setMetadata(metaData);

            transportHandler.setMediaInfo(uri, metaData);
            transportHandler.setPositionInfo(uri, metaData);
        }

        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        System.out.println("RendererPlaying.play triggered");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> pause() {
        System.out.println("RendererPlaying.pause triggered");
        return RendererPausedPlay.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        System.out.println("RendererPlaying.next triggered");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        System.out.println("RendererPlaying.previous triggered");
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        System.out.println("RendererPlaying.seek triggered");
        if(unit == SeekMode.ABS_TIME || unit == SeekMode.REL_TIME){
            System.out.println("Seeking to " + target);

            Duration duration = CherryUtil.stringToDuration(target);
            rendererHandler.setVideoSeek(duration);
        }
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        // Stop playing!
        System.out.println("RendererPlaying.stop triggered");
        return RendererStopped.class;
    }
}
