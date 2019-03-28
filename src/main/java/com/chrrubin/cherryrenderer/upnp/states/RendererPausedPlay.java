package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import javafx.util.Duration;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.PausedPlay;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class RendererPausedPlay extends PausedPlay {

    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();

    public RendererPausedPlay(AVTransport avTransport){
        super(avTransport);
    }

    @Override
    public void onEntry(){
        super.onEntry();
        System.out.println("Entered PausedPlay state");

        rendererHandler.setRendererState(RendererState.PAUSED);
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
        System.out.println("Exited PausedPlay state");
    }

    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {

        System.out.println("RendererPausedPlay.SetTransportURI triggered");

        if(uri != rendererHandler.getUri()) {
            rendererHandler.setUri(uri);
            rendererHandler.setMetadata(metaData);

            getTransport().setMediaInfo(
                    new MediaInfo(uri.toString(), metaData)
            );
        }

        return RendererPausedPlay.class;
    }

    public Class<? extends AbstractState> stop() {
        System.out.println("RendererPausedPlay.stop triggered");
        return RendererStopped.class;
    }

    public Class<? extends AbstractState> play(String speed) {
        System.out.println("RendererPausedPlay.play triggered");
        return RendererPlaying.class;
    }

    public Class<? extends AbstractState> pause() {
        System.out.println("RendererPausedPlay.pause triggered");
        return RendererPausedPlay.class;
    }

    public Class<? extends AbstractState> seek(SeekMode unit, String target){
        System.out.println("RendererPausedPlay.seek triggered");
        if(unit == SeekMode.ABS_TIME || unit == SeekMode.REL_TIME){
            System.out.println("Seeking to " + target);
            Duration duration = CherryUtil.stringToDuration(target);
            rendererHandler.setVideoSeek(duration);
        }

        return RendererPausedPlay.class;
    }
}
