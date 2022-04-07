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
import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.model.DataSchemaField;
import org.gbif.ipt.model.DataSchemaFieldMapping;
import org.gbif.ipt.model.DataSchemaMapping;
import org.gbif.ipt.model.DataSubschema;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.frictionlessdata.datapackage.Package;
import io.frictionlessdata.datapackage.Profile;
import io.frictionlessdata.datapackage.resource.FilebasedResource;
import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.schema.Schema;

public class GenerateDataPackage extends ReportingTask implements Callable<Map<String, Integer>> {

  private enum STATE {
    WAITING, STARTED, DATAFILES, METADATA, BUNDLING, COMPLETED, ARCHIVING, VALIDATING, CANCELLED, FAILED
  }

  private static final Pattern ESCAPE_CHARS = Pattern.compile("[\t\n\r]");

  private final Resource resource;
  private final SourceManager sourceManager;
  private final AppConfig cfg;
  private STATE state = STATE.WAITING;
  private Exception exception;
  private File dataPackageFolder;
  private Package dataPackage;
  private int currRecords = 0;
  private int currRecordsSkipped = 0;
  private String currSchema;
  private String currSubschema;

  @Inject
  public GenerateDataPackage(@Assisted Resource resource, @Assisted ReportHandler handler, DataDir dataDir,
                      SourceManager sourceManager, AppConfig cfg) throws IOException {
    super(1000, resource.getShortname(), handler, dataDir);
    this.resource = resource;
    this.sourceManager = sourceManager;
    this.cfg = cfg;
  }

