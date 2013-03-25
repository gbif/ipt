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

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.XmlFileUtils;

import com.thoughtworks.xstream.XStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * TODO: Documentation
 * 
 * 
 */
public class EmlManagerImpl implements EmlManager {
  protected static final Log log = LogFactory.getLog(EmlManagerImpl.class);
  private static final String EML_TEMPLATE = "/WEB-INF/pages/eml.ftl";

  @Autowired
  public AppConfig cfg;

  @Autowired
  @Qualifier("resourceManager")
  public GenericResourceManager<Resource> resourceManager;

  @Autowired
  private Configuration freemarker;

  private final XStream xstream = new XStream();

  public Eml load(Resource resource) {
    if (resource == null) {
      throw new NullPointerException("EML file requires resource");
    }
    Eml metadata = null;
    if (resource.getId() != null) {
      // load existing data. This is a persistent resource
      File metadataFile = cfg.getMetadataFile(resource.getId());
      try {
        Reader reader = XmlFileUtils.getUtf8Reader(metadataFile);
        metadata = (Eml) xstream.fromXML(reader);
      } catch (FileNotFoundException e) {
        log.error(String.format("EML Metadata file not found for resource %s",
            resource.getId()));
      }
    }
    if (metadata == null) {
      // file not found or new resource
      metadata = new Eml();
      // copy some resource default data that is not delegated (title &
      // description are)
      metadata.getResourceCreator().setEmail(resource.getContactEmail());
      metadata.getResourceCreator().setLastName(resource.getContactName());
    }

    metadata.setResource(resource);
    return metadata;
  }

  public Eml publishNewEmlVersion(Resource resource) throws IOException {
    Eml metadata = load(resource);
    int version = metadata.increaseEmlVersion();
    metadata.setPubDate(new Date());
    try {
      // overwrite current EML file
      File currEmlFile = cfg.getEmlFile(resource.getId());
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("eml", metadata);
      String eml = FreeMarkerTemplateUtils.processTemplateIntoString(
          freemarker.getTemplate(EML_TEMPLATE), data);
      Writer out = XmlFileUtils.startNewUtf8XmlFile(currEmlFile);
      out.write(eml);
      out.close();
      // also create archived fixed version
      File versionedEmlFile = cfg.getEmlFile(resource.getId(), version);
      FileUtils.copyFile(currEmlFile, versionedEmlFile);
      // persist EML with new version
      save(metadata);
      log.info("Published new EML version " + metadata.getEmlVersion()
          + " for resource " + resource.getTitle());
    } catch (TemplateException e) {
      log.error("Freemarker template exception", e);
      throw new IOException("Freemarker template exception");
    }
    return metadata;
  }

  /**
   * serialises the metadata document into a file using XMLStream?
   */
  public void save(Eml metadata) {
    // update persistent EML properties on resource
    Resource res = metadata.getResource();
    // now persist EML file (resource must have ID now)
    try {
      File metadataFile = cfg.getMetadataFile(res.getId());
      Writer writer = XmlFileUtils.startNewUtf8XmlFile(metadataFile);
      xstream.toXML(metadata, writer);
      writer.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
