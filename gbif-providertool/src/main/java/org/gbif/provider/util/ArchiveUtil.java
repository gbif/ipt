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
package org.gbif.provider.util;

// TODO: Internationalisation on messages throughout.

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.DcTerm;
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
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.ViewMappingManager;
import org.gbif.provider.service.impl.BaseManager;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * This class can be used to transform a Darwin Core Archive into Resource
 * {@link ExtensionMapping}s.
 * 
 */
public class ArchiveUtil<T extends Resource> extends BaseManager {

  /**
   * An archive request encapsulates an archive file location and a resource to
   * process.
   * 
   * @param <T> the type of resource
   */
  public static class ArchiveRequest<T extends Resource> {

    /**
     * Static factory method for requests.
     * 
     * @param <T> the resource type
     * @param location the darwin core archive file location
     * @param resource the resource
     * @return a new request
     */
    @SuppressWarnings("unchecked")
    public static <T extends Resource> ArchiveRequest<T> with(File location,
        T resource) {
      checkNotNull(location, "Location is null");
      checkArgument(location.exists() && location.canRead()
          && location.getParentFile().canWrite(),
          "Insufficient filesystem permissions: " + location);
      checkNotNull(resource, "Resource is null");
      checkArgument(resource.getId() != null, "Resource id is null");
      return new ArchiveRequest(location, resource);
    }

    private final File location;
    private T resource;

    private ArchiveRequest(File location, T resource) {
      this.location = location;
      this.resource = resource;
    }

    public File getLocation() {
      return location;
    }

    public T getResource() {
      return resource;
    }
  }

  /**
   * An archive response encapsulates a resource, messages that were generated
   * while processing the request, and a flag that indicates if processing was
   * successful.
   * 
   * @param <T> the resource type
   */
  public static class ArchiveResponse<T> {
    private final T resource;
    private final ImmutableSet<String> messages;
    private final boolean success;

    private ArchiveResponse(T resource, ImmutableSet<String> messages,
        boolean success) {
      this.resource = resource;
      this.messages = messages;
      this.success = success;
    }

    public ImmutableSet<String> getMessages() {
      return messages;
    }

    public T getResource() {
      return resource;
    }

    public boolean isSuccess() {
      return success;
    }
  }

  /**
   * Finite state machine for processing the <core> of a Darwin Core Archive.
   */
  private class CoreStateMachine {
    State state = State.INITIAL;
    ArchiveFile coreFile;
    String rowType;
    Extension extension;
    ExtensionMapping mapping = new ExtensionMapping();
    SourceFile sourceFile;
    ImmutableSet.Builder<String> msgBuilder = ImmutableSet.builder();

    private CoreStateMachine(ArchiveFile core) {
      this.coreFile = core;
    }

    ImmutableSet<String> getMessages() {
      return msgBuilder.build();
    }

    ImmutableMap<SourceFile, ExtensionMapping> process()
        throws IllegalStateException {
      try {
        sourceFile = getSourceFile(coreFile);
        while (state != State.DONE) {
          switch (state) {

            case INITIAL:
              rowType = coreFile.getRowType();
              state = hasRowType(rowType) ? State.HAS_ROW_TYPE
                  : State.NO_ROW_TYPE;
              break;

            case NO_ROW_TYPE:
              extension = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
              state = State.HAS_EXTENSION;
              break;

            case HAS_ROW_TYPE:
              loadExtension();
              state = State.HAS_EXTENSION;
              break;

            case HAS_EXTENSION:
              mapping = ExtensionMapping.with(extension);
              boolean hasFields = hasFields(coreFile);
              boolean hasHeader = hasHeader(coreFile);
              if (!hasFields && hasHeader) {
                state = State.NO_FIELDS_HAS_HEADER;
              } else if (!hasFields && !hasHeader) {
                state = State.NO_FIELDS_NO_HEADER;
              } else if (hasFields && hasHeader) {
                state = State.HAS_FIELDS_HAS_HEADER;
              } else {
                state = State.HAS_FIELDS_NO_HEADER;
              }
              break;

            case NO_FIELDS_HAS_HEADER:
              mapping = buildExtensionMappings(extension, mapping, coreFile);
              if (request.resource instanceof OccurrenceResource) {
                saveOccurrenceResourceExtensionMappings(mapping, coreFile);
              } else if (request.resource instanceof ChecklistResource) {
                saveChecklistResourceExtensionMappings(mapping, coreFile);
              }
              state = State.DONE;
              break;

            case NO_FIELDS_NO_HEADER:
              // TODO: This is not a valid case for a Darwin Core Archive.
              if (request.resource instanceof OccurrenceResource) {
                saveOccurrenceResourceExtensionMappings(mapping, coreFile);
              } else if (request.resource instanceof ChecklistResource) {
                saveChecklistResourceExtensionMappings(mapping, coreFile);
              }
              state = State.DONE;
              break;

            case HAS_FIELDS_NO_HEADER:
            case HAS_FIELDS_HAS_HEADER:
              // An archive is defined by the field elements if it has them,
              // regardless of header.
              mapping = addPropertyMappings(extension, mapping, coreFile,
                  msgBuilder);
              state = State.DONE;
              break;

          }
        }
      } catch (Exception e) {
        String msg = "Unable to process core: " + e.toString();
        haltOnIllegalState(msg);
      }
      return ImmutableMap.of(sourceFile, mapping);
    }

