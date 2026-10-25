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
import org.gbif.ipt.model.datapackage.metadata.FrictionlessLicense;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.utils.MetadataUtils;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.apache.logging.log4j.Level;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.frictionlessdata.datapackage.exceptions.DataPackageValidationException;
import io.frictionlessdata.datapackage.JSONBase;
import io.frictionlessdata.datapackage.Package;
import io.frictionlessdata.datapackage.Profile;
import io.frictionlessdata.datapackage.resource.FilebasedResource;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.exception.PrimaryKeyException;
import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.schema.Schema;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.DATA_PACKAGE_EXTENSION;
import static org.gbif.ipt.config.Constants.DATA_PACKAGE_NAME;

public class GenerateDataPackage extends ReportingTask implements Callable<Map<String, Integer>> {

  private enum STATE {
    WAITING, STARTED, DATARESOURCES, METADATA, BUNDLING, COMPLETED, ARCHIVING, VALIDATING, CANCELLED, FAILED
  }

  private static final Pattern ESCAPE_CHARS = Pattern.compile("[\t\n\r]");
  // descriptor properties a ColDP's additional metadata properties can't set: the descriptor's own, and those it takes
  // from the ColDP's metadata, which the package is only created with as it expects them
  private static final Set<String> DESCRIPTOR_OWN_PROPERTIES = Set.of("profile", "id", "name", "resources", "created",
      "version", "title", "description", "contributors", "keywords", "homepage", "image", "licenses");

  private final Resource resource;
  private final SourceManager sourceManager;
  private final DataPackageSchemaManager schemaManager;
  private MetadataReader metadataReader;
  private final AppConfig cfg;
  private STATE state = STATE.WAITING;
  private Exception exception;
  private File dataPackageFolder;
  private Package dataPackage;
  private int currRecords = 0;
  private int currRecordsSkipped = 0;
  private String currSchema;
  private String currTableSchema;
  // record counts by extension <rowType, count>
  private Map<String, Integer> recordsByTableSchema = new HashMap<>();

