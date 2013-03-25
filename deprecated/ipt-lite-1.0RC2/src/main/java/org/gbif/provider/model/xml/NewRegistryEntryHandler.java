package org.gbif.provider.model.xml;

import org.apache.commons.lang.ArrayUtils;
import org.gbif.provider.model.ResourceMetadata;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Super simple SAX handler that extracts all element and attribute content from any XML document.
 * The resulting string is concatenating all content and inserts a space at every element or attribute start.
 * @author markus
 *
 */
public class NewRegistryEntryHandler extends DefaultHandler{
	private String content;
	public String organisationKey;
	public String resourceKey;
	public String serviceKey;
	public String password;
	public String key;

	@Override
	public void startDocument() throws SAXException {
		content="";
		key="";
		organisationKey="";
		resourceKey="";
		serviceKey="";
		password="";
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		content += String.valueOf(ArrayUtils.subarray(ch, start, start+length));
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		content = "";
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (name.equalsIgnoreCase("user")){
			
		} else if (name.equalsIgnoreCase("password")){
			password=content;
		} else if (name.equalsIgnoreCase("key")){
			key=content.replaceAll("\\s", "");
		} else if (name.equalsIgnoreCase("organisationKey")){
			organisationKey=content.replaceAll("\\s", "");
		} else if (name.equalsIgnoreCase("organizationKey")){
			organisationKey=content.replaceAll("\\s", "");
		} else if (name.equalsIgnoreCase("resourceKey")){
			resourceKey=content.replaceAll("\\s", "");
		} else if (name.equalsIgnoreCase("serviceKey")){
			serviceKey=content.replaceAll("\\s", "");
		}
		content = "";
	}

	
}