    private void loadExtension() {
      if (rowType.equalsIgnoreCase("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord")
          || rowType.equalsIgnoreCase("http://rs.tdwg.org/dwc/terms/Occurrence")
          || rowType.equalsIgnoreCase("http://rs.tdwg.org/dwc/terms/Taxon")
          || rowType.equalsIgnoreCase("http://rs.tdwg.org/dwc/terms/DarwinCore")) {
        extension = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
      } else {
        try {
          extension = extensionManager.getExtensionByRowType(rowType);
        } catch (NonUniqueResultException e) {
        }
      }
      if (extension == null) {
        String msg = String.format(
            "Unable to process archive %s: Unrecognized core rowType %s for file %s. Check that the extension is installed.",
            request.location.getName(), rowType, new File(
                coreFile.getLocation()).getName());
        log.warn(msg);
        msgBuilder.add(msg);
        sourceManager.remove(sourceFile.getId());
        haltOnIllegalState(msg);
      }
      // The resource can type can be determined from the archive meta.xml
      // rowType attribute if the extension for it has been loaded.
      // TODO: Confirm that we do not want this check:
      // if (extension.getId().equals(Constants.DARWIN_CORE_EXTENSION_ID)
      // && !(request.resource instanceof OccurrenceResource)) {
      // String msg =
      // "Unable to process archive because it represents an OccurrenceResource but you are creating a "
      // + request.resource.getClass().getSimpleName();
      // msgBuilder.add(msg);
      // haltOnIllegalState(msg);
      // }
      if (!extension.getId().equals(Constants.DARWIN_CORE_EXTENSION_ID)
          && (request.resource instanceof OccurrenceResource)) {
        String msg = String.format(
            "Unable to process archive %s because it doesn't represent an OccurrenceResource but the rowType is %s",
            request.resource.getClass().getSimpleName(), rowType);
        msgBuilder.add(msg);
        sourceManager.remove(sourceFile.getId());
        haltOnIllegalState(msg);
      }
      msgBuilder.add(String.format("Processed file %s with core rowType %s",
          new File(coreFile.getLocation()).getName(), rowType));
    }
  }

  /**
   * Finite state machine for processing <extension> in a Darwin Core Archive.
   */
  private class ExtensionStateMachine {
    State state = State.INITIAL;
    ArchiveFile extensionFile;
    String rowType;
    Extension extension;
    ExtensionMapping mapping = new ExtensionMapping();
    SourceFile sourceFile;
    ImmutableSet.Builder<String> msgBuilder = ImmutableSet.builder();

    private ExtensionStateMachine(ArchiveFile extensionArchiveFile) {
      this.extensionFile = extensionArchiveFile;
    }

    ImmutableSet<String> getMessages() {
      return msgBuilder.build();
    }

