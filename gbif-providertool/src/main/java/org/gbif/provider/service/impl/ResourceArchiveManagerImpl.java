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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.ResourceArchiveManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.ViewMappingManager;
import org.gbif.provider.service.impl.ResourceArchive.Type;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.MalformedTabFileException;
import org.gbif.provider.util.TabFileReader;
import org.gbif.provider.util.XmlFileUtils;
import org.gbif.provider.util.ZipUtil;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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

    static ResourceArchiveImpl create(Type type, File file,
        ImmutableMap<SourceFile, ExtensionMapping> coreMapping,
        ImmutableMap<SourceFile, ExtensionMapping> mappings, Eml eml) {
      return new ResourceArchiveImpl(type, file, coreMapping, mappings, eml);
    }

    final File file;
    final Eml eml;
    final ImmutableMap<SourceFile, ExtensionMapping> coreMapping;
    final ImmutableMap<SourceFile, ExtensionMapping> extensionMappings;
    final Type type;

    ResourceArchiveImpl(Type type, File file,
        ImmutableMap<SourceFile, ExtensionMapping> coreMapping,
        ImmutableMap<SourceFile, ExtensionMapping> extensionMappings, Eml eml) {
      this.type = type;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.gbif.provider.service.impl.ResourceArchive#getType()
     */
    public Type getType() {
      return type;
    }
  }
  /**
   * This class can be used for transforming an {@link ArchiveFile} into
   * corresponding {@link SourceFile} and {@link ExtensionMapping} objects.
   * 
   */
  private static class ArchiveAdapter {

    AppConfig appConfig;
    ExtensionManager extensionManager;
    ExtensionPropertyManager propertyManager;
    File archiveLocation;
    SourceInspectionManager sourceInspectionManager;
    SourceManager sourceManager;
    private Resource resource;

    /**
     * Gives {@link ArchiveAdapter} a reference to services. Ideally services
     * would be static, but Spring DI via @Autowired doesn't support static
     * injection.
     * 
     * @param resource
     * 
     * @param extensionManager
     * @param propertyManager void
     * @param archiveLocation
     */
    ArchiveAdapter(Resource resource, AppConfig appConfig,
        ExtensionManager extensionManager,
        ExtensionPropertyManager propertyManager,
        SourceInspectionManager sourceInspectionManager,
        SourceManager sourceManager, File archiveLocation) {
      this.resource = resource;
      this.appConfig = appConfig;
      this.extensionManager = extensionManager;
      this.propertyManager = propertyManager;
      this.sourceInspectionManager = sourceInspectionManager;
      this.sourceManager = sourceManager;
      this.archiveLocation = archiveLocation;
    }

    /**
     * Returns the {@link Type} of an {@link Archive}. If the archive is null,
     * the archive is assumed to be an invalid Darwin Core Archive. In this
     * case, there are two cases to check. First, the archive could have
     * contained just an eml.xml file in which case it's a metadata type.
     * Second, the archive could have contained multiple data files without a
     * metafile.
     * 
     * @param a the archive
     * @return Type type of archive
     * @throws UnsupportedArchiveException
     */
    public Type getType(Archive a) throws UnsupportedArchiveException {
      Type archiveType = null;
      if (a == null) {
        try {
          if (getEmlIfExists(archiveLocation) != null) {
            archiveType = Type.METADATA;
          }
        } catch (IOException e) {
          // TODO: Look for multiple data files.
        }
        return archiveType;
      }
      // Sanity check:
      if (a.getCore() == null || a.getCore().getRowType() == null) {
        throw new UnsupportedArchiveException(
            "The core archive file or its rowType is null or empty: "
                + archiveLocation);
      }
      String coreRowType = a.getCore().getRowType().toLowerCase();
      if (CHECKLIST_ROW_TYPES.contains(coreRowType)) {
        archiveType = Type.CHECKLIST;
      } else if (OCCURRENCE_ROW_TYPES.contains(coreRowType)
          || (a.getExtensions().isEmpty() && coreRowType.trim().length() == 0)) {
        // We checked for empty archive extensions and rowType in the predicate
        // because if there are none, it's a single data file (not eml since we
        // already checked for that) which is clearly an occurrence type:
        archiveType = Type.OCCURRENCE;
      }
      return archiveType;
    }

    /**
     * If the file location is compressed as a ZIP or GZIP archive, it is
     * expanded into the same location as the archive itself. Otherwise no
     * action is taken.
     * 
     * @param location the file location of the archive.
     * @return
     * @throws IOException
     */
    File expandIfCompressed(File location) throws IOException {
      String name = location.getName();
      if (name.endsWith(".zip") || GzipUtils.isCompressedFilename(name)) {
        File directory = location.getParentFile();
        CompressionUtil.decompressFile(directory, location);
        location = directory;
      } else if (location.isFile()) {
        location = location.getParentFile();
      }
      return location;
    }

    /**
     * If a valid eml.xml exists at a given location, creates an {@link Eml}
     * instance and returns it. Otherwise returns null.
     * 
     * @param location the eml.xml file or a directory containing it
     * @return Eml if it exists, null otherwise
     * @throws IOException
     */
    Eml getEmlIfExists(File location) throws IOException {
      Eml eml = null;
      String path = null;
      File directory = null;

      // TODO: Can we infer the eml filename from an archive?
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
      if (rowType == null || rowType.trim().length() == 0) {
        if (isCore) {
          extension = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
          extension.setCore(true);
          return extension;
        } else {
          // TODO: No matching extensions in H2. What action to take?
          log.warn("No extension found since rowType is missing");
          return null;
        }
      }

      try {
        extension = extensionManager.getExtensionByRowType(rowType);
      } catch (NonUniqueResultException e) {
        // TODO: Duplicate extensions in H2. What action to take?
        log.warn("Duplicate extension found: " + rowType);
      }
      if (isCore && extension == null) {
        log.warn("No extension found for: " + rowType);
        extension = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
        extension.setCore(true);
      } else if (isCore) {
        extension.setCore(true);
      }
      return extension;
    }

    /**
     * Returns an {@link ExtensionMapping} for an {@link ArchiveFile}.
     * 
     * If an {@link Extension} is not installed for the archive files rowType,
     * this method returns null.
     * 
     * If an {@link ExtensionProperty} cannot be found for a {@link ConceptTerm}
     * qualified name, it is ignored.
     * 
     * If the source file header cannot be read,
     * 
     * Note: If an extension cannot be created but isCore is true, an extension
     * mapping for Darwin Core will be created and returned.
     * 
     * @param f the archive file
     * @param isCore true if the archive file represents the core
     * @return ExtensionMapping
     * @throws UnsupportedArchiveException
     */
    ExtensionMapping getExtensionMapping(ArchiveFile f, boolean isCore)
        throws UnsupportedArchiveException {
      Extension extension;

      extension = getExtension(f.getRowType(), isCore);
      if (extension == null) {
        log.warn("Unsupported extention: " + f.getRowType()
            + ". Ignoring the entire archive file " + f.getLocation());
        return null;
      }

      ExtensionMapping em = ExtensionMapping.with(extension);
      SourceFile source = getSourceFile(f);
      boolean hasHeader = f.getIgnoreHeaderLines() > 0;
      List<String> header = Lists.newArrayList();
      try {
        if (hasHeader) {
          header = getHeader(source);
        }
      } catch (Exception e) {
        // TODO: Do we return null if this happens?
        log.warn("Unsupported header: " + f.getLocation());
      }
      em.setSource(source);
      ExtensionProperty ep;
      PropertyMapping pm;
      ConceptTerm ct;
      ArchiveField af;
      if (isCore) {
        f.getFields().put(f.getId().getTerm(), f.getId());
      }
      for (Entry<ConceptTerm, ArchiveField> entry : f.getFields().entrySet()) {
        ct = entry.getKey();
        af = entry.getValue();

        if (ct == null || af == null) {
          log.warn("ConceptTerm or ArchiveField is null in " + f.getLocation());
          continue;
        }
        if (ct.qualifiedName() == null) {
          log.warn("ConceptTerm.qualifiedName is null in " + ct.simpleName());
          continue;
        }

        // Looks up an existing extension property:
        ep = propertyManager.getProperty(extension, ct.qualifiedName());
        if (ep == null) {
          log.warn("Unsupported ExtensionProperty: " + ct.qualifiedName());
          continue;
        }
        extension.addProperty(ep);

        // Tries to figure out a column name for the property mapping:
        String column = null;
        if (!hasHeader) {
          column = ct.simpleName();
          // If this is <coreid>, set the ExtensionMappings coreIdColumn:
          // if (af.getIndex() != null && af.getIndex() == 0) {
          // em.setCoreIdColumn(column);
          // }
        } else {
          if (af.getIndex() == null) {
            if (af.getDefaultValue() != null) {
              // Handles a static mapping:
              column = af.getDefaultValue();
            } else {
              // Index and default value are both null so we skip this property:
              continue;
            }
          } else {
            // Handles a dynamic mapping:
            try {
              column = header.get(af.getIndex());
              // If this is <coreid>, set the ExtensionMappings coreIdColumn:
              // if (af.getIndex() != null && af.getIndex() == 0) {
              // em.setCoreIdColumn(column);
              // }
            } catch (IndexOutOfBoundsException e) {
              // The index and header didn't match up so we skip this property:
              continue;
            }
          }
        }

        pm = PropertyMapping.with(ep, column, af.getDefaultValue());
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
     * @throws UnsupportedArchiveException
     */
    ImmutableMap<SourceFile, ExtensionMapping> getExtensionMappings(
        Archive archive) throws UnsupportedArchiveException {
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
          // TODO? Spec says ingore file and warn user.
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
     * Gets the the source file header.
     * 
     * @param source the source file
     * @return list of String headers
     * @throws IOException
     * @throws MalformedTabFileException List<String>
     */
    @SuppressWarnings("static-access")
    List<String> getHeader(SourceFile source) throws IOException,
        MalformedTabFileException {
      File f = appConfig.getResourceSourceFile(resource.getId(),
          source.getName());
      TabFileReader reader = new TabFileReader(f, true);
      List<String> headers;
      if (source.hasHeaders()) {
        headers = Arrays.asList(reader.getHeader());
      } else {
        // create numbered column names if no headers are present
        int numCols = reader.getHeader().length;
        headers = new ArrayList<String>();
        int i = 1;
        while (i <= numCols) {
          headers.add(String.format("col%03d", i));
          i++;
        }
      }
      reader.close();
      return headers;
    }

    /**
     * Returns a new {@link SourceFile} from an {@link ArchiveFile}. If the
     * archive file in unreadable, an {@link UnsupportedArchiveException} is
     * thrown.
     * 
     * @param af the archive file
     * @return SourceFile
     * @throws UnsupportedArchiveException
     */
    SourceFile getSourceFile(ArchiveFile af) throws UnsupportedArchiveException {
      File file = new File(af.getLocation());
      if (!file.exists() || !file.isFile() || !file.canRead()) {
        throw new UnsupportedArchiveException("Unable to read archive file: "
            + file);
      }
      SourceFile s = sourceManager.getSourceByFilename(resource.getId(),
          file.getName());
      if (s == null) {
        s = new SourceFile();
        s.setFilename(file.getName());
        s.setDateUploaded(new Date());
      }

      // Important for when we create the PropertyMapping for this source file!
      s.setHeaders(af.getIgnoreHeaderLines() > 0);
      s.setResource((DataResource) resource);
      sourceManager.save(s);
      return s;
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
  private SourceInspectionManager sourceInspectionManager;

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
   * These properties are used to check the core rowType of an archive metafile.
   * Note that the values are all lower case.
   */
  private static final ImmutableSet<String> CHECKLIST_ROW_TYPES = ImmutableSet.of("http://rs.tdwg.org/dwc/terms/taxon");
  private static final ImmutableSet<String> OCCURRENCE_ROW_TYPES = ImmutableSet.of(
      "http://rs.tdwg.org/dwc/xsd/simpledarwincore/simpledarwinrecord",
      "http://rs.tdwg.org/dwc/terms/occurrence",
      "http://rs.tdwg.org/dwc/terms/darwincore");

  @Autowired
  @Qualifier("propertyMappingManager")
  private GenericManager<PropertyMapping> propertyMappingManager;

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
    checkNotNull(resource, "Resource is null");
    checkNotNull(archive, "Archive is null");
    checkNotNull(archive.getType(), "Archive type is null");
    String errorMsg = "Resource type does not match archive type";
    SourceFile coreSource;
    ExtensionMapping coreMapping;
    ExtensionMapping em;
    switch (archive.getType()) {
      case CHECKLIST:
        checkArgument(resource instanceof ChecklistResource, errorMsg);
        ChecklistResource cr = (ChecklistResource) resource;
        checklistResourceManager.save(cr);
        coreSource = archive.getCoreSourceFile();
        coreSource.setResource(cr);
        sourceManager.save(coreSource);
        coreMapping = archive.getExtensionMapping(coreSource);
        coreMapping.setResource(cr);
        coreMapping.setSource(coreSource);
        cr.addExtensionMapping(coreMapping);
        extensionMappingManager.save(coreMapping);
        for (SourceFile s : archive.getExtensionSourceFiles()) {
          s.setResource(cr);
          sourceManager.save(s);
          em = archive.getExtensionMapping(s);
          em.setResource(cr);
          em.setSource(s);
          extensionMappingManager.save(em);
          cr.addExtensionMapping(em);
          for (PropertyMapping pm : em.getPropertyMappingsSorted()) {
            propertyMappingManager.save(pm);
          }
        }
        checklistResourceManager.save(cr);
        resource = (R) cr;
        break;
      case OCCURRENCE:
        checkArgument(resource instanceof OccurrenceResource, errorMsg);
        OccurrenceResource r = (OccurrenceResource) resource;
        occResourceManager.save(r);
        coreSource = archive.getCoreSourceFile();
        coreSource.setResource(r);
        sourceManager.save(coreSource);
        coreMapping = archive.getExtensionMapping(coreSource);
        coreMapping.setResource(r);
        coreMapping.setSource(coreSource);
        r.addExtensionMapping(coreMapping);
        extensionMappingManager.save(coreMapping);
        for (SourceFile s : archive.getExtensionSourceFiles()) {
          s.setResource(r);
          sourceManager.save(s);
          em = archive.getExtensionMapping(s);
          em.setResource(r);
          em.setSource(s);
          extensionMappingManager.save(em);
          r.addExtensionMapping(em);
          for (PropertyMapping pm : em.getPropertyMappingsSorted()) {
            propertyMappingManager.save(pm);
          }
        }
        occResourceManager.save(r);
        resource = (R) r;
        break;
      case METADATA:
        // We bind eml with the resource after this switch statement...
        break;
    }

    // Binds eml with the resource and saves it to metadata.xml file:
    Eml eml = archive.getEml();
    if (eml != null) {
      eml.setResource(resource);
      emlManager.serialize(eml);
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

    Eml eml = emlManager.deserialize(resource);
    emlManager.toXmlFile(eml);
    File emlFile = cfg.getEmlFile(resource.getId());
    if (emlFile.exists()) {
      files.add(emlFile);
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
    // TODO: Dispatch to createArchive(DataResource)?
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
    // TODO: Just create a new resource and return bind(resource, archive)?
    // TODO: Use case for this?
    R resource = null;
    return resource;
  }

  /**
   * Opens the archive using the dwc-archive-reader project.
   * 
   * @see org.gbif.provider.service.DataArchiveManager#openArchive(java.io.File,
   *      Resource, boolean)
   */
  @SuppressWarnings("unchecked")
  public <A extends ResourceArchive> A openArchive(File location,
      Resource resource, boolean normalise) throws IOException,
      UnsupportedArchiveException {

    checkNotNull(location, "Location cannot be null");
    checkArgument(location.canRead(), "Location cannot be read: " + location);

    // We create an archive adapter on each request to mitigate any concurrency
    // issues:
    ArchiveAdapter adapter = new ArchiveAdapter(resource, cfg,
        extensionManager, extensionPropertyManager, sourceInspectionManager,
        sourceManager, location);

    // Expands the archive if it's compressed. The files are stored in the same
    // directory as the archive:
    File archiveLocation = adapter.expandIfCompressed(location);
    adapter.archiveLocation = archiveLocation;
    log.info("Archive opened: " + location);

    Type archiveType = null;
    Archive archive = null;

    /*
     * Tries to open the archive. This might fail if the archive is not a valid
     * Darwin Core Archive (i.e., if it has a single eml.xml file or multiple
     * data files without a metafile). These are cases that we must support for
     * the IPT since it allows users to upload multiple data files or a single
     * eml file.
     */
    try {
      archive = ArchiveFactory.openArchive(archiveLocation, normalise);
    } catch (UnsupportedArchiveException e) {
      // Ignore this exception until we investigate the archive type.
      log.warn("Invalid Darwin Core Archive: " + location);
    }

    // Note that the archive might be null here, but getType() handles that for
    // us:
    archiveType = adapter.getType(archive);
    if (archiveType == null) {
      // Now we know for sure that this is definitely not a supported archive:
      throw new UnsupportedArchiveException("Unknown archive type: " + location);
    }

    log.info(String.format("Archive %s is type %s", location, archiveType));

    ImmutableMap<SourceFile, ExtensionMapping> coreMapping;
    ImmutableMap<SourceFile, ExtensionMapping> extensionMappings;
    A resourceArchive = null;

    // Creates the eml if it exits in the archive:
    Eml eml = adapter.getEmlIfExists(archiveLocation);
    if (eml != null) {
      log.info("Eml was found in the archive: " + location);
    } else {
      log.info("Eml was not found in the archive: " + location);
    }

    SourceFile source = null;

    // Creates the resulting resource archive based on the archive type, which
    // is the same for both occurrence and checklist:
    switch (archiveType) {
      case OCCURRENCE:
      case CHECKLIST:
        // Gets extension mapping for core:
        source = adapter.getSourceFile(archive.getCore());
        coreMapping = ImmutableMap.of(source, adapter.getExtensionMapping(
            archive.getCore(), true));
        // Gets extension mappings for extensions:
        extensionMappings = adapter.getExtensionMappings(archive);
        resourceArchive = (A) ResourceArchiveImpl.create(archiveType,
            archive.getLocation(), coreMapping, extensionMappings, eml);
        break;
      case METADATA:
        // Metadata archive doesn't have core or extension mappings:
        coreMapping = ImmutableMap.of();
        extensionMappings = ImmutableMap.of();
        resourceArchive = (A) ResourceArchiveImpl.create(Type.METADATA,
            archiveLocation, coreMapping, extensionMappings, eml);
        break;
      default:
        // TODO: Check for multiple data files supported by IPT
    }

    return resourceArchive;
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
    } else if (col.equalsIgnoreCase("classs")) {
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