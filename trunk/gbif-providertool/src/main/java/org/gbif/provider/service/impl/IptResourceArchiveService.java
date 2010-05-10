/*
 * Copyright 2010 Global Biodiversity Informatics Facility.
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.CompressionUtil;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.EmlFactory;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ResourceArchiveService;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.XmlFileUtils;
import org.gbif.provider.util.ZipUtil;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FileUtils;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.xml.sax.SAXException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * H2 supports tab file dumps out of the box Apart from the default CSV, it
 * allows to override delimiters so pure tab files can be created like this:
 * CALL CSVWRITE('/Users/markus/Desktop/test.txt', 'select id, label from taxon
 * order by label', 'utf8', ' ', '')
 * 
 */
public class IptResourceArchiveService extends BaseManager implements
    ResourceArchiveService {

  private static class ArchiveUtil {

    /**
     * @param location
     * @return Eml
     * @throws IOException
     */
    public static Eml getEmlIfExists(File location) throws IOException {
      Eml eml = null;
      String path = null;
      File directory = null;
      final String emlFileName = "eml.xml";
      if (location.isFile()) {
        directory = location.getParentFile();
      } else {
        directory = location;
      }
      String[] list = directory.list(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.equalsIgnoreCase(emlFileName);
        }
      });
      if (list != null && list.length > 0) {
        try {
          path = String.format("%s/%s", directory.getPath(), emlFileName);
          eml = EmlFactory.build(new FileInputStream(new File(path)));
        } catch (SAXException e) {
          throw new IOException(e.toString());
        } catch (FileNotFoundException e) {
          throw new IOException(e.toString());
        } catch (IOException e) {
          throw e;
        }
      }
      return eml;
    }

    /**
     * If the file location is compressed as a ZIP or GZIP archive, it is
     * expanded into a new folder (named after the archive but without the
     * extension) and the folder is returned.
     * 
     * For example, /foo/bar/baz.zip would be expanded into the /foo/bar/baz
     * directory.
     * 
     * If the file location is not an archive, null is returned.
     * 
     * @param location the file location
     * @return expanded archive directory or null if the file wasn't an archive
     * @throws IOException
     */
    static File expandIfCompressed(File location) throws IOException {
      File directory = location;
      String name = location.getName(), path = null, parent = location.getParent();
      if (name.endsWith(".zip")) {
        name = name.split(".zip")[0];
        path = String.format("%s/%s", parent, name);
        directory = new File(path);
        directory.mkdir();
        CompressionUtil.decompressFile(directory, location);
      } else if (GzipUtils.isCompressedFilename(name)) {
        name = GzipUtils.getUncompressedFilename(name);
        path = String.format("%s/%s", parent, name);
        directory = new File(path);
        directory.mkdir();
        CompressionUtil.decompressFile(directory, location);
      }
      return directory;
    }
  }

  private static class ResourceArchiveImpl implements ResourceArchive {

    static ResourceArchiveImpl create(File file,
        ImmutableMultimap<SourceFile, Extension> sources, Eml eml) {
      return new ResourceArchiveImpl(file, sources, eml);
    }

    final File file;
    final Eml eml;
    final ImmutableMultimap<SourceFile, Extension> sources;

    /**
     * @param type2
     * @param file2
     * @param dataSources2
     * @param eml2
     */
    ResourceArchiveImpl(File file,
        ImmutableMultimap<SourceFile, Extension> sources, Eml eml) {
      this.file = file;
      this.sources = sources;
      this.eml = eml;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gbif.provider.service.impl.ResourceArchive#getArchiveFile()
     */
    public File getArchiveFile() {
      return file;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gbif.provider.service.impl.ResourceArchive#getDataSources()
     */
    @SuppressWarnings("unchecked")
    public <T extends SourceFile> ImmutableSet<T> getSourceFiles() {
      return (ImmutableSet<T>) sources.keySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gbif.provider.service.impl.ResourceArchive#getEml()
     */
    public Eml getEml() {
      return eml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gbif.provider.service.impl.ResourceArchive#getExtensions()
     */
    public ImmutableSet<Extension> getExtensions() {
      return ImmutableSet.copyOf(sources.values());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gbif.provider.service.impl.ResourceArchive#getExtensions(org.gbif
     * .provider.model.SourceBase)
     */
    public <T extends SourceFile> ImmutableSet<Extension> getExtensions(T source) {
      return ImmutableSet.copyOf(sources.get(source));
    }
  }

  public static final String OCC_SKIP_COLUMNS = "|occurrenceID|";
  // TODO: amend TAX_SKIP_COLUMNS for ratified DwC
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

  @Autowired
  private ExtensionManager extensionManager;

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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.DataArchiveManager#createArchive(org.gbif.provider
   * .model.Resource)
   */
  public <R extends Resource, A extends ResourceArchive> A createArchive(
      R resource) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.DataArchiveManager#createResource(org.gbif.provider
   * .service.ResourceArchive)
   */
  public <R extends Resource, A extends ResourceArchive> R createResource(
      A archive) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  // /*
  // * (non-Javadoc)
  // *
  // * @see
  // org.gbif.provider.service.DataArchiveManager#openArchive(java.io.File)
  // */
  // public Archive openArchive(File location, boolean normaliseExtension)
  // throws IOException, UnsupportedArchiveException {
  // checkNotNull(location, "Archive is null");
  // checkArgument(location.canRead(), "Archive is not readable: " + location);
  // File directory = ArchiveUtil.expandIfCompressed(location);
  // if (directory != null) {
  // return ArchiveFactory.openArchive(directory, normaliseExtension);
  // }
  // return ArchiveFactory.openArchive(location, normaliseExtension);
  // }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.DataArchiveManager#getCoreSourceFile(org.gbif
   * .dwc.text.Archive)
   */
  public SourceFile getCoreSourceFile(Archive archive) throws IOException {
    checkNotNull(archive, "Archive is null");
    checkNotNull(archive.getCore(), "Archive core is null");
    checkNotNull(archive.getCore().getTitle(), "Archive core title is null");
    SourceFile core = new SourceFile(new File(archive.getCore().getTitle()));
    return core;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.DataArchiveManager#getCoreSourceFiles(org.gbif
   * .dwc.text.Archive)
   */
  public ImmutableSet<Extension> getExtensions(Archive archive)
      throws IOException {
    checkNotNull(archive, "Archive is null");
    checkNotNull(archive.getExtensions(), "Archive extensions are null");
    if (archive.getExtensions().isEmpty()) {
      return ImmutableSet.of();
    }
    Extension extension;
    ImmutableSet.Builder<Extension> b = ImmutableSet.builder();
    for (ArchiveFile f : archive.getExtensions()) {
      try {
        extension = extensionManager.getExtensionByUri(f.getRowType());
        if (extension != null) {
          b.add(extension);
        } else {
          log.warn("Null extension returned for namespace: " + f.getRowType());
        }
      } catch (NonUniqueResultException e) {
        log.warn("Duplicate extension namespace was found: " + f.getRowType());
      }
    }
    return b.build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.DataArchiveManager#openArchive(java.io.File,
   * boolean)
   */
  @SuppressWarnings("unchecked")
  public <A extends ResourceArchive> A openArchive(File location,
      boolean normaliseExtension) throws IOException,
      UnsupportedArchiveException {
    checkNotNull(location, "Location cannot be null");
    checkArgument(location.canRead(), "Location cannot be read: " + location);
    File directory = ArchiveUtil.expandIfCompressed(location);
    Archive a;
    Eml eml = null;
    if (directory != null) {
      a = ArchiveFactory.openArchive(directory, normaliseExtension);
      eml = ArchiveUtil.getEmlIfExists(directory);
    } else {
      a = ArchiveFactory.openArchive(location, normaliseExtension);
    }
    ImmutableMultimap.Builder<SourceFile, Extension> sources = ImmutableMultimap.builder();
    ImmutableSet<Extension> extensions = getExtensions(a);

    // Extensions can be empty when the archive contains *only* a data file. The
    // assumption is that ArchiveFactory is returning a valid archive, so in the
    // absence of extensions, we associate the core source file with Darwin
    // Core.
    if (extensions.isEmpty()) {
      extensions = ImmutableSet.of(extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID));
    }

    SourceFile coreSourceFile = getCoreSourceFile(a);
    sources.putAll(coreSourceFile, extensions);
    return (A) ResourceArchiveImpl.create(a.getLocation(), sources.build(), eml);
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
    } else if (col.equalsIgnoreCase("group")) {
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