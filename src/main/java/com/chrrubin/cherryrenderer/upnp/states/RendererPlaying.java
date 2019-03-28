package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
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
        System.out.println("Entered Playing state");

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
        System.out.println("Exited Playing state");
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
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
        // FIXME: May freeze service when seeking beyond player buffer.
        System.out.println("RendererPlaying.seek triggered");
        if(unit == SeekMode.ABS_TIME || unit == SeekMode.REL_TIME){
            System.out.println("Seeking to " + target);
            rendererHandler.setVideoSeek(CherryUtil.stringToDuration(target));
        }
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        System.out.println("RendererPlaying.stop triggered");
        // FIXME: Trying to set a new video while video is playing hangs program.
        //  Though it worked fine for setting the exact same video for some reason
        return RendererStopped.class;
    }
}