    ImmutableMap<SourceFile, ExtensionMapping> process()
        throws IllegalStateException {
      try {
        sourceFile = getSourceFile(extensionFile);
        while (state != State.DONE) {
          switch (state) {

            case INITIAL:
              rowType = extensionFile.getRowType();
              state = hasRowType(rowType) ? State.HAS_ROW_TYPE
                  : State.NO_ROW_TYPE;
              break;

            case NO_ROW_TYPE:
              String msg = "Archive file didn't have a rowType: "
                  + extensionFile.getLocation();
              log.warn(msg);
              msgBuilder.add(msg);
              state = State.DONE;
              break;

            case HAS_ROW_TYPE:
              try {
                extension = extensionManager.getExtensionByRowType(rowType);
              } catch (NonUniqueResultException e) {
              }
              if (extension == null) {
                msg = String.format(
                    "Skipping extension file %s because an extension corresponding to rowType %s could not be found in this IPT instance",
                    new File(extensionFile.getLocation()).getName(), rowType);
                log.warn(msg);
                msgBuilder.add(msg);
                sourceManager.remove(sourceFile.getId());
                state = State.DONE;
                break;
              }
              state = State.HAS_EXTENSION;
              msgBuilder.add(String.format("Processed %s with extension %s",
                  new File(extensionFile.getLocation()).getName(), rowType));
              break;

            case HAS_EXTENSION:
              mapping = ExtensionMapping.with(extension);
              boolean hasFields = hasFields(extensionFile);
              boolean hasHeader = hasHeader(extensionFile);
              if (!hasFields && hasHeader) {
                state = State.NO_FIELDS_HAS_HEADER;
              } else if (!hasFields && !hasHeader) {
                state = State.NO_FIELDS_NO_HEADER;
              } else if (hasFields && hasHeader) {
                state = State.HAS_FIELDS_HAS_HEADER;
              } else {
                state = State.HAS_FIELDS_NO_HEADER;
              }
              break;

            case HAS_FIELDS_NO_HEADER: // TODO: Correct?
            case HAS_FIELDS_HAS_HEADER:
              mapping = addPropertyMappings(extension, mapping, extensionFile,
                  msgBuilder);
              state = State.DONE;
              break;

            case NO_FIELDS_NO_HEADER:
              // TODO: How to handle index (<core index=0) here?
              if (request.resource instanceof OccurrenceResource) {
                saveOccurrenceResourceExtensionMappings(mapping, extensionFile);
              } else if (request.resource instanceof ChecklistResource) {
                saveChecklistResourceExtensionMappings(mapping, extensionFile);
              }
              state = State.DONE;
              break;

            case NO_FIELDS_HAS_HEADER:
              mapping = buildExtensionMappings(extension, mapping,
                  extensionFile);
              if (request.resource instanceof OccurrenceResource) {
                saveOccurrenceResourceExtensionMappings(mapping, extensionFile);
              } else if (request.resource instanceof ChecklistResource) {
                saveChecklistResourceExtensionMappings(mapping, extensionFile);
              }
              state = State.DONE;
              break;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        String msg = "Unable to process core: " + e.toString();
        sourceManager.remove(sourceFile.getId());
        haltOnIllegalState(msg);
      }
      return ImmutableMap.of(sourceFile, mapping);
    }
  }

  private static enum State {
    NO_ROW_TYPE, HAS_ROW_TYPE, NO_FIELDS, HAS_EXTENSION, NO_FIELDS_NO_HEADER, NO_FIELDS_HAS_HEADER, DONE, INITIAL, HAS_FIELDS_HAS_HEADER, HAS_FIELDS_NO_HEADER;
  }

  @Autowired
  private ChecklistResourceManager checklistResourceManager;

  @Autowired
  private OccResourceManager occResourceManager;

  @Autowired
  private ViewMappingManager extensionMappingManager;

  @Autowired
  private ExtensionPropertyManager epManager;

  @Autowired
  private ExtensionManager extensionManager;

  private ArchiveRequest<T> request;

  @Autowired
  protected AppConfig cfg;

  @Autowired
  private ExtensionPropertyManager extensionPropertyManager;

  @Autowired
  private SourceInspectionManager sourceInspector;

  @Autowired
  private SourceManager sourceManager;

  @Autowired
  @Qualifier("propertyMappingManager")
  private GenericManager<PropertyMapping> propertyMappingManager;

  ArchiveUtil() {
  }

  private ArchiveUtil(ArchiveRequest<T> request) {
    this.request = request;
  }

  /**
   * Static factory method that initializes a new archive utility with a
   * request.
   * 
   * @param request the request
   */
  public ArchiveUtil<T> init(ArchiveRequest<T> request) {
    checkNotNull(request, "Request is null");
    this.request = request;
    return this;
    // TODO: How to use Spring for DI of new ArchiveUtil instance?
    // return new ArchiveUtil<T>(request);
  }

  /**
   * Synchronously processes the request and returns the response.
   * 
   * @return ArchiveResponse<T>
   */
  public ArchiveResponse<T> process() {
    return transform();
  }

  protected ExtensionMapping addPropertyMappings(Extension extension,
      ExtensionMapping mapping, ArchiveFile archiveFile,
      Builder<String> msgBuilder) throws Exception {
    ExtensionProperty ep = null;
    PropertyMapping pm;
    ConceptTerm concept;
    ArchiveField field;
    String msg;
    String conceptName = null;
    ImmutableList<String> header = getHeader(archiveFile);
    for (Entry<ConceptTerm, ArchiveField> entry : archiveFile.getFields().entrySet()) {
      concept = entry.getKey();
      field = entry.getValue();

      if (concept == null || field == null) {
        msg = "Warning: ConceptTerm or ArchiveField is null in "
            + archiveFile.getLocation();
        log.warn(msg);
        msgBuilder.add(msg);
        continue;
      }
      if (concept.qualifiedName() == null) {
        msg = "Warning: ConceptTerm.qualifiedName is null in "
            + concept.simpleName();
        log.warn(msg);
        msgBuilder.add(msg);
        continue;
      }

      // Looks up an existing extension property:
      String qn = concept.qualifiedName();
      ep = extensionPropertyManager.getProperty(extension, qn);
      if (ep == null) {
        // Avoid spurious errors for special cases.
        if (!qn.toLowerCase().endsWith("sourceid")
            && !qn.toLowerCase().endsWith("source")
            && !qn.toLowerCase().endsWith("classs")) {
          msg = "Warning: Unsupported extension property: "
              + concept.qualifiedName();
          log.warn(msg);
          msgBuilder.add(msg);
          continue;
        }
      }
      // If there is no extension property, don't add it.
      if (ep == null) {
        continue;
      }

      extension.addProperty(ep);

      // Static mapping:
      if (field.getIndex() == null) {
        if (field.getDefaultValue() != null) {
          conceptName = field.getDefaultValue();
        }
      } else {
        try {
          conceptName = header.get(field.getIndex());
        } catch (Exception e) {
          msg = "Warning: Unable to determine concept name for "
              + field.getTerm().simpleName();
          log.warn(msg);
          msgBuilder.add(msg);
          continue;
        }
      }

      pm = PropertyMapping.with(ep, conceptName, "");
      pm.setProperty(ep);
      pm.setViewMapping(mapping);
      mapping.addPropertyMapping(pm);

      // If the ArchiveFile has a Id column, set it in the mapping
      if (hasIdIndex(archiveFile)) {
        Integer i = getIdIndex(archiveFile);
        String s = header.get(i);
        mapping.setCoreIdColumn(s);
      } else {
        mapping.setCoreIdColumn(header.get(0));
      }
    }
    if (request.resource instanceof OccurrenceResource) {
      saveOccurrenceResourceExtensionMappings(mapping, archiveFile);
    } else if (request.resource instanceof ChecklistResource) {
      saveChecklistResourceExtensionMappings(mapping, archiveFile);
    }
    return mapping;
  }

  /**
   * 
   * void
   * 
   * @throws Exception
   */
  protected ExtensionMapping buildExtensionMappings(Extension extension,
      ExtensionMapping mapping, ArchiveFile core) throws Exception {
    List<String> header = getHeader(core);
    ExtensionProperty ep;
    PropertyMapping pm;
    for (String concept : header) {
      ep = epManager.getPropertyByName(extension, concept);
      // TODO: Confirm that we skip this extension property:
      if (ep == null) {
        log.warn("Warning: ArchiveUtil.java buildExtensionMappings(): No extension property found for extension "
            + extension.getName() + " with property name " + concept);
        continue;
      }
      extension.addProperty(ep);
      // TODO: Confirm that this default value is correct:
      String defaultValue = "";
      pm = PropertyMapping.with(ep, concept, defaultValue);
      pm.setProperty(ep);
      pm.setViewMapping(mapping);
      mapping.addPropertyMapping(pm);
      // TODO: Confirm this is correct:
      if (hasIdIndex(core)) {
        mapping.setCoreIdColumn(header.get(getIdIndex(core)));
      } else {
        mapping.setCoreIdColumn(header.get(0));
      }
    }
    return mapping;
  }

  protected Integer getFieldIndex(ArchiveFile core, String term) {
    if (hasFieldIndex(core, term)) {
      return core.getField(term).getIndex();
    }
    return null;
  }

  protected Integer getIdIndex(ArchiveFile core) {
    if (hasIdIndex(core)) {
      return core.getId().getIndex();
    }
    log.warn("Returning a null core id index");
    return null;
  }

  protected void haltOnIllegalState(String msg) {
    log.error("Unable to process archive: " + msg);
    throw new IllegalStateException(msg);
  }

  /**
   * @param extensionFile - the file to look in
   * @param String term - the term to look for in the file
   * @return boolean
   */
  protected boolean hasFieldIndex(ArchiveFile core, String term) {
    ArchiveField fi = core.getField(term);
    return fi != null && fi.getIndex() != null;
  }

  protected boolean hasFields(ArchiveFile core) {
    return core.getFields() != null && !core.getFields().isEmpty();
  }

  protected boolean hasHeader(ArchiveFile af) {
    // Always returning false because the <field> elements contain the presumed
    // reliable mapping information. The specification allows an archive text
    // file to have an arbitrary number of pre-data lines, but no way to insure
    // that any of these is an actual column header.
    return false;
    // return af.getIgnoreHeaderLines() >= 1;
    // Integer ignoreHeaderLine = af.getIgnoreHeaderLines();
    // return ignoreHeaderLine == null ? false : ignoreHeaderLine == 1;
  }

  /**
   * @param extensionFile - the file to look in
   * @return boolean
   */
  protected boolean hasIdIndex(ArchiveFile core) {
    ArchiveField coreId = core.getId();
    return coreId != null && coreId.getIndex() != null;
  }

  protected boolean hasRowType(String rowType) {
    return rowType != null && rowType.trim().length() > 0;
  }

  @SuppressWarnings("unchecked")
  protected void saveChecklistResourceExtensionMappings(
      ExtensionMapping mapping, ArchiveFile core) throws IOException {
    // Save resource:
    ChecklistResource r = (ChecklistResource) request.resource;
    checklistResourceManager.save(r);

    // Save source file:
    SourceFile source = getSourceFile(core);

    // Save mapping:
    mapping.setResource(r);
    mapping.setSource(source);
    r.addExtensionMapping(mapping);
    extensionMappingManager.save(mapping);
    for (PropertyMapping p : mapping.getPropertyMappingsSorted()) {
      extensionPropertyManager.save(p.getProperty());
      propertyMappingManager.save(p);
    }
    extensionMappingManager.save(mapping);
    checklistResourceManager.save(r);

    request.resource = (T) r;
  }

  @SuppressWarnings("unchecked")
  protected void saveOccurrenceResourceExtensionMappings(
      ExtensionMapping mapping, ArchiveFile core) throws IOException {
    // Save resource:
    OccurrenceResource r = (OccurrenceResource) request.resource;
    occResourceManager.save(r);

    // Save source file:
    SourceFile source = getSourceFile(core);

    // Save mapping:
    mapping.setResource(r);
    mapping.setSource(source);
    r.addExtensionMapping(mapping);
    extensionMappingManager.save(mapping);
    for (PropertyMapping p : mapping.getPropertyMappingsSorted()) {
      extensionPropertyManager.save(p.getProperty());
      propertyMappingManager.save(p);
    }
    extensionMappingManager.save(mapping);
    occResourceManager.save(r);

    request.resource = (T) r;
  }

  /**
   * If the file location is compressed as a ZIP or GZIP archive, it is expanded
   * into the same location as the archive itself. Otherwise no action is taken.
   * 
   * @param location the file location of the archive.
   * @return
   * @throws IOException
   */
  private File expandIfCompressed(File location) throws IOException {
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
   * Gets the the source file header.
   * 
   * @param af the source file
   * @return list of String headers
   * @throws Exception
   * @throws MalformedTabFileException List<String>
   */
  private ImmutableList<String> getHeader(final ArchiveFile af)
      throws Exception {

    // Create header here from field elements.
    // Assume a valid archive (i.e., field elements exists for all mapped terms,
    // indexes are valid, etc.).
    // Build a list that we will then sort by the indexes
    List<Map.Entry<ConceptTerm, ArchiveField>> list = new LinkedList<Map.Entry<ConceptTerm, ArchiveField>>();
    final Map.Entry<ConceptTerm, ArchiveField> idterm = new Map.Entry<ConceptTerm, ArchiveField>() {
      ArchiveField field;

      public ConceptTerm getKey() {
        return DcTerm.identifier;
      }

      public ArchiveField getValue() {
        return field;
      }

      public ArchiveField setValue(ArchiveField field) {
        return this.field = field;
      }
    };
    idterm.setValue(af.getId());
    list.add(idterm);
    list.addAll(af.getFields().entrySet());

    // Sort the list using an anonymous inner class implementing Comparator
    // for the compare method
    Collections.sort(list,
        new Comparator<Map.Entry<ConceptTerm, ArchiveField>>() {
          public int compare(Map.Entry<ConceptTerm, ArchiveField> entry,
              Map.Entry<ConceptTerm, ArchiveField> entry1) {
            return entry.getValue().getIndex().compareTo(
                entry1.getValue().getIndex());
          }
        });

    ImmutableList.Builder<String> b = ImmutableList.builder();

    for (Map.Entry<ConceptTerm, ArchiveField> me : list) {
      ArchiveField afield = me.getValue();
      if (afield.getTerm() == null) {
        b.add("id");
      } else {
        b.add(afield.getTerm().simpleName());
      }
    }
    return b.build();
  }

  /**
   * @param af
   * @return String
   */
  private String getSeparator(ArchiveFile af) {
    String separator = String.valueOf(af.getFieldsTerminatedBy());
    if (separator == null || separator.length() == 0) {
      separator = ",";
    }
    log.info("Found separator " + separator + " for ArchiveFile "
        + af.getLocation());
    return separator;
  }

  private SourceFile getSourceFile(ArchiveFile af) throws IOException {
    File file = null;
    SourceFile s = null;
    try {
      file = new File(af.getLocation());
      s = sourceManager.getSourceByFilename(request.resource.getId(),
          file.getName());
      if (s == null) {
        s = new SourceFile();
        s.setDateUploaded(new Date());
      }
      s.setName(file.getName());
      s.setHeaders(hasHeader(af));
      s.setNumLinesToSkip(af.getIgnoreHeaderLines());
      s.setResource((DataResource) request.resource);
      s.setCsvFileHeader(Joiner.on(',').skipNulls().join(getHeader(af)));
      s.setSeparator(getSeparator(af));
      s.setArchiveFile(true);
      s.setEncoding(af.getEncoding());
      sourceManager.save(s);
    } catch (Exception e) {
      throw new IOException("Unable to open " + file + " - " + e.toString());
    }
    return s;
  }

  private ArchiveResponse<T> transform() {
    Archive archive = null;
    File archiveLocation = null;
    try {
      archiveLocation = expandIfCompressed(request.location);
      archive = ArchiveFactory.openArchive(archiveLocation, true);
      if (archive == null) {
        // TODO: check for EML only or multiple data files.
        throw new UnsupportedArchiveException("Archive is null");
      }
    } catch (UnsupportedArchiveException e) {
      return new ArchiveResponse<T>(request.resource,
          ImmutableSet.of(e.toString()), false);
    } catch (IOException e) {
      return new ArchiveResponse<T>(request.resource,
          ImmutableSet.of(e.toString()), false);
    }
    ImmutableSet.Builder<String> messages = ImmutableSet.builder();
    // TODO: test that archive is well-formed with field elements for column
    // headers.

    CoreStateMachine coreFsm = new CoreStateMachine(archive.getCore());
    try {
      coreFsm.process();
    } catch (Exception e) {
      return new ArchiveResponse<T>(request.resource, coreFsm.getMessages(),
          false);
    }
    messages.addAll(coreFsm.getMessages());
    ExtensionStateMachine extensionFsm;
    for (ArchiveFile extension : archive.getExtensions()) {
      extensionFsm = new ExtensionStateMachine(extension);
      try {
        extensionFsm.process();
        messages.addAll(extensionFsm.getMessages());
      } catch (Exception e) {
        messages.addAll(extensionFsm.getMessages());
        return new ArchiveResponse<T>(request.resource, messages.build(), false);
      }
      messages.addAll(extensionFsm.getMessages());
    }
    return new ArchiveResponse<T>(request.resource, messages.build(), true);
  }
}
