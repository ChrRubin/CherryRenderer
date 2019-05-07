package com.chrrubin.cherryrenderer;

import javafx.scene.media.Media;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

public abstract class MediaObject {
    private URI uri;
    private String title;
    private transient String xmlMetadata;

    public MediaObject(URI uri){
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public String getUriString(){
        return uri.toString();
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getXmlMetadata() {
        return xmlMetadata;
    }

    public String getPrettyXmlMetadata() throws TransformerException {
        Source xmlInput = new StreamSource(new StringReader(xmlMetadata));
        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(xmlInput, new StreamResult(stringWriter));

        return stringWriter.toString().trim();
    }

    public void setXmlMetadata(String xmlMetadata) {
        this.xmlMetadata = xmlMetadata;
    }

    public Media toJFXMedia(){
        return new Media(getUriString());
    }
}