  public GenerateDataPackage(
      Resource resource,
      ReportHandler handler,
      DataDir dataDir,
      SourceManager sourceManager,
      DataPackageSchemaManager schemaManager,
      AppConfig cfg,
      MetadataReader metadataReader) {
    super(1000, resource.getShortname(), handler, dataDir);
    this.resource = resource;
    this.sourceManager = sourceManager;
    this.schemaManager = schemaManager;
    this.cfg = cfg;
    this.metadataReader = metadataReader;
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
  Package getDataPackage() {
    return dataPackage;
  }

  @Override
  public Map<String, Integer> call() throws Exception {
    try {
      checkForInterruption();
      setState(STATE.STARTED);

      // initial reporting
      addMessage(Level.INFO, "Data Package generation started for version #" + resource.getDataPackageMetadataVersion());

      // create a temp dir to copy all files to
      dataPackageFolder = dataDir.tmpDir();

      // create the datapackage descriptor (datapackage.json), then add resources to it
      addMetadata();
      createDataResources();

      // validation is a part of frictionless datapackage generation
      // zip archive and copy to resource folder
      bundleArchive();

      // reporting
      addMessage(Level.INFO, "Archive version #" + resource.getDataPackageMetadataVersion() + " generated successfully!");

      // set final state
      setState(STATE.COMPLETED);

      return recordsByTableSchema;
    } catch (GeneratorException e) {
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
      case VALIDATING ->
          "Validating Data Package, " + currRecords + " for Data Resource <em>" + currTableSchema + "</em>";
      case ARCHIVING -> "Archiving version of data package";
      case CANCELLED -> "Data Package generation cancelled";
      case FAILED -> "Data Package generation failed";
    };
  }

  /**
   * Zips the data package folder. A temp version is created first, and when successful, it's moved into the resource's
   * data directory.
   *
   * @throws GeneratorException   if data package could not be zipped or moved
   * @throws InterruptedException if executing thread was interrupted
   */
  void bundleArchive() throws Exception {
    checkForInterruption();
    setState(STATE.BUNDLING);
    File zip = null;
    BigDecimal version = resource.getDataPackageMetadataVersion();
    try {
      // check keys first
      checkRelationsAndPrimaryKeys();

      // create zip
      zip = dataDir.tmpFile(DATA_PACKAGE_NAME, DATA_PACKAGE_EXTENSION);
      if (COL_DP.equals(resource.getCoreType())) {
        dataPackage.write(zip, this::writeCustomColDPMetadata, true);
      } else {
        dataPackage.write(zip, true);
      }

      if (zip.exists()) {
        // move to data dir with versioned name
        File versionedFile = dataDir.resourceDataPackageFile(resource.getShortname(), version);
        if (versionedFile.exists()) {
          FileUtils.forceDelete(versionedFile);
        }
        FileUtils.moveFile(zip, versionedFile);
      } else {
        throw new GeneratorException("Archive bundling failed: temp archive not created: " + zip.getAbsolutePath());
      }
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while bundling data package", e);
    } catch (UncheckedIOException e) {
      // e.g. writing a ColDP's metadata.yaml failed
      throw new GeneratorException("Problem occurred while bundling data package", e.getCause());
    } catch (DataPackageValidationException | PrimaryKeyException | ForeignKeyException e) {
      throw new GeneratorException(e.getMessage());
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
   * Apart from standard frictionless metadata ColDP archive must contain specific metadata.yaml file. The archive can't
   * be published without it: the archive is written with it, which can't throw an IOException, so it's unchecked.
   *
   * @throws UncheckedIOException if it can't be written
   */
  private void writeCustomColDPMetadata(Path outputDir) {
    Path target = outputDir.getFileSystem().getPath("metadata.yaml");
    try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
      metadataReader.writeValue(writer, resource.getDataPackageMetadata());
    } catch (IOException e) {
      log.error("Failed to write metadata.yaml", e);
      addMessage(Level.ERROR, "Failed to write metadata.yaml: " + e.getMessage());
      throw new UncheckedIOException(e);
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
  private void checkForInterruption(int line) throws InterruptedException {
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
    exception = e;
    state = (exception instanceof InterruptedException) ? STATE.CANCELLED : STATE.FAILED;
    report();
  }

  /**
   * Create data files.
   *
   * @throws GeneratorException   if the resource had no core file that was mapped
   * @throws InterruptedException if the thread was interrupted
   */
  private void createDataResources() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.DATARESOURCES);
    if (resource.getDataPackageIdentifier() == null && CollectionUtils.isEmpty(resource.getDataPackageMappings())) {
      throw new GeneratorException("Data package identifier or mappings are not set");
    }

    List<DataPackageMapping> allMappings = resource.getDataPackageMappings();
    Set<String> mappedTableSchemas = allMappings.stream()
        .map(DataPackageMapping::getDataPackageTableSchemaName)
        .map(DataPackageTableSchemaName::getName)
        .collect(Collectors.toSet());
    DataPackageSchema dataPackageSchema = resource.getDataPackageMappings().get(0).getDataPackageSchema();
    currSchema = dataPackageSchema.getName();

    // before starting to add a table schema, check all required schemas mapped
    checkRequiredTableSchemasMapped(mappedTableSchemas, dataPackageSchema);

    checkMappingsResolved(allMappings, dataPackageSchema);

    for (DataPackageTableSchema tableSchema : dataPackageSchema.getTableSchemas()) {
      // skip un-mapped (optional) schemas
      if (!mappedTableSchemas.contains(tableSchema.getName())) {
        continue;
      }

      report();
      try {
        addDataResource(currSchema, tableSchema, allMappings);
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
   * @param dataPackageSchema  data schema
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
   * @throws InterruptedException     if the thread was interrupted
   * @throws IOException              if problems occurred while persisting new data resources
   * @throws GeneratorException       if any problem was encountered writing data resources
   */
  public void addDataResource(String schemaName, DataPackageTableSchema tableSchema, List<DataPackageMapping> allMappings) throws IOException,
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

    // file header
    String header = fields.stream()
        .map(DataPackageField::getName)
        .collect(Collectors.joining(",", "", "\n"));

    // total column count (number of fields in the tableSchema)
    int totalColumns = fields.size();

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

          dumpData(writer, dataPackageMapping, fieldMappingsInSchemaOrder(fields, dataPackageMapping), totalColumns);

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
    @SuppressWarnings({"rawtypes"})
    io.frictionlessdata.datapackage.resource.Resource packageResource =
        new FilebasedResource(
            tableSchema.getName(),
            Collections.singleton(new File(fn)),
            dataPackageFolder);
    packageResource.setProfile(Profile.PROFILE_TABULAR_DATA_RESOURCE);
    packageResource.setFormat(io.frictionlessdata.datapackage.resource.Resource.FORMAT_CSV);
    if (tableSchema.getUrl() != null) {
      ((JSONBase) packageResource).getOriginalReferences().put(JSONBase.JSON_KEY_SCHEMA, tableSchema.getUrl().toString());
    }

    // the installed table schema, the one the mappings were made against
    File schemaFile = schemaManager.getTableSchemaFile(allMappings.get(0).getDataPackageSchema(), tableSchema.getName());
    if (!schemaFile.exists()) {
      addMessage(Level.ERROR, "Table schema " + tableSchema.getName() + " is not installed");
      throw new GeneratorException("Table schema " + tableSchema.getName()
          + " is not installed, reinstall the data package schema: " + schemaFile);
    }

    try {
      Schema schema = Schema.fromJson(schemaFile, true);
      packageResource.setSchema(schema);
    } catch (ValidationException e) {
      log.error("Failed to validate schema {}. Errors: {}", tableSchema.getName(), e.getMessages(), e);
      addMessage(Level.ERROR, "Failed to validate schema " + tableSchema.getName());
      // set the last error report!
      setState(e);
      throw new GeneratorException("Validation error while adding schema file", e);
    } catch (Exception e) {
      log.error("Fatal Package Generator Error encountered while adding schema data {}", tableSchema.getIdentifier(), e);
      addMessage(Level.ERROR, "Error while adding schema data " + tableSchema.getIdentifier());
      // set the last error report!
      setState(e);
      throw new GeneratorException("Error while adding schema file", e);
    }

    // add resource to package
    if (dataPackage == null) {
      dataPackage = new Package(Collections.singleton(packageResource));
    } else {
      dataPackage.addResource(packageResource);
    }

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
   * Field mappings in the order of the table schema fields, null for fields that aren't mapped: values are written by
   * position. The mapping's own order may differ, e.g. after a schema update.
   *
   * @param fields table schema fields
   * @param mapping mapping
   * @return field mappings, one per table schema field
   */
  static List<DataPackageFieldMapping> fieldMappingsInSchemaOrder(List<DataPackageField> fields,
                                                                  DataPackageMapping mapping) {
    Map<String, DataPackageFieldMapping> fieldMappingsByName = new HashMap<>();
    for (DataPackageFieldMapping fieldMapping : mapping.getFields()) {
      if (fieldMapping.getField() != null) {
        fieldMappingsByName.putIfAbsent(fieldMapping.getField().getName(), fieldMapping);
      }
    }
    return fields.stream()
        .map(field -> fieldMappingsByName.get(field.getName()))
        .collect(Collectors.toList());
  }

  /**
   * Write data resource for mappings.
   *
   * @param writer                   file writer for single data resource
   * @param schemaMapping            schema mapping
   * @param tableSchemaFieldMappings field mappings
   * @param dataFileRowSize          number of columns in data resource
   * @throws GeneratorException   if there was an error writing data resource for mapping.
   * @throws InterruptedException if the thread was interrupted
   */
  private void dumpData(Writer writer, DataPackageMapping schemaMapping,
                        List<DataPackageFieldMapping> tableSchemaFieldMappings, int dataFileRowSize)
      throws GeneratorException, InterruptedException {
    RecordFilter filter = schemaMapping.getFilter();
    int recordsWithError = 0;
    int linesWithWrongColumnNumber = 0;
    int recordsFiltered = 0;
    int emptyLines = 0;
    ClosableReportingIterator<String[]> iter = null;
    int line = 0;
    Optional<Integer> maxMappedColumnIndexOpt = tableSchemaFieldMappings.stream()
        .filter(Objects::nonNull)
        .map(DataPackageFieldMapping::getIndex)
        .filter(Objects::nonNull)
        .max(Comparator.naturalOrder());

    try {
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

          if (maxMappedColumnIndexOpt.isPresent() && in.length <= maxMappedColumnIndexOpt.get()) {
            writePublicationLogMessage("Line with fewer columns than mapped. SourceBase:"
                + schemaMapping.getSource().getName()
                + " Line #" + line + " has " + in.length + " Columns: " + printLine(in));
            // input row is smaller than the highest mapped column. Resize array by adding nulls
            String[] in2 = new String[maxMappedColumnIndexOpt.get() + 1];
            System.arraycopy(in, 0, in2, 0, in.length);
            in = in2;
            linesWithWrongColumnNumber++;
          }

          // initialize translated values and add id column
          String[] translated = new String[dataFileRowSize];

          // filter this record?
          boolean alreadyTranslated = false;
          if (filter != null && filter.getColumn() != null && filter.getComparator() != null
            && filter.getParam() != null) {
            boolean matchesFilter;
            if (filter.getFilterTime() == RecordFilter.FilterTime.AfterTranslation) {
              // need to apply translations first
              applyTranslations(tableSchemaFieldMappings, in, translated);
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
            applyTranslations(tableSchemaFieldMappings, in, translated);
          }

          // concatenate values
          String newRow = commaRow(translated);

          // write a new row (skip if null)
          if (newRow != null) {
            writer.write(newRow);
            currRecords++;
          }
        }
      }
    } catch (InterruptedException e) {
      // set the last error report!
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
  private void applyTranslations(List<DataPackageFieldMapping> inCols, String[] in, String[] translated) {
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
   * Generates a single comma delimited row from the list of values of the provided array.
   * </br>
   * Note all line breaking characters in the value get replaced with an empty string before its added to the row.
   * </br>
   * The row ends in a newline character.
   *
   * @param columns the array of values from the source
   * @return the comma delimited String, {@code null} if provided array only contained null values
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
    setState(GenerateDataPackage.STATE.METADATA);
    try {
      String type = resource.getCoreType();

      if (CAMTRAP_DP.equals(type)) {
        addCamtrapMetadata();
      } else if (COL_DP.equals(type)) {
        addColMetadata();
      } else {
        addMessage(Level.WARN, "Metadata was not added: unknown type " + type);
      }

    } catch (Exception e) {
      addMessage(Level.ERROR, e.getMessage());
      throw new GeneratorException("Problem occurred while adding metadata file to data package folder", e);
    }
    // final reporting
    addMessage(Level.INFO, "Metadata added");
  }

  /**
   * Creates the data package descriptor from the ColDP metadata (metadata.yaml, which goes in the archive too). The
   * package is created from it as JSON: properties like version, created, homepage, contributors and licenses are
   * only read from JSON, they're left out when set on a package.
   */
  void addColMetadata() throws Exception {
    File metadataFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
    ColMetadata colMetadata = metadataReader.readValue(metadataFile, ColMetadata.class);

    Map<String, Object> descriptor = new LinkedHashMap<>();
    descriptor.put("created", MetadataUtils.formatDataPackageCreated(Instant.now()));
    putIfNotEmpty(descriptor, "version", colMetadata.getVersion());
    putIfNotEmpty(descriptor, "title", colMetadata.getTitle());
    putIfNotEmpty(descriptor, "description", colMetadata.getDescription());
    // ColDP agents and license as the descriptor has them
    putIfNotEmpty(descriptor, "contributors", MetadataUtils.colDpContributorsForDescriptor(colMetadata));
    putIfNotEmpty(descriptor, "keywords", colMetadata.getKeyword());
    putIfNotEmpty(descriptor, "homepage", MetadataUtils.httpUrlOrNull(colMetadata.getUrl()));
    putIfNotEmpty(descriptor, "image", MetadataUtils.httpUrlOrNull(colMetadata.getLogo()));
    List<FrictionlessLicense> licenses = MetadataUtils.colDpLicensesForDescriptor(colMetadata.getLicense());
    if (licenses.isEmpty() && StringUtils.isNotBlank(colMetadata.getLicense())) {
      addMessage(Level.WARN, "License " + colMetadata.getLicense()
          + " is not known, it's left out of datapackage.json and only in metadata.yaml");
    }
    putIfNotEmpty(descriptor, "licenses", licenses);

    // additional properties
    colMetadata.getAdditionalProperties().forEach((key, value) -> {
      if (!DESCRIPTOR_OWN_PROPERTIES.contains(key)) {
        descriptor.put(key, value);
      }
    });
    descriptor.put("resources", Collections.emptyList());

    dataPackage = new Package(new ObjectMapper().writeValueAsString(descriptor), dataPackageFolder.toPath(), false);
  }

  private static void putIfNotEmpty(Map<String, Object> descriptor, String name, Object value) {
    if (value instanceof String s ? StringUtils.isNotBlank(s)
        : value instanceof Collection<?> c ? !c.isEmpty() : value != null) {
      descriptor.put(name, value);
    }
  }

  private void addCamtrapMetadata() throws Exception {
    File metadataFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());

    dataPackage = new Package(metadataFile.toPath(), false);
    setDataPackageProperty("created", MetadataUtils.formatDataPackageCreated(Instant.now()));
  }

  private void setDataPackageProperty(String name, Object property) {
    if (property != null) {
      dataPackage.setProperty(name, property);
    }
  }

  /**
   * Checks the keys of each table, reading its values: a value its field doesn't allow fails too. The table is named
   * in the message, which otherwise only names the field.
   */
  private void checkRelationsAndPrimaryKeys() throws Exception {
    boolean isFkValidationEnabled = cfg.isDatapackageForeignKeysValidationEnabled();
    for (io.frictionlessdata.datapackage.resource.Resource<?> dataPackageResource : dataPackage.getResources()) {
      try {
        if (isFkValidationEnabled) {
          dataPackageResource.checkRelations(dataPackage);
        }
        dataPackageResource.checkPrimaryKeys();
      } catch (DataPackageValidationException | PrimaryKeyException | ForeignKeyException e) {
        String message = "Table " + dataPackageResource.getName() + ": " + e.getMessage();
        addMessage(Level.ERROR, message);
        throw new GeneratorException(message, e);
      }
    }
  }
}