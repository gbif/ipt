/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.task;

import org.gbif.api.model.common.DOI;
import org.gbif.dwc.Archive;
import org.gbif.dwc.ArchiveField;
import org.gbif.dwc.ArchiveFile;
import org.gbif.dwc.DwcFiles;
import org.gbif.dwc.MetaDescriptorWriter;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.utils.file.ClosableReportingIterator;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;
import org.gbif.utils.text.LineComparator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class GenerateDwca extends ReportingTask implements Callable<Map<String, Integer>> {

  private enum STATE {
    WAITING, STARTED, DATAFILES, METADATA, BUNDLING, COMPLETED, ARCHIVING, VALIDATING, CANCELLED, FAILED
  }

  private static final Pattern escapeChars = Pattern.compile("[\t\n\r]");
  private final Resource resource;
  // record counts by extension <rowType, count>
  private Map<String, Integer> recordsByExtension = new HashMap<>();
  private Archive archive;
  private File dwcaFolder;
  // status reporting
  private int currRecords = 0;
  private int currRecordsSkipped = 0;
  private String currExtension;
  private STATE state = STATE.WAITING;
  private final SourceManager sourceManager;
  private final VocabulariesManager vocabManager;
  private Map<String, String> basisOfRecords;
  private Exception exception;
  private AppConfig cfg;
  private static final int ID_COLUMN_INDEX = 0;
  public static final String CHARACTER_ENCODING = "UTF-8";
  private static final TermFactory TERM_FACTORY = TermFactory.instance();
  private static final String SORTED_FILE_PREFIX = "sorted_";
  private static final org.gbif.utils.file.FileUtils GBIF_FILE_UTILS = new org.gbif.utils.file.FileUtils();
  public static final String CANCELLED_STATE_MSG = "Archive generation cancelled";
  public static final String ID_COLUMN_NAME = "id";
  public static final String TEXT_FILE_EXTENSION = ".txt";
  public static final String WILDCARD_CHARACTER = "*";

  public static final Set<DwcTerm> DWC_MULTI_VALUE_TERMS;

  private static final Comparator<String> IGNORE_CASE_COMPARATOR = Comparator.nullsFirst(String::compareToIgnoreCase);

  static {
    Set<DwcTerm> dwcTermsInternal = new HashSet<>();
    dwcTermsInternal.add(DwcTerm.recordedBy);
    dwcTermsInternal.add(DwcTerm.preparations);
    dwcTermsInternal.add(DwcTerm.associatedMedia);
    dwcTermsInternal.add(DwcTerm.associatedReferences);
    dwcTermsInternal.add(DwcTerm.associatedSequences);
    dwcTermsInternal.add(DwcTerm.associatedTaxa);
    dwcTermsInternal.add(DwcTerm.otherCatalogNumbers);
    dwcTermsInternal.add(DwcTerm.associatedOccurrences);
    dwcTermsInternal.add(DwcTerm.associatedOrganisms);
    dwcTermsInternal.add(DwcTerm.previousIdentifications);
    dwcTermsInternal.add(DwcTerm.higherGeography);
    dwcTermsInternal.add(DwcTerm.georeferencedBy);
    dwcTermsInternal.add(DwcTerm.georeferenceSources);
    dwcTermsInternal.add(DwcTerm.typeStatus);
    dwcTermsInternal.add(DwcTerm.identifiedBy);
    dwcTermsInternal.add(DwcTerm.identificationReferences);
    dwcTermsInternal.add(DwcTerm.higherClassification);
    dwcTermsInternal.add(DwcTerm.measurementDeterminedBy);
    DWC_MULTI_VALUE_TERMS = Collections.unmodifiableSet(dwcTermsInternal);
  }

  @Inject
  public GenerateDwca(@Assisted Resource resource, @Assisted ReportHandler handler, DataDir dataDir,
    SourceManager sourceManager, AppConfig cfg, VocabulariesManager vocabManager) throws IOException {
    super(1000, resource.getShortname(), handler, dataDir);
    this.resource = resource;
    this.sourceManager = sourceManager;
    this.cfg = cfg;
    this.vocabManager = vocabManager;
  }

  /**
   * Adds a single data file for a list of extension mappings that must all be mapped to the same extension.
   * </br>
   * The ID column is always the 1st column (index 0) and is always equal to the core record identifier that has been
   * mapped (e.g. occurrenceID, taxonID, etc).
   *
   * @param mappings list of ExtensionMapping
   * @param rowLimit maximum number of rows to write
   * @throws IllegalArgumentException if not all mappings are mapped to the same extension
   * @throws InterruptedException if the thread was interrupted
   * @throws IOException if problems occurred while persisting new data files
   * @throws GeneratorException if any problem was encountered writing data file
   */
  public void addDataFile(List<ExtensionMapping> mappings, @Nullable Integer rowLimit) throws IOException,
    IllegalArgumentException, InterruptedException, GeneratorException {
    checkForInterruption();
    if (mappings == null || mappings.isEmpty()) {
      return;
    }

    // update reporting
    currRecords = 0;
    currRecordsSkipped = 0;
    Extension ext = mappings.get(0).getExtension();
    currExtension = ext.getTitle();

    // verify that all mappings share this extension
    for (ExtensionMapping m : mappings) {
      if (!ext.equals(m.getExtension())) {
        throw new IllegalArgumentException(
          "All mappings for a single data file need to be mapped to the same extension: " + ext.getRowType());
      }
    }

    // create new tab file with the help of the Archive class representing the core file or an extension
    ArchiveFile af = ArchiveFile.buildTabFile();
    af.setRowType(TERM_FACTORY.findTerm(ext.getRowType()));
    af.setEncoding(CHARACTER_ENCODING);
    af.setDateFormat("YYYY-MM-DD");
    // in the generated file column 0 will be the id column
    ArchiveField idField = new ArchiveField();
    idField.setIndex(ID_COLUMN_INDEX);
    af.setId(idField);

    // find the union of all terms mapped and make them a field in the final archive
    Set<Term> mappedConceptTerms = addFieldsToArchive(mappings, af);

    // retrieve the ordered list of mapped ExtensionProperty
    List<ExtensionProperty> propertyList = getOrderedMappedExtensionProperties(ext, mappedConceptTerms);

    // reassign indexes ordered by Extension
    assignIndexesOrderedByExtension(propertyList, af);

    // total column count is equal to id column + mapped columns
    int totalColumns = 1 + propertyList.size();

    // create file name from extension name, with incremental suffix to resolve name conflicts (e.g. taxon.txt,
    // taxon2.txt, taxon3.txt)
    String extensionName = (ext.getName() == null) ? "f" : ext.getName().toLowerCase().replaceAll("\\s", "_");
    String fn = createFileName(dwcaFolder, extensionName);

    // open new file writer for single data file
    File dataFile = new File(dwcaFolder, fn);
    // add source file location

    // ready to go though each mapping and dump the data
    try (Writer writer = org.gbif.utils.file.FileUtils.startNewUtf8File(dataFile)) {
      af.addLocation(dataFile.getName());
      addMessage(Level.INFO, "Start writing data file for " + currExtension);
      boolean headerWritten = false;
      for (ExtensionMapping m : mappings) {
        // prepare index ordered list of all output columns apart from id column
        PropertyMapping[] inCols = new PropertyMapping[totalColumns];
        for (ArchiveField f : af.getFields().values()) {
          if (f.getIndex() != null && f.getIndex() > ID_COLUMN_INDEX) {
            inCols[f.getIndex()] = m.getField(f.getTerm().qualifiedName());
          }
        }

        // write header line 1 time only to file
        if (!headerWritten) {
          writeHeaderLine(propertyList, totalColumns, af, writer);
          headerWritten = true;
        }

        // write data (records) to file
        dumpData(writer, inCols, m, totalColumns, rowLimit, resource.getDoi());
        // store record number by extension rowType
        recordsByExtension.put(ext.getRowType(), currRecords);
      }
    } catch (IOException e) {
      // some error writing this file, report
      log.error("Fatal DwC-A Generator Error encountered while writing header line to data file", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error writing header line to data file", e);
    }

    // add archive file to archive
    if (resource.getCoreRowType() != null && resource.getCoreRowType().equalsIgnoreCase(ext.getRowType())) {
      archive.setCore(af);
    } else {
      archive.addExtension(af);
    }

    // final reporting
    addMessage(Level.INFO, "Data file written for " + currExtension + " with " + currRecords + " records and "
      + totalColumns + " columns");
    // how many records were skipped?
    if (currRecordsSkipped > 0) {
      addMessage(Level.WARN, "!!! " + currRecordsSkipped + " records were skipped for " + currExtension
        + " due to errors interpreting line, or because the line was empty");
    }
  }

  /**
   * Write the header column line to file.
   * 
   * @param propertyList ordered list of all ExtensionProperty that have been mapped across all mappings for a single
   *        Extension
   * @param totalColumns total number of columns in header
   * @param af tab file with representing the core file or an extension
   * @param writer file writer
   * @throws IOException if writing the header line failed
   */
  private void writeHeaderLine(List<ExtensionProperty> propertyList, int totalColumns, ArchiveFile af, Writer writer)
    throws IOException {
    String[] headers = new String[totalColumns];
    // reserve 1st column for "id"
    headers[ID_COLUMN_INDEX] = ID_COLUMN_NAME;
    // add remaining mapped-column names
    int c = 1;
    for (ExtensionProperty property : propertyList) {
      headers[c] = property.simpleName();
      c++;
    }
    // write header line - once per mapping
    String headerLine = tabRow(headers);
    af.setIgnoreHeaderLines(1);
    writer.write(headerLine);
  }

  /**
   * Adds EML file to DwC-A folder.
   * 
   * @throws GeneratorException if EML file could not be copied to DwC-A folder
   * @throws InterruptedException if executing thread was interrupted
   */
  private void addEmlFile() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.METADATA);
    try {
      FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname()), new File(dwcaFolder,
        DataDir.EML_XML_FILENAME));
      archive.setMetadataLocation(DataDir.EML_XML_FILENAME);
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while adding EML file to DwC-A folder", e);
    }
    // final reporting
    addMessage(Level.INFO, "EML file added");
  }

  /**
   * Build a new ArchiveField having a ConceptTerm, plus optional multi-value delimiter.
   * </br>
   * Since all default values ​​will be written in the data file, they won't be expressed in the archive file (meta.xml).
   * That's why the default value is always set to null.
   * 
   * @param term ConceptTerm
   * @param delimitedBy multi-value delimiter
   *
   * @return ArchiveField created
   */
  private ArchiveField buildField(Term term, @Nullable String delimitedBy) {
    ArchiveField f = new ArchiveField();
    f.setTerm(term);
    f.setDefaultValue(null);

    // is this term a multi-value field, and has a multi-value delimiter been configured?
    if (delimitedBy != null && term instanceof DwcTerm && DWC_MULTI_VALUE_TERMS.contains(term)) {
      f.setDelimitedBy(delimitedBy);
    }

    return f;
  }

  /**
   * Zips the DwC-A folder. A temp version is created first, and when successful, it it moved into the resource's
   * data directory.
   * 
   * @throws GeneratorException if DwC-A could not be zipped or moved
   * @throws InterruptedException if executing thread was interrupted
   */
  private void bundleArchive() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.BUNDLING);
    File zip = null;
    BigDecimal version = resource.getEmlVersion();
    try {
      // create zip
      zip = dataDir.tmpFile("dwca", ".zip");
      CompressionUtil.zipDir(dwcaFolder, zip);
      if (zip.exists()) {
        // move to data dir with versioned name
        File versionedFile = dataDir.resourceDwcaFile(resource.getShortname(), version);
        if (versionedFile.exists()) {
          FileUtils.forceDelete(versionedFile);
        }
        FileUtils.moveFile(zip, versionedFile);
      } else {
        throw new GeneratorException("Archive bundling failed: temp archive not created: " + zip.getAbsolutePath());
      }
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while bundling DwC-A", e);
    } finally {
      // cleanup zip directory, if compression was incomplete for example due to Exception
      // if moving zip to data dir was successful, it won't exist any more and cleanup will be skipped
      if (zip != null && zip.exists()) {
        FileUtils.deleteQuietly(zip);
      }
    }
    // final reporting
    addMessage(Level.INFO, "Archive has been compressed");
  }

  /**
   * Validate the DwC-A:
   * -ensure that if the core record identifier is mapped (e.g. occurrenceID, taxonID, etc) it is present on all
   * rows, and is unique
   * 
   * @throws GeneratorException if DwC-A could not be validated
   * @throws InterruptedException if executing thread was interrupted
   */
  private void validate() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.VALIDATING);

    try {
      // retrieve newly generated archive - decompressed
      Archive arch = DwcFiles.fromLocation(dwcaFolder.toPath());
      // populate basisOfRecord lookup HashMap
      loadBasisOfRecordMapFromVocabulary();
      // perform validation on core file (includes core ID and basisOfRecord validation)
      validateCoreDataFile(arch.getCore(), !arch.getExtensions().isEmpty());
      // extra check for event core - publish warning if there aren't any associated occurrences
      if (isEventCore(arch)) {
        validateEventCore(arch);
      }
      // perform validation on extension files
      if (!arch.getExtensions().isEmpty()) {
        validateExtensionDataFiles(arch.getExtensions());
      }
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while validating DwC-A", e);
    }
    // final reporting
    addMessage(Level.INFO, "Archive validated");
  }

  /**
   * Sort the data file of a Darwin Core Archive by a column. Sorting is case sensitive.
   * 
   * @param file unsorted file
   * @param column column to sort by file by
   *
   * @return the data file of the Archive sorted by column
   * @throws IOException if the sort fails for whatever reason
   */
  private File sortCoreDataFile(ArchiveFile file, int column) throws IOException {
    // retrieve the core file
    File unsorted = file.getLocationFile();

    // create a new file that will store the records sorted by column
    File sorted = new File(unsorted.getParentFile(), SORTED_FILE_PREFIX + unsorted.getName());
    // get the ignore column rows, delimiter, enclosed by, newline character
    int headerLines = file.getIgnoreHeaderLines();
    String columnDelimiter = file.getFieldsTerminatedBy();
    Character enclosedBy = file.getFieldsEnclosedBy();
    String newlineDelimiter = file.getLinesTerminatedBy();

    // keep track of how long the sort takes
    long time = System.currentTimeMillis();

    // sort by column
    LineComparator lineComparator =
      new LineComparator(column, columnDelimiter, enclosedBy, IGNORE_CASE_COMPARATOR);
    GBIF_FILE_UTILS
      .sort(unsorted, sorted, CHARACTER_ENCODING, column, columnDelimiter, enclosedBy, newlineDelimiter,
        headerLines, lineComparator, true);
    log.debug("Finished sorting file " + unsorted.getAbsolutePath() + " in "
        + (System.currentTimeMillis() - time) / 1000 + " secs, check: " + sorted.getAbsoluteFile());

    return sorted;
  }

  /**
   * For each extension data file:
   * </br>
   * -validate each record has an id
   * -validate basisOfRecord in extensions having occurrence rowType
   * -validate occurrenceId in extensions having occurrence rowType (if mapped)
   *
   * @param extensions Set of Archive extension data files (not core data files)
   */
  private void validateExtensionDataFiles(Set<ArchiveFile> extensions)
    throws InterruptedException, GeneratorException, IOException {
    for (ArchiveFile extension: extensions) {
      validateExtensionDataFile(extension);
    }
  }

  /**
   * Populate basisOfRecords map from XML vocabulary, used to validate basisOfRecord values.
   */
  private void loadBasisOfRecordMapFromVocabulary() {
    if (basisOfRecords == null) {
      basisOfRecords = new HashMap<>();
      basisOfRecords
        .putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_BASIS_OF_RECORDS, Locale.ENGLISH.getLanguage(), false));
      basisOfRecords = MapUtils.getMapWithLowercaseKeys(basisOfRecords);
    }
  }

  /**
   * Validates that each record has a non empty ID, which is used to link the extension record and core record together.
   * </br>
   * Validates that each occurrence record has an occurrenceID, and that each occurrenceID is unique.
   * Performs this check only if the occurrenceID term has actually been mapped.
   * </br>
   * Validates that each occurrence record has a basisOfRecord, and that each basisOfRecord matches the
   * DwC Type Vocabulary.
   *
   * @param extFile extension file to validate
   *
   * @throws GeneratorException   if validation was interrupted due to an error
   * @throws InterruptedException if the thread was interrupted
   * @throws java.io.IOException  if a problem occurred sorting file, or opening iterator on it for example
   */
  private void validateExtensionDataFile(ArchiveFile extFile)
    throws GeneratorException, InterruptedException, IOException {
    Objects.requireNonNull(resource.getCoreRowType());
    addMessage(Level.INFO, "Validating the extension file: " + extFile.getTitle()
                           + ". Depending on the number of records, this can take a while.");
    // get the core record ID term
    Term id = TERM_FACTORY.findTerm(AppConfig.coreIdTerm(resource.getCoreRowType()));
    Term occurrenceId = TERM_FACTORY.findTerm(Constants.DWC_OCCURRENCE_ID);
    Term basisOfRecord = TERM_FACTORY.findTerm(Constants.DWC_BASIS_OF_RECORD);

    int basisOfRecordIndex = -1;
    if (isOccurrenceFile(extFile)) {
      // fail immediately if occurrence core doesn't contain basisOfRecord mapping
      if (!extFile.hasTerm(basisOfRecord)) {
        addMessage(Level.ERROR,
          "Archive validation failed, because required term basisOfRecord was not mapped in the occurrence extension data file: "
          + extFile.getTitle());
        throw new GeneratorException("Can't validate DwC-A for resource " + resource.getShortname()
                                     + "Required term basisOfRecord was not mapped in the occurrence extension data file: "
                                     + extFile.getTitle());
      }
      addMessage(Level.INFO, "? Validating the basisOfRecord in the occurrence extension data file is always present and its "
                             + "value matches the Darwin Core Type Vocabulary.");

      if (extFile.hasTerm(occurrenceId)) {
        addMessage(Level.INFO, "? Validating the occurrenceId in occurrence extension data file is always present and unique. ");
      } else {
        addMessage(Level.WARN,
          "No occurrenceId found in occurrence extension. To be indexed by GBIF, each occurrence record within a resource must have a unique record level identifier.");
      }
      // find index of basisOfRecord
      basisOfRecordIndex = extFile.getField(basisOfRecord).getIndex();
    }

    // validate the extension ID has been mapped
    if (extFile.getId() == null) {
      addMessage(Level.ERROR, "Archive validation failed, because the ID field " + id.simpleName() + "was not mapped in the extension data file: "
        + extFile.getTitle());
      throw new GeneratorException("Can't validate DwC-A for resource " + resource.getShortname()
                                   + ". The ID field was not mapped in the extension data file: "
                                   + extFile.getTitle());
    }
    addMessage(Level.INFO, "? Validating the ID field " + id.simpleName() + " is always present in extension data file. ");

    // find index of column to sort file by - use occurrenceId term index if mapped, ID column otherwise
    int sortColumnIndex = (extFile.hasTerm(occurrenceId) && extFile.getField(occurrenceId).getIndex() != null) ?
      extFile.getField(occurrenceId).getIndex() : ID_COLUMN_INDEX;

    // create a sorted data file
    File sortedFile = sortCoreDataFile(extFile, sortColumnIndex);

    // metrics
    int recordsWithNoId = 0;
    AtomicInteger recordsWithNoOccurrenceId = new AtomicInteger(0);
    AtomicInteger recordsWithDuplicateOccurrenceId = new AtomicInteger(0);
    AtomicInteger recordsWithNoBasisOfRecord = new AtomicInteger(0);
    AtomicInteger recordsWithNonMatchingBasisOfRecord = new AtomicInteger(0);
    AtomicInteger recordsWithAmbiguousBasisOfRecord = new AtomicInteger(0);

    // reporting
    currRecords = 0;
    currRecordsSkipped = 0;
    currExtension = extFile.getTitle();

    // create an iterator on the new sorted data file
    CSVReader reader = CSVReaderFactory.build(sortedFile,
            CHARACTER_ENCODING,
            extFile.getFieldsTerminatedBy(),
            extFile.getFieldsEnclosedBy(),
            extFile.getIgnoreHeaderLines());

    String lastId = null;
    try {
      while (reader.hasNext()) {
        currRecords++;
        if (currRecords % 1000 == 0) {
          checkForInterruption(currRecords);
          reportIfNeeded();
        }
        String[] record = reader.next();
        if (record == null || record.length == 0) {
          continue;
        }
        // Exception on reading row was encountered
        if (reader.hasRowError() && reader.getException() != null) {
          throw new GeneratorException("A fatal error was encountered while trying to validate sorted extension data file: "
                  + reader.getErrorMessage(), reader.getException());
        } else {
          // check id exists
          if (StringUtils.isBlank(record[ID_COLUMN_INDEX])) {
            recordsWithNoId++;
          }
          if (isOccurrenceFile(extFile)) {
            if (extFile.hasTerm(occurrenceId)) {
              lastId = validateIdentifier(record[sortColumnIndex], lastId, recordsWithNoOccurrenceId,
                recordsWithDuplicateOccurrenceId);
            }
            validateBasisOfRecord(record[basisOfRecordIndex], currRecords, recordsWithNoBasisOfRecord,
              recordsWithNonMatchingBasisOfRecord, recordsWithAmbiguousBasisOfRecord);
          }
        }
      }
    } catch (InterruptedException e) {
      // set last error report!
      setState(e);
      throw e;

    } catch (Exception e) {
      // some error validating this file, report
      log.error("Exception caught while validating extension file", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error while validating extension file occurred on line " + currRecords, e);

    } finally {
      // Exception on advancing cursor was encountered?
      if (!reader.hasRowError() && reader.getErrorMessage() != null) {
        writePublicationLogMessage("Error reading data: " + reader.getErrorMessage());
      }
      reader.close();
      // always cleanup the sorted file, it must not be included in the dwca directory when compressed
      FileUtils.deleteQuietly(sortedFile);
    }

    // some final reporting..
    if (recordsWithNoId > 0) {
      addMessage(Level.ERROR, recordsWithNoId
                              + " line(s) in extension missing an ID " + id.simpleName() + ", which is required when linking the extension record and core record together");
      throw new GeneratorException(
        "Can't validate DwC-A for resource " + resource.getShortname() + ". Each line in extension must have an ID " + id.simpleName() + ", which is required in order to link the extension to the core ");
    } else {
      addMessage(Level.INFO, "\u2713 Validated each line in extension has an ID " + id.simpleName());
      writePublicationLogMessage("No lines in extension are missing an ID " + id.simpleName());
    }

    if (isOccurrenceFile(extFile)) {
      if (extFile.hasTerm(occurrenceId)) {
        summarizeIdentifierValidation(recordsWithNoOccurrenceId, recordsWithDuplicateOccurrenceId,
          occurrenceId.simpleName());
      }
      summarizeBasisOfRecordValidation(recordsWithNoBasisOfRecord, recordsWithNonMatchingBasisOfRecord,
        recordsWithAmbiguousBasisOfRecord);
    }
  }

  /**
   * Validate the Archive's core data file has an ID for each row, and that each ID is unique. Perform this check
   * only if the core record ID term (e.g. occurrenceID, taxonID, etc) has actually been mapped.
   * </br>
   * If the core has rowType occurrence, validate the core data file has a basisOfRecord for each row, and
   * that each basisOfRecord matches the DwC Type Vocabulary.
   * </br>
   * If the core has rowType event, validate there are associated occurrences.
   *
   * @param coreFile core ArchiveFile
   * @param archiveHasExtensions true if Archive has extensions, false otherwise
   *
   * @throws GeneratorException   if validation was interrupted due to an error
   * @throws InterruptedException if the thread was interrupted
   * @throws java.io.IOException  if a problem occurred sorting core file, or opening iterator on it for example
   */
  private void validateCoreDataFile(ArchiveFile coreFile, boolean archiveHasExtensions) throws GeneratorException, InterruptedException, IOException {
    Objects.requireNonNull(resource.getCoreRowType());
    addMessage(Level.INFO, "Validating the core file: " + coreFile.getTitle()
                           + ". Depending on the number of records, this can take a while.");

    // get the core record ID term
    Term id = TERM_FACTORY.findTerm(AppConfig.coreIdTerm(resource.getCoreRowType()));
    Term basisOfRecord = TERM_FACTORY.findTerm(Constants.DWC_BASIS_OF_RECORD);

    int basisOfRecordIndex = -1;
    if (isOccurrenceFile(coreFile)) {
      // fail immediately if occurrence core doesn't contain basisOfRecord mapping
      if (!coreFile.hasTerm(basisOfRecord)) {
        addMessage(Level.ERROR,
          "Archive validation failed, because required term basisOfRecord was not mapped in the occurrence core");
        throw new GeneratorException("Can't validate DwC-A for resource " + resource.getShortname()
                                     + ". Required term basisOfRecord was not mapped in the occurrence core");
      }

      addMessage(Level.INFO, "? Validating the core basisOfRecord is always present and its "
                             + "value matches the Darwin Core Type Vocabulary.");

      // find index of basisOfRecord
      basisOfRecordIndex = coreFile.getField(basisOfRecord).getIndex();
    }

    // validate the core ID / record identifier (e.g. occurrenceID, taxonID) if it has been mapped
    if (coreFile.hasTerm(id) || archiveHasExtensions) {
      String msg = "? Validating the core ID field " + id.simpleName() + " is always present and unique.";
      if (archiveHasExtensions) {
        msg = msg + " Note: the core ID field is required to link core records and extension records together. ";
      }
      addMessage(Level.INFO, msg);
    }

    if (!coreFile.hasTerm(id)) {
      addMessage(Level.WARN, coreFile.getTitle() + " does not have the core ID field " + id.simpleName()
          + ". The data cannot be indexed on GBIF.");
    }

    // reporting
    currRecords = 0;
    currRecordsSkipped = 0;
    currExtension = coreFile.getTitle();

    // create a new core data file sorted by ID column 0
    File sortedCore = sortCoreDataFile(coreFile, ID_COLUMN_INDEX);

    // create an iterator on the new sorted core data file
    CSVReader reader = CSVReaderFactory
      .build(sortedCore, CHARACTER_ENCODING, coreFile.getFieldsTerminatedBy(), coreFile.getFieldsEnclosedBy(),
        coreFile.getIgnoreHeaderLines());

    // metrics
    AtomicInteger recordsWithNoId = new AtomicInteger(0);
    AtomicInteger recordsWithDuplicateId = new AtomicInteger(0);
    AtomicInteger recordsWithNoBasisOfRecord = new AtomicInteger(0);
    AtomicInteger recordsWithNonMatchingBasisOfRecord = new AtomicInteger(0);
    AtomicInteger recordsWithAmbiguousBasisOfRecord = new AtomicInteger(0);

    String lastId = null;
    try {
      while (reader.hasNext()) {
        currRecords++;
        if (currRecords % 1000 == 0) {
          checkForInterruption(currRecords);
          reportIfNeeded();
        }
        String[] record = reader.next();
        if (record == null || record.length == 0) {
          continue;
        }
        // Exception on reading row was encountered
        if (reader.hasRowError() && reader.getException() != null) {
          throw new GeneratorException(
            "A fatal error was encountered while trying to validate sorted core data file: " + reader.getErrorMessage(),
                  reader.getException());
        } else {
          // validate record id if it is mapped, or if archive has extensions (required to link core to extension)
          if (coreFile.hasTerm(id) || archiveHasExtensions) {
            lastId = validateIdentifier(record[ID_COLUMN_INDEX], lastId, recordsWithNoId, recordsWithDuplicateId);
          }
          if (isOccurrenceFile(coreFile)) {
            validateBasisOfRecord(record[basisOfRecordIndex], currRecords, recordsWithNoBasisOfRecord,
              recordsWithNonMatchingBasisOfRecord, recordsWithAmbiguousBasisOfRecord);
          }
        }
      }
    } catch (InterruptedException e) {
      // set last error report!
      setState(e);
      throw e;
    } catch (Exception e) {
      // some error validating this file, report
      log.error("Exception caught while validating archive", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error while validating archive occurred on line " + currRecords, e);
    } finally {
      // Exception on advancing cursor was encountered?
      if (!reader.hasRowError() && reader.getErrorMessage() != null) {
        writePublicationLogMessage("Error reading data: " + reader.getErrorMessage());
      }
      reader.close();
      // always cleanup the sorted file, it must not be included in the dwca directory when compressed
      FileUtils.deleteQuietly(sortedCore);
    }

    // some final reporting..
    if (coreFile.hasTerm(id) || archiveHasExtensions) {
      summarizeIdentifierValidation(recordsWithNoId, recordsWithDuplicateId, id.simpleName());
    }
    if (isOccurrenceFile(coreFile)) {
      summarizeBasisOfRecordValidation(recordsWithNoBasisOfRecord, recordsWithNonMatchingBasisOfRecord,
        recordsWithAmbiguousBasisOfRecord);
    }
  }

  /**
   * Check id exists, and check that the id is unique, using case insensitive comparison against another id,
   * e.g. FISHES:1 and fishes:1 are equal.
   *
   * @param id                     identifier value
   * @param lastId                 identifier value from last iteration
   * @param recordsWithNoId        number of records with no id so far
   * @param recordsWithDuplicateId number of records with duplicate ids so far
   *
   * @return identifier value
   */
  private String validateIdentifier(String id, String lastId, AtomicInteger recordsWithNoId, AtomicInteger recordsWithDuplicateId) {
    // check id exists
    if (StringUtils.isBlank(id)) {
      recordsWithNoId.getAndIncrement();
    }

    // check id is unique, using case insensitive comparison. E.g. FISHES:1 and fishes:1 are equal
    if (StringUtils.isNotBlank(lastId) && StringUtils.isNotBlank(id)) {
      if (id.equalsIgnoreCase(lastId)) {
        writePublicationLogMessage("Duplicate id found: " + id);
        recordsWithDuplicateId.getAndIncrement();
      }
    }
    // set so id gets compared on next iteration
    return id;
  }

  /**
   * Check basisOfRecord exists, and check basisOfRecord matches vocabulary (lower case comparison).
   * E.g. specimen matches Specimen are equal. Lastly, check basisOfRecord matches ambiguous "occurrence"
   * (lower case comparison).
   *
   * @param bor                                 basisOfRecord value
   * @param line                                line/record number
   * @param recordsWithNoBasisOfRecord          number of records with no basisOfRecord so far
   * @param recordsWithNonMatchingBasisOfRecord number of records with basisOfRecord not matching vocabulary so far
   * @param recordsWithAmbiguousBasisOfRecord   number of records with ambiguous basisOfRecord so far
   */
  private void validateBasisOfRecord(String bor, int line, AtomicInteger recordsWithNoBasisOfRecord,
    AtomicInteger recordsWithNonMatchingBasisOfRecord, AtomicInteger recordsWithAmbiguousBasisOfRecord) {
    // check basisOfRecord exists
    if (StringUtils.isBlank(bor)) {
      recordsWithNoBasisOfRecord.getAndIncrement();
    } else {
      // check basisOfRecord matches vocabulary (lower case comparison). E.g. specimen matches Specimen are equal
      if (!basisOfRecords.containsKey(bor.toLowerCase())) {
        writePublicationLogMessage("Line #" + line + " has basisOfRecord [" + bor
                                   + "] that does not match the Darwin Core Type Vocabulary");
        recordsWithNonMatchingBasisOfRecord.getAndIncrement();
      }
      // check basisOfRecord matches ambiguous "occurrence" (lower case comparison)
      else if (bor.equalsIgnoreCase("occurrence")) {
        recordsWithAmbiguousBasisOfRecord.getAndIncrement();
      }
    }
  }

  /**
   * Check if event core has an occurrence mapping, with at least one associated occurrence. Otherwise publish
   * warning message.
   *
   * @param arch Archive
   */
  private void validateEventCore(Archive arch) throws GeneratorException {
    boolean validEventCore = true;
    // test if occurrence extension mapped
    ArchiveFile occurrenceExtension = arch.getExtension(DwcTerm.Occurrence);
    if (occurrenceExtension == null) {
      validEventCore = false;
    }
    // test if it has at least one record
    else {
      if (!occurrenceExtension.iterator().hasNext()) {
        validEventCore = false;
      }
    }
    if (!validEventCore) {
      addMessage(Level.WARN, "The sampling event resource has no associated occurrences.");
    }
  }

  /**
   * Report basisOfRecord validation (shared by two methods 1. validateBasisOfRecord(ArchiveFile archiveFile)
   * 2. validateCoreDataFile(Archive arch).
   *
   * @param recordsWithNoBasisOfRecord          number of records with no basisOfRecord
   * @param recordsWithNonMatchingBasisOfRecord number of records with basisOfRecord not matching DwC Type Vocabulary
   * @param recordsWithAmbiguousBasisOfRecord   number of records with basisOfRecord equal to 'occurrence'
   *
   * @throws GeneratorException if validation threshold exceeded
   */
  private void summarizeBasisOfRecordValidation(AtomicInteger recordsWithNoBasisOfRecord,
    AtomicInteger recordsWithNonMatchingBasisOfRecord, AtomicInteger recordsWithAmbiguousBasisOfRecord)
    throws GeneratorException {
    // add empty BoR user message
    if (recordsWithNoBasisOfRecord.get() > 0) {
      addMessage(Level.ERROR, recordsWithNoBasisOfRecord + " line(s) are missing a basisOfRecord");
    } else {
      writePublicationLogMessage("No lines are missing a basisOfRecord");
    }

    // add non matching BoR user message
    if (recordsWithNonMatchingBasisOfRecord.get() > 0) {
      addMessage(Level.ERROR, recordsWithNonMatchingBasisOfRecord
                              + " line(s) have basisOfRecord that does not match the Darwin Core Type Vocabulary "
                              + "(please note comparisons are case insensitive)");
    } else {
      writePublicationLogMessage("All lines have basisOfRecord that matches the Darwin Core Type Vocabulary");
    }

    // add ambiguous BoR user message
    if (recordsWithAmbiguousBasisOfRecord.get() > 0) {
      addMessage(Level.WARN, recordsWithAmbiguousBasisOfRecord
                             + " line(s) use ambiguous basisOfRecord 'occurrence'. It is advised that occurrence be "
                             + "reserved for cases when the basisOfRecord is unknown. Otherwise, a more specific "
                             + "basisOfRecord should be chosen.");
    } else {
      writePublicationLogMessage("No lines have ambiguous basisOfRecord 'occurrence'.");
    }

    // if there was 1 or more records missing a basisOfRecord, or having a non matching basisOfRecord, validation fails
    if (recordsWithNoBasisOfRecord.get() == 0 && recordsWithNonMatchingBasisOfRecord.get() == 0) {
      addMessage(Level.INFO,
        "✓ Validated each line has a basisOfRecord, and each basisOfRecord matches the Darwin Core Type Vocabulary");
    } else {
      addMessage(Level.ERROR,
        "Archive validation failed, because not every row in the occurrence file(s) has a valid basisOfRecord "
        + "(please note all basisOfRecord must match Darwin Core Type Vocabulary, and comparisons are case "
        + "insensitive)");
      throw new GeneratorException("Can't validate DwC-A for resource " + resource.getShortname()
                                   + ". Each row in the occurrence file(s) must have a basisOfRecord, and each "
                                   + "basisOfRecord must match the Darwin Core Type Vocabulary (please note "
                                   + "comparisons are case insensitive)");
    }
  }

  /**
   * Report identifier validation (shared by two methods 1. validateOccurrenceDataFile(ArchiveFile archiveFile)
   * 2. validateCoreDataFile(Archive arch).
   *
   * @param recordsWithNoId        number of records with no id
   * @param recordsWithDuplicateId number of records with duplicate ids
   * @param term                   name of identifier term being validated
   *
   * @throws GeneratorException if validation threshold exceeded
   */
  private void summarizeIdentifierValidation(AtomicInteger recordsWithNoId, AtomicInteger recordsWithDuplicateId,
    String term) throws GeneratorException {
    // add empty ids user message
    if (recordsWithNoId.get() > 0) {
      addMessage(Level.ERROR, recordsWithNoId + " line(s) missing " + term);
    } else {
      writePublicationLogMessage("No lines are missing " + term);
    }

    // add duplicate ids user message
    if (recordsWithDuplicateId.get() > 0) {
      addMessage(Level.ERROR, recordsWithDuplicateId + " line(s) having a duplicate " + term
                              + " (please note comparisons are case insensitive)");
    } else {
      writePublicationLogMessage("No lines have duplicate " + term);
    }

    // if there was 1 or more records missing an ID, or having a duplicate ID, validation fails
    if (recordsWithNoId.get() == 0 && recordsWithDuplicateId.get() == 0) {
      addMessage(Level.INFO, "✓ Validated each line has a " + term + ", and each " + term + " is unique");
    } else {
      addMessage(Level.ERROR, "Archive validation failed, because not every line has a unique " + term
                              + " (please note comparisons are case insensitive)");
      throw new GeneratorException(
        "Can't validate DwC-A for resource " + resource.getShortname() + ". Each line must have a " + term
        + ", and each " + term + " must be unique (please note comparisons are case insensitive)");
    }
  }

  /**
   * @return true if the file has occurrence rowType.
   */
  private boolean isOccurrenceFile(ArchiveFile archiveFile) {
    return archiveFile.getRowType().equals(DwcTerm.Occurrence);
  }

  /**
   * @return true if the archive core file has event rowType.
   */
  private boolean isEventCore(Archive arch) {
    return arch.getCore().getRowType().equals(DwcTerm.Event);
  }

  /**
   * Method responsible for all stages of DwC-A file generation.
   * 
   * @return number of records published in core file
   * @throws GeneratorException if DwC-A generation fails for any reason
   */
  @Override
  public Map<String, Integer> call() throws Exception {
    try {
      checkForInterruption();
      setState(STATE.STARTED);

      // initial reporting
      addMessage(Level.INFO, "Archive generation started for version #" + resource.getEmlVersion());

      // create a temp dir to copy all dwca files to
      dwcaFolder = dataDir.tmpDir();
      archive = new Archive();

      // create data files
      createDataFiles();

      // copy eml file
      addEmlFile();

      // create meta.xml
      createMetaFile();

      // perform some validation, e.g. ensure all core record identifiers are present and unique
      validate();

      // zip archive and copy to resource folder
      bundleArchive();

      // reporting
      addMessage(Level.INFO, "Archive version #" + resource.getEmlVersion() + " generated successfully!");

      // set final state
      setState(STATE.COMPLETED);

      return recordsByExtension;
    } catch (GeneratorException e) {
      // set last error report!
      setState(e);

      // write exception to publication log file when IPT is in debug mode, otherwise just log it
      if (cfg.debug()) {
        writeFailureToPublicationLog(e);
      } else {
        log.error(
          "Exception occurred trying to generate Darwin Core Archive for resource " + resource.getTitleAndShortname()
          + ": " + e.getMessage(), e);
      }

      // rethrow exception, which gets wrapped in an ExecutionException and re caught when calling Future.get
      throw e;
    } catch (InterruptedException e) {
      setState(e);
      writeFailureToPublicationLog(e);
      throw e;
    } catch (Exception e) {
      setState(e);
      writeFailureToPublicationLog(e);
      throw new GeneratorException(e);
    } finally {
      // cleanup temp dir that was used to store dwca files
      if (dwcaFolder != null && dwcaFolder.exists()) {
        FileUtils.deleteQuietly(dwcaFolder);
      }
      // ensure publication log writer is closed
      closePublicationLogWriter();
    }
  }

  /**
   * Checks if the executing thread has been interrupted, i.e. DwC-A generation was cancelled.
   * 
   * @throws InterruptedException if the thread was found to be interrupted
   */
  private void checkForInterruption() throws InterruptedException {
    if (Thread.interrupted()) {
      StatusReport report = report();
      String msg = "Interrupting dwca generator. Last status: " + report.getState();
      log.info(msg);
      throw new InterruptedException(msg);
    }
  }

  /**
   * Checks if the executing thread has been interrupted, i.e. DwC-A generation was cancelled.
   * 
   * @param line number of lines currently processed at the time of the check
   * @throws InterruptedException if the thread was found to be interrupted
   */
  private void checkForInterruption(int line) throws InterruptedException {
    if (Thread.interrupted()) {
      StatusReport report = report();
      String msg = "Interrupting dwca generator at line " + line + ". Last status: " + report.getState();
      log.info(msg);
      throw new InterruptedException(msg);
    }
  }

  @Override
  protected boolean completed() {
    return STATE.COMPLETED == this.state;
  }

  /**
   * Create data files.
   * 
   * @throws GeneratorException if the resource had no core file that was mapped
   * @throws InterruptedException if the thread was interrupted
   */
  private void createDataFiles() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.DATAFILES);
    if (!resource.hasCore() || resource.getCoreRowType() == null
        || resource.getCoreMappings().get(0).getSource() == null) {
      throw new GeneratorException("Core is not mapped");
    }
    for (Extension ext : resource.getMappedExtensions()) {
      report();
      try {
        addDataFile(resource.getMappings(ext.getRowType()), null);
      } catch (IOException | IllegalArgumentException e) {
        throw new GeneratorException("Problem occurred while writing data file", e);
      }
    }
    // final reporting
    addMessage(Level.INFO, "All data files completed");
    report();
  }

  /**
   * Create meta.xml file.
   * 
   * @throws GeneratorException if meta.xml file creation failed
   * @throws InterruptedException if the thread was interrupted
   */
  private void createMetaFile() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.METADATA);
    try {
      MetaDescriptorWriter.writeMetaFile(new File(dwcaFolder, "meta.xml"), archive);
    } catch (IOException e) {
      throw new GeneratorException("Meta.xml file could not be written", e);
    }
    // final reporting
    addMessage(Level.INFO, "meta.xml archive descriptor written");
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.task.ReportingTask#currentException()
   */
  @Override
  protected Exception currentException() {
    return exception;
  }

  @Override
  protected String currentState() {
    switch (state) {
      case WAITING:
        return "Not started yet";
      case STARTED:
        return "Starting archive generation";
      case DATAFILES:
        return "Processing record " + currRecords + " for data file <em>" + currExtension + "</em>";
      case METADATA:
        return "Creating metadata files";
      case BUNDLING:
        return "Compressing archive";
      case COMPLETED:
        return "Archive generated!";
      case VALIDATING:
        return "Validating archive, " + currRecords + " for data file <em>" + currExtension + "</em>";
      case ARCHIVING:
        return "Archiving version of archive";
      case CANCELLED:
        return CANCELLED_STATE_MSG;
      case FAILED:
        return "Failed. Fatal error!";
      default:
        return "You should never see this";
    }
  }

  /**
   * Write data file for mapping.
   *
   * @param writer file writer for single data file
   * @param inCols index ordered list of all output columns apart from id column
   * @param mapping mapping
   * @param dataFileRowSize number of columns in data file
   * @param rowLimit maximum number of rows to write
   * @throws GeneratorException if there was an error writing data file for mapping.
   * @throws InterruptedException if the thread was interrupted
   */
  private void dumpData(Writer writer, PropertyMapping[] inCols, ExtensionMapping mapping, int dataFileRowSize,
    @Nullable Integer rowLimit, @Nullable DOI doi)
    throws GeneratorException, InterruptedException {
    final String idSuffix = StringUtils.trimToEmpty(mapping.getIdSuffix());
    final RecordFilter filter = mapping.getFilter();
    // get maximum column index to check incoming rows for correctness
    int maxColumnIndex = mapping.getIdColumn() == null ? -1 : mapping.getIdColumn();
    for (PropertyMapping pm : mapping.getFields()) {
      if (pm.getIndex() != null && maxColumnIndex < pm.getIndex()) {
        maxColumnIndex = pm.getIndex();
      }
    }

    int recordsWithError = 0;
    int linesWithWrongColumnNumber = 0;
    int recordsFiltered = 0;
    int emptyLines = 0;
    ClosableReportingIterator<String[]> iter = null;
    int line = 0;
    try {
      // get the source iterator
      iter = sourceManager.rowIterator(mapping.getSource());

      while (iter.hasNext()) {
        line++;
        if (line % 1000 == 0) {
          checkForInterruption(line);
          reportIfNeeded();
        }
        String[] in = iter.next();
        if (in == null || in.length == 0) {
          continue;
        }

        // Exception on reading row was encountered, meaning record is incomplete and not written
        if (iter.hasRowError()) {
          writePublicationLogMessage("Error reading line #" + line + "\n" + iter.getErrorMessage());
          recordsWithError++;
          currRecordsSkipped++;
        }
        // empty line was encountered, meaning record only contains empty values and not written
        else if (isEmptyLine(in)) {
          writePublicationLogMessage("Empty line was skipped. SourceBase:"
                                     + mapping.getSource().getName() + " Line #" + line + ": " + printLine(in));
          emptyLines++;
          currRecordsSkipped++;
        } else {

          if (in.length <= maxColumnIndex) {
            writePublicationLogMessage("Line with fewer columns than mapped. SourceBase:"
              + mapping.getSource().getName()
              + " Line #" + line + " has " + in.length + " Columns: " + printLine(in));
            // input row is smaller than the highest mapped column. Resize array by adding nulls
            String[] in2 = new String[maxColumnIndex + 1];
            System.arraycopy(in, 0, in2, 0, in.length);
            in = in2;
            linesWithWrongColumnNumber++;
          }

          String[] record = new String[dataFileRowSize];

          // filter this record?
          boolean alreadyTranslated = false;
          if (filter != null && filter.getColumn() != null && filter.getComparator() != null
            && filter.getParam() != null) {
            boolean matchesFilter;
            if (filter.getFilterTime() == RecordFilter.FilterTime.AfterTranslation) {
              applyTranslations(inCols, in, record, mapping.isDoiUsedForDatasetId(), doi);
              matchesFilter = filter.matches(in);
              alreadyTranslated = true;
            } else {
              matchesFilter = filter.matches(in);
            }
            if (!matchesFilter) {
              writePublicationLogMessage("Line did not match the filter criteria and was skipped. SourceBase:"
                + mapping.getSource().getName() + " Line #" + line + ": " + printLine(in));
              recordsFiltered++;
              continue;
            }
          }

          // add id column - either an existing column or the line number
          if (mapping.getIdColumn() == null) {
            record[ID_COLUMN_INDEX] = null;
          } else if (mapping.getIdColumn().equals(ExtensionMapping.IDGEN_LINE_NUMBER)) {
            record[ID_COLUMN_INDEX] = line + idSuffix;
          } else if (mapping.getIdColumn().equals(ExtensionMapping.IDGEN_UUID)) {
            record[ID_COLUMN_INDEX] = UUID.randomUUID().toString();
          } else if (mapping.getIdColumn() >= 0) {
            record[ID_COLUMN_INDEX] = StringUtils.isBlank(in[mapping.getIdColumn()]) ? idSuffix
              : in[mapping.getIdColumn()] + idSuffix;
          }

          // go through all archive fields
          if (!alreadyTranslated) {
            applyTranslations(inCols, in, record, mapping.isDoiUsedForDatasetId(), doi);
          }
          String newRow = tabRow(record);
          if (newRow != null) {
            writer.write(newRow);
            currRecords++;
            // don't exceed row limit (e.g. only want to write X number of rows used to preview first X rows of file)
            if (rowLimit != null && currRecords >= rowLimit) {
              break;
            }
          }
        }
      }
    } catch (InterruptedException e) {
      // set last error report!
      setState(e);
      throw e;
    } catch (Exception e) {
      // some error writing this file, report
      log.error("Fatal DwC-A Generator Error encountered", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error writing data file for mapping " + mapping.getExtension().getTitle()
        + " in source " + mapping.getSource().getName() + ", line " + line, e);
    } finally {
      if (iter != null) {
        // Exception on advancing cursor encountered?
        if (!iter.hasRowError() && iter.getErrorMessage() != null) {
          writePublicationLogMessage("Error reading data: " + iter.getErrorMessage());
        }
        try {
          iter.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    // common message part used in constructing all reporting messages below
    String mp = " for mapping " + mapping.getExtension().getTitle() + " in source " + mapping.getSource().getName();

    // add lines incomplete message
    if (recordsWithError > 0) {
      addMessage(Level.WARN, recordsWithError + " record(s) skipped due to errors" + mp);
    } else {
      writePublicationLogMessage("No lines were skipped due to errors" + mp);
    }

    // add empty lines message
    if (emptyLines > 0) {
      addMessage(Level.WARN, emptyLines + " empty line(s) skipped" + mp);
    } else {
      writePublicationLogMessage("No lines were skipped due to errors" + mp);
    }

    // add wrong lines user message
    if (linesWithWrongColumnNumber > 0) {
      addMessage(Level.WARN, linesWithWrongColumnNumber + " line(s) with fewer columns than mapped" + mp);
    } else {
      writePublicationLogMessage("No lines with fewer columns than mapped" + mp);
    }

    // add filter message
    if (recordsFiltered > 0) {
      addMessage(Level.INFO, recordsFiltered
        + " line(s) did not match the filter criteria and got skipped " + mp);
    } else {
      writePublicationLogMessage("All lines match the filter criteria" + mp);
    }
  }

  /**
   * Sets an exception and state of the worker to FAILED. The final StatusReport is generated at the end.
   * 
   * @param e exception
   */
  private void setState(Exception e) {
    exception = e;
    state = (exception instanceof InterruptedException) ? STATE.CANCELLED : STATE.FAILED;
    report();
  }

  /**
   * Sets only the state of the worker. The final StatusReport is generated at the end.
   * 
   * @param s STATE of worker
   */
  private void setState(STATE s) {
    state = s;
    report();
  }

  /**
   * Generates a single tab delimited row from the list of values of the provided array.
   * </br>
   * Note all line breaking characters in the value get replaced with an empty string before its added to the row.
   * </br>
   * The row ends in a newline character.
   *
   * @param columns the array of values to join together, may not be null
   *
   * @return the tab delimited String, {@code null} if provided array only contained null values
   */
  protected String tabRow(String[] columns) {
    Objects.requireNonNull(columns);
    boolean empty = true;
    for (int i = 0; i < columns.length; i++) {
      if (columns[i] != null) {
        empty = false;
        columns[i] = StringUtils.trimToNull(escapeChars.matcher(columns[i]).replaceAll(" "));
      }
    }
    if (empty) {
      return null;
    }
    return StringUtils.join(columns, '\t') + "\n";
  }

  /**
   * Apply translations or default values to row, for all mapped properties.
   * </br>
   * The method starts by iterating through all mapped properties, checking each one if it has been translated or a
   * default value provided. The original value in the row is then replaced with the translated or default value.
   * A record array representing the values to be written to the data file is also updated.
   *
   * @param inCols values array, of columns in row that have been mapped
   * @param in values array, of all columns in row
   * @param doiUsedForDatasetId true if mapping should use resource DOI as datasetID, false otherwise
   * @param doi DOI assigned to resource
   */
  private void applyTranslations(PropertyMapping[] inCols, String[] in, String[] record, boolean doiUsedForDatasetId,
    DOI doi) {
    for (int i = 1; i < inCols.length; i++) {
      PropertyMapping pm = inCols[i];
      String val = null;
      if (pm != null) {
        if (pm.getIndex() != null) {
          val = in[pm.getIndex()];
          // translate value?
          if (pm.getTranslation() != null && pm.getTranslation().containsKey(val)) {
            val = pm.getTranslation().get(val);
            // update value in original record
            in[pm.getIndex()] = val;
          }
        }
        // use default value for null values
        if (val == null) {
          val = pm.getDefaultValue();
        }
        // use DOI for datasetID property?
        if (pm.getTerm().qualifiedName().equalsIgnoreCase(Constants.DWC_DATASET_ID) && doiUsedForDatasetId
            && doi != null) {
          val = doi.getDoiString();
        }
      }
      // add value to data file record
      record[i] = val;
    }
  }

  /**
   * Print a line representation of a string array used for logging.
   * 
   * @param in String array
   * @return line
   */
  private String printLine(String[] in) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < in.length; i++) {
      sb.append(in[i]);
      if (i != in.length - 1) {
        sb.append("; ");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Write message from exception to publication log file as a new line but suffocate any exception thrown.
   * 
   * @param e exception to write message from
   */
  private void writeFailureToPublicationLog(Throwable e) {
    StringBuilder sb = new StringBuilder();
    sb.append("Archive generation failed!\n");

    // write exception as nicely formatted string
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    sb.append(sw);

    // write to publication log file
    writePublicationLogMessage(sb.toString());
  }

  /**
   * First we need to find the union of all terms mapped (in all files) for a single Extension. Then make each mapped
   * term a field in the final archive. Static/default mappings are not stored for a field, since they are not
   * expressed in meta.xml but instead get written to the data file.
   * 
   * @param mappings list of ExtensionMapping
   * @param af ArchiveFile
   *
   * @return set of conceptTerms that have been mapped (in all files) for a single Extension
   */
  private Set<Term> addFieldsToArchive(List<ExtensionMapping> mappings, ArchiveFile af) throws GeneratorException{

    Set<Term> mappedConceptTerms = new HashSet<>();
    for (ExtensionMapping m : mappings) {
      // multi-value field delimiter, part of each source data configuration
      String delimitedBy = StringUtils.trimToNull(m.getSource().getMultiValueFieldsDelimitedBy());

      for (PropertyMapping pm : m.getFields()) {
        Term term = TERM_FACTORY.findTerm(pm.getTerm().qualifiedName());
        // ensure Extension has concept term
        if (term != null && m.getExtension().getProperty(term) != null) {
          if (af.hasTerm(term)) {
            ArchiveField field = af.getField(term);
            mappedConceptTerms.add(term);

            // multi-value delimiter must be same across all sources
            if (field.getDelimitedBy() != null && !field.getDelimitedBy().equals(delimitedBy)) {
              throw new GeneratorException(
                "More than one type of multi-value field delimiter is being used in the source files mapped to the "
                + m.getExtension().getName()
                + " extension. Please either ensure all source files mapped to this extension use the same delimiter, otherwise just leave the delimiter blank.");
            }
          } else {
            if ((pm.getIndex() != null && pm.getIndex() >= 0) || pm.getIndex() == null) {
              log.debug(
                "Handling property mapping for term: " + term.qualifiedName() + " (index " + pm.getIndex() + ")");
              af.addField(buildField(term, delimitedBy));
              mappedConceptTerms.add(term);
            }
          }
        }
      }

      // if Extension has datasetID concept term, check if resource DOI should be used as value for mapping
      ExtensionProperty ep = m.getExtension().getProperty(DwcTerm.datasetID.qualifiedName());
      if (ep != null && m.isDoiUsedForDatasetId()) {
        log.debug("Detected that resource DOI to be used as value for datasetID mapping..");
        // include datasetID field in ArchiveFile
        ArchiveField f = buildField(DwcTerm.datasetID, null);
        af.addField(f);
        // include datasetID field mapping in ExtensionMapping
        PropertyMapping pm = new PropertyMapping(f);
        pm.setTerm(ep);
        m.getFields().add(pm);
        // include datasetID in set of all terms mapped for Extension
        mappedConceptTerms.add(DwcTerm.datasetID);
      }
    }
    return mappedConceptTerms;
  }

  /**
   * Iterate through ordered list of those ExtensionProperty that have been mapped, and reassign the ArchiveFile
   * ArchiveField indexes, based on the order of their appearance in the ordered list be careful to reserve index 0 for
   * the ID column
   * 
   * @param propertyList ordered list of those ExtensionProperty that have been mapped
   * @param af ArchiveFile
   */
  private void assignIndexesOrderedByExtension(List<ExtensionProperty> propertyList, ArchiveFile af) {
    for (int propertyIndex = 0; propertyIndex < propertyList.size(); propertyIndex++) {
      ExtensionProperty extensionProperty = propertyList.get(propertyIndex);
      // retrieve the dwc-api Term corresponding to ExtensionProperty
      Term term = TERM_FACTORY.findTerm(extensionProperty.getQualname());
      // lookup ArchiveField using dwc-api Term
      ArchiveField f = af.getField(term);
      if (f != null && f.getIndex() == null) {
        // create new field index corresponding to its position in ordered list of columns indexed
        // +1 because index 0 is reserved for ID column
        int fieldIndex = propertyIndex + 1;
        // assign ArchiveField new index so that meta.xml file mirrors the ordered field order
        f.setIndex(fieldIndex);
      } else {
        log.warn("Skipping ExtensionProperty: " + extensionProperty.getQualname());
      }
    }
  }

  /**
   * Retrieve the ordered list of all Extension's mapped ExtensionProperty. Ordering is done according to Extension.
   * 
   * @param ext Extension
   * @param mappedConceptTerms set of all mapped ConceptTerm
   * @return ordered list of mapped ExtensionProperty
   */
  private List<ExtensionProperty>
    getOrderedMappedExtensionProperties(Extension ext, Set<Term> mappedConceptTerms) {
    // start with all Extension's ExtensionProperty, in natural order
    List<ExtensionProperty> propertyList = new ArrayList<>(ext.getProperties());

    // matching (below) should be done on the qualified Normalised Name
    Set<String> names = new HashSet<>();
    for (Term conceptTerm : mappedConceptTerms) {
      names.add(conceptTerm.qualifiedName());
    }

    // remove all ExtensionProperty that have not been mapped, leaving the ordered list of those that have been
    for (Iterator<ExtensionProperty> iterator = propertyList.iterator(); iterator.hasNext();) {
      ExtensionProperty extensionProperty = iterator.next();
      if (extensionProperty.qualifiedName() != null) {
        if (!names.contains(extensionProperty.qualifiedName())) {
          iterator.remove();
        }
      }
    }
    return propertyList;
  }

  /**
   * This method checks whether a competing file name exists in the folder where DwC-A files are written to.
   * If a competing file name exists, a numerical suffix is appended to the file name, to differentiate it from the
   * existing files' names. The numerical suffix is incrementing, and is equal to the number of existing files with
   * this name.
   * </br>
   * E.g. the initial name has no suffix (taxon.txt), but subsequent names look like (taxon2.txt, taxon3.txt, etc).
   *
   * Before IPT v2.2 the DwC-A file name has been determined from the extension name. When two extensions had the same
   * name, this caused one file to be overwritten - see Issue 1087.
   *
   * @param dwcaFolder folder where DwC-A files are written to
   * @param extensionName name of extension writing file for
   *
   * @return name of file for DwC-A file to be written
   */
  protected String createFileName(File dwcaFolder, String extensionName) {
    String wildcard = extensionName + WILDCARD_CHARACTER + TEXT_FILE_EXTENSION;
    FileFilter fileFilter = new WildcardFileFilter(wildcard, IOCase.INSENSITIVE);
    File[] files = dwcaFolder.listFiles(fileFilter);
    if (files.length > 0) {
      int max = 1;
      String fileName = null;
      for (File file: files) {
        try {
          fileName = file.getName();
          int suffixEndIndex = fileName.indexOf(TEXT_FILE_EXTENSION);
          String suffix = file.getName().substring(extensionName.length(), suffixEndIndex);
          int suffixInt = Integer.parseInt(suffix);
          if (suffixInt >= max) {
            max = suffixInt;
          }
        } catch (NumberFormatException e) {
          log.debug("No numerical suffix could be parsed from file name: " + StringUtils.trimToEmpty(fileName));
        }
      }
      return extensionName + (max + 1) + TEXT_FILE_EXTENSION;
    }
    return extensionName + TEXT_FILE_EXTENSION;
  }

  /**
   * Required for preview mapping feature, on manage resource page.
   *
   * @param dwcaFolder DwC-A directory
   */
  public void setDwcaFolder(File dwcaFolder) {
    this.dwcaFolder = dwcaFolder;
  }

  /**
   * Required for preview mapping feature, on manage resource page.
   *
   * @param archive DwC Archive
   */
  public void setArchive(Archive archive) {
    this.archive = archive;
  }

  /**
   * Check if each string in array is empty. Method joins each string together and then checks if it is blank. A
   * blank string represents an empty line in a source data file.
   *
   * @param line string array
   *
   * @return true if each string in array is empty, false otherwise
   */
  private boolean isEmptyLine(String[] line) {
    String joined = Arrays.stream(line)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(""));
    return StringUtils.isBlank(joined);
  }
}
