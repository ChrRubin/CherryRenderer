package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.ApplicationHelper;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.PausedPlay;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class RendererPausedPlay extends PausedPlay {

    public RendererPausedPlay(AVTransport avTransport){
        super(avTransport);
    }

    @Override
    public void onEntry(){
        super.onEntry();
        System.out.println("Entered PausedPlay state");
        ApplicationHelper.setRendererState(RendererState.PAUSED);
    }

    public void onExit(){
        System.out.println("Exited PausedPlay state");
    }

    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {

        System.out.println("RendererPausedPlay.SetTransportURI triggered");

        if(uri != ApplicationHelper.getUri()) {
            ApplicationHelper.setUri(uri);
            ApplicationHelper.setMetadata(metaData);

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
        // FIXME: 500 error returned by renderer
        return RendererPlaying.class;
    }

    public Class<? extends AbstractState> seek(SeekMode unit, String target){
        return RendererPausedPlay.class;
    }
}
