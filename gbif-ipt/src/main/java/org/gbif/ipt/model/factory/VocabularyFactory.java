/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model.factory;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;

import com.google.inject.Inject;

import org.apache.commons.digester.Digester;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Building from XML definitions
 */
public class VocabularyFactory {
  public static final String VOCABULARY_NAMESPACE = "http://rs.gbif.org/thesaurus/";
  protected static Logger log = Logger.getLogger(VocabularyFactory.class);
  private HttpClient httpClient;
  private SAXParserFactory saxf;

  @Inject
  public VocabularyFactory(HttpClient httpClient, SAXParserFactory saxf) {
    super();
    this.httpClient = httpClient;
    this.saxf = saxf;
  }

  /**
   * Builds thesauri from the supplied Strings which should be URLs
   * 
   * @param urls To build thesauri from
   * @return The collection of thesauri
   */
  public Collection<Vocabulary> build(Collection<String> urls) {
    List<Vocabulary> thesauri = new LinkedList<Vocabulary>();

    for (String urlAsString : urls) {
      GetMethod method = new GetMethod(urlAsString);
      method.setFollowRedirects(true);
      try {
        httpClient.executeMethod(method);
        InputStream is = method.getResponseBodyAsStream();
        try {
          Vocabulary tv = build(is);
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
   * Builds a Vocabulary from the supplied input stream
   * 
   * @param is For the XML
   * @return The extension
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  public Vocabulary build(InputStream is) throws IOException, SAXException, ParserConfigurationException {
    Digester digester = new Digester(saxf.newSAXParser());
    digester.setNamespaceAware(true);
    digester.setXIncludeAware(false);
    digester.setRuleNamespaceURI(VOCABULARY_NAMESPACE);

    Vocabulary tv = new Vocabulary();
    digester.push(tv);

    // build the thesaurus
    digester.addCallMethod("*/thesaurus", "setTitle", 1);
    digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "title"));

    digester.addCallMethod("*/thesaurus", "setDescription", 1);
    digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "description"));

    digester.addCallMethod("*/thesaurus", "setLink", 1);
    digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "relation"));

    digester.addCallMethod("*/thesaurus", "setUri", 1);
    digester.addRule("*/thesaurus", new CallParamNoNSRule(0, "URI"));

    // build the concept
    digester.addObjectCreate("*/concept", VocabularyConcept.class);

    digester.addCallMethod("*/concept", "setLink", 1);
    digester.addRule("*/concept", new CallParamNoNSRule(0, "relation"));

    digester.addCallMethod("*/concept", "setDescription", 1);
    digester.addRule("*/concept", new CallParamNoNSRule(0, "description"));

    digester.addCallMethod("*/concept", "setUri", 1);
    digester.addRule("*/concept", new CallParamNoNSRule(0, "URI"));

    digester.addCallMethod("*/concept", "setIdentifier", 1);
    digester.addRule("*/concept", new CallParamNoNSRule(0, "identifier"));

    // build the terms
    digester.addObjectCreate("*/preferred/term", VocabularyTerm.class);
    VocabularyTerm t = new VocabularyTerm();

    digester.addCallMethod("*/preferred/term", "setLang", 1);
    digester.addRule("*/preferred/term", new CallParamNoNSRule(0, "lang"));

    digester.addCallMethod("*/preferred/term", "setTitle", 1);
    digester.addRule("*/preferred/term", new CallParamNoNSRule(0, "title"));

    digester.addSetNext("*/preferred/term", "addPreferredTerm");

    // build alternative terms
    digester.addObjectCreate("*/alternative/term", VocabularyTerm.class);
    VocabularyTerm talt = new VocabularyTerm();

    digester.addCallMethod("*/alternative/term", "setLang", 1);
    digester.addRule("*/alternative/term", new CallParamNoNSRule(0, "lang"));

    digester.addCallMethod("*/alternative/term", "setTitle", 1);
    digester.addRule("*/alternative/term", new CallParamNoNSRule(0, "title"));

    digester.addSetNext("*/alternative/term", "addAlternativeTerm");

    // add concept
    digester.addSetNext("*/concept", "addConcept");

    digester.parse(is);
    return tv;
  }

  /**
   * @param url To build from
   * @return The thesaurus or null on error
   */
  public Vocabulary build(String url) {
    List<Vocabulary> thesauri = new LinkedList<Vocabulary>();

    GetMethod method = new GetMethod(url);
    method.setFollowRedirects(true);
    try {
      httpClient.executeMethod(method);
      InputStream is = method.getResponseBodyAsStream();
      try {
        Vocabulary tv = build(is);
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
