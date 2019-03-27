package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.RendererEventBus;
import javafx.util.Duration;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.PausedPlay;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class RendererPausedPlay extends PausedPlay {

    private RendererEventBus rendererEventBus = RendererEventBus.getInstance();

    public RendererPausedPlay(AVTransport avTransport){
        super(avTransport);
    }

    @Override
    public void onEntry(){
        super.onEntry();
        System.out.println("Entered PausedPlay state");

        Duration totalTime = rendererEventBus.getVideoTotalTime();
        Duration currentTime = rendererEventBus.getVideoCurrentTime();

        getTransport().setPositionInfo(new PositionInfo(1,
                CherryUtil.durationToString(totalTime),
                rendererEventBus.getUri().toString(),
                CherryUtil.durationToString(currentTime),
                CherryUtil.durationToString(currentTime)
        ));

        getTransport().getLastChange().setEventedValue(
                getTransport().getInstanceId(),
                new AVTransportVariable.RelativeTimePosition(CherryUtil.durationToString(currentTime)),
                new AVTransportVariable.AbsoluteTimePosition(CherryUtil.durationToString(currentTime)),
                new AVTransportVariable.CurrentMediaDuration(CherryUtil.durationToString(totalTime))
        );

        rendererEventBus.setRendererState(RendererState.PAUSED);
    }

    public void onExit(){
        System.out.println("Exited PausedPlay state");
    }

    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {

        System.out.println("RendererPausedPlay.SetTransportURI triggered");

        if(uri != rendererEventBus.getUri()) {
            rendererEventBus.setUri(uri);
            rendererEventBus.setMetadata(metaData);

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

    public Class<? extends AbstractState> seek(SeekMode unit, String target){
        return RendererPausedPlay.class;
    }
}
