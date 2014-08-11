package com.malek.alldebrid.alldebrid.utils;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {
    String elementValue = null;
    Boolean elementOn = false;
    public static XMLGettersSetters data = null;

    public static XMLGettersSetters getXMLData() {
        return data;
    }

    public static void setXMLData(XMLGettersSetters data) {
        XMLHandler.data = data;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        elementOn = true;
        if (localName.equals("account")) {
            data = new XMLGettersSetters();
        }
    }

    /**
     * This will be called when the tags of the XML end.
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        elementOn = false;
        if (localName.equalsIgnoreCase("pseudo"))
            data.setPseudo(elementValue);
        else if (localName.equalsIgnoreCase("email"))
            data.setEmail(elementValue);
        else if (localName.equalsIgnoreCase("type"))
            data.setType(elementValue);
        else if (localName.equalsIgnoreCase("cookie"))
            data.setCookie(elementValue);
        else if (localName.equalsIgnoreCase("date"))
            data.setDate(elementValue);
    }

    /**
     * This is called to get the tags value
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (elementOn) {
            elementValue = new String(ch, start, length);
            elementOn = false;
        }
    }

}