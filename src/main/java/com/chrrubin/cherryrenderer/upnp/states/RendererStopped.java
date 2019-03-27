package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.RendererEventBus;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Stopped;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class RendererStopped extends Stopped {

    private RendererEventBus rendererEventBus = RendererEventBus.getInstance();

    public RendererStopped(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        // Optional: Stop playing, release resources, etc.
        System.out.println("Entered Stopped state");

        rendererEventBus.setRendererState(RendererState.STOPPED);
    }

    public void onExit(){
        System.out.println("Exited Stopped state");
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        // This operation can be triggered in any state, you should think
        // about how you'd want your player to react. If we are in Stopped
        // state nothing much will happen, except that you have to set
        // the media and position info, just like in MyRendererNoMediaPresent.
        // However, if this would be the MyRendererPlaying state, would you
        // prefer stopping first?

        System.out.println("RendererStopped.SetTransportURI triggered");

        // FIXME: Renderer goes to an infinite loop when setting URI while playing - PLAYING - STOPPPED - STOPPED - STOPPED...
        rendererEventBus.setUri(uri);
        rendererEventBus.setMetadata(metaData);

        getTransport().setMediaInfo(
                new MediaInfo(uri.toString(), metaData)
        );

        getTransport().setPositionInfo(
                new PositionInfo(1, metaData, uri.toString())
        );

        getTransport().getLastChange().setEventedValue(
                getTransport().getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );

        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        System.out.println("RendererStopped.stop triggered");
        /// Same here, if you are stopped already and someone calls STOP, well...
        
        // FIXME: The immediate transition from current to desired state not supported. java.lang.reflect.InvocationTargetException

        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        System.out.println("RendererStopped.play triggered");
        // It's easier to let this classes' onEntry() method do the work
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        System.out.println("RendererStopped.next triggered");
        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        System.out.println("RendererStopped.previous triggered");
        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        System.out.println("RendererStopped.seek triggered");
        // Implement seeking with the stream in stopped state!
        return RendererStopped.class;
    }
}
