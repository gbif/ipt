package org.gbif.provider.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.xml.sax.SAXException;

/**
 * Building from XML definitions
 * @author tim
 */
public class ExtensionFactory {
	protected static Log log = LogFactory.getLog(ExtensionFactory.class);
	protected static HttpClient httpClient =  new HttpClient(new MultiThreadedHttpConnectionManager());
	
	
	
	/**
	 * Builds extensions from the supplied Strings which should be URLs
	 * @param urls To build extensions from 
	 * @return The collection of Extensions
	 */
	public static Collection<Extension> build(Collection<String> urls) {
		List<Extension> extensions = new LinkedList<Extension>();
		
		for (String urlAsString : urls) {
			GetMethod method = new GetMethod(urlAsString);
			method.setFollowRedirects(true);
 			try {
				httpClient.executeMethod(method);
				InputStream is = method.getResponseBodyAsStream();
				try {
					Extension e = build(is);
					log.info("Successfully parsed extension: " + e.getTitle());
					extensions.add(e);
					
				} catch (SAXException e) {
					log.error("Unable to parse XML for extension: " + e.getMessage(), e);
				} finally {
					is.close();					 
				}
			} catch (Exception e) {
				log.error(e);
				
			} finally {
				 try {
					method.releaseConnection();
				} catch (RuntimeException e) {
				}
			}
		}
		
		return extensions;
	}
	
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
