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
package org.gbif.provider.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;

import com.thoughtworks.xstream.XStream;

import static org.gbif.provider.util.XmlFileUtils.getUtf8Reader;
import static org.gbif.provider.util.XmlFileUtils.startNewUtf8XmlFile;
import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.EmlFactory;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * This class can be used to load and save {@link Eml} objects and publish new
 * EML versions for specific resources.
 * 
 */
public class EmlManagerImpl implements EmlManager {

  protected static final Log log = LogFactory.getLog(EmlManagerImpl.class);

  // TODO: Should this be moved into Constants?
  private static final String EML_TEMPLATE = "/WEB-INF/pages/eml.ftl";

  /**
   * Writes an {@link Eml} object to an XML file using a Freemarker
   * {@link Configuration}. Returns true if the XML file is succesfully written
   * and false otherwise.
   * 
   * @param f the XML file to write to
   * @param c the Freemarker configuration
   * @param eml the EML object
   * @return true if EML is written to XML, false otherwise
   */
  private static boolean writeEmlXmlFile(File f, Configuration c, Eml eml) {
    try {
      checkNotNull(f, "XML file was null");
      checkNotNull(c, "Freemarker configuration was null");
      checkNotNull(eml, "Eml object was null");
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("eml", eml);
      String data = processTemplateIntoString(c.getTemplate(EML_TEMPLATE), map);
      Writer out = startNewUtf8XmlFile(f);
      out.write(data);
      out.close();
      return true;
    } catch (Exception e) {
      log.error(e.toString());
      return false;
    }
  }

  @Autowired
  public AppConfig cfg;

  @Autowired
  @Qualifier("resourceManager")
  public GenericResourceManager<Resource> resourceManager;

  @Autowired
  private Configuration freemarker;

  private final XStream xstream = new XStream();

  /**
   * Loads and returns an {@link Eml} object for a {@link Resource} by reading
   * in the metadata XML file specified by {@link AppConfig}.
   * 
   * If the resource is not yet persistent or if there is an error reading the
   * XML file, a new Eml object is created, written to XML, and then returned.
   */
  @SuppressWarnings("static-access")
  public Eml deserialize(Resource resource) {
    checkNotNull(resource, "Resource was null");
    Eml eml = null;
    Long rid = resource.getId();
    if (rid != null) {
      File f = null;
      try {
        f = cfg.getMetadataFile(rid);
        eml = (Eml) xstream.fromXML(getUtf8Reader(f));
        eml.setResource(resource);
      } catch (FileNotFoundException e) {
        log.error(String.format("%s not found for resource %s", f, rid));
        log.info(String.format("Creating new Eml object for resource %s", rid));
        eml = new Eml();
        eml.setResource(resource);
        // eml.getResourceCreator().setEmail(resource.getContactEmail());
        // eml.getResourceCreator().setLastName(resource.getContactName());
        eml.getResourceCreator().setRole(Role.ORIGINATOR);
        eml.getMetadataProvider().setRole(Role.METADATA_PROVIDER);
        eml.setPubDate(new Date());
        serialize(eml);
        // writeEmlXmlFile(cfg.getEmlFile(rid), freemarker, eml);
      }
    }
    return eml;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.EmlManager#fromXml(java.io.File)
   */
  public Eml fromXml(File xmlFile) throws IOException, SAXException {
    Eml eml = EmlFactory.build(new FileInputStream(xmlFile));
    return eml;
  }

  @SuppressWarnings("static-access")
  public Eml publishNewEmlVersion(Resource resource) throws IOException {
    Eml metadata = deserialize(resource);
    int version = metadata.increaseEmlVersion();
    metadata.setPubDate(new Date());
    try {
      // overwrite current EML file
      File currEmlFile = cfg.getEmlFile(resource.getId());
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("eml", metadata);
      String eml = processTemplateIntoString(
          freemarker.getTemplate(EML_TEMPLATE), data);
      Writer out = startNewUtf8XmlFile(currEmlFile);
      out.write(eml);
      out.close();
      // also create archived fixed version
      File versionedEmlFile = cfg.getEmlFile(resource.getId(), version);
      FileUtils.copyFile(currEmlFile, versionedEmlFile);
      // persist EML with new version
      serialize(metadata);
      log.info("Published new EML version " + metadata.getEmlVersion()
          + " for resource " + resource.getTitle());
    } catch (TemplateException e) {
      log.error("Freemarker template exception", e);
      throw new IOException("Freemarker template exception");
    }
    return metadata;
  }

  /**
   * Serializes this manager's {@link Eml} object into an XML file. The location
   * and name of this file is specified by {@link AppConfig}.
   * 
   */
  @SuppressWarnings("static-access")
  public void serialize(Eml eml) {
    Preconditions.checkNotNull(eml);
    Preconditions.checkArgument(eml.getResource() != null,
        "Eml resource is null");
    Preconditions.checkArgument(eml.getResource().getId() != null,
        "Eml resource id is null");
    // update persistent EML properties on resource
    Resource res = eml.getResource();
    // now persist EML file (resource must have ID now)
    try {
      File metadataFile = cfg.getMetadataFile(res.getId());
      Writer writer = startNewUtf8XmlFile(metadataFile);
      xstream.toXML(eml, writer);
      writer.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.EmlManager#toXml(org.gbif.provider.model.eml.Eml)
   */
  public void toXmlFile(Eml eml) throws IOException {
    Preconditions.checkNotNull(eml, "Eml is null");
    Preconditions.checkNotNull(eml.getResource(), "Eml resource is null");
    publishNewEmlVersion(eml.getResource());
  }
}
