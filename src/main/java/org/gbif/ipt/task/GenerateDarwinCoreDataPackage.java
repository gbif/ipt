/*
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

import org.gbif.datapackage.DataPackage;
import org.gbif.datapackage.DataPackageConstants;
import org.gbif.datapackage.DataPackageResource;
import org.gbif.dp.analysis.api.AnalysisExecution;
import org.gbif.dp.analysis.api.DatapackageAnalysisResult;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.DataPackageTableSchemaRequirement;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.utils.DocBookLanguages;
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.ipt.utils.LicenseConverterUtils;
import org.gbif.ipt.utils.MetadataUtils;
import org.gbif.metadata.eml.ipt.model.Agent;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.code.externalsorting.ExternalSort;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.gbif.ipt.config.Constants.DATA_PACKAGE_EXTENSION;
import static org.gbif.ipt.config.Constants.DATA_PACKAGE_NAME;

public class GenerateDarwinCoreDataPackage extends ReportingTask implements Callable<Map<String, Long>> {

  private enum STATE {
    WAITING, STARTED, DATARESOURCES, METADATA, BUNDLING, COMPLETED, ARCHIVING, VALIDATING, CANCELLED, FAILED
  }

  private static final Pattern ESCAPE_CHARS = Pattern.compile("[\t\n\r]");

  // DwC-DP table schema keys on identifiers that may point outside the dataset
  private static final String WEAK_PRIMARY_KEY = "weakPrimaryKey";
  private static final String WEAK_FOREIGN_KEYS = "weakForeignKeys";

  private static final ObjectMapper MAPPER = JsonMapper.builder()
      .addModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .build();

  private final Resource resource;
  private final SourceManager sourceManager;
  private final DataPackageSchemaManager schemaManager;
  private final AppConfig cfg;
  private STATE state = STATE.WAITING;
  // step generation failed or was cancelled at
  private STATE failedStep;
  private Exception exception;
  private File dataPackageFolder;
  private DataPackage dataPackage;
  private long currRecords = 0;
  private long currRecordsSkipped = 0;
  private String currTableSchema;
  // record counts by extension <rowType, count>
  private Map<String, Long> recordsByTableSchema = new HashMap<>();

  public GenerateDarwinCoreDataPackage(
      Resource resource,
      ReportHandler handler,
      DataDir dataDir,
      SourceManager sourceManager,
      DataPackageSchemaManager schemaManager,
      AppConfig cfg) {
    super(1000, resource.getShortname(), handler, dataDir);
    this.resource = resource;
    this.sourceManager = sourceManager;
    this.schemaManager = schemaManager;
    this.cfg = cfg;
  }

  @Override
  public Map<String, Long> call() throws Exception {
    try {
      checkForInterruption();
      setState(STATE.STARTED);

      // initial reporting
      addMessage(Level.INFO, "Data Package generation started for version #" + resource.getDataPackageMetadataVersion());

      // the validation report of a previous publication must not pass for this one's
      FileUtils.deleteQuietly(dataDir.resourceDataPackageValidationReportFile(resource.getShortname()));

      // create a temp dir to copy all files to
      dataPackageFolder = dataDir.tmpDir();

      // copy datapackage descriptor file (datapackage.json)
      addMetadata();
      // create data resources
      createDataResources();

      // validate archive
      validateArchive();

      // zip archive
      File zip = bundleArchive();

      // copy zip to resource folder
      archive(zip);

      // reporting
      addMessage(Level.INFO, "Archive version #" + resource.getDataPackageMetadataVersion() + " generated successfully!");

      // set final state
      setState(STATE.COMPLETED);

      return recordsByTableSchema;
    } catch (GeneratorException e) {
      addFailureMessage(e);
      // set the last error report!
      setState(e);

      // write exception to publication log file when IPT is in debug mode, otherwise just log it
      if (cfg.debug()) {
        writeFailureToPublicationLog(e);
      } else {
        log.error(
            "Exception occurred trying to generate data package for resource " + resource.getTitleAndShortname()
                + ": " + e.getMessage(), e);
      }

      // rethrow exception, which gets wrapped in an ExecutionException and re caught when calling Future.get
      throw e;
    } catch (InterruptedException e) {
      setState(e);
      writeFailureToPublicationLog(e);
      throw e;
    } catch (Exception e) {
      addFailureMessage(e);
      setState(e);
      writeFailureToPublicationLog(e);
      throw new GeneratorException(e);
    } finally {
      // cleanup temp dir that was used to store data package files
      if (dataPackageFolder != null && dataPackageFolder.exists()) {
        FileUtils.deleteQuietly(dataPackageFolder);
      }
      // ensure publication log writer is closed
      closePublicationLogWriter();
    }
  }

  /**
   * Adds the root cause of a failure to the messages, so it shows on the publication page and in the publication log,
   * also outside debug mode.
   */
  private void addFailureMessage(Exception e) {
    addMessage(Level.ERROR, "Data Package generation failed: " + rootCauseMessage(e));
  }

  static String rootCauseMessage(Throwable e) {
    Throwable rootCause = ExceptionUtils.getRootCause(e);
    if (rootCause == null) {
      rootCause = e;
    }
    return StringUtils.defaultIfBlank(rootCause.getMessage(), rootCause.getClass().getSimpleName());
  }

  /**
   * Sets the folder data resources are written to. Normally created in {@link #call()}, exposed for tests only.
   */
  void setDataPackageFolder(File dataPackageFolder) {
    this.dataPackageFolder = dataPackageFolder;
  }

  /**
   * Data package being generated, exposed for tests only.
   */
  DataPackage getDataPackage() {
    return dataPackage;
  }

  /**
   * Sets the data package being generated, exposed for tests only.
   */
  void setDataPackage(DataPackage dataPackage) {
    this.dataPackage = dataPackage;
  }

  @Override
  protected boolean completed() {
    return STATE.COMPLETED == this.state;
  }

  @Override
  protected Exception currentException() {
    return exception;
  }

  @Override
  protected String currentState() {
    return switch (state) {
      case WAITING -> "Not started yet";
      case STARTED -> "Starting Data Package generation";
      case DATARESOURCES -> "Processing record " + currRecords + " for Data Resource <em>" + currTableSchema + "</em>";
      case METADATA -> "Creating metadata files";
      case BUNDLING -> "Compressing Data Package (archive)";
      case COMPLETED -> "Data Package generated!";
      case VALIDATING -> "Validating Data Package";
      case ARCHIVING -> "Archiving version of data package";
      case CANCELLED -> "Data Package generation cancelled";
      case FAILED -> "Data Package generation failed";
    };
  }

  @Override
  protected String currentStep() {
    return failedStep != null ? failedStep.name() : state.name();
  }

  void validateArchive() throws Exception {
    checkForInterruption();
    setState(STATE.VALIDATING);

    AnalysisExecution<DatapackageAnalysisResult> validationResult;
    try {
      validationResult = dataPackage.validate(dataPackageFolder);
    } catch (IOException | RuntimeException e) {
      // the analyser crashed: no validation report
      throw new GeneratorException("Data package validation could not be run: " + rootCauseMessage(e), e);
    }

    // persist the report to the permanent resource dir - dataPackageFolder is a temp
    // dir that gets wiped in call()'s finally block, so it can't live there
    writeValidationReport(validationResult);

    boolean hasViolations = validationResult.result().resourceAnalysisResults().stream().anyMatch(r ->
        !r.foreignKeyViolations().isEmpty()
            || r.primaryKeyViolation() != null
            || !r.dataTypeViolations().isEmpty())
        || !validationResult.result().emlValidation().valid()
        || !validationResult.result().descriptorValidation().valid();

    if (hasViolations) {
      addMessage(Level.ERROR, "Data package failed validation, see validation report for details");
      throw new GeneratorException("Data package validation failed for resource " + resource.getShortname());
    }

    addMessage(Level.INFO, "Data package validated successfully");
  }

  /**
   * Zips the data package folder. A temp version is created first, and when successful, it's moved into the resource's
   * data directory in {@link #archive(File)}.
   *
   * @throws GeneratorException if the data package could not be zipped
   * @throws InterruptedException if executing thread was interrupted
   */
  private File bundleArchive() throws Exception {
    checkForInterruption();
    setState(STATE.BUNDLING);
    File zip = dataDir.tmpFile(DATA_PACKAGE_NAME, DATA_PACKAGE_EXTENSION);
    try {
      dataPackage.write(zip, dataPackageFolder, this::writeEMLMetadata);

      if (!zip.exists()) {
        throw new GeneratorException("Archive bundling failed: temp archive not created: " + zip.getAbsolutePath());
      }
    } catch (IOException e) {
      if (zip.exists()) {
        FileUtils.deleteQuietly(zip);
      }
      throw new GeneratorException("Problem occurred while bundling data package", e);
    }
    addMessage(Level.INFO, "Archive has been compressed");
    return zip;
  }

  /**
   * Moves the zip package into the resource's data directory.
   *
   * @throws GeneratorException if the data package could not be moved
   * @throws InterruptedException if executing thread was interrupted
   */
  private void archive(File zip) throws Exception {
    checkForInterruption();
    setState(STATE.ARCHIVING);
    BigDecimal version = resource.getDataPackageMetadataVersion();
    try {
      File versionedFile = dataDir.resourceDataPackageFile(resource.getShortname(), version);
      if (versionedFile.exists()) {
        FileUtils.forceDelete(versionedFile);
      }
      FileUtils.moveFile(zip, versionedFile);
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while archiving data package version", e);
    } finally {
      if (zip.exists()) {
        FileUtils.deleteQuietly(zip);
      }
    }
    addMessage(Level.INFO, "Archive version #" + version + " saved");
  }

  private void writeValidationReport(AnalysisExecution<DatapackageAnalysisResult> results) {
    try {
      File reportFile = dataDir.resourceDataPackageValidationReportFile(resource.getShortname());
      MAPPER.writeValue(reportFile, results);
    } catch (IOException e) {
      // don't let a report-writing failure mask the real validation outcome, but don't hide it either
      log.error("Failed to write data package validation report", e);
      addMessage(Level.WARN, "Could not write validation report: " + e.getMessage());
    }
  }

  /**
   * Apart from a standard frictionless metadata, DwCA v2 occurrence must contain a EML file.
   */
  private void writeEMLMetadata(Path outputDir) {
    Path target = outputDir.getFileSystem().getPath("eml.xml");
    try {
      File sourceFile = dataDir.resourceEmlFile(resource.getShortname());
      Path sourcePath = sourceFile.toPath();
      Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error("Failed to write eml.xml", e);
      addMessage(Level.ERROR, "Failed to write eml.xml");
    }
  }

  /**
   * Checks if the executing thread has been interrupted, i.e. generation was cancelled.
   *
   * @throws InterruptedException if the thread was found to be interrupted
   */
  private void checkForInterruption() throws InterruptedException {
    if (Thread.interrupted()) {
      StatusReport report = report();
      String msg = "Interrupting data package generator. Last status: " + report.getState();
      log.info(msg);
      throw new InterruptedException(msg);
    }
  }

  /**
   * Checks if the executing thread has been interrupted, i.e. package generation was cancelled.
   *
   * @param line number of lines currently processed at the time of the check
   * @throws InterruptedException if the thread was found to be interrupted
   */
  private void checkForInterruption(long line) throws InterruptedException {
    if (Thread.interrupted()) {
      StatusReport report = report();
      String msg = "Interrupting package generator at line " + line + ". Last status: " + report.getState();
      log.info(msg);
      throw new InterruptedException(msg);
    }
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
   * Sets an exception and state of the worker to FAILED. The final StatusReport is generated at the end.
   *
   * @param e exception
   */
  private void setState(Exception e) {
    // keep the first failure and the step that was running then, the most specific; later calls are for its wrappers
    if (exception == null) {
      failedStep = state;
      exception = e;
    }
    state = (exception instanceof InterruptedException) ? STATE.CANCELLED : STATE.FAILED;
    report();
  }

  /**
   * Create data files.
   *
   * @throws GeneratorException if the resource had no core file that was mapped
   * @throws InterruptedException if the thread was interrupted
   */
  private void createDataResources() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.DATARESOURCES);
    if (CollectionUtils.isEmpty(resource.getDataPackageMappings())) {
      throw new GeneratorException("Data package mappings are not set");
    }

    List<DataPackageMapping> allMappings = resource.getDataPackageMappings();
    Set<String> mappedTableSchemas = allMappings.stream()
        .map(DataPackageMapping::getDataPackageTableSchemaName)
        .map(DataPackageTableSchemaName::getName)
        .collect(Collectors.toSet());
    DataPackageSchema dataPackageSchema = resource.getDataPackageMappings().get(0).getDataPackageSchema();

    // before starting to add a table schema, check all required schemas mapped
    checkRequiredTableSchemasMapped(mappedTableSchemas, dataPackageSchema);

    checkMappingsResolved(allMappings, dataPackageSchema);

    for (DataPackageTableSchema tableSchema : dataPackageSchema.getTableSchemas()) {
      // skip unmapped (optional) schemas
      if (!mappedTableSchemas.contains(tableSchema.getName())) {
        continue;
      }

      report();
      try {
        addDataResource(tableSchema, allMappings, true);
      } catch (IOException | IllegalArgumentException e) {
        throw new GeneratorException("Problem occurred while writing Data Resource", e);
      }
    }

    // final reporting
    addMessage(Level.INFO, "All Data Resources completed");
    report();
  }

  /**
   * Fails if a mapping refers to a table not in the installed schema (anymore), its data would be left out. Mapped
   * fields not in the installed schema are left out with a warning: they may be kept on purpose until reviewed.
   *
   * @param allMappings mappings of the resource
   * @param dataPackageSchema installed schema
   */
  void checkMappingsResolved(List<DataPackageMapping> allMappings, DataPackageSchema dataPackageSchema)
      throws GeneratorException {
    List<String> missingTables = allMappings.stream()
        .filter(DataPackageMapping::isTableSchemaMissing)
        .map(DataPackageMapping::getLabel)
        .toList();
    if (!missingTables.isEmpty()) {
      String msg = "Mappings refer to tables not in " + dataPackageSchema.getName() + " v" + dataPackageSchema.getVersion()
          + ", delete or remap them: " + String.join(", ", missingTables);
      addMessage(Level.ERROR, msg);
      throw new GeneratorException(msg);
    }

    for (DataPackageMapping mapping : allMappings) {
      List<String> unresolvedFields = mapping.getUnresolvedFieldNames();
      if (!unresolvedFields.isEmpty()) {
        addMessage(Level.WARN, "Mapping " + mapping.getLabel() + ": fields not in " + dataPackageSchema.getName() + " v"
            + dataPackageSchema.getVersion() + " are not published: " + String.join(", ", unresolvedFields));
      }
    }
  }

  /**
   * Checks if all required schemas mapped, otherwise throws an exception.
   *
   * @param mappedTableSchemas mapped table schemas
   * @param dataPackageSchema data schema
   */
  private void checkRequiredTableSchemasMapped(Set<String> mappedTableSchemas, DataPackageSchema dataPackageSchema)
      throws GeneratorException {
    DataPackageTableSchemaRequirement requirements = dataPackageSchema.getTableSchemasRequirements();

    if (requirements != null) {
      DataPackageTableSchemaRequirement.ValidationResult validationResult = requirements.validate(mappedTableSchemas);

      if (!validationResult.isValid()) {
        throw new GeneratorException(validationResult.getReason());
      }
    }
  }

  /**
   * Adds a single data resource for a tableSchema mapping.
   *
   * @throws IllegalArgumentException if not all mappings are mapped to the same extension
   * @throws InterruptedException if the thread was interrupted
   * @throws IOException if problems occurred while persisting new data resources
   * @throws GeneratorException if any problem was encountered writing data resources
   */
  public void addDataResource(DataPackageTableSchema tableSchema, List<DataPackageMapping> allMappings,
                              boolean filterMapped) throws IOException,
      IllegalArgumentException, InterruptedException, GeneratorException {
    checkForInterruption();
    if (tableSchema == null || CollectionUtils.isEmpty(allMappings)) {
      return;
    }

    // update reporting
    currRecords = 0;
    currRecordsSkipped = 0;
    currTableSchema = tableSchema.getName();

    List<DataPackageField> fields = tableSchema.getFields();
    Set<String> mappedFieldNames = new HashSet<>();
    // output columns (field names in table schema order), shared by all mappings of this table schema
    List<String> columns;

    if (filterMapped) {
      for (DataPackageMapping dataPackageMapping : allMappings) {
        if (dataPackageMapping.getDataPackageTableSchemaName().getName().equals(tableSchema.getName())) {
          // filter those that are mapped or have a default value
          dataPackageMapping.getFields().stream()
              .filter(DataPackageFieldMapping::isMapped)
              .forEach(dpfm -> {
                if (dpfm.getField() != null && dpfm.getField().getName() != null) {
                  mappedFieldNames.add(dpfm.getField().getName());
                } else {
                  log.error("Null field mapping for tables schema {}: {}", tableSchema.getName(), dpfm.getField());
                }
              });
        }
      }

      columns = fields.stream()
          .map(DataPackageField::getName)
          .filter(mappedFieldNames::contains)
          .collect(Collectors.toList());
    } else {
      columns = fields.stream()
          .map(DataPackageField::getName)
          .collect(Collectors.toList());
    }

    // file header
    String header = String.join(",", columns) + "\n";
    // total column count
    int totalColumns = columns.size();

    String fn = tableSchema.getName() + ".csv";
    File dataFile = new File(dataPackageFolder, fn);

    // ready to go through each mapping and dump the data
    try (Writer writer = org.gbif.utils.file.FileUtils.startNewUtf8File(dataFile)) {
      addMessage(Level.INFO, "Start creating Data Resource " + tableSchema.getName());
      boolean headerWritten = false;

      for (DataPackageMapping dataPackageMapping : allMappings) {
        if (dataPackageMapping.getDataPackageTableSchemaName().getName().equals(tableSchema.getName())) {
          // write header line 1 time only to file
          if (!headerWritten) {
            writer.write(header);
            headerWritten = true;
          }

          dumpData(writer, columns, dataPackageMapping, allMappings);

          // store record number by extension rowType
          recordsByTableSchema.put(tableSchema.getName(), currRecords);
        }
      }
    } catch (IOException e) {
      // some error writing this file, report
      log.error("Fatal Package Generator Error encountered while writing header line to Data Resource", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error writing header line to Data Resource", e);
    }

    // create resource from file
    DataPackageResource packageResource = DataPackageResource.fromFile(tableSchema.getName(), fn);
    packageResource.setProfile(DataPackageConstants.PROFILE_TABULAR_DATA_RESOURCE);
    packageResource.setFormat(DataPackageConstants.FORMAT_CSV);
    if (tableSchema.getUrl() != null) {
      packageResource.setSchemaUrl(tableSchema.getUrl().toString());
    }

    // the installed table schema, the one the mappings were made against
    File schemaFile = schemaManager.getTableSchemaFile(allMappings.get(0).getDataPackageSchema(), tableSchema.getName());
    if (!schemaFile.exists()) {
      addMessage(Level.ERROR, "Table schema " + tableSchema.getName() + " is not installed");
      throw new GeneratorException("Table schema " + tableSchema.getName()
          + " is not installed, reinstall the data package schema: " + schemaFile);
    }

    try {
      org.gbif.datapackage.DataPackageTableSchema schema
          = org.gbif.datapackage.DataPackageTableSchema.fromFile(schemaFile);

      // for example, for DwC DP add only mapped fields, not all the schema
      if (filterMapped) {
        removeUnmappedFields(schema, mappedFieldNames);
        packageResource.setInlineSchema(true);
      }

      packageResource.setTableSchema(schema);
    } catch (Exception e) {
      log.error("Fatal Package Generator Error encountered while adding schema data {}", tableSchema.getIdentifier(), e);
      addMessage(Level.ERROR, "Error while adding schema data " + tableSchema.getIdentifier());
      // set the last error report!
      setState(e);
      throw new GeneratorException("Error while adding schema file", e);
    }

    // add resource to package
    if (dataPackage == null) {
      dataPackage = new DataPackage();
    }
    dataPackage.addResource(packageResource);

    // final reporting
    addMessage(Level.INFO, "Data Resource " + currTableSchema + " created with " + currRecords + " records and "
        + totalColumns + " columns");
    // how many records were skipped?
    if (currRecordsSkipped > 0) {
      addMessage(Level.WARN, "!!! " + currRecordsSkipped + " records were skipped for " + currTableSchema
          + " due to errors interpreting line, or because the line was empty");
    }
  }

  /**
   * Write data resource for mappings.
   *
   * @param writer file writer for single data resource
   * @param columns output columns (field names) of the data resource, in order
   * @param schemaMapping schema mapping
   * @param allMappings all mappings of the resource, for keys linked to another table's mapping
   * @throws GeneratorException if there was an error writing data resource for mapping.
   * @throws InterruptedException if the thread was interrupted
   */
  private void dumpData(Writer writer, List<String> columns, DataPackageMapping schemaMapping,
                        List<DataPackageMapping> allMappings)
      throws GeneratorException, InterruptedException {
    RecordFilter filter = schemaMapping.getFilter();
    long recordsWithError = 0;
    long linesWithWrongColumnNumber = 0;
    long recordsFiltered = 0;
    long emptyLines = 0;
    ClosableReportingIterator<String[]> iter = null;
    long line = 0;

    // field mappings of this mapping that are mapped or have a default value by field name
    Map<String, DataPackageFieldMapping> mappingsByFieldName = new HashMap<>();
    for (DataPackageFieldMapping dpfm : schemaMapping.getFields()) {
      if (dpfm.getField() != null && dpfm.isMapped()) {
        mappingsByFieldName.put(dpfm.getField().getName(), dpfm);
      }
    }

    // one entry per output column, null where this mapping doesn't provide the column,
    // so rows of all mappings of the same table schema line up with the header
    List<DataPackageFieldMapping> usedMappings = columns.stream()
        .map(mappingsByFieldName::get)
        .collect(Collectors.toList());

    Optional<Integer> maxColumnIndexOpt = java.util.stream.Stream.concat(
            usedMappings.stream().filter(Objects::nonNull).map(DataPackageFieldMapping::getIndex),
            schemaMapping.getSplitColumns() == null ? java.util.stream.Stream.empty() : schemaMapping.getSplitColumns().stream())
        .filter(Objects::nonNull)
        .max(Comparator.naturalOrder());

    // the rows of this mapping's table made from each source row: one, or one per list item, with generated values
    DataPackageRowBuilder rowBuilder = new DataPackageRowBuilder(schemaMapping, columns, allMappings,
        sourceManager.columns(schemaMapping.getSource()));
    if (!rowBuilder.getProblems().isEmpty()) {
      for (DataPackageRowBuilder.Problem problem : rowBuilder.getProblems()) {
        addMessage(Level.ERROR, "Mapping " + schemaMapping.getLabel() + ": " + problem);
      }
      throw new GeneratorException("Mapping " + schemaMapping.getLabel() + " can't make its rows: "
          + rowBuilder.getProblems().get(0));
    }
    // rows merging by a key go to a file first, sorted by key afterwards
    boolean merging = rowBuilder.getMergeColumn() >= 0;
    File mergeFile = null;
    Writer mergeWriter = null;

    try {
      if (merging) {
        mergeFile = Files.createTempFile("dp-merge", ".txt").toFile();
        mergeWriter = org.gbif.utils.file.FileUtils.startNewUtf8File(mergeFile);
      }
      // get the source iterator
      iter = sourceManager.rowIterator(schemaMapping.getSource());

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
              + schemaMapping.getSource().getName() + " Line #" + line + ": " + printLine(in));
          emptyLines++;
          currRecordsSkipped++;
        } else {

          if (maxColumnIndexOpt.isPresent() && in.length <= maxColumnIndexOpt.get()) {
            writePublicationLogMessage("Line with fewer columns than mapped. SourceBase:"
                + schemaMapping.getSource().getName()
                + " Line #" + line + " has " + in.length + " Columns: " + printLine(in));
            // input row is smaller than the highest mapped column. Resize array by adding nulls
            String[] in2 = new String[maxColumnIndexOpt.get() + 1];
            System.arraycopy(in, 0, in2, 0, in.length);
            in = in2;
            linesWithWrongColumnNumber++;
          }

          // initialize translated values and add id column
          String[] translated = new String[usedMappings.size()];

          // filter this record?
          boolean alreadyTranslated = false;
          if (filter != null && filter.getColumn() != null && filter.getComparator() != null
              && filter.getParam() != null) {
            boolean matchesFilter;
            if (filter.getFilterTime() == RecordFilter.FilterTime.AfterTranslation) {
              // need to apply translations first
              applyTranslations(usedMappings, in, translated);
              matchesFilter = filter.matches(in);
              alreadyTranslated = true;
            } else {
              matchesFilter = filter.matches(in);
            }
            if (!matchesFilter) {
              writePublicationLogMessage("Line did not match the filter criteria and was skipped. SourceBase:"
                  + schemaMapping.getSource().getName() + " Line #" + line + ": " + printLine(in));
              recordsFiltered++;
              continue;
            }
          }

          // apply translations and default values
          if (!alreadyTranslated) {
            applyTranslations(usedMappings, in, translated);
          }

          for (String[] built : rowBuilder.build(in)) {
            if (merging) {
              mergeWriter.write(MergeLines.write(built[rowBuilder.getMergeColumn()], line, built));
              continue;
            }
            // concatenate values
            String newRow = commaRow(built);

            // write a new row (skip if null)
            if (newRow != null) {
              writer.write(newRow);
              currRecords++;
            }
          }
        }
      }
      if (merging) {
        mergeWriter.close();
        mergeWriter = null;
        writeMerged(writer, columns, schemaMapping, mergeFile);
      }
    } catch (InterruptedException e) {
      // set the last error report!
      setState(e);
      throw e;
    } catch (GeneratorException e) {
      setState(e);
      throw e;
    } catch (Exception e) {
      // some error writing this file, report
      log.error("Fatal Data Package Generator Error encountered", e);
      // set the last error report!
      setState(e);
      throw new GeneratorException("Error writing Data Resource for mapping " + currTableSchema
          + " in source " + schemaMapping.getSource().getName() + ", line " + line, e);
    } finally {
      if (mergeWriter != null) {
        try {
          mergeWriter.close();
        } catch (IOException e) {
          log.error("Error while closing the merge file", e);
        }
      }
      if (mergeFile != null) {
        FileUtils.deleteQuietly(mergeFile);
      }
      if (iter != null) {
        // Exception on advancing cursor encountered?
        if (!iter.hasRowError() && iter.getErrorMessage() != null) {
          writePublicationLogMessage("Error reading data: " + iter.getErrorMessage());
        }
        try {
          iter.close();
        } catch (Exception e) {
          log.error("Error while closing iterator", e);
        }
      }
    }

    // common message part used in constructing all reporting messages below
    String mp = " for mapping " + schemaMapping.getDataPackageSchema().getTitle() + " in source " + schemaMapping.getSource().getName();

    // add a "lines incomplete" message
    if (recordsWithError > 0) {
      addMessage(Level.WARN, recordsWithError + " record(s) skipped due to errors" + mp);
    } else {
      writePublicationLogMessage("No lines were skipped due to errors" + mp);
    }

    // add an "empty lines" message
    if (emptyLines > 0) {
      addMessage(Level.WARN, emptyLines + " empty line(s) skipped" + mp);
    } else {
      writePublicationLogMessage("No lines were skipped due to errors" + mp);
    }

    // add a "wrong lines" user message
    if (linesWithWrongColumnNumber > 0) {
      addMessage(Level.WARN, linesWithWrongColumnNumber + " line(s) with fewer columns than mapped" + mp);
    } else {
      writePublicationLogMessage("No lines with fewer columns than mapped" + mp);
    }

    // add a "filter" message
    if (recordsFiltered > 0) {
      addMessage(Level.INFO, recordsFiltered
          + " line(s) did not match the filter criteria and got skipped " + mp);
    } else {
      writePublicationLogMessage("All lines match the filter criteria" + mp);
    }
  }

  /**
   * Writes the rows of a merging mapping: sorted by key, one row per key. Rows with the same key and the same values
   * are one row; rows with the same key but different values stop publishing, the IPT doesn't pick a value.
   */
  private void writeMerged(Writer writer, List<String> columns, DataPackageMapping schemaMapping, File mergeFile)
      throws IOException, GeneratorException, InterruptedException {
    File sorted = Files.createTempFile("dp-merge-sorted", ".txt").toFile();
    long merged = 0;
    long conflicting = 0;
    try {
      List<File> batches = ExternalSort.sortInBatch(mergeFile, ExternalSort.defaultcomparator,
          ExternalSort.DEFAULTMAXTEMPFILES, StandardCharsets.UTF_8, null, false);
      ExternalSort.mergeSortedFiles(batches, sorted, ExternalSort.defaultcomparator, StandardCharsets.UTF_8);
      try (BufferedReader reader = Files.newBufferedReader(sorted.toPath(), StandardCharsets.UTF_8)) {
        MergeLines first = null;
        boolean firstConflicting = false;
        long n = 0;
        for (String l = reader.readLine(); l != null; l = reader.readLine()) {
          if (++n % 1000 == 0) {
            checkForInterruption(n);
          }
          MergeLines row = MergeLines.read(l);
          if (first != null && first.key().equals(row.key())) {
            merged++;
            List<DataPackageRowBuilder.Conflict> conflicts = DataPackageRowBuilder.compare(columns, row.key(),
                first.values(), first.line(), row.values(), row.line());
            if (!conflicts.isEmpty()) {
              if (!firstConflicting) {
                conflicting++;
                firstConflicting = true;
                if (conflicting <= 20) {
                  DataPackageRowBuilder.Conflict c = conflicts.get(0);
                  addMessage(Level.ERROR, schemaMapping.getLabel() + ": " + schemaMapping.getEffectiveMergeKey() + " "
                      + c.key() + " has different values for " + c.field() + ": \"" + StringUtils.defaultString(c.first())
                      + "\" (line " + c.firstLine() + ") and \"" + StringUtils.defaultString(c.other()) + "\" (line "
                      + c.otherLine() + ")");
                }
              }
            }
            continue;
          }
          first = row;
          firstConflicting = false;
          String newRow = commaRow(row.values());
          if (newRow != null) {
            writer.write(newRow);
            currRecords++;
          }
        }
      }
    } finally {
      Files.deleteIfExists(sorted.toPath());
    }
    if (merged > 0) {
      addMessage(Level.INFO, merged + " repeated row(s) merged by " + schemaMapping.getEffectiveMergeKey()
          + " for mapping " + schemaMapping.getLabel());
    }
    if (conflicting > 0) {
      throw new GeneratorException(conflicting + " " + schemaMapping.getEffectiveMergeKey() + " value(s) of mapping "
          + schemaMapping.getLabel() + " have rows with different values. Make the source agree, or map the fields "
          + "that differ elsewhere.");
    }
  }

  /**
   * A row of a merging mapping in the merge file: key, source line and values on one line, so the file sorts by key and,
   * within a key, by line.
   */
  record MergeLines(String key, long line, String[] values) {
    private static final char SEP = '\u001F';
    private static final String NULL = "\\0";

    static String write(String key, long line, String[] values) {
      StringBuilder sb = new StringBuilder(escape(key)).append('\t').append(String.format(Locale.ROOT, "%015d", line));
      for (String v : values) {
        sb.append(SEP).append(v == null ? NULL : escape(v));
      }
      return sb.append('\n').toString();
    }

    static MergeLines read(String l) {
      int tab = l.indexOf('\t');
      String key = unescape(l.substring(0, tab));
      String[] parts = l.substring(tab + 1).split(String.valueOf(SEP), -1);
      String[] values = new String[parts.length - 1];
      for (int i = 1; i < parts.length; i++) {
        values[i - 1] = NULL.equals(parts[i]) ? null : unescape(parts[i]);
      }
      return new MergeLines(key, Long.parseLong(parts[0]), values);
    }

    private static String escape(String v) {
      if (v == null) {
        return "";
      }
      return v.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r")
          .replace(String.valueOf(SEP), "\\u");
    }

    private static String unescape(String v) {
      StringBuilder sb = new StringBuilder(v.length());
      for (int i = 0; i < v.length(); i++) {
        char c = v.charAt(i);
        if (c == '\\' && i + 1 < v.length()) {
          char n = v.charAt(++i);
          switch (n) {
            case 't' -> sb.append('\t');
            case 'n' -> sb.append('\n');
            case 'r' -> sb.append('\r');
            case 'u' -> sb.append(SEP);
            default -> sb.append(n);
          }
        } else {
          sb.append(c);
        }
      }
      return sb.toString();
    }
  }

  /**
   * Apply translations or default values to row, for all mapped properties.
   * </br>
   * The method starts by iterating through all mapped properties, checking each one if it has been translated or a
   * default value provided. The original value in the row is then replaced with the translated or default value.
   * A record array representing the values to be written to the data resource is also updated.
   *
   * @param inCols values array, of columns in row that have been mapped
   * @param in values array, of all columns in row
   * @param translated translated values
   */
  private void applyTranslations(List<DataPackageFieldMapping> inCols,
                                 String[] in, String[] translated) {
    for (int i = 0; i < inCols.size(); i++) {
      DataPackageFieldMapping mapping = inCols.get(i);
      String val = null;
      if (mapping != null) {
        if (mapping.getIndex() != null) {
          val = in[mapping.getIndex()];
          // translate value?
          if (mapping.getTranslation() != null && mapping.getTranslation().containsKey(val)) {
            val = mapping.getTranslation().get(val);
            // update value in original record
            in[mapping.getIndex()] = val;
          }
        }
        // use default value for null values
        if (val == null) {
          val = mapping.getDefaultValue();
        }
      }
      // add value to data resource record
      translated[i] = val;
    }
  }

  /**
   * Generates a single comma-delimited row from the list of values of the provided array.
   * </br>
   * Note all line breaking characters in the value get replaced with an empty string before its added to the row.
   * </br>
   * The row ends in a newline character.
   *
   * @param columns the array of values from the source
   *
   * @return the comma-delimited String, {@code null} if provided array only contained null values
   */
  protected String commaRow(String[] columns) {
    Objects.requireNonNull(columns);
    boolean empty = true;

    for (int i = 0; i < columns.length; i++) {
      if (columns[i] != null) {
        empty = false;
        columns[i] = StringUtils.trimToNull(ESCAPE_CHARS.matcher(columns[i]).replaceAll(""));

        boolean containsDoubleQuotes = StringUtils.contains(columns[i], '"');
        boolean containsComma = StringUtils.contains(columns[i], ',');

        // Escape double quotes if present
        if (containsDoubleQuotes) {
          // escape double quotes
          columns[i] = Strings.CS.replace(columns[i], "\"", "\"\"");
        }

        // commas break the whole line, wrap in double quotes
        // same for double quotes
        if (containsComma || containsDoubleQuotes) {
          columns[i] = StringUtils.wrap(columns[i], '"');
        }
      }
    }

    if (empty) {
      return null;
    }

    return StringUtils.join(columns, ',') + "\n";
  }

  /**
   * Check if each string in array is empty. Method joins each string together and then checks if it is blank. A
   * blank string represents an empty line in a source data resource.
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
    sb.append("Data package generation failed!\n");

    // write exception as nicely formatted string
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    sb.append(sw);

    // write to publication log file
    writePublicationLogMessage(sb.toString());
  }

  /**
   * Adds metadata to data package.
   *
   * @throws GeneratorException if there are issues with metadata file
   * @throws InterruptedException if executing thread was interrupted
   */
  private void addMetadata() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.METADATA);
    try {
      addDataPackageMetadata();
      addEml();
    } catch (Exception e) {
      addMessage(Level.ERROR, e.getMessage());
      throw new GeneratorException("Problem occurred while adding metadata file to data package folder", e);
    }
    // final reporting
    addMessage(Level.INFO, "Metadata added");
  }

  /**
   * Loads the data package metadata and adds the properties computed from the resource, its EML and installed schema.
   */
  void addDataPackageMetadata() throws Exception {
    File metadataFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
    dataPackage = DataPackage.load(metadataFile);
    setDataPackageProperty("created", MetadataUtils.formatDataPackageCreated(Instant.now()));

    File emlFile = dataDir.resourceEmlFile(resource.getShortname());
    Eml eml = EmlUtils.loadWithLocale(emlFile, Locale.US);

    // the profile of the installed schema, the analyser validates against its version
    DataPackageSchema dataPackageSchema = schemaManager.get(resource.getDataPackageIdentifier());
    if (dataPackageSchema == null || StringUtils.isBlank(dataPackageSchema.getProfile())) {
      throw new GeneratorException("Data package schema " + resource.getDataPackageIdentifier()
          + " is not installed or has no profile, update or reinstall it");
    }
    setDataPackageProperty("profile", dataPackageSchema.getProfile());
    setDataPackageProperty("name", resource.getShortname());
    setContributorsDataPackageProperty(eml);
    setDataPackageStringProperty("title", eml.getTitle());
    setDataPackageStringProperty("description",
        DocBookLanguages.defaultContent(eml.getDescription(), eml.getMetadataLanguage()));
    setLicenseDataPackageProperty(eml);
  }

  private void setLicenseDataPackageProperty(Eml eml) {
    setDataPackageProperty("licenses",
        List.of(Map.of("name", LicenseConverterUtils.fullLicenseToShort(eml.getIntellectualRights()))));
  }

  private void setContributorsDataPackageProperty(Eml eml) {
    List<Map<String, Object>> contributors = buildContributors(eml);
    // the data package schema requires at least one contributor if the property is present
    if (!contributors.isEmpty()) {
      setDataPackageProperty("contributors", contributors);
    }
  }

  /**
   * Builds data package contributors from EML agents: creators become authors, contacts maintainers, metadata
   * providers wranglers. Associated parties get a role derived from their EML role, see
   * {@link #associatedPartyRole(String)}.
   * </br>
   * The roles round-trip with the registry's DwC-DP metadata parser (author → AUTHOR, maintainer → POINT_OF_CONTACT,
   * wrangler → METADATA_AUTHOR, publisher → PUBLISHER).
   */
  List<Map<String, Object>> buildContributors(Eml eml) {
    List<Map<String, Object>> contributors = new ArrayList<>();

    eml.getCreators().forEach(a -> addContributor(contributors, a, "author"));
    eml.getContacts().forEach(a -> addContributor(contributors, a, "maintainer"));
    eml.getMetadataProviders().forEach(a -> addContributor(contributors, a, "wrangler"));
    eml.getAssociatedParties().forEach(a -> addContributor(contributors, a, associatedPartyRole(a.getRole())));

    return List.copyOf(contributors);
  }

  /**
   * Maps an EML (GBIF agentRole vocabulary) role to a data package contributor role. Roles with an exact data package
   * equivalent are mapped, any other role is kept verbatim (data package roles are free text), a missing role
   * becomes "contributor".
   */
  static String associatedPartyRole(String emlRole) {
    String role = StringUtils.trimToNull(emlRole);
    if (role == null) {
      return "contributor";
    }

    return switch (role.toLowerCase(Locale.ROOT)) {
      case "author" -> "author";
      case "publisher" -> "publisher";
      case "pointofcontact" -> "maintainer";
      case "metadataprovider" -> "wrangler";
      default -> role;
    };
  }

  private void addContributor(List<Map<String, Object>> contributors, Agent agent, String role) {
    if (agent == null || agent.isEmpty()) {
      return;
    }

    String organisation = StringUtils.trimToNull(agent.getOrganisation());
    Optional<String> position = firstNonBlank(agent.getPosition());

    // title is required by the data package schema: name, otherwise organisation, otherwise position
    String title = Optional.ofNullable(agent.getFullName())
        .or(() -> Optional.ofNullable(organisation))
        .or(() -> position)
        .orElse(null);
    if (title == null) {
      log.warn("Contributor skipped, no name, organisation or position: {}", agent);
      return;
    }

    Map<String, Object> contributor = new LinkedHashMap<>();
    contributor.put("title", title);

    if (organisation != null) {
      contributor.put("organization", organisation);
    }

    firstNonBlank(agent.getEmail()).ifPresent(email -> contributor.put("email", email));
    firstNonBlank(agent.getHomepage()).ifPresent(path -> contributor.put("path", path));

    contributor.put("role", role);

    // keep key order (title first) in the serialized descriptor
    contributors.add(Collections.unmodifiableMap(contributor));
  }

  private Optional<String> firstNonBlank(List<String> values) {
    return values.stream().filter(StringUtils::isNotBlank).findFirst();
  }

  private void addEml() throws Exception {
    try {
      FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname()), new File(dataPackageFolder,
          DataDir.EML_XML_FILENAME));
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while adding EML file to DwC-A folder", e);
    }

    // final reporting
    addMessage(Level.INFO, "EML file added");
  }

  private void setDataPackageProperty(String name, Object property) {
    if (property != null) {
      dataPackage.setProperty(name, property);
    }
  }

  private void setDataPackageStringProperty(String name, String property) {
    if (StringUtils.isNotEmpty(property)) {
      dataPackage.setProperty(name, property);
    }
  }

  /**
   * Removes unmapped fields from the table schema, together with the keys on them, so the descriptor doesn't declare
   * keys on columns that aren't in the data resource: primary key, foreign keys and DwC-DP's weak primary key and
   * weak foreign keys (kept untyped in the schema's additional properties).
   *
   * @param schema table schema, modified in place
   * @param mappedFieldNames names of mapped fields (or with a default value)
   */
  static void removeUnmappedFields(org.gbif.datapackage.DataPackageTableSchema schema, Set<String> mappedFieldNames) {
    schema.getFields().removeIf(f -> !mappedFieldNames.contains(f.getName()));
    schema.getForeignKeys().removeIf(fk -> isUnusedKey(fk.getFields(), mappedFieldNames));

    // primary key fields are required in DwC-DP, so they should always be mapped
    if (isUnusedKey(schema.getPrimaryKey(), mappedFieldNames)) {
      schema.setPrimaryKey(new ArrayList<>());
    }

    Map<String, Object> properties = schema.getAdditionalProperties();
    if (properties.containsKey(WEAK_PRIMARY_KEY)
        && isUnusedKey(keyFields(properties.get(WEAK_PRIMARY_KEY)), mappedFieldNames)) {
      properties.remove(WEAK_PRIMARY_KEY);
    }

    if (properties.get(WEAK_FOREIGN_KEYS) instanceof List<?> weakForeignKeys) {
      List<?> usedWeakForeignKeys = weakForeignKeys.stream()
          .filter(fk -> fk instanceof Map<?, ?> fkMap && !isUnusedKey(keyFields(fkMap.get("fields")), mappedFieldNames))
          .collect(Collectors.toList());

      if (usedWeakForeignKeys.isEmpty()) {
        properties.remove(WEAK_FOREIGN_KEYS);
      } else {
        properties.put(WEAK_FOREIGN_KEYS, usedWeakForeignKeys);
      }
    }
  }

  /**
   * A key is unused if it has no fields or any of its fields isn't mapped.
   */
  private static boolean isUnusedKey(List<String> keyFields, Set<String> mappedFieldNames) {
    return keyFields == null || keyFields.isEmpty() || !mappedFieldNames.containsAll(keyFields);
  }

  /**
   * Key fields from an untyped "fields" (or weak primary key) value: a field name or a list of field names.
   */
  private static List<String> keyFields(Object value) {
    if (value instanceof String field) {
      return List.of(field);
    } else if (value instanceof List<?> fields) {
      return fields.stream().map(String::valueOf).collect(Collectors.toList());
    }
    return List.of();
  }
}
