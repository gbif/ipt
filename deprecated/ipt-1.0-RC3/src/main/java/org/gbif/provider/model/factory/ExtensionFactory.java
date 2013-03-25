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

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.service.util.ThesaurusHandlingRule;

import org.apache.commons.digester.Digester;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Building from XML definitions.
 * 
 * Because an extension can reference thesauri, this is a 2 pass parsing
 * process.
 * 
 * <pre>
 * - the first pass looks for thesauri, that need built
 * - these are built, and then a map<thesaurusURL, ThesaurusVocabulary> is put onto the root of the second parse stack
 * - this map is then used to get the ThesaurusVocabulary in the second pass of parsing
 * </pre>
 * 
 */
public class ExtensionFactory {
  protected static Log log = LogFactory.getLog(ExtensionFactory.class);
  protected static HttpClient httpClient = new HttpClient(
      new MultiThreadedHttpConnectionManager());

  /**
   * Builds an extension from the supplied input stream
   * 
   * @param is For the XML
   * @return The extension
   * @throws SAXException
   * @throws IOException
   */
  public static Extension build(InputStream is,
      Map<String, ThesaurusVocabulary> url2ThesaurusMap) throws IOException,
      SAXException {
    Digester digester = new Digester();
    digester.setNamespaceAware(false);

    // this is important. The root of the stack is the urlMapping,
    // and the next is the extension being built. Methods on the root are called
    // when a thesaurus is found
    digester.push(url2ThesaurusMap);
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

    digester.addCallMethod("*/property", "setName", 1);
    digester.addCallParam("*/property", 0, "name");

    digester.addCallMethod("*/property", "setNamespace", 1);
    digester.addCallParam("*/property", 0, "namespace");

    digester.addCallMethod("*/property", "setRequired", 1);
    digester.addCallParam("*/property", 0, "required");

    digester.addCallMethod("*/property", "setRequired", 1);
    digester.addCallParam("*/property", 0, "required");

    digester.addCallMethod("*/property", "setLink", 1);
    digester.addCallParam("*/property", 0, "description");

    digester.addCallMethod("*/property", "setColumnLength", 1);
    digester.addCallParam("*/property", 0, "columnLength");

    // This is a special rule that will use the url2ThesaurusMap
    // to set the ThesaurusVocabulary based on the attribute "thesaurus"
    digester.addRule("*/property", new ThesaurusHandlingRule());

    digester.addSetNext("*/property", "addProperty");

    digester.parse(is);
    return e;
  }

  /**
   * This will parse the given stream, looking for thesaurus URLs referenced in
   * the extension
   * 
   * @param is To parse
   * @return A Set<String> that represent URLs for thesauri referenced in the
   *         extensions definition
   * @throws IOException Unable to access the extension
   * @throws SAXException Unable to parse response
   */
  public static Set<String> thesaurusURLs(InputStream is) throws IOException,
      SAXException {
    Digester digester = new Digester();
    digester.setNamespaceAware(false);
    Set<String> urls = new HashSet<String>();
    digester.push(urls);

    // build the properties
    digester.addCallMethod("*/property", "add", 1);
    digester.addCallParam("*/property", 0, "thesaurus");
    digester.parse(is);
    return urls;
  }

  /**
   * Returns the thesaurus URLs or an empty set of the thesaurus referenced by
   * the extension
   * 
   * @param extensionUrl To check
   * @return The Set<String> urls
   */
  public static Set<String> thesaurusURLs(String extensionUrl)
      throws IOException, SAXException {
    GetMethod method = new GetMethod(extensionUrl);
    method.setFollowRedirects(true);
    try {
      httpClient.executeMethod(method);
      InputStream is = method.getResponseBodyAsStream();
      try {
        return thesaurusURLs(is);

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
    return new HashSet<String>();
  }

  @Autowired
  private ThesaurusManager thesaurusManager;

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
        log.error("Unable to access extension definition defined at "
            + urlAsString, e);
      } catch (SAXException e) {
        log.error("Unable to parse extension definition defined at "
            + urlAsString, e);
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
   */
  public Extension build(String url) throws IOException, SAXException {
    Set<String> thesaurusURLs = thesaurusURLs(url);

    // build a map of
    // "http://thesaurusURI" -> thesaurus ID
    Map<String, ThesaurusVocabulary> url2ThesaurusMap = new HashMap<String, ThesaurusVocabulary>();
    for (String t : thesaurusURLs) {
      log.info("Building referenced thesaurus: " + t);
      ThesaurusVocabulary tv = ThesaurusFactory.build(t);
      log.info("Referenced thesaurus: " + tv);
      if (tv != null) {
        thesaurusManager.synchronise(tv);
        url2ThesaurusMap.put(t, tv);
      } else {
        log.warn("Extension[" + url + "] references an invalid vocabulary[" + t
            + "]");
      }
    }
    log.info("Thesauri: " + url2ThesaurusMap);

    GetMethod method = new GetMethod(url);
    method.setFollowRedirects(true);
    try {
      httpClient.executeMethod(method);
      InputStream is = method.getResponseBodyAsStream();
      try {
        Extension e = build(is, url2ThesaurusMap);
        log.info("Successfully parsed extension: " + e.getTitle());
        return e;

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

  public void setThesaurusManager(ThesaurusManager thesaurusManager) {
    this.thesaurusManager = thesaurusManager;
  }
}
