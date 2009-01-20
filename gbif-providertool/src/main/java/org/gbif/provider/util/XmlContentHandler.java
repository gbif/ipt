package org.gbif.provider.util;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.stream.events.Attribute;

import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Super simple SAX handler that extracts all element and attribute content from any XML document.
 * The resulting string is concatenating all content and inserts a space at every element or attribute start.
 * @author markus
 *
 */
public class XmlContentHandler extends DefaultHandler{
	private String content="";

	@Override
	public void startDocument() throws SAXException {
		content="";
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		content += String.valueOf(ArrayUtils.subarray(ch, start, start+length));
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		int i = attributes.getLength();
		while (i>0){
			i--;
			content += " "+attributes.getValue(i);
		}
		content += " ";
	}

	public String getContent() {
		return content;
	}
}
