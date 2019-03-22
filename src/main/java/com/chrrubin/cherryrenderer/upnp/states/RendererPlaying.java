package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.ApplicationHelper;
import javafx.util.Duration;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.*;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RendererPlaying extends Playing {
//    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private URI uri;

    public RendererPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        // Start playing now!
        System.out.println("Entered Playing state");
        ApplicationHelper.setRendererState(RendererState.PLAYING);
//        executorService.scheduleWithFixedDelay(this::updatePositionInfo, 0, 1, TimeUnit.SECONDS);
    }

    public void onExit(){
//        System.out.println("Shutting down executor service");
//        executorService.shutdown();
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        // Your choice of action here, and what the next state is going to be!

        this.uri = uri;

        if(uri != ApplicationHelper.getUri()) {
            ApplicationHelper.setUri(uri);
            ApplicationHelper.setMetadata(metaData);

            getTransport().setMediaInfo(
                    new MediaInfo(uri.toString(), metaData)
            );

            getTransport().getLastChange().setEventedValue(
                    getTransport().getInstanceId(),
                    new AVTransportVariable.CurrentPlayMode(PlayMode.NORMAL)
                    );

        }

        updatePositionInfo();

        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        updatePositionInfo();
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> pause() {
        // TODO: Doesn't work? Control point sends pause request successfully but it doesn't get caught. It does trigger updatePositionInfo (though that might be from another call)
        updatePositionInfo();
        return RendererPausedPlay.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        updatePositionInfo();
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        updatePositionInfo();
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        updatePositionInfo();
        if(unit == SeekMode.ABS_TIME || unit == SeekMode.REL_TIME){
            ApplicationHelper.setRendererState(RendererState.SEEKING);
            // TODO: translate target to Duration
        }
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        // Stop playing!
        return RendererStopped.class;
    }

    private void updatePositionInfo(){
        if(ApplicationHelper.getVideoCurrentTime() != null && ApplicationHelper.getVideoTotalTime() != null){
            Duration videoTotalTime = ApplicationHelper.getVideoTotalTime();
            Duration videoCurrentTime = ApplicationHelper.getVideoCurrentTime();

            System.out.println("Setting current position to " + ApplicationHelper.durationToString(videoCurrentTime));

            getTransport().setPositionInfo(new PositionInfo(1,
                    ApplicationHelper.durationToString(videoTotalTime),
                    uri.toString(),
                    ApplicationHelper.durationToString(videoCurrentTime),
                    ApplicationHelper.durationToString(videoCurrentTime)
                    ));
            // TODO: This doesn't seem to be updating GetPositionInfo response?
        }
    }
}
