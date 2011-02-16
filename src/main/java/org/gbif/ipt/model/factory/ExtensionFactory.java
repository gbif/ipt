/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model.factory;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Building from XML definitions. Because an extension can reference thesauri, this is a 2 pass parsing process.
 * 
 * <pre>
 * - the first pass looks for thesauri, that need built
 * - these are built, and then a map<thesaurusURL, Vocabulary> is put onto the root of the second parse stack
 * - this map is then used to get the Vocabulary in the second pass of parsing
 * </pre>
 */
@Singleton
public class ExtensionFactory {
  protected static Logger log = Logger.getLogger(ExtensionFactory.class);
  public static final String EXTENSION_NAMESPACE = "http://rs.gbif.org/extension/";
  private ThesaurusHandlingRule thesaurusRule;
  private SAXParserFactory saxf;
  private HttpClient client;

  @Inject
  public ExtensionFactory(ThesaurusHandlingRule thesaurusRule, SAXParserFactory factory, DefaultHttpClient client) {
    super();
    this.thesaurusRule = thesaurusRule;
    this.saxf = factory;
    this.client = client;
  }

  /**
   * Builds extensions from the supplied Strings which should be URLs
   * 
   * @param urls To build extensions from
   * @return The collection of Extensions
   */
  public Collection<Extension> build(Collection<String> urls) {
    List<Extension> extensions = new LinkedList<Extension>();

    for (String urlAsString : urls) {
      try {
        Extension e = build(urlAsString);
        if (e != null) {
          extensions.add(e);
        }
      } catch (IOException e) {
        log.error("Unable to access extension definition defined at " + urlAsString, e);
      } catch (SAXException e) {
        log.error("Unable to parse extension definition defined at " + urlAsString, e);
      }
    }

    return extensions;
  }

  /**
   * Builds an extension from the supplied input stream
   * 
   * @param is For the XML
   * @return The extension
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  public Extension build(InputStream is) throws IOException, SAXException, ParserConfigurationException {

    // in order to deal with arbitrary namespace prefixes we need to parse namespace aware!
    Digester digester = new Digester(saxf.newSAXParser());
    digester.setRuleNamespaceURI(EXTENSION_NAMESPACE);

    Extension e = new Extension();
    digester.push(e);

    digester.addCallMethod("*/extension", "setTitle", 1);
    digester.addRule("*/extension", new CallParamNoNSRule(0, "title"));

    digester.addCallMethod("*/extension", "setName", 1);
    digester.addCallParam("*/extension", 0, "name");

    digester.addCallMethod("*/extension", "setNamespace", 1);
    digester.addCallParam("*/extension", 0, "namespace");

    digester.addCallMethod("*/extension", "setRowType", 1);
    digester.addCallParam("*/extension", 0, "rowType");

    digester.addCallMethod("*/extension", "setLink", 1);
    digester.addRule("*/extension", new CallParamNoNSRule(0, "relation"));

    digester.addCallMethod("*/extension", "setDescription", 1);
    digester.addRule("*/extension", new CallParamNoNSRule(0, "description"));

    digester.addCallMethod("*/extension", "setSubject", 1);
    digester.addRule("*/extension", new CallParamNoNSRule(0, "subject"));

    // build the properties
    digester.addObjectCreate("*/property", ExtensionProperty.class);

    digester.addCallMethod("*/property", "setQualname", 1);
    digester.addCallParam("*/property", 0, "qualName");

    digester.addCallMethod("*/property", "setName", 1);
    digester.addCallParam("*/property", 0, "name");

    digester.addCallMethod("*/property", "setNamespace", 1);
    digester.addCallParam("*/property", 0, "namespace");

    digester.addCallMethod("*/property", "setGroup", 1);
    digester.addCallParam("*/property", 0, "group");

    digester.addCallMethod("*/property", "setRequired", 1);
    digester.addCallParam("*/property", 0, "required");

    digester.addCallMethod("*/property", "setLink", 1);
    digester.addRule("*/property", new CallParamNoNSRule(0, "relation"));

    digester.addCallMethod("*/property", "setDescription", 1);
    digester.addRule("*/property", new CallParamNoNSRule(0, "description"));

    digester.addCallMethod("*/property", "setExamples", 1);
    digester.addCallParam("*/property", 0, "examples");

    digester.addCallMethod("*/property", "setType", 1);
    digester.addCallParam("*/property", 0, "type");

    // This is a special rule that will use the url2ThesaurusMap
    // to set the Vocabulary based on the attribute "thesaurus"
    digester.addRule("*/property", thesaurusRule);

    digester.addSetNext("*/property", "addProperty");

    digester.parse(is);

    return e;
  }

  /**
   * Builds an extension from the supplied input stream
   * 
   * @param is For the XML
   * @return The extension
   * @throws SAXException
   * @throws IOException
   */
  public Extension build(String url) throws IOException, SAXException {

    HttpGet get = new HttpGet(url);

    // execute
    try {
      HttpResponse response = client.execute(get);
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        // copy stream to local file
        InputStream is = entity.getContent();
        try {
          Extension e = build(is);
          log.info("Successfully parsed extension: " + e.getTitle());
          return e;

        } catch (SAXException e) {
          log.error("Unable to parse XML for extension: " + e.getMessage(), e);
        } finally {
          is.close();
        }
        entity.consumeContent();
      }
    } catch (Exception e) {
      log.error(e);
    }

    // close http connection
    return null;
  }

}
