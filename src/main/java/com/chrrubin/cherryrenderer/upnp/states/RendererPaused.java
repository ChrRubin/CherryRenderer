package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.ApplicationHelper;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.PausedPlay;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;

import java.net.URI;

public class RendererPaused extends PausedPlay {

    public RendererPaused(AVTransport avTransport){
        super(avTransport);
        ApplicationHelper.setRendererState(RendererState.PAUSED);
    }

    @Override
    public void onEntry(){
        super.onEntry();
    }

    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        if(uri != ApplicationHelper.getUri()) {
            ApplicationHelper.setUri(uri);
            ApplicationHelper.setMetadata(metaData);

            getTransport().setMediaInfo(
                    new MediaInfo(uri.toString(), metaData)
            );
            return RendererPlaying.class;
        }
        else {
            return RendererPaused.class;
        }
    }

    public Class<? extends AbstractState> stop() {
        return RendererStopped.class;
    }

    public Class<? extends AbstractState> play(String speed) {
        return RendererPlaying.class;
    }
}
