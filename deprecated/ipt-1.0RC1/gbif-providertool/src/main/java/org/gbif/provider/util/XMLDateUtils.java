package org.gbif.provider.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * XSD dateTime utilities
 * 
 * TODO: this does NOT take into account timezones properly...
 * 
 * @author tim
 */
public class XMLDateUtils {
	private static SimpleDateFormat xsdDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static Log log = LogFactory.getLog(XMLDateUtils.class);
	
	/**
	 * Parses a date or swallows errors with a warning log
	 * @param xmlDateTime To parse
	 * @return The date or null
	 */
	public static Date toDate(String xmlDateTime) {
		if ( xmlDateTime.length() != 25 )  {
            log.warn("Date not in expected xml datetime format (not 25 characters): " + xmlDateTime);
        } else {		
	        StringBuilder sb = new StringBuilder(xmlDateTime);
	        sb.deleteCharAt(22);
			try {
				return xsdDateFormat.parse(sb.toString());            
			} catch (ParseException e) {
				log.warn("Ignoring issued since unparsable: " + xmlDateTime);
			}
        }
		return null;
	}
}
