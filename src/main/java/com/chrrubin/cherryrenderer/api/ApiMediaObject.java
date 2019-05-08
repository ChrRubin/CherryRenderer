package com.chrrubin.cherryrenderer.api;

import com.chrrubin.cherryrenderer.MediaObject;

import java.net.URI;

public class ApiMediaObject extends MediaObject {
    public ApiMediaObject(URI uri, String title){
        super(uri);
        super.setTitle(title);
        super.setXmlMetadata("<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
                "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
                "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
                "xmlns:sec=\"http://www.sec.co.kr/\">" +
                "<item id=\"1\" parentID=\"99\" restricted=\"0\">" +
                "<dc:title>" +
                title +
                "</dc:title>" +
                "<dc:creator/>" +
                "<upnp:class>object.item.videoItem</upnp:class>" +
                "</item>" +
                "</DIDL-Lite>");
    }

    public ApiMediaObject(MediaObject clone){
        this(clone.getUri(), clone.getTitle());
    }
}
