package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.ApplicationHelper;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class RendererPlaying extends Playing {
    public RendererPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        // Start playing now!
        ApplicationHelper.setRendererState(RendererState.PLAYING);

    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        // Your choice of action here, and what the next state is going to be!
        if(uri != ApplicationHelper.getUri()) {
            ApplicationHelper.setUri(uri);
            ApplicationHelper.setMetadata(metaData);

            getTransport().setMediaInfo(
                    new MediaInfo(uri.toString(), metaData)
            );
        }
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> pause() {
        return RendererPaused.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        // Stop playing!
        return RendererStopped.class;
    }
}