  @Override
  public Map<String, Integer> call() throws Exception {
    try {
      checkForInterruption();
      setState(STATE.STARTED);

      // initial reporting
      addMessage(Level.INFO, "Data package generation started for version #" + resource.getEmlVersion());

      // create a temp dir to copy all dwca files to
      dataPackageFolder = dataDir.tmpDir();

      // create data files
      createDataFiles();

      // TODO: 06/04/2022 EML!
      // copy eml file
//      addEmlFile();

      // TODO: 06/04/2022 validation!
      // perform some validation, e.g. ensure all core record identifiers are present and unique
//      validate();

      // zip archive and copy to resource folder
      bundleArchive();

      // reporting
      addMessage(Level.INFO, "Archive version #" + resource.getEmlVersion() + " generated successfully!");

      // set final state
      setState(STATE.COMPLETED);

      // TODO: 06/04/2022 what is supposed to be returned?
      Map<String, Integer> result = new HashMap<>();
      result.put(resource.getSchemaIdentifier(), 0);
      return result;
    } catch (GeneratorException e) {
      // set last error report!
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
    switch (state) {
      case WAITING:
        return "Not started yet";
      case STARTED:
        return "Starting data package generation";
      case DATAFILES:
        return "Processing record " + currRecords + " for data file <em>" + currSubschema + "</em>";
      case METADATA:
        return "Creating metadata files";
      case BUNDLING:
        return "Compressing data package (archive)";
      case COMPLETED:
        return "Data package generated!";
      case VALIDATING:
        return "Validating data package, " + currRecords + " for data file <em>" + currSubschema + "</em>";
      case ARCHIVING:
        return "Archiving version of data package";
      case CANCELLED:
        return "Data package generation cancelled";
      case FAILED:
        return "Failed. Fatal error!";
      default:
        return "You should never see this";
    }
  }

  /**
   * Zips the data package folder. A temp version is created first, and when successful, it's moved into the resource's
   * data directory.
   *
   * @throws GeneratorException if data package could not be zipped or moved
   * @throws InterruptedException if executing thread was interrupted
   */
  private void bundleArchive() throws Exception {
    checkForInterruption();
    setState(STATE.BUNDLING);
    File zip = null;
    BigDecimal version = resource.getEmlVersion();
    try {
      // create zip
      zip = dataDir.tmpFile("data_package", ".zip");
      dataPackage.write(zip, true);

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
      throw new GeneratorException("Problem occurred while bundling data package", e);
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
   * Checks if the executing thread has been interrupted, i.e. generation was cancelled.
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
   * @throws GeneratorException if the resource had no core file that was mapped
   * @throws InterruptedException if the thread was interrupted
   */
  private void createDataFiles() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.DATAFILES);
    if (resource.getSchemaIdentifier() == null && CollectionUtils.isEmpty(resource.getDataSchemaMappings())) {
      throw new GeneratorException("Schema identifier or mappings are not set");
    }

    List<DataSchemaMapping> allMappings = resource.getDataSchemaMappings();
    DataSchema dataSchema = resource.getDataSchemaMappings().get(0).getDataSchema();
    currSchema = dataSchema.getName();

    for (DataSubschema subSchema : dataSchema.getSubSchemas()) {
      report();
      try {
        addDataFile(subSchema, allMappings);
      } catch (IOException | IllegalArgumentException e) {
        throw new GeneratorException("Problem occurred while writing data file", e);
      }
    }

    // final reporting
    addMessage(Level.INFO, "All data files completed");
    report();
  }

  /**
   * Adds a single data file for a subschema mapping.
   *
   * @throws IllegalArgumentException if not all mappings are mapped to the same extension
   * @throws InterruptedException if the thread was interrupted
   * @throws IOException if problems occurred while persisting new data files
   * @throws GeneratorException if any problem was encountered writing data file
   */
  public void addDataFile(DataSubschema subschema, List<DataSchemaMapping> allMappings) throws IOException,
      IllegalArgumentException, InterruptedException, GeneratorException {
    checkForInterruption();
    if (subschema == null || CollectionUtils.isEmpty(allMappings)) {
      return;
    }

    // update reporting
    currRecords = 0;
    currRecordsSkipped = 0;
    currSubschema = subschema.getName();

    List<DataSchemaField> fields = subschema.getFields();

    // file header
    String header = fields.stream()
        .map(DataSchemaField::getName)
        .collect(Collectors.joining(",", "", "\n"));

    // total column count (number of fields in the subschema)
    int totalColumns = fields.size();

    String fn = subschema.getName() + ".csv";
    // TODO: 05/04/2022 file name collision? what if already exists?
    File dataFile = new File(dataPackageFolder, fn);

    // ready to go through each mapping and dump the data
    try (Writer writer = org.gbif.utils.file.FileUtils.startNewUtf8File(dataFile)) {
      addMessage(Level.INFO, "Start writing data file for " + subschema.getName());
      boolean headerWritten = false;

      for (DataSchemaMapping dataSchemaMapping : allMappings) {
        // TODO: 05/04/2022 check not null
        LinkedHashSet<DataSchemaFieldMapping> subschemaFieldMappings = dataSchemaMapping.getFields().get(subschema.getName());

        // write header line 1 time only to file
        if (!headerWritten) {
          writer.write(header);
          headerWritten = true;
        }

        dumpData(writer, dataSchemaMapping, subschemaFieldMappings, totalColumns);
      }
    } catch (IOException e) {
      // some error writing this file, report
      log.error("Fatal Package Generator Error encountered while writing header line to data file", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error writing header line to data file", e);
    }

    // create resource from file
    @SuppressWarnings({"rawtypes", "unchecked"})
    io.frictionlessdata.datapackage.resource.Resource packageResource =
        new FilebasedResource(
            subschema.getName(),
            Collections.singleton(new File(fn)),
            dataPackageFolder);
    packageResource.setProfile(Profile.PROFILE_TABULAR_DATA_RESOURCE);

    // set schema
    String filename = org.gbif.ipt.utils.FileUtils.getSuffixedFileName(resource.getSchemaIdentifier() + "_" + currSubschema, ".json");
    File schemaFile = dataDir.configFile(".dataSchemas" + "/" + currSchema + "/" + filename);

    try {
      Schema schema = Schema.fromJson(schemaFile, true);
      packageResource.setSchema(schema);
    } catch (Exception e) {
      log.error("Fatal Package Generator Error encountered while adding schema data", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error adding schema file", e);
    }

    // add resource to package
    if (dataPackage == null) {
      dataPackage = new Package(Collections.singleton(packageResource));
    } else {
      dataPackage.addResource(packageResource);
    }

    // final reporting
    addMessage(Level.INFO, "Data file written for " + currSubschema + " with " + currRecords + " records and "
        + totalColumns + " columns");
    // how many records were skipped?
    if (currRecordsSkipped > 0) {
      addMessage(Level.WARN, "!!! " + currRecordsSkipped + " records were skipped for " + currSubschema
          + " due to errors interpreting line, or because the line was empty");
    }
  }

  /**
   * Write data file for mappings.
   *
   * @param writer file writer for single data file
   * @param schemaMapping schema mapping
   * @param subschemaFieldMappings field mappings
   * @param dataFileRowSize number of columns in data file
   * @throws GeneratorException if there was an error writing data file for mapping.
   * @throws InterruptedException if the thread was interrupted
   */
  private void dumpData(Writer writer, DataSchemaMapping schemaMapping,
                        LinkedHashSet<DataSchemaFieldMapping> subschemaFieldMappings, int dataFileRowSize)
      throws GeneratorException, InterruptedException {
    int recordsWithError = 0;
    int linesWithWrongColumnNumber = 0;
    int recordsFiltered = 0;
    int emptyLines = 0;
    ClosableReportingIterator<String[]> iter = null;
    int line = 0;
    Optional<Integer> maxMappedColumnIndexOpt = subschemaFieldMappings.stream()
        .map(DataSchemaFieldMapping::getIndex)
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

          String newRow = commaRow(in, subschemaFieldMappings);
          writer.write(newRow);
          currRecords++;
        }
      }
    } catch (InterruptedException e) {
      // set last error report!
      setState(e);
      throw e;
    } catch (Exception e) {
      // some error writing this file, report
      log.error("Fatal Data Package Generator Error encountered", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error writing data file for mapping " + currSubschema
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
    String mp = " for mapping " + schemaMapping.getDataSchema().getTitle() + " in source " + schemaMapping.getSource().getName();

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
  }

  /**
   * Generates a single comma delimited row from the list of values of the provided array.
   * </br>
   * Note all line breaking characters in the value get replaced with an empty string before its added to the row.
   * </br>
   * The row ends in a newline character.
   *
   * @param columns the array of values from the source
   * @param subschemaFieldMappings field mappings
   *
   * @return the comma delimited String, {@code null} if provided array only contained null values
   */
  protected String commaRow(String[] columns, LinkedHashSet<DataSchemaFieldMapping> subschemaFieldMappings) {
    Objects.requireNonNull(columns);
    StringBuilder sb = new StringBuilder();
    Iterator<DataSchemaFieldMapping> iter = subschemaFieldMappings.iterator();

    while (iter.hasNext()) {
      DataSchemaFieldMapping fieldMapping = iter.next();

      // append iv value is mapped
      if (fieldMapping.getIndex() != null) {
        sb.append(ESCAPE_CHARS.matcher(columns[fieldMapping.getIndex()]).replaceAll(""));
      }

      if (iter.hasNext()) {
        sb.append(",");
      }
    }

    return sb.append("\n").toString();
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
}
