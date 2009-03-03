package org.gbif.provider.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.RegexRules;
import org.apache.commons.digester.SimpleRegexMatcher;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.xml.sax.SAXException;

/**
 * Building from XML definitions
 * @author tim
 */
public class ThesaurusFactory {
	protected static Log log = LogFactory.getLog(ThesaurusFactory.class);
	protected static HttpClient httpClient =  new HttpClient(new MultiThreadedHttpConnectionManager());
	
	/**
	 * Builds thesauri from the supplied Strings which should be URLs
	 * @param urls To build thesauri from 
	 * @return The collection of thesauri
	 */
	public static Collection<ThesaurusVocabulary> build(Collection<String> urls) {
		List<ThesaurusVocabulary> thesauri = new LinkedList<ThesaurusVocabulary>();
		
		for (String urlAsString : urls) {
			GetMethod method = new GetMethod(urlAsString);
			method.setFollowRedirects(true);
 			try {
				httpClient.executeMethod(method);
				InputStream is = method.getResponseBodyAsStream();
				try {
					ThesaurusVocabulary tv = build(is);
					log.info("Successfully parsed Thesaurus: " + tv.getTitle());
					thesauri.add(tv);
					
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
		
		return thesauri;
	}
	
	/**
	 * Builds a ThesaurusVocabulary from the supplied input stream
	 * @param is For the XML
	 * @return The extension
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static ThesaurusVocabulary build(InputStream is) throws IOException, SAXException {
		Digester digester = new Digester();
		digester.setNamespaceAware(false);
		// using regular expressions to allow for prefixed attributes in differing namespaces
		//digester.setRules( new RegexRules(new SimpleRegexMatcher()) );
		//digester.setRules( new ExtendedBaseRules() );
		//digester.startPrefixMapping("dc2", "http://purl.org/dc/terms/");
		
		ThesaurusVocabulary tv = new ThesaurusVocabulary();
		digester.push(tv);
		
		
		// modified is not being set... should it default to now?
		//digester.setRuleNamespaceURI("http://purl.org/dc/terms/");
		digester.addCallMethod("*/thesaurus", "setTitle", 1);
		digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "title"));
		
		digester.addCallMethod("*/thesaurus", "setLink", 1);
		digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "description"));
		
		digester.addCallMethod("*/thesaurus", "setUri", 1);
		digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "URI"));
		
		// build the concept
		digester.addObjectCreate("*/concept", ThesaurusConcept.class);
		
		ThesaurusConcept tc = new ThesaurusConcept();
		//tc.setConceptOrder(conceptOrder)
		//tc.setIdentifier(identifier)
		//tc.setIssued(issued)
		//tc.setLink(link)
		//tc.setTerms(terms)
		
		//tc.setUri(uri)
		//tc.setVocabulary(vocabulary)
		ThesaurusTerm t = new ThesaurusTerm();
		
		
		
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
		return tv;
	}
	
}
