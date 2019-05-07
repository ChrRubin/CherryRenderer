package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.MediaObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

public class UpnpMediaObject extends MediaObject {
    public UpnpMediaObject(URI uri, String xmlMetadata) throws ParserConfigurationException, XPathExpressionException, IOException, SAXException{
        super(uri);
        super.setXmlMetadata(xmlMetadata);
        super.setTitle(getTitleFromXml(xmlMetadata));
    }

    /**
     * Parses the metadata XML to get the title of the media object
     * @return Title string of video
     */
    private String getTitleFromXml(String xml) throws ParserConfigurationException, XPathExpressionException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("/DIDL-Lite/item/*[local-name() = 'title']");

        return ((String)expr.evaluate(document, XPathConstants.STRING)).trim();
    }
}
