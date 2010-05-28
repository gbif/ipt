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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.CompressionUtil;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.SourceFile;
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
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * 
 */
public class ArchiveUtil<T extends Resource> extends BaseManager {

  public static class Request<T extends Resource> {

    @SuppressWarnings("unchecked")
    public static <T extends Resource> Request<T> with(File location, T resource) {
      checkNotNull(location, "Location is null");
      checkArgument(location.exists() && location.canRead()
          && location.getParentFile().canWrite(),
          "Insufficient filesystem permissions: " + location);
      checkNotNull(resource, "Resource is null");
      checkArgument(resource.getId() != null, "Resource id is null");
      return new Request(location, resource);
    }

    private final File location;
    private T resource;

    private Request(File location, T resource) {
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

  public static class Response<T> {
    private final T resource;
    private final ImmutableSet<String> messages;

    private Response(T resource, ImmutableSet<String> messages) {
      this.resource = resource;
      this.messages = messages;
    }

    public ImmutableSet<String> getMessages() {
      return messages;
    }

    public T getResource() {
      return resource;
    }

  }

  private static enum ArchiveState {
    UNSUPPORTED, SUPPORTED, CORE_ONLY, CORE_WITH_EXTENSIONS;
  }

  private static enum Core {
    NO_ROW_TYPE, HAS_ROW_TYPE, NO_FIELDS, HAS_EXTENSION, NO_FIELDS_NO_HEADER, NO_FIELDS_HAS_HEADER, HAS_FIELDS, NO_ID, HAS_ID, INVALID, DONE, CREATE_PROPERTY_MAPPINGS, INITIAL, SAVE, HAS_FIELDS_HAS_HEADER, HAS_FIELDS_NO_HEADER;
  }

  private class CoreStateMachine {
    Core state = Core.INITIAL;
    ArchiveFile core;
    String rowType;
    Extension extension;
    ExtensionMapping mapping;
    SourceFile sourceFile;

    private CoreStateMachine(ArchiveFile core) {
      this.core = core;
    }

    ImmutableMap<SourceFile, ExtensionMapping> process()
        throws IllegalStateException {
      try {
        sourceFile = getSourceFile(core);
        while (state != Core.DONE) {
          switch (state) {

            case INITIAL:
              rowType = core.getRowType();
              state = hasRowType() ? Core.HAS_ROW_TYPE : Core.NO_ROW_TYPE;
              break;

            case NO_ROW_TYPE:
              extension = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
              state = Core.HAS_EXTENSION;
              break;

            case HAS_ROW_TYPE:
              if (rowType.equalsIgnoreCase("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord")
                  || rowType.equalsIgnoreCase("http://rs.tdwg.org/dwc/terms/Occurrence")) {
                extension = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
              } else {
                try {
                  extension = extensionManager.getExtensionByRowType(rowType);
                } catch (NonUniqueResultException e) {
                }
              }
              if (extension == null) {
                String msg = String.format(
                    "Unrecognized rowType %s (skipping)", rowType);
                log.warn(msg);
                haltOnIllegalState(msg);
              }
              state = Core.HAS_EXTENSION;
              break;

            case HAS_EXTENSION:
              mapping = ExtensionMapping.with(extension);
              if (!(request.resource instanceof OccurrenceResource)) {
                String msg = "Tried to upload an occurrence archive to a "
                    + request.resource.getClass().getSimpleName();
                haltOnIllegalState(msg);
              }
              boolean hasFields = hasFields();
              boolean hasHeader = hasHeader(core);
              if (!hasFields && hasHeader) {
                state = Core.NO_FIELDS_HAS_HEADER;
              } else if (!hasFields && !hasHeader) {
                state = Core.NO_FIELDS_NO_HEADER;
              } else if (hasFields && hasHeader) {
                state = Core.HAS_FIELDS_HAS_HEADER;
              } else {
                state = Core.HAS_FIELDS_NO_HEADER;
              }
              break;

            case HAS_FIELDS_HAS_HEADER:
              ExtensionProperty ep = null;
              PropertyMapping pm;
              ConceptTerm concept;
              ArchiveField field;
              String conceptName = null;
              for (Entry<ConceptTerm, ArchiveField> entry : core.getFields().entrySet()) {
                concept = entry.getKey();
                field = entry.getValue();

                if (concept == null || field == null) {
                  log.warn("ConceptTerm or ArchiveField is null in "
                      + core.getLocation());
                  continue;
                }
                if (concept.qualifiedName() == null) {
                  log.warn("ConceptTerm.qualifiedName is null in "
                      + concept.simpleName());
                  continue;
                }

                // Looks up an existing extension property:
                ep = extensionPropertyManager.getProperty(extension,
                    concept.qualifiedName());
                if (ep == null) {
                  log.warn("Unsupported ExtensionProperty: "
                      + concept.qualifiedName());
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
                    conceptName = getHeader(core).get(field.getIndex());
                  } catch (Exception e) {
                    log.warn("Unable to determine concept name for " + field);
                    continue;
                  }
                }

                pm = PropertyMapping.with(ep, conceptName, "");
                pm.setProperty(ep);
                pm.setViewMapping(mapping);
                mapping.addPropertyMapping(pm);

                // TODO: Confirm this is correct:
                if (hasIdIndex()) {
                  mapping.setCoreIdColumn(getHeader(core).get(getIdIndex(core)));
                } else {
                  mapping.setCoreIdColumn(getHeader(core).get(0));
                }
              }
              if (request.resource instanceof OccurrenceResource) {
                saveOccurrenceResourceExtensionMappings();
              }
              state = Core.DONE;
              break;

            case NO_FIELDS_NO_HEADER:
              // TODO: How to handle index (<core index=0) here?
              if (request.resource instanceof OccurrenceResource) {
                saveOccurrenceResourceExtensionMappings();
              }
              state = Core.DONE;
              break;

            case NO_FIELDS_HAS_HEADER:
              buildExtensionMappings();
              if (request.resource instanceof OccurrenceResource) {
                saveOccurrenceResourceExtensionMappings();
              }
              state = Core.DONE;
              break;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        String msg = "Unable to process core: " + e.toString();
        haltOnIllegalState(msg);
      }
      return ImmutableMap.of(sourceFile, mapping);
    }

    /**
     * 
     * void
     * 
     * @throws IOException
     */
    private void buildExtensionMappings() throws IOException {
      List<String> header = getHeader(core);
      ExtensionProperty ep;
      PropertyMapping pm;
      for (String concept : header) {
        ep = epManager.getPropertyByName(extension, concept);
        // TODO: Confirm that we skip this extension property:
        if (ep == null) {
          log.warn("No extension property found for extension "
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
        if (hasIdIndex()) {
          mapping.setCoreIdColumn(header.get(getIdIndex(core)));
        } else {
          mapping.setCoreIdColumn(getHeader(core).get(0));
        }
      }
    }

    private Integer getIdIndex(ArchiveFile core) {
      if (hasIdIndex()) {
        return core.getId().getIndex();
      }
      log.warn("Returning a null core id index");
      return null;
    }

    private void haltOnIllegalState(String msg) {
      log.error(msg);
      throw new IllegalStateException(msg);
    }

    private boolean hasFields() {
      return core.getFields() != null && !core.getFields().isEmpty();
    }

    /**
     * In a metafile, the ignoreHeaderLines property of <core> specifies the
     * number lines to ignore from the beginning of the file. So if it's zero,
     * that means there are no lines to ignore and we don't have a header. The
     * default value is zero, so if it's null then we also don't have a header.
     * 
     * TODO: What if it's greater than 1?
     * 
     * @param core
     * @return boolean
     */
    private boolean hasHeader(ArchiveFile core) {
      Integer ignoreHeaderLine = core.getIgnoreHeaderLines();
      boolean result = ignoreHeaderLine == null ? false : ignoreHeaderLine == 1;
      return result;
    }

    /**
     * @param core
     * @return boolean
     */
    private boolean hasIdIndex() {
      ArchiveField coreId = core.getId();
      return coreId != null && coreId.getIndex() != null;
    }

    private boolean hasRowType() {
      return rowType != null && rowType.trim().length() > 0;
    }

    @SuppressWarnings("unchecked")
    private void saveOccurrenceResourceExtensionMappings() throws IOException {
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
  }

  @Autowired
  private OccResourceManager occResourceManager;

  @Autowired
  private ViewMappingManager extensionMappingManager;

  @Autowired
  private ExtensionPropertyManager epManager;

  @Autowired
  private ExtensionManager extensionManager;

  private Request<T> request;

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

  public ArchiveUtil<T> init(Request<T> request) {
    checkNotNull(request, "Request is null");
    this.request = request;
    return this;
  }

  public Response<T> process() {
    return transform();
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
   * @throws IOException
   * @throws MalformedTabFileException List<String>
   */
  private ImmutableList<String> getHeader(ArchiveFile af) throws IOException {
    File f = new File(af.getLocation());
    char separator = getSeparator(af).charAt(0);
    Charset charset = null;
    try {
      charset = Charset.forName(af.getEncoding());
    } catch (IllegalCharsetNameException e) {
    } catch (IllegalArgumentException e) {
    }
    if (charset == null) {
      log.warn("Setting charset to " + charset + " for file "
          + af.getLocation());
      charset = Charsets.ISO_8859_1;
    }
    ImmutableList<String> header = sourceInspector.getHeader(f, charset,
        separator);
    //      
    // ImmutableList.copyOf(Splitter.on(separator).trimResults().split(
    // Files.readFirstLine(f, charset)));
    // if (!hasHeader(af)) {
    // ImmutableList.Builder<String> b = ImmutableList.builder();
    // // TODO: Confirm this appraoch.
    // for (int i = 0; i < header.size(); i++) {
    // b.add(String.format("col%03d", i));
    // }
    // header = b.build();
    // }
    return header;
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
        s.setName(file.getName());
        s.setDateUploaded(new Date());
        s.setHeaders(hasHeader(af));
        s.setResource((DataResource) request.resource);
      }
      s.setCsvFileHeader(Joiner.on(',').skipNulls().join(getHeader(af)));
      s.setSeparator(getSeparator(af));
      sourceManager.save(s);
    } catch (Exception e) {
      throw new IOException("Unable to open " + file + " - " + e.toString());
    }
    if (s == null) {
      throw new NullPointerException(
          "Unable to create SourceFile from ArchiveFile: " + af.getLocation());
    }
    return s;
  }

  private boolean hasHeader(ArchiveFile af) {
    Integer ignoreHeaderLine = af.getIgnoreHeaderLines();
    return ignoreHeaderLine == null ? false : ignoreHeaderLine == 1;
  }

  private Response<T> transform() {
    Archive archive = null;
    try {
      File archiveLocation = expandIfCompressed(request.location);
      archive = ArchiveFactory.openArchive(archiveLocation, true);
      if (archive == null) {
        throw new UnsupportedArchiveException("Archive is null");
      }
    } catch (UnsupportedArchiveException e) {
      return new Response<T>(request.resource, ImmutableSet.of(e.toString()));
    } catch (IOException e) {
      return new Response<T>(request.resource, ImmutableSet.of(e.toString()));
    }
    new CoreStateMachine(archive.getCore()).process();
    return new Response<T>(request.resource, ImmutableSet.of("Success!"));
  }
}
