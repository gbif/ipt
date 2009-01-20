package org.gbif.provider.tapir;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.tapir.filter.FilterFactory;

/**
 * Template factory for building the template from a remote URL
 * @author tim
 */
public class TemplateFactory {
	protected static Log log = LogFactory.getLog(TemplateFactory.class);
	
	
	public static Template buildTemplate(URL templateURL, Map<String, String> requestParameters) throws ParseException {
		Template template = new Template();
		
		// for code simplicity, do 2 parses, first for the filter, 
		// and then the other parameters
		String templateAsString = readTemplate(templateURL);
		setFilter(templateURL, requestParameters, template, templateAsString);
		parseParameters(templateURL, template, templateAsString);
		return template;
	}

	// sets the filter only on the template
	private static void setFilter(URL templateURL,
			Map<String, String> requestParameters, Template template,
			String templateAsString) throws ParseException {
		ByteArrayInputStream bais = new ByteArrayInputStream(templateAsString.getBytes());
		try {
			template.setFilter(FilterFactory.build(bais, requestParameters));
		} catch (Exception e) {
			throw new ParseException("Unable to parse the response from url[" + templateURL.toString() + "]", e);				
		} finally {
			try {
				bais.close();
			} catch (IOException e) {
			}
		}
	}
	
	// sets all the parameters (other than filter) on the template
	private static void parseParameters(URL templateURL, Template template,
			String templateAsString) throws ParseException {
		ByteArrayInputStream bais = new ByteArrayInputStream(templateAsString.getBytes());
		try {
			Digester digester = new Digester();
			digester.setNamespaceAware(true);
			digester.push(template);
			
			// inventory parsing
			digester.addCallMethod("inventoryTemplate", "setOperation", 1, new Class[]{TapirOperation.class});
			digester.addObjectParam("inventoryTemplate", 0, TapirOperation.inventory);
			digester.addObjectCreate("inventoryTemplate/concepts", LinkedHashMap.class);
			digester.addCallMethod("inventoryTemplate/concepts/concept", "put", 2);
			digester.addCallParam("inventoryTemplate/concepts/concept", 0, "id");
			digester.addCallParam("inventoryTemplate/concepts/concept", 1, "tagName");
			digester.addSetNext("inventoryTemplate/concepts", "setConcepts");			

			// search parsing
			digester.addCallMethod("searchTemplate", "setOperation", 1, new Class[]{TapirOperation.class});
			digester.addObjectParam("searchTemplate", 0, TapirOperation.search);				
			digester.addObjectCreate("searchTemplate/orderBy", LinkedHashMap.class);
			digester.addCallMethod("searchTemplate/orderBy/concept", "put", 2);
			digester.addCallParam("searchTemplate/orderBy/concept", 0, "id");
			digester.addCallParam("searchTemplate/orderBy/concept", 1, "descend");
			digester.addSetNext("searchTemplate/orderBy", "setOrderByStringMap");
			digester.addCallMethod("searchTemplate/externalOutputModel", "setModel", 1);
			digester.addCallParam("searchTemplate/externalOutputModel", 0, "location");
			
			digester.parse(bais);			
		} catch (Exception e) {
			throw new ParseException("Unable to parse the response from url[" + templateURL.toString() + "]", e);				
		} finally {
			try {
				bais.close();
			} catch (IOException e) {
			}
		}
	}


	/**
	 * Gets the template and returns it as a String
	 */
	protected static String readTemplate(URL templateURL) throws ParseException {
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = templateURL.openConnection().getInputStream();
			br = new BufferedReader(
					new InputStreamReader(is));
			
			StringBuffer sb = new StringBuffer();
			String inputLine;
	        while ((inputLine = br.readLine()) != null) { 
	        	sb.append(inputLine);
	        }
	        
	        String templateAsString = sb.toString();
	        if (log.isDebugEnabled()) {
	        	log.debug("Template: " + templateAsString);
	        }
	        return templateAsString;	        
	        
		} catch (IOException e) {
				throw new ParseException("Unable to read the template from url[" + templateURL.toString() + "]", e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}
}
