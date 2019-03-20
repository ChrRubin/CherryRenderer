package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.upnp.states.RendererState;

import java.net.URI;

public class ApplicationHelper {
    private static RendererState rendererState = null;
    private static URI uri = null;
    private static String metadata = null;
    private ApplicationHelper(){}

    public static RendererState getRendererState() {
        return rendererState;
    }

    public static void setRendererState(RendererState rendererState) {
        ApplicationHelper.rendererState = rendererState;
    }

    public static URI getUri() {
        return uri;
    }

    public static void setUri(URI uri) {
        ApplicationHelper.uri = uri;
    }

    public static String getMetadata() {
        return metadata;
    }

    public static void setMetadata(String metadata) {
        ApplicationHelper.metadata = metadata;
    }
}
