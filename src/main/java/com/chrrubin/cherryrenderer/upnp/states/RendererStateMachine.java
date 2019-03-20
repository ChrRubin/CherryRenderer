package com.chrrubin.cherryrenderer.upnp.states;


import org.fourthline.cling.support.avtransport.impl.AVTransportStateMachine;
import org.seamless.statemachine.States;

@States({
        RendererNoMediaPresent.class,
        RendererStopped.class,
        RendererPlaying.class,
        RendererPaused.class
})
public interface RendererStateMachine extends AVTransportStateMachine {
}
