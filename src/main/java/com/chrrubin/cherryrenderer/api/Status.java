package com.chrrubin.cherryrenderer.api;

import com.chrrubin.cherryrenderer.upnp.states.RendererState;

public class Status implements Response{
    private RendererState status;

    public Status(){}

    public Status(RendererState status){
        this.status = status;
    }

    public RendererState getStatus() {
        return status;
    }

    public void setStatus(RendererState status) {
        this.status = status;
    }
}
