/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model.factory;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.service.util.CallParamNoNSRule;

import org.apache.commons.digester.Digester;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Building from XML definitions
 */
public class ThesaurusFactory {
  protected static Log log = LogFactory.getLog(ThesaurusFactory.class);
  protected static HttpClient httpClient = new HttpClient(
      new MultiThreadedHttpConnectionManager());

  /**
   * Builds thesauri from the supplied Strings which should be URLs
   * 
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
   * 
   * @param is For the XML
   * @return The extension
   * @throws SAXException
   * @throws IOException
   */
  public static ThesaurusVocabulary build(InputStream is) throws IOException,
      SAXException {
    Digester digester = new Digester();
    digester.setNamespaceAware(false);
    // using regular expressions to allow for prefixed attributes in differing
    // namespaces
    // digester.setRules( new RegexRules(new SimpleRegexMatcher()) );
    // digester.setRules( new ExtendedBaseRules() );
    // digester.startPrefixMapping("dc2", "http://purl.org/dc/terms/");

    ThesaurusVocabulary tv = new ThesaurusVocabulary();
    digester.push(tv);

    // build the thesaurus
    digester.addCallMethod("*/thesaurus", "setTitle", 1);
    digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "title"));

    digester.addCallMethod("*/thesaurus", "setLink", 1);
    digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "description"));

    digester.addCallMethod("*/thesaurus", "setUri", 1);
    digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "URI"));

    // modified is not being set... should it default to now?

    // build the concept
    digester.addObjectCreate("*/concept", ThesaurusConcept.class);

    digester.addCallMethod("*/concept", "setLink", 1);
    digester.addRule("*/concept", new CallParamNoNSRule(0, "description"));

    digester.addCallMethod("*/concept", "setUri", 1);
    digester.addRule("*/concept", new CallParamNoNSRule(0, "URI"));

    digester.addCallMethod("*/concept", "setIdentifier", 1);
    digester.addRule("*/concept", new CallParamNoNSRule(0, "identifier"));

    digester.addCallMethod("*/concept", "setIssuedXSDDateTime", 1);
    digester.addRule("*/concept", new CallParamNoNSRule(0, "issued"));

    // these are not set
    // tc.setConceptOrder(conceptOrder)
    // tc.setLink(link)

    // build the terms
    digester.addObjectCreate("*/preferred/term", ThesaurusTerm.class);
    ThesaurusTerm t = new ThesaurusTerm();

    digester.addCallMethod("*/preferred/term", "setCreatedXSDDateTime", 1);
    digester.addRule("*/preferred/term", new CallParamNoNSRule(0, "created"));

    digester.addCallMethod("*/preferred/term", "setModifiedXSDDateTime", 1);
    digester.addRule("*/preferred/term", new CallParamNoNSRule(0, "modified"));

    digester.addCallMethod("*/preferred/term", "setLang", 1);
    digester.addRule("*/preferred/term", new CallParamNoNSRule(0, "lang"));

    digester.addCallMethod("*/preferred/term", "setPreferred", 1);
    digester.addObjectParam("*/preferred/term", 0, "true");

    digester.addCallMethod("*/preferred/term", "setTitle", 1);
    digester.addRule("*/preferred/term", new CallParamNoNSRule(0, "title"));

    // these are not set
    // t.setRelation(relation)
    // t.setSource(source)

    digester.addSetNext("*/preferred/term", "addTerm");
    digester.addSetNext("*/concept", "addConcept");

    digester.parse(is);
    return tv;
  }

  /**
   * @param url To build from
   * @return The thesaurus or null on error
   */
  public static ThesaurusVocabulary build(String url) {
    List<ThesaurusVocabulary> thesauri = new LinkedList<ThesaurusVocabulary>();

    GetMethod method = new GetMethod(url);
    method.setFollowRedirects(true);
    try {
      httpClient.executeMethod(method);
      InputStream is = method.getResponseBodyAsStream();
      try {
        ThesaurusVocabulary tv = build(is);
        log.info("Successfully parsed Thesaurus: " + tv.getTitle());
        return tv;

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

    return null;
  }

}
