package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.RendererEventBus;
import javafx.util.Duration;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class RendererPlaying extends Playing {
    private RendererEventBus rendererEventBus = RendererEventBus.getInstance();

    public RendererPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        // Start playing now!
        System.out.println("Entered Playing state");

        rendererEventBus.setRendererState(RendererState.PLAYING);

        //TODO: This doesn't seem like a good way of implementing this, so it's commented out for now
//        rendererEventBus.getVideoCurrentTimeChangedEvent().addListener(currentTime -> {
//            System.out.println("Setting current position to " + CherryUtil.durationToString(currentTime));
//            Duration totalTime = rendererEventBus.getVideoTotalTime();
//
//            getTransport().setPositionInfo(new PositionInfo(1,
//                    CherryUtil.durationToString(totalTime),
//                    rendererEventBus.getUri().toString(),
//                    CherryUtil.durationToString(currentTime),
//                    CherryUtil.durationToString(currentTime)
//                    ));
//
//            getTransport().getLastChange().setEventedValue(
//                    getTransport().getInstanceId(),
//                    new AVTransportVariable.RelativeTimePosition(CherryUtil.durationToString(currentTime)),
//                    new AVTransportVariable.AbsoluteTimePosition(CherryUtil.durationToString(currentTime)),
//                    new AVTransportVariable.CurrentMediaDuration(CherryUtil.durationToString(totalTime))
//            );
//        });

    }

    public void onExit(){
//        System.out.println("Shutting down executor service");
//        executorService.shutdown();
        System.out.println("Exited Playing state");
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        // Your choice of action here, and what the next state is going to be!

        System.out.println("RendererPlaying.setTransportURI triggered");

        if(uri != rendererEventBus.getUri()) {
            rendererEventBus.setUri(uri);
            rendererEventBus.setMetadata(metaData);

            getTransport().setMediaInfo(
                    new MediaInfo(uri.toString(), metaData)
            );
        }

//        updatePositionInfo();

        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        System.out.println("RendererPlaying.play triggered");
//        updatePositionInfo();
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> pause() {
        System.out.println("RendererPlaying.pause triggered");
//        updatePositionInfo();
        return RendererPausedPlay.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        System.out.println("RendererPlaying.next triggered");
//        updatePositionInfo();
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        System.out.println("RendererPlaying.previous triggered");
//        updatePositionInfo();
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        System.out.println("RendererPlaying.seek triggered");
//        updatePositionInfo();
        if(unit == SeekMode.ABS_TIME || unit == SeekMode.REL_TIME){
            System.out.println("Seeking to " + target);

            Duration duration = CherryUtil.stringToDuration(target);
            rendererEventBus.setVideoSeek(duration);
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
