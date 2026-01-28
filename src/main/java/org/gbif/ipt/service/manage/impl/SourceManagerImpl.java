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
package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.Archive;
import org.gbif.dwc.ArchiveFile;
import org.gbif.dwc.DwcFiles;
import org.gbif.dwc.UnsupportedArchiveException;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExcelFileSource;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.RowIterable;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlMetadata;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.file.DataFile;
import org.gbif.ipt.service.file.FileStoreManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.manage.ResourceUpdateListener;
import org.gbif.ipt.utils.URLUtils;
import org.gbif.utils.file.ClosableIterator;
import org.gbif.utils.file.ClosableReportingIterator;
import org.gbif.utils.file.csv.UnknownDelimitersException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class SourceManagerImpl extends BaseManager implements SourceManager {

  private static class ColumnIterator implements ClosableIterator<Object> {

    private final ClosableReportingIterator<String[]> rows;
    private final int column;

    ColumnIterator(RowIterable source, int column) throws IOException {
      rows = source.rowIterator();
      this.column = column;
    }

    @Override
    public void close() throws Exception {
      rows.close();
    }

    @Override
    public boolean hasNext() {
      return rows.hasNext();
    }

    @Override
    public Object next() {
      String[] row = rows.next();
      if (row == null || row.length < column) {
        return null;
      }
      return row[column];
    }

    @Override
    public void remove() {
      // unsupported
    }
  }

  private class SqlColumnIterator implements ClosableIterator<Object> {

    private final Connection conn;
    private final Statement stmt;
    private final ResultSet rs;
    private final int column;
    private boolean hasNext;
    private final String sourceName;

    public SqlColumnIterator(SqlSource source, int column) throws SQLException {
      this(source, column, source.getSql());
    }

    public SqlColumnIterator(SqlSource source, int column, int limit) throws SQLException {
      this(source, column, source.getSqlLimited(limit));
    }

    /**
     * SqlColumnIterator constructor
     *
     * @param source of the sql data
     * @param column to inspect, zero based numbering as used in the dwc archives
     * @param sql statement to query in the sql source
     */
    private SqlColumnIterator(SqlSource source, int column, String sql) throws SQLException {
      this.conn = getDbConnection(source);
      this.stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      source.getRdbms().enableLargeResultSet(this.stmt);
      this.column = column + 1;
      LOG.debug("Executing SQL {}", sql);
      this.rs = stmt.executeQuery(sql);
      this.hasNext = rs.next();
      sourceName = source.getName();
    }

    @Override
    public void close() {
      if (rs != null) {
        try {
          rs.close();
          stmt.close();
          conn.close();
        } catch (SQLException e) {
          LOG.error("Can't close iterator for SQL source {}", sourceName, e);
        }
      }

    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public Object next() {
      String val = null;
      if (hasNext) {
        try {
          // forward rs cursor
          hasNext = rs.next();
          val = rs.getString(column);
        } catch (SQLException e2) {
          hasNext = false;
        }
      }
      return val;
    }

    @Override
    public void remove() {
      // unsupported
    }
  }

  private class SqlRowIterator implements ClosableReportingIterator<String[]> {

    private final Connection conn;
    private final Statement stmt;
    private final ResultSet rs;
    private boolean hasNext;
    private final String sourceName;
    private final int rowSize;
    private boolean rowError;
    private String errorMessage;
    private Exception exception;
    private long rowCounter = 0;

    SqlRowIterator(SqlSource source) throws SQLException {
      this.conn = getDbConnection(source);
      this.stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      source.getRdbms().enableLargeResultSet(this.stmt);
      LOG.debug("Executing SQL {}", source.getSql());
      this.rs = stmt.executeQuery(source.getSql());
      this.rowSize = rs.getMetaData().getColumnCount();
      this.hasNext = rs.next();
      sourceName = source.getName();
      this.rowError = false;
    }

    @Override
    public void close() {
      if (rs != null) {
        try {
          rs.close();
          stmt.close();
          conn.close();
        } catch (SQLException e) {
          LOG.error("Can't close iterator for SQL source {}", sourceName, e);
        }
      }

    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public String[] next() {
      rowCounter++;
      String[] val = new String[rowSize];
      if (hasNext) {
        try {
          resetReportingIterator();
          int gotTo = 0; // field reached in row
          try {
            for (int i = 1; i <= rowSize; i++) {
              String s = rs.getString(i);
              val[i - 1] = s;

              if (s != null && s.indexOf('\u0000') >= 0) {
                String colName = rs.getMetaData().getColumnLabel(i);
                String colType = rs.getMetaData().getColumnTypeName(i);

                LOG.error(
                    "NUL detected at source. row={}, col={}, type={}, length={}, firstNulPos={}",
                    rowCounter,
                    colName,
                    colType,
                    s.length(),
                    s.indexOf('\u0000')
                );

                LOG.error("Trying to get as nString...");
                s  = rs.getNString(i);
                if (s != null && s.indexOf('\u0000') >= 0) {
                  LOG.error("NUL still present");
                } else {
                  LOG.info("No NUL present!");
                }

              }
              gotTo = i;
            }
          } catch (SQLException exOnRow) {
            LOG.debug("Exception caught reading row: {}", exOnRow.getMessage(), exOnRow);
            rowError = true;
            exception = exOnRow;

            // construct error message showing exception and problem row
            StringBuilder msg = new StringBuilder();
            msg.append("Exception caught reading row: ");
            msg.append(exOnRow.getMessage());
            msg.append("\n");
            msg.append("Row: ");
            for (int i = 0; i < gotTo; i++) {
              msg.append("[").append(val[i]).append("]");
            }
            errorMessage = msg.toString();
          } finally {
            // forward rs cursor
            hasNext = rs.next();
          }
        } catch (SQLException e2) {
          // Exception on advancing cursor, assume no more rows.
          LOG.debug("Exception caught advancing cursor: {}", e2.getMessage(), e2);
          hasNext = false;
          exception = e2;
          errorMessage = e2.getMessage();
        }
      }
      return val;
    }

    /**
     * Reset all iterator reporting parameters.
     */
    private void resetReportingIterator() {
      rowError = false;
      exception = null;
      errorMessage = null;
    }

    @Override
    public void remove() {
      // unsupported
    }

    @Override
    public boolean hasRowError() {
      return rowError;
    }

    @Override
    public String getErrorMessage() {
      return errorMessage;
    }

    @Override
    public Exception getException() {
      return exception;
    }
  }

  // default fetch sized used in SQL statements
  private static final int FETCH_SIZE = 10;
  // the maximum time in seconds that a driver will wait while attempting to connect to a database
  private static final int CONNECTION_TIMEOUT_SECS = 5;

  private static final String ACCEPTED_FILE_NAMES = "[\\w.\\-\\s\\)\\(]+";

  private FileStoreManager fileStoreManager;

  // Allowed characters in file names: alphanumeric characters, plus ".", "-", "_", ")", "(", and " "
  private Pattern acceptedPattern = Pattern.compile(ACCEPTED_FILE_NAMES);
  private final List<ResourceUpdateListener> listeners = new ArrayList<>();

  public SourceManagerImpl(AppConfig cfg, DataDir dataDir, FileStoreManager fileStoreManager) {
    super(cfg, dataDir);
    this.fileStoreManager = fileStoreManager;
  }

  // TODO: 24/04/2023 implement
  public static void copyArchiveFileProperties(File from, TextFileSource to) {
    to.setEncoding("UTF-8");
    to.setFieldsEnclosedBy(null);
    to.setFieldsTerminatedBy(",");
    to.setIgnoreHeaderLines(1);
    to.setDateFormat("YYYY-MM-DD");
  }
  public static void copyArchiveFileProperties(ArchiveFile from, TextFileSource to) {
    to.setEncoding(from.getEncoding());
    to.setFieldsEnclosedBy(from.getFieldsEnclosedBy() == null ? "\"" : from.getFieldsEnclosedBy().toString());
    to.setFieldsTerminatedBy(from.getFieldsTerminatedBy());
    to.setIgnoreHeaderLines(from.getIgnoreHeaderLines());
    to.setDateFormat(from.getDateFormat());
  }

  public static void copyArchiveFileProperties(ArchiveFile from, UrlSource to) {
    to.setEncoding(from.getEncoding());
    to.setFieldsEnclosedBy(from.getFieldsEnclosedBy() == null ? "\"" : from.getFieldsEnclosedBy().toString());
    to.setFieldsTerminatedBy(from.getFieldsTerminatedBy());
    to.setIgnoreHeaderLines(from.getIgnoreHeaderLines());
    to.setDateFormat(from.getDateFormat());
  }

  /**
   * Tests if the file name is composed of alphanumeric characters, plus ".", "-", "_", ")", "(", and " ".
   *
   * @param fileName the file name
   *
   * @return <tt> if accepted, <tt>false</tt> otherwise
   */
  protected boolean acceptableFileName(String fileName) {
    boolean matches = acceptedPattern.matcher(fileName).matches();
    if (!matches) {
      LOG.error("File name contains illegal characters: {}", fileName);
    }
    return matches;
  }

  private ExcelFileSource addExcelFile() throws ImportException {
    ExcelFileSource src = new ExcelFileSource();
    // TODO: encoding, header rows, date format?
    src.setSheetIdx(0);
    return src;
  }

  private TextFileSource addTextFile(File file) throws ImportException {
    TextFileSource src = new TextFileSource();
    try {
      // analyze individual files using the dwca reader
      Archive arch = DwcFiles.fromLocation(file.toPath());
      copyArchiveFileProperties(arch.getCore(), src);
    } catch (UnknownDelimitersException e) {
      // this file is invalid
      LOG.warn(e.getMessage());
      throw new ImportException(e);
    } catch (IOException e) {
      LOG.warn(e.getMessage());
      throw new ImportException(e);
    } catch (UnsupportedArchiveException e) {
      // fine, cant read it with dwca library, but might still be a valid file for manual setup
      LOG.warn(e.getMessage());
    }
    return src;
  }

  @Override
  public FileSource add(Resource resource, File file, String fileName) throws ImportException,
    InvalidFilenameException {
    LOG.debug("ADDING SOURCE {} FROM {}", fileName, file.getAbsolutePath());

    if (acceptableFileName(fileName)) {
      FileSource src;
      String suffix = FilenameUtils.getExtension(fileName);
      if (suffix != null && (suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx"))) {
        src = addExcelFile();
      } else {
        src = addTextFile(file);
      }

      src.setName(fileName);
      src.setResource(resource);

      try {
        // copy file
        File ddFile = dataDir.sourceFile(resource, src);
        try {
          FileUtils.copyFile(file, ddFile);
        } catch (IOException e1) {
          throw new ImportException(e1);
        }
        src.setFile(ddFile);
        src.setLastModified(new Date());

        // add to resource, allow overwriting existing ones
        // if the file is uploaded not for the first time
        resource.addSource(src, true);
      } catch (AlreadyExistingException e) {
        throw new ImportException(e);
      }

      // analyze file
      analyze(src);
      return src;
    } else {
      throw new InvalidFilenameException("Filename contains illegal characters");
    }
  }

  @Override
  public UrlSource add(Resource resource, URI url, String sourceName) throws ImportException {
    LOG.debug("ADDING URL SOURCE {}", url);

    UrlSource src;
    String filename = FilenameUtils.getName(url.toString());
    LOG.debug("File name: {}", filename);

    String finalSourceName;
    if (StringUtils.isEmpty(sourceName)) {
      finalSourceName = FilenameUtils.getBaseName(url.toString());
      LOG.debug("No source name provided, extract from URL: {}", finalSourceName);
    } else {
      finalSourceName = sourceName;
    }

    src = new UrlSource();
    File file = dataDir.sourceFile(resource, filename);
    src.setFile(file);
    src.setProcessing(true);

    downloadDataFromUrlAsync(resource, url, sourceName);

    src.setName(finalSourceName);
    src.setUrl(url);
    src.setResource(resource);

    try {
      UrlMetadata urlMetadata = org.gbif.ipt.utils.FileUtils.fetchUrlMetadata(url.toString());
      src.setFileSize(urlMetadata.getContentLength());
    } catch (IOException e) {
      LOG.error("Failed to read URL metadata from {}: {}", url.toString(), e.getMessage());
    }

    try {
      src.setLastModified(new Date());

      resource.addSource(src, true);
    } catch (AlreadyExistingException e) {
      throw new ImportException(e);
    }

    return src;
  }

  private void downloadDataFromUrlAsync(Resource resource, URI url, String sourceName) {
    LOG.info("Staring asynchronous download data from the URL {}", url);
    UUID key = UUID.randomUUID();

    try {
      String encodedFileURL = url.toString();

      Optional<String> redirectedUrl = URLUtils.getRedirectedUrl(encodedFileURL);
      if (redirectedUrl.isPresent()) {
        encodedFileURL = redirectedUrl.get();
      }

      FileStoreManager.AsyncDownloadResult downloadResult =
          fileStoreManager.downloadDataFile(
              encodedFileURL,
              dataDir.tmpFile(key.toString()).getAbsolutePath(),
              resultDataFile -> {
                LOG.info(
                    "File has been downloaded and decompressed from URL {}, key {}", url, key);
                processSuccessfulDownload(key, resource, sourceName, resultDataFile);
              },
              err -> {
                LOG.error("Error processing file", err);
                processFailedDownload(key, resource, sourceName, err.getMessage());
              });
      LOG.info("Asynchronous download {}", downloadResult);
    } catch (Exception e) {
      LOG.error("Failed to download data from URL {}", url);
      processFailedDownload(key, resource, sourceName, e.getMessage());
    }
  }

  private void processSuccessfulDownload(UUID key, Resource resource, String sourceName, DataFile dataFile) {
    LOG.info("Processing successful download {}", key);
    LOG.info("Data file: {}", dataFile);

    UrlSource src = (UrlSource) resource.getSource(sourceName);

    File downloadDir = dataDir.tmpFile(key.toString());
    try (Stream<Path> paths = Files.list(downloadDir.toPath()).filter(Files::isRegularFile)) {
      List<Path> files = paths.collect(Collectors.toList());

      // look for occurrence
      Optional<Path> targetFile;
      Optional<Path> matchingOccurrence = files.stream()
          .filter(path -> {
            String name = path.getFileName().toString().toLowerCase();
            return name.contains("occurrence");
          })
          .findFirst();
      targetFile = matchingOccurrence;

      if (matchingOccurrence.isEmpty()) {
        // look for event
        Optional<Path> matchingEvent = files.stream()
            .filter(path -> {
              String name = path.getFileName().toString().toLowerCase();
              return name.contains("event");
            })
            .findFirst();
        targetFile = matchingEvent;

        if (matchingEvent.isEmpty()) {
          // take largest
          targetFile = files.stream()
              .max(Comparator.comparingLong(path -> {
                try {
                  return Files.size(path);
                } catch (IOException e) {
                  return -1L;
                }
              }));
        }
      }

      if (targetFile.isPresent()) {
        processFile(resource, targetFile.get(), src);

        // Log the rest as skipped
        for (Path file : files) {
          if (!targetFile.get().equals(file)) {
            LOG.info("Skipped: {}", file.getFileName());
          }
        }
      } else {
        LOG.error("Failed to get file for processing. Resource {}, source {}", resource.getShortname(), sourceName);
      }

      FileUtils.deleteDirectory(downloadDir);

      notifyListeners(resource);
    } catch (IOException e) {
      LOG.error("Failed to process downloaded file(s) {}", key, e);
    } finally {
      src.setProcessing(false);
    }
  }

  private void processFile(Resource resource, Path targetFile, UrlSource src) throws IOException {
    // Process the target file
    LOG.info("Processing: {}", targetFile.getFileName());
    File sourceFile = dataDir.sourceFile(resource, targetFile.getFileName().toString());

    sourceFile.createNewFile();

    FileUtils.copyFile(targetFile.toFile(), sourceFile);
    LOG.info("Filed {} copied to resource {} sources",  targetFile.getFileName(), resource.getShortname());

    src.setFile(sourceFile);
    // analyze individual files using the dwca reader
    Archive arch = DwcFiles.fromLocation(sourceFile.toPath());
    copyArchiveFileProperties(arch.getCore(), src);

    LOG.info("Start analyzing source file {}", sourceFile);
    src.analyze();
  }

  private void processFailedDownload(UUID key, Resource resource, String sourceName, String errorMessage) {
    LOG.error("Error processing source {} with key {} for resource {}: {}",
        sourceName, key, resource.getShortname(), errorMessage);
    Source source = resource.getSource(sourceName);
    if (source != null) {
      source.setProcessing(false);
      source.setReadable(false);
    }
    notifyListeners(resource);
  }

  @Override
  public String analyze(Source source) {
    if (source instanceof SqlSource) {
      return analyze((SqlSource) source);
    } else if (source instanceof UrlSource) {
      return analyzeAsync((UrlSource) source);
    } else {
      return analyze((FileSource) source);
    }
  }

  private String analyzeAsync(UrlSource src) {
    // skip already in process
    if (src.isProcessing()) {
      LOG.info("URL source {} is already processing", src.getName());
      return null;
    }

    src.setProcessing(true);

    // download data from the URL and analyze it
    downloadDataFromUrlAsync(src.getResource(), src.getUrl(), src.getName());
    return null;
  }

  private String analyze(UrlSource src) {
    File logFile = dataDir.sourceLogFile(src.getResource().getShortname(), src.getName());
    try {
      FileUtils.deleteQuietly(logFile);

      Set<Integer> emptyLines;
      try {
        emptyLines = src.analyze();
      } catch (IOException e) {
        return e.getMessage();
      }

      try (BufferedWriter logWriter = Files.newBufferedWriter(logFile.toPath(), StandardCharsets.UTF_8)) {
        logWriter.write(
            "Log for source name:" + src.getName() + " from resource: " + src.getResource().getShortname() + "\n");
        if (!emptyLines.isEmpty()) {
          for (Integer i : emptyLines.stream().sorted().collect(Collectors.toList())) {
            logWriter.write("Line: " + i + " [EMPTY LINE]\n");
          }
        } else {
          logWriter.write("No rows were skipped in this source");
        }

        logWriter.flush();
      }
    } catch (IOException e) {
      LOG.warn("Can't write source log file {}", logFile.getAbsolutePath(), e);
    }

    return null;
  }

  private String analyze(SqlSource ss) {
    String problem = null;
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = getDbConnection(ss);
      // test sql
      if (StringUtils.trimToNull(ss.getSql()) != null) {
        stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if ((ss.getJdbcDriver() != null) && !ss.getJdbcDriver().contains("odbc")) {
            stmt.setFetchSize(FETCH_SIZE);
        }
        LOG.debug("Executing SQL {}", ss.getSqlLimited(FETCH_SIZE));
        rs = stmt.executeQuery(ss.getSqlLimited(FETCH_SIZE));
        // get number of columns
        ResultSetMetaData meta = rs.getMetaData();
        ss.setColumns(meta.getColumnCount());
        ss.setReadable(true);
      }
    } catch (SQLException e) {
      LOG.warn("Can't read SQL source {}", ss, e);
      problem = e.getMessage();
      ss.setReadable(false);
    } finally {
      // close result set, statement, and connection in that order
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          LOG.error("ResultSet could not be closed: {}", e.getMessage(), e);
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          LOG.error("Statement could not be closed: {}", e.getMessage(), e);
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          LOG.error("Connection could not be closed: {}", e.getMessage(), e);
        }
      }
    }
    return problem;
  }

  private String analyze(FileSource src) {
    String problem = null;
    File logFile = dataDir.sourceLogFile(src.getResource().getShortname(), src.getName());
    try {
      FileUtils.deleteQuietly(logFile);

      Set<Integer> emptyLines;
      try {
        emptyLines = src.analyze();
      } catch (IOException e) {
        return e.getMessage();
      }

      try (BufferedWriter logWriter = Files.newBufferedWriter(logFile.toPath(), StandardCharsets.UTF_8)) {
        logWriter.write(
            "Log for source name:" + src.getName() + " from resource: " + src.getResource().getShortname() + "\n");
        if (!emptyLines.isEmpty()) {
          for (Integer i : emptyLines.stream().sorted().collect(Collectors.toList())) {
            logWriter.write("Line: " + i + " [EMPTY LINE]\n");
          }
        } else {
          logWriter.write("No rows were skipped in this source");
        }

        logWriter.flush();
      }
    } catch (IOException e) {
      LOG.warn("Can't write source log file {}", logFile.getAbsolutePath(), e);
      problem = e.getMessage();
    }

    return problem;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> columns(Source source) {
    if (source == null) {
      return new ArrayList<>();
    }

    if (source instanceof SqlSource) {
      return columns((SqlSource) source);
    }

    if (source instanceof UrlSource) {
      return ((UrlSource) source).columns();
    }

    return ((FileSource) source).columns();
  }

  private List<String> columns(SqlSource source) {
    List<String> columns = new ArrayList<>();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = getDbConnection(source);
      if (con != null) {
        // test sql
        stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if ((source.getJdbcDriver() != null) && !source.getJdbcDriver().contains("odbc")) {
            stmt.setFetchSize(1);
        }
        LOG.debug("Executing SQL {}", source.getSqlLimited(1));
        rs = stmt.executeQuery(source.getSqlLimited(1));
        // get column metadata
        ResultSetMetaData meta = rs.getMetaData();
        int idx = 1;
        int max = meta.getColumnCount();
        while (idx <= max) {
          columns.add(meta.getColumnLabel(idx));
          idx++;
        }
      } else {
        String msg = "Can't read SQL source, the connection couldn't be created with the current parameters";
        columns.add(msg);
        LOG.warn("{} {}", msg, source);
      }
    } catch (SQLException e) {
      LOG.warn("Can't read SQL source {}", source, e);
    } finally {
      // close result set, statement, and connection in that order
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          LOG.error("ResultSet could not be closed: {}", e.getMessage(), e);
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          LOG.error("Statement could not be closed: {}", e.getMessage(), e);
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          LOG.error("Connection could not be closed: {}", e.getMessage(), e);
        }
      }
    }
    return columns;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean delete(Resource resource, Source source) {
    if (source == null) {
      return false;
    }

    resource.deleteSource(source);

    if (source instanceof UrlSource us) {
      // also delete the local data file
      boolean delete = us.getFile().delete();

      if (!delete) {
        LOG.error("Failed to delete the local file {} for the URL source {} of the resource {}",
            us.getFile().getAbsolutePath(), source.getName(), resource.getShortname());
      }
    }

    if (source instanceof TextFileSource fs) {
      // also delete source data file
      boolean delete = fs.getFile().delete();

      if (!delete) {
        LOG.error("Failed to delete the file {} for the Text file source {} of the resource {}",
            fs.getFile().getAbsolutePath(), source.getName(), resource.getShortname());
      }
    }

    if (source instanceof ExcelFileSource es) {
      // also delete source data file if no further source uses it
      boolean del = true;
      for (Source src : resource.getSources()) {
        if (!src.equals(es) && src.isExcelSource() && ((ExcelFileSource) src).getFile().equals(es.getFile())) {
          // another Excel source using the same file, don't delete
          del = false;
          break;
        }
      }
      if (del) {
        del = es.getFile().delete();

        if (!del) {
          LOG.error("Failed to delete the file {} for the Excel file source {} of the resource {}",
              es.getFile().getAbsolutePath(), source.getName(), resource.getShortname());
        }
      }
    }
    return true;
  }

  private Connection getDbConnection(SqlSource source) throws SQLException {
    Connection conn = null;
    // try to connect to db via simple JDBC
    if (source.getHost() != null && source.getJdbcUrl() != null && source.getJdbcDriver() != null) {
      try {
        DriverManager.setLoginTimeout(CONNECTION_TIMEOUT_SECS);
        Class.forName(source.getJdbcDriver());
        conn = DriverManager.getConnection(source.getJdbcUrl(), source.getUsername(), source.getPassword());
        // Disable Auto Commit to allow use of cursors (in PostgreSQL, but probably others too).
        conn.setAutoCommit(false);

        // If an SQLWarning object is available, log its warning(s). There may be multiple warnings chained.
        SQLWarning warn = conn.getWarnings();
        while (warn != null) {
          LOG.warn("SQLWarning: state={}, message={}, vendor={}", warn.getSQLState(), warn.getMessage(), warn
              .getErrorCode());
          warn = warn.getNextWarning();
        }
      } catch (java.lang.ClassNotFoundException e) {
        String msg =
          String
            .format(
              "Couldn't load JDBC driver to create new external datasource connection with JDBC Class=%s and URL=%s. Error: %s",
              source.getJdbcDriver(), source.getJdbcUrl(), e.getMessage());
        LOG.warn(msg, e);
        throw new SQLException(msg, e);
      } catch (Exception e) {
        String msg = String
          .format("Couldn't create new external datasource connection with JDBC Class=%s, URL=%s, user=%s. Error: %s",
            source.getJdbcDriver(), source.getJdbcUrl(), source.getUsername(), e.getMessage());
        LOG.warn(msg, e);
        throw new SQLException(msg);
      }
    }
    return conn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> inspectColumn(Source source, int column, int maxValues, int maxRows) throws SourceException {
    Set<String> values = new HashSet<>();
    try (ClosableIterator<Object> iter = iterSourceColumn(source, column, maxRows)){
      // get distinct values
      while (iter.hasNext() && (maxValues < 1 || values.size() < maxValues)) {
        Object obj = iter.next();
        if (obj != null) {
          String val = obj.toString();
          values.add(val);
        }
      }
    } catch (Exception e) {
      LOG.error(e);
      throw new SourceException("Error reading source " + source.getName() + ": " + e.getMessage());
    }
    return values;
  }

  /**
   * @param limit limit for the recordset passed into the sql. If negative or zero no limit will be used
   */
  private ClosableIterator<Object> iterSourceColumn(Source source, int column, int limit) throws Exception {
    if (source instanceof SqlSource src) {
      if (limit > 0) {
        return new SqlColumnIterator(src, column, limit);
      } else {
        return new SqlColumnIterator(src, column);
      }
    } else {
      return new ColumnIterator((RowIterable) source, column);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String[]> peek(Source source, int rows) {
    if (source instanceof SqlSource) {
      return peek((SqlSource) source, rows);
    } else {
      // Excel, file and URL sources
      return peek((RowIterable) source, rows);
    }
  }

  private List<String[]> peek(RowIterable source, int rows) {
    List<String[]> preview = new ArrayList<>();
    if (source != null) {
      try (ClosableReportingIterator<String[]> iter = source.rowIterator()){
        while (rows > 0 && iter.hasNext()) {
          rows--;
          preview.add(iter.next());
        }
      } catch (Exception e) {
        LOG.warn("Can't peek into source {}", ((Source) source).getName(), e);
      }
    }

    return preview;
  }

  private List<String[]> peek(SqlSource source, int rows) {
    List<String[]> preview = new ArrayList<>();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = getDbConnection(source);
      if (con != null) {
        // test SQL
        stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if ((source.getJdbcDriver() != null) && !source.getJdbcDriver().contains("odbc")) {
            stmt.setFetchSize(rows);
        }
        LOG.debug("Executing SQL {}", source.getSqlLimited(rows + 1));
        rs = stmt.executeQuery(source.getSqlLimited(rows + 1));
        // loop over result
        while (rows > 0 && rs.next()) {
          rows--;
          String[] row = new String[source.getColumns()];
          for (int idx = 0; idx < source.getColumns(); idx++) {
            row[idx] = rs.getString(idx + 1);
          }
          preview.add(row);
        }
      }
    } catch (SQLException e) {
      LOG.warn("Can't read SQL source {}", source, e);
    } finally {
      // close result set, statement, and connection in that order
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          LOG.error("ResultSet could not be closed: {}", e.getMessage(), e);
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          LOG.error("Statement could not be closed: {}", e.getMessage(), e);
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          LOG.error("Connection could not be closed: {}", e.getMessage(), e);
        }
      }
    }
    return preview;
  }

  @Override
  public ClosableReportingIterator<String[]> rowIterator(Source source) throws SourceException {
    if (source == null) {
      return null;
    }

    try {
      if (source instanceof SqlSource) {
        return new SqlRowIterator((SqlSource) source);
      } else {
        // Excel, file and URL sources
        return ((RowIterable) source).rowIterator();
      }
    } catch (Exception e) {
      LOG.error("Exception while reading source {}", source.getName(), e);
      throw new SourceException("Can't build iterator for source " + source.getName() + " :" + e.getMessage());
    }
  }

  @Override
  public void addListener(ResourceUpdateListener listener) {
    listeners.add(listener);
  }

  private void notifyListeners(Resource resource) {
    for (ResourceUpdateListener listener : listeners) {
      listener.onSourceUpdated(resource);
    }
  }
}
