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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.XmlFileUtils;
import org.gbif.provider.util.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * TODO: Documentation.
 * 
 */
public class DataArchiveManagerImpl extends BaseManager implements
    DataArchiveManager {

  // H2 supports tab file dumps out of the box
  // Apart from the default CSV, it allows to override delimiters so pure tab
  // files can be created like this:
  // CALL CSVWRITE('/Users/markus/Desktop/test.txt', 'select id, label from
  // taxon order by label', 'utf8', ' ', '')

  public static final String OCC_SKIP_COLUMNS = "|occurrenceID|";
//  TODO: amend TAX_SKIP_COLUMNS for ratified DwC
  public static final String TAX_SKIP_COLUMNS = "|ScientificName|TaxonID|Kingdom|Phylum|Class|Order|Family|Genus|Subgenus|HigherTaxonID|HigherTaxon|AcceptedTaxonID|AcceptedTaxon|BasionymID|Basionym|";
  private static final String CSVWRITE = "CALL CSVWRITE('%s', '%s', 'utf8')";
  private static final String DESCRIPTOR_TEMPLATE = "/WEB-INF/pages/dwcarchive-meta.ftl";

  @Autowired
  protected AppConfig cfg;
  @Autowired
  protected AnnotationManager annotationManager;
  @Autowired
  private IptNamingStrategy namingStrategy;
  @Autowired
  private Configuration freemarker;

  public File createArchive(DataResource resource) throws IOException,
      IllegalStateException {
    if (resource.getCoreMapping() == null) {
      throw new IllegalStateException(
          "Resource needs at least a core mapping to create a data archive");
    }
    Map<File, ExtensionMapping> extensionFiles = new HashMap<File, ExtensionMapping>();
    Set<File> files = new HashSet<File>();
    File coreFile = null;
    // individual archive files
    try {
      if (resource instanceof OccurrenceResource) {
        coreFile = dumpOccCore(resource.getCoreMapping());
      } else if (resource instanceof ChecklistResource) {
        coreFile = dumpTaxCore(resource.getCoreMapping());
      } else {
        log.error("Unknown resource class "
            + resource.getClass().getCanonicalName());
      }
    } catch (Exception e) {
      annotationManager.annotateResource(resource,
          "Could not write data archive file for extension "
              + resource.getCoreMapping().getExtension().getName()
              + " of resource " + resource.getTitle());
    }
    for (ExtensionMapping view : resource.getExtensionMappings()) {
      try {
        if (resource instanceof ChecklistResource) {
          extensionFiles.put(dumpTaxExtension(view), view);
        } else {
          extensionFiles.put(dumpOccExtension(view), view);
        }
      } catch (Exception e) {
        annotationManager.annotateResource(resource,
            "Could not write data archive file for extension "
                + view.getExtension().getName() + " of resource "
                + resource.getTitle());
      }
    }
    files.addAll(extensionFiles.keySet());
    files.add(coreFile);

    // meta descriptor file
    File descriptor = writeDescriptor(resource, extensionFiles, coreFile);
    if (descriptor.exists()) {
      files.add(descriptor);
    } else {
      log.error("Archive descriptor could not be generated");
    }

    // zip archive
    File archive = cfg.getArchiveFile(resource.getId());
    File eml = cfg.getEmlFile(resource.getId());
    if (eml.exists()) {
      files.add(eml);
    } else {
      log.warn("No EML file existing to include in archive");
    }
    ZipUtil.zipFiles(files, archive);

    return archive;
  }

  private String buildPropertySelect(String prefix, ExtensionMapping view) {
    String select = "";
    for (ExtensionProperty p : view.getMappedProperties()) {
      select += "," + prefix + getColumnName(p.getName());
    }
    return select;
  }

  private String buildPropertySelect(String prefix,
      List<ExtensionProperty> properties) {
    String select = "";
    for (ExtensionProperty p : properties) {
      select += "," + prefix + getColumnName(p.getName());
    }
    return select;
  }

  private File dumpFile(File file, String select) throws IOException,
      SQLException {
    if (file.exists()) {
      file.delete();
    }
    File dir = file.getParentFile();
    if (!dir.exists()) {
      FileUtils.forceMkdir(dir);
    }
    file.createNewFile();
    log.debug("Created archive file " + file.getAbsolutePath());
    String sql = String.format(CSVWRITE, file.getAbsolutePath(), select);
    log.debug(sql);
    getConnection().prepareStatement(sql).execute();
    return file;
  }

  private File dumpOccCore(ExtensionMapping view) throws IOException,
      SQLException {
    File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
    List<ExtensionProperty> properties = getOccurrenceProperties(view);
    String select = String.format(
        "SELECT guid, modified, link, source_id %s FROM Darwin_Core where resource_fk=%s",
        buildPropertySelect("", properties), view.getResourceId());
    return dumpFile(file, select);
  }

  private File dumpOccExtension(ExtensionMapping view) throws IOException,
      SQLException {
    File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
    String select = String.format(
        "SELECT c.guid %s FROM %s e join darwin_core c on c.id=e.coreid where e.resource_fk=%s",
        buildPropertySelect("e.", view),
        namingStrategy.extensionTableName(view.getExtension()),
        view.getResourceId());
    return dumpFile(file, select);
  }

  private File dumpTaxCore(ExtensionMapping view) throws IOException,
      SQLException {
    File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
    List<ExtensionProperty> properties = getChecklistProperties(view);
    // TODO: Modified SELECT statement for ratified DwC output.
    String select = String.format(
        "SELECT t.guid, t.modified, t.link, t.source_id, t.label as ScientificName, acc.guid as AcceptedTaxonID,acc.label AcceptedTaxon, p.guid as HigherTaxonID,p.label as HigherTaxon, bas.guid as BasionymID,bas.label as Basionym %s FROM taxon t left join taxon p on t.parent_fk=p.id left join taxon acc on t.acc_fk=acc.id left join taxon bas on t.bas_fk=bas.id WHERE t.resource_fk=%s",
        buildPropertySelect("t.", properties), view.getResourceId());
    return dumpFile(file, select);
  }

  private File dumpTaxExtension(ExtensionMapping view) throws IOException,
      SQLException {
    File file = cfg.getArchiveFile(view.getResourceId(), view.getExtension());
    String select = String.format(
        "SELECT c.guid %s FROM %s e join taxon c on c.id=e.coreid where e.resource_fk=%s",
        buildPropertySelect("e.", view),
        namingStrategy.extensionTableName(view.getExtension()),
        view.getResourceId());
    return dumpFile(file, select);
  }

  private List<ExtensionProperty> getChecklistProperties(ExtensionMapping view) {
    List<ExtensionProperty> properties = new ArrayList<ExtensionProperty>();
    for (ExtensionProperty p : view.getMappedProperties()) {
      if (ChecklistResource.DWC_GROUPS.contains(p.getGroup())
          && !TAX_SKIP_COLUMNS.contains("|" + p.getName() + "|")) {
        properties.add(p);
      }
    }
    return properties;
  }

  private String getColumnName(String propName) {
    String col = namingStrategy.propertyToColumnName(propName);
    // check reserved sql words
    if (col.equalsIgnoreCase("order")) {
      col = "orderrr as \"ORDER\" ";
    } else if (col.equalsIgnoreCase("class")) {
      col = "classs as \"CLASS\" ";
    } else if( col.equalsIgnoreCase("group")){
      col = "grouppp as \"GROUP\" ";
    }
    return col;
  }

  private List<ExtensionProperty> getOccurrenceProperties(ExtensionMapping view) {
    List<ExtensionProperty> properties = new ArrayList<ExtensionProperty>();
    for (ExtensionProperty p : view.getMappedProperties()) {
      if (!OCC_SKIP_COLUMNS.contains("|" + p.getName() + "|")) {
        properties.add(p);
      }
    }
    return properties;
  }

  private File writeDescriptor(DataResource resource,
      Map<File, ExtensionMapping> archiveFiles, File coreFile) {
    Map<String, ExtensionMapping> fileMap = new HashMap<String, ExtensionMapping>();
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("cfg", cfg);
    data.put("resource", resource);
    data.put("rowType", resource.getCoreMapping().getExtension().getRowType());
    List<ExtensionProperty> coreProperties = new ArrayList<ExtensionProperty>();
    if (resource instanceof ChecklistResource) {
      data.put("isChecklist", true);
      coreProperties = getChecklistProperties(resource.getCoreMapping());
    } else {
      data.put("isChecklist", false);
      coreProperties = getOccurrenceProperties(resource.getCoreMapping());
    }
    data.put("coreProperties", coreProperties);
    data.put("coreFilename", coreFile.getName());

    for (File f : archiveFiles.keySet()) {
      fileMap.put(f.getName(), archiveFiles.get(f));
    }
    data.put("fileMap", fileMap);
    File descriptor = cfg.getArchiveDescriptor(resource.getId());
    try {
      String page = FreeMarkerTemplateUtils.processTemplateIntoString(
          freemarker.getTemplate(DESCRIPTOR_TEMPLATE), data);
      Writer out = XmlFileUtils.startNewUtf8XmlFile(descriptor);
      out.write(page);
      out.close();
      log.info("Created DarwinCore archive descriptor with " + fileMap.size()
          + " files for resource " + resource.getTitle());
    } catch (TemplateException e) {
      log.error("Freemarker template exception", e);
    } catch (IOException e) {
      log.error("IO Error when writing dwc archive descriptor", e);
    }
    return descriptor;
  }

}
