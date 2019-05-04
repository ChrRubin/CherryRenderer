package com.chrrubin.cherryrenderer;

import javafx.scene.media.Media;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

public class MediaObject {
    private URI uri;
    private String xmlMetadata;

    public MediaObject(URI uri, String xmlMetadata) {
        this.uri = uri;
        this.xmlMetadata = xmlMetadata;
    }

    public URI getUri() {
        return uri;
    }

    public String getUriString(){
        return uri.toString();
    }

    public String getXmlMetadata() {
        return xmlMetadata;
    }

    public String getPrettyXml() throws TransformerException {
        Source xmlInput = new StreamSource(new StringReader(xmlMetadata));
        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(xmlInput, new StreamResult(stringWriter));

        return stringWriter.toString().trim();
    }

    public Media toJFXMedia(){
        return new Media(uri.toString());
    }

    /**
     * Parses the metadata XML to get the title of the media object
     * @return Title string of video
     */
    public String getTitle() throws ParserConfigurationException, XPathExpressionException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlMetadata)));

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("/DIDL-Lite/item/*[local-name() = 'title']");

        return (String)expr.evaluate(document, XPathConstants.STRING);
    }
}
