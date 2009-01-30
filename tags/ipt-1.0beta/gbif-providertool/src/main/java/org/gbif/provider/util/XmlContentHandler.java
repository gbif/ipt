package org.gbif.provider.util;

import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Attributes;
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

	// strips redundant whitespace
	public String getContent() {
		String[] words = content.split("\\s");
		StringBuffer sb = new StringBuffer();
		for (String w : words) {
			if (w != null && w.trim().length()>0) {
				sb.append(w);
				sb.append(" ");
			}
		}
		return sb.toString().trim();
	}
}
