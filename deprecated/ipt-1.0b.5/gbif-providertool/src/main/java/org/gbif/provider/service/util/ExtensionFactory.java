package org.gbif.provider.service.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.xml.sax.SAXException;

/**
 * Building from XML definition 
 * @author tim
 */
public class ExtensionFactory {
	
	
	/**
	 * Builds an extension from the supplied input stream
	 * @param is For the XML
	 * @return The extension
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static Extension build(InputStream is) throws IOException, SAXException {
		Digester digester = new Digester();
		digester.setNamespaceAware(false);
		Extension e = new Extension();
		digester.push(e);
		
		
		digester.addCallMethod("*/extension", "setTitle", 1);
		digester.addCallParam("*/extension", 0, "title");
		
		digester.addCallMethod("*/extension", "setName", 1);
		digester.addCallParam("*/extension", 0, "name");
		
		digester.addCallMethod("*/extension", "setNamespace", 1);
		digester.addCallParam("*/extension", 0, "namespace");
		
		digester.addCallMethod("*/extension", "setLink", 1);
		digester.addCallParam("*/extension", 0, "description");
		
		// build the properties
		digester.addObjectCreate("*/property", ExtensionProperty.class);
		
		digester.addCallMethod("*/property", "setQualName", 1);
		digester.addCallParam("*/property", 0, "qualName");

		digester.addCallMethod("*/property", "setRequired", 1);
		digester.addCallParam("*/property", 0, "required");
		
		digester.addCallMethod("*/property", "setRequired", 1);
		digester.addCallParam("*/property", 0, "required");
		
		digester.addCallMethod("*/property", "setLink", 1);
		digester.addCallParam("*/property", 0, "description");
		
		digester.addCallMethod("*/property", "setColumnLength", 1);
		digester.addCallParam("*/property", 0, "columnLength");
		
		digester.addSetNext("*/property", "addProperty");
		
		digester.parse(is);
		return e;
	}
	
}
