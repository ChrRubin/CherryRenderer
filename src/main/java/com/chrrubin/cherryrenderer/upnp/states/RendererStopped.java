package com.chrrubin.cherryrenderer.upnp.states;

import com.chrrubin.cherryrenderer.upnp.RendererHandler;
import com.chrrubin.cherryrenderer.upnp.TransportHandler;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Stopped;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;
import java.util.logging.Logger;

public class RendererStopped extends Stopped {
    final private Logger LOGGER = Logger.getLogger(RendererStopped.class.getName());

    private RendererHandler rendererHandler = RendererHandler.getInstance();
    private TransportHandler transportHandler = TransportHandler.getInstance();

    public RendererStopped(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        LOGGER.fine("Entered Stopped state");

        transportHandler.setTransport(getTransport());
        rendererHandler.setRendererState(RendererState.STOPPED);
    }

    public void onExit(){
        LOGGER.fine("Exited Stopped state");
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        LOGGER.finer("Setting transport URI...");
        LOGGER.finest("URI: " + uri.toString());
        LOGGER.finest("Metadata: " + metaData);

        rendererHandler.setUri(uri);
        rendererHandler.setMetadata(metaData);

        transportHandler.setMediaInfo(uri, metaData);
        transportHandler.setPositionInfo(uri, metaData);

        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        LOGGER.finer("Stop invoked");
        // FIXME: The immediate transition from current to desired state not supported. java.lang.reflect.InvocationTargetException
        // TODO: Now it actually works without me trying to fix it???? Keep this in mind in case it happens again
        // FIXME: Yup it's happening again
        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> play(String speed) {
        LOGGER.finer("Play invoked");
        // TODO: Sometimes program goes into a Stop.SetURI - Stop.Play - Play.Stop - Stop.SetURI... cycle.
        //  Not sure if it's the control point or program, further investigation required

        // TODO: All Screen queue function getting 701 errors going from Stopped to Playing when playing next on queue
        return RendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        LOGGER.finer("Next invoked");
        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        LOGGER.finer("Previous invoked");
        return RendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        LOGGER.finer("Seek invoked");
        return RendererStopped.class;
    }
}
