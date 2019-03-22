package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.ApplicationHelper;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.PausedPlay;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;

import java.net.URI;

public class RendererPausedPlay extends PausedPlay {

    public RendererPausedPlay(AVTransport avTransport){
        super(avTransport);
        System.out.println("Entered PausedPlay state");
        ApplicationHelper.setRendererState(RendererState.PAUSED);
    }

    @Override
    public void onEntry(){
        super.onEntry();

        if(ApplicationHelper.getVideoCurrentTime() != null && ApplicationHelper.getVideoTotalTime() != null){
            getTransport().getLastChange().setEventedValue(
                    getTransport().getInstanceId(),
                    new AVTransportVariable.CurrentTrackDuration(ApplicationHelper.durationToString(ApplicationHelper.getVideoTotalTime())),
                    new AVTransportVariable.AbsoluteTimePosition(ApplicationHelper.durationToString(ApplicationHelper.getVideoCurrentTime())),
                    new AVTransportVariable.RelativeTimePosition(ApplicationHelper.durationToString(ApplicationHelper.getVideoCurrentTime()))
            );
        }
    }

    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
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
        return RendererStopped.class;
    }

    public Class<? extends AbstractState> play(String speed) {
        return RendererPlaying.class;
    }
}
