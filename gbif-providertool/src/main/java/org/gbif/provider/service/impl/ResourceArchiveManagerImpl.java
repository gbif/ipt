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

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.CompressionUtil;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.EmlFactory;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.ResourceArchiveManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.ViewMappingManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.XmlFileUtils;
import org.gbif.provider.util.ZipUtil;

import com.google.common.collect.ImmutableMap;
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
import java.util.Map.Entry;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.xml.sax.SAXException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * 
 * TODO: Amend TAX_SKIP_COLUMNS for ratified DwC.
 * 
 * Note: H2 supports tab file dumps out of the box Apart from the default CSV,
 * it allows to override delimiters so pure tab files can be created like this:
 * CALL CSVWRITE('/Users/markus/Desktop/test.txt', 'select id, label from taxon
 * order by label', 'utf8', ' ', '')
 * 
 */
public class ResourceArchiveManagerImpl extends BaseManager implements
    ResourceArchiveManager {

  /**
   * This class is an internal implementation of {@link ResourceArchive}.
   * 
   */
  static class ResourceArchiveImpl implements ResourceArchive {

    static ResourceArchiveImpl create(File file,
        ImmutableMap<SourceFile, ExtensionMapping> coreMapping,
        ImmutableMap<SourceFile, ExtensionMapping> mappings, Eml eml) {
      return new ResourceArchiveImpl(file, coreMapping, mappings, eml);
    }

    final File file;
    final Eml eml;
    final ImmutableMap<SourceFile, ExtensionMapping> coreMapping;
    final ImmutableMap<SourceFile, ExtensionMapping> extensionMappings;

    ResourceArchiveImpl(File file,
        ImmutableMap<SourceFile, ExtensionMapping> coreMapping,
        ImmutableMap<SourceFile, ExtensionMapping> extensionMappings, Eml eml) {
      this.file = file;
      this.coreMapping = coreMapping;
      this.extensionMappings = extensionMappings;
      this.eml = eml;
    }

    public File getArchiveFile() {
      return file;
    }

    @SuppressWarnings("unchecked")
    public <S extends SourceFile> S getCoreSourceFile() {
      return coreMapping.isEmpty() ? null
          : (S) coreMapping.keySet().iterator().next();
    }

    public Eml getEml() {
      return eml;
    }

    @SuppressWarnings("unchecked")
    public <M extends ExtensionMapping, S extends SourceFile> M getExtensionMapping(
        S source) {
      M mapping = null;
      if (coreMapping.containsKey(source)) {
        mapping = (M) coreMapping.get(source);
      } else if (extensionMappings.containsKey(source)) {
        mapping = (M) extensionMappings.get(source);
      }
      return mapping;
    }

    @SuppressWarnings("unchecked")
    public <S extends SourceFile> ImmutableSet<S> getExtensionSourceFiles() {
      return (ImmutableSet<S>) extensionMappings.keySet();
    }
  }
  /**
   * This class can be used for transforming an {@link ArchiveFile} into
   * corresponding {@link SourceFile} and {@link ExtensionMapping} objects.
   * 
   */
  private static class ArchiveAdapter {

    ExtensionManager extensionManager;
    ExtensionPropertyManager propertyManager;
    File archiveLocation;

    /**
     * Gives {@link ArchiveAdapter} a reference to services. Ideally services
     * would be static, but Spring DI via @Autowired doesn't support static
     * injection.
     * 
     * @param extensionManager
     * @param propertyManager void
     * @param archiveLocation
     */
    ArchiveAdapter(ExtensionManager extensionManager,
        ExtensionPropertyManager propertyManager, File archiveLocation) {
      this.extensionManager = extensionManager;
      this.propertyManager = propertyManager;
      this.archiveLocation = archiveLocation;
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
    File expandIfCompressed(File location) throws IOException {
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
      } else if (name.equalsIgnoreCase("eml.xml")) {
        directory = new File(location.getParentFile().getPath());
      }
      return directory;
    }

    /**
     * If a valid eml.xml exists at a given location, creates an {@link Eml}
     * instance and returns it. Otherwise returns null.
     * 
     * @param location the eml.xml file or a directory containing it
     * @return Eml
     * @throws IOException
     */
    Eml getEmlIfExists(File location) throws IOException {
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
     * This method returns an {@link Extension} given a row type attribute (an
     * URI) from the <core> or <extension> element of a metafile.
     * 
     * Note: This method returns null if duplicate extensions are found in H2 or
     * if H2 doesn't contain the extension.
     * 
     * Note: If an extension cannot be created but isCore is true, an extension
     * for Darwin Core will be returned.
     * 
     * @param rowType the extension URI
     * @param isCore true if the rowType is from a <core> element in a metafile
     * @return Extension
     */
    Extension getExtension(String rowType, boolean isCore) {
      Extension extension = null;
      try {
        extension = extensionManager.getExtensionByUri(rowType);
      } catch (NonUniqueResultException e) {
        // TODO: Duplicate extensions in H2. What action to take?
        log.warn("Duplicate extension found: " + rowType);
      }
      if (extension == null) {
        if (isCore) {
          extension = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
        } else {
          // TODO: No matching extensions in H2. What action to take?
          log.warn("No extension found for: " + rowType);
        }
      }
      if (isCore) {
        extension.setCore(true);
      }
      return extension;
    }

    /**
     * Returns an {@link ExtensionMapping} for an {@link ArchiveFile}.
     * 
     * Note: If an {@link Extension} or {@link ExtensionProperty} cannot be
     * created from the archive file, this method returns null.
     * 
     * Note: If an extension cannot be created but isCore is true, an extension
     * mapping for Darwin Core will be created and returned.
     * 
     * @param f the archive file
     * @param isCore true if the archive file represents the core
     * @return ExtensionMapping
     */
    ExtensionMapping getExtensionMapping(ArchiveFile f, boolean isCore) {
      Extension extension = getExtension(f.getRowType(), isCore);
      if (extension == null) {
        // TODO?
        return null;
      }
      ExtensionMapping em = ExtensionMapping.with(extension);
      SourceFile source = getSourceFile(f);
      em.setSource(source);
      ExtensionProperty ep;
      PropertyMapping pm;
      ConceptTerm ct;
      ArchiveField af;
      for (Entry<ConceptTerm, ArchiveField> entry : f.getFields().entrySet()) {
        ct = entry.getKey();
        af = entry.getValue();
        ep = getExtensionProperty(extension, ct.qualifiedName());
        if (ep == null) {
          // TODO?
          log.warn("Unable to load ExtensionProperty for: "
              + ct.qualifiedName());
          continue;
        }
        extension.addProperty(ep);
        pm = PropertyMapping.with(ep, ct.simpleName(), af.getDefaultValue());
        pm.setProperty(ep);
        pm.setViewMapping(em);
        em.addPropertyMapping(pm);
      }
      return em;
    }

    /**
     * Returns an {@link ImmutableMap} mapping of {@link SourceFile} to its
     * {@link ExtensionMapping} for all extensions in an {@link ArchiveFile}
     * (excluding core).
     * 
     * Note: If an extension mapping cannot be created for a source file, the
     * source file is ignored.
     * 
     * @param archive the archive
     * @return ImmutableMap<SourceFile, ExtensionMapping>
     */
    ImmutableMap<SourceFile, ExtensionMapping> getExtensionMappings(
        Archive archive) {
      checkNotNull(archive, "Archive is null");
      checkNotNull(archive.getExtensions(), "Archive extensions are null");
      if (archive.getExtensions().isEmpty()) {
        return ImmutableMap.of();
      }
      ImmutableMap.Builder<SourceFile, ExtensionMapping> b = ImmutableMap.builder();
      ExtensionMapping em;
      SourceFile source;
      for (ArchiveFile f : archive.getExtensions()) {
        em = getExtensionMapping(f, false);
        if (em == null) {
          // TODO?
          log.warn("Unable to create ExtensionMapping for ArchiveFile "
              + f.getTitle());
          continue;
        }
        source = getSourceFile(f);
        b.put(source, em);
      }
      return b.build();
    }

    /**
     * This method returns an {@link ExtensionProperty} given a qualified name.
     * 
     * Note: This method returns null if duplicate properties are found in H2 or
     * if H2 does not contain the property.
     * 
     * @param extension
     * 
     * @param qualifiedName the qualified name
     * @return ExtensionProperty
     */
    ExtensionProperty getExtensionProperty(Extension extension,
        String qualifiedName) {
      ExtensionProperty ep = null;
      try {
        ep = propertyManager.getProperty(extension, qualifiedName);
      } catch (NonUniqueResultException e) {
        // TODO: Duplicate extension properties in H2. What action to take?
      }
      if (ep == null) {
        // TODO: No matching ExtensionProperties in H2. What action to take?
      }
      return ep;
    }

    /**
     * Returns a new {@link SourceFile} from an {@link ArchiveFile}.
     * 
     * @param f
     * @return SourceFile
     */
    SourceFile getSourceFile(ArchiveFile f) {
      String p = String.format("%s/%s", archiveLocation.getPath(), f.getTitle());
      return new SourceFile(new File(p));
    }
  }

  private final static Log log = LogFactory.getLog(ResourceArchiveManager.class);
  public static final String OCC_SKIP_COLUMNS = "|occurrenceID|";
  public static final String TAX_SKIP_COLUMNS = "|ScientificName|TaxonID|Kingdom|Phylum|Class|Order|Family|Genus|Subgenus|HigherTaxonID|HigherTaxon|AcceptedTaxonID|AcceptedTaxon|BasionymID|Basionym|";
  private static final String CSVWRITE = "CALL CSVWRITE('%s', '%s', 'utf8')";
  private static final String DESCRIPTOR_TEMPLATE = "/WEB-INF/pages/dwcarchive-meta.ftl";

  @Autowired
  protected AppConfig cfg;

  @Autowired
  protected AnnotationManager annotationManager;

  @Autowired
  private SourceManager sourceManager;

  @Autowired
  private IptNamingStrategy namingStrategy;

  @Autowired
  private Configuration freemarker;

  @Autowired
  private EmlManager emlManager;

  @Autowired
  private ChecklistResourceManager checklistResourceManager;

  @Autowired
  private OccResourceManager occResourceManager;

  @Autowired
  private ViewMappingManager extensionMappingManager;

  @Autowired
  @Qualifier("resourceManager")
  private GenericResourceManager<Resource> metaResourceManager;

  @Autowired
  private ExtensionManager extensionManager;

  @Autowired
  private ExtensionPropertyManager extensionPropertyManager;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ResourceArchiveService#bind(org.gbif.provider
   * .model.Resource, org.gbif.provider.service.impl.ResourceArchive)
   */
  @SuppressWarnings("unchecked")
  public <R extends Resource, A extends ResourceArchive> R bind(R resource,
      A archive) {

    // TODO: Infer resource type from rowType. If rowType for core source is
    // http://rs.tdwg.org/dwc/terms/Taxon, then it's a ChecklistResource.

    if (archive.getCoreSourceFile() == null && archive.getEml() != null) {
      // Handles an EML-only archive:
      Eml eml = archive.getEml();
      eml.setResource(resource);
      emlManager.save(eml);
    } else {
      // Handles a normal DwC archive:
      OccurrenceResource r = (OccurrenceResource) resource;
      occResourceManager.save(r);
      SourceFile coreSource = archive.getCoreSourceFile();
      coreSource.setResource(r);
      sourceManager.save(coreSource);
      ExtensionMapping coreMapping = archive.getExtensionMapping(coreSource);
      coreMapping.setResource(r);
      coreMapping.setSource(coreSource);
      r.addExtensionMapping(coreMapping);
      extensionMappingManager.save(coreMapping);
      ExtensionMapping em;
      for (SourceFile s : archive.getExtensionSourceFiles()) {
        s.setResource(r);
        sourceManager.save(s);
        em = archive.getExtensionMapping(s);
        em.setResource(r);
        em.setSource(s);
        extensionMappingManager.save(em);
        r.addExtensionMapping(em);
      }
      occResourceManager.save(r);
      resource = (R) r;
    }
    return resource;
  }

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
  @SuppressWarnings("unchecked")
  public <R extends Resource, A extends ResourceArchive> R createResource(
      A archive) throws IOException {
    R resource = null;
    if (archive.getCoreSourceFile() == null && archive.getEml() != null) {
      // Handles an EML-only archive:
      resource = (R) new Resource();
      metaResourceManager.save(resource);
      Eml eml = archive.getEml();
      eml.setResource(resource);
      emlManager.save(eml);
    } else {
      // Handles a normal DwC archive:
      OccurrenceResource r = new OccurrenceResource();
      r.setDirty();
      occResourceManager.save(r);
      SourceFile coreSource = archive.getCoreSourceFile();
      coreSource.setResource(r);
      sourceManager.save(coreSource);
      ExtensionMapping coreMapping = archive.getExtensionMapping(coreSource);
      coreMapping.setResource(r);
      coreMapping.setSource(coreSource);
      r.addExtensionMapping(coreMapping);
      extensionMappingManager.save(coreMapping);
      ExtensionMapping em;
      for (SourceFile s : archive.getExtensionSourceFiles()) {
        s.setResource(r);
        sourceManager.save(s);
        em = archive.getExtensionMapping(s);
        em.setResource(r);
        em.setSource(s);
        extensionMappingManager.save(em);
        r.addExtensionMapping(em);
      }
      occResourceManager.save(r);
      resource = (R) r;
    }
    return resource;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.DataArchiveManager#openArchive(java.io.File,
   * boolean)
   */
  @SuppressWarnings("unchecked")
  public <A extends ResourceArchive> A openArchive(File location,
      boolean normalise) throws IOException, UnsupportedArchiveException {
    checkNotNull(location, "Location cannot be null");
    checkArgument(location.canRead(), "Location cannot be read: " + location);
    Archive a = null;
    Eml eml = null;

    ArchiveAdapter adapter = new ArchiveAdapter(extensionManager,
        extensionPropertyManager, location);

    // Expands and opens the archive:
    File archiveLocation = adapter.expandIfCompressed(location);
    adapter.archiveLocation = archiveLocation;

    try {
      a = ArchiveFactory.openArchive(archiveLocation, normalise);
    } catch (UnsupportedArchiveException e) {
      // Before giving up we check for eml.xml:
      eml = adapter.getEmlIfExists(archiveLocation);
      if (eml != null) {
        ImmutableMap<SourceFile, ExtensionMapping> coreMapping = ImmutableMap.of();
        ImmutableMap<SourceFile, ExtensionMapping> extensionMappings = ImmutableMap.of();
        // TODO: Serialize eml to metadata.xml
        return (A) ResourceArchiveImpl.create(location, coreMapping,
            extensionMappings, eml);
      }
      // TODO: Check for multiple data files supported by IPT
      throw e;
    }

    // Gets extension mapping for core:
    ImmutableMap<SourceFile, ExtensionMapping> coreMapping;
    SourceFile source = adapter.getSourceFile(a.getCore());
    coreMapping = ImmutableMap.of(source, adapter.getExtensionMapping(
        a.getCore(), true));

    // Gets extension mappings for extensions:
    ImmutableMap<SourceFile, ExtensionMapping> extensionMappings;
    extensionMappings = adapter.getExtensionMappings(a);

    // Gets Eml if it exists:
    eml = adapter.getEmlIfExists(archiveLocation);

    // Returns our internal ResourceArchive implementation:
    A ra = (A) ResourceArchiveImpl.create(a.getLocation(), coreMapping,
        extensionMappings, eml);

    return ra;
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