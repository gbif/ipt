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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
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
  private HttpClient client;
  private SAXParserFactory saxf;

  @Inject
  public VocabularyFactory(DefaultHttpClient httpClient, SAXParserFactory saxf) {
    super();
    this.client = httpClient;
    this.saxf = saxf;
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

    HttpGet get = new HttpGet(url);

    // execute
    try {
      HttpResponse response = client.execute(get);
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        // copy stream to local file
        InputStream is = entity.getContent();
        try {
          Vocabulary tv = build(is);
          log.info("Successfully parsed Thesaurus: " + tv.getTitle());
          return tv;

        } catch (SAXException e) {
          log.error("Unable to parse XML for extension: " + e.getMessage(), e);
        } finally {
          is.close();
        }
        // close http connection
        entity.consumeContent();
      }
    } catch (Exception e) {
      log.error(e);
    }

    return null;
  }

}
