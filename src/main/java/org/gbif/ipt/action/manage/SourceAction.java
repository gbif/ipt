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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.CompressionUtil.UnsupportedCompressionType;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class SourceAction extends ManagerBaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(SourceAction.class);

  private SourceManager sourceManager;
  private JdbcSupport jdbcSupport;
  private DataDir dataDir;
  // config
  private Source source;
  private String rdbms;
  private String problem;
  private String sqlSourcePassword;
  // to store password internally
  private String sqlSourcePasswordCache;
  // URL
  private String url;
  private String sourceName;
  // file upload
  private File file;
  private String fileContentType;
  private String fileFileName;
  private boolean analyze = false;
  // preview
  private List<String> columns;
  private List<String[]> peek;
  private int peekRows = 10;
  private int analyzeRows = 1000;

  private String sourceType;

  @Inject
  public SourceAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, SourceManager sourceManager, JdbcSupport jdbcSupport, DataDir dataDir) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.sourceManager = sourceManager;
    this.jdbcSupport = jdbcSupport;
    this.dataDir = dataDir;
  }

  public String add() throws IOException {
    if (UiSourceType.SOURCE_URL.value.equals(sourceType)) {
      return addUrlSource();
    } else if (UiSourceType.SOURCE_FILE.value.equals(sourceType)) {
      return addFileSource();
    } else if (UiSourceType.SOURCE_SQL.value.equals(sourceType)) {
      return addSqlSource();
    } else {
      addActionError(getText("manage.source.type.nullOrUnknown"));
      return ERROR;
    }
  }

  private String addUrlSource() {
    if (StringUtils.isEmpty(url)) {
      addActionError(getText("manage.source.url.empty"));
      return ERROR;
    }

    if (StringUtils.isEmpty(sourceName)) {
      addActionError(getText("manage.source.name.empty"));
      return ERROR;
    }

    // prepare a new, empty URL source
    source = new UrlSource();
    source.setResource(resource);
    URI urlWrapped = URI.create(url);

    // check URL is fine otherwise throw an exception
    try {
      HttpURLConnection connection = (HttpURLConnection) urlWrapped.toURL().openConnection();
      int responseCode = connection.getResponseCode();

      // check not found
      if (responseCode == 404) {
        addActionError(getText("manage.source.url.notFound", new String[] {url}));
        return ERROR;
      }

      // check text file (or no extension)
      String extension = FilenameUtils.getExtension(url);
      if (!extension.isEmpty() && !"txt".equals(extension) && !"tsv".equals(extension) && !"csv".equals(extension) && !"zip".equals(extension)) {
        addActionError(getText("manage.source.url.invalidExtension", new String[] {url, extension}));
        return ERROR;
      }
    } catch (IOException e) {
      addActionError(getText("manage.source.url.invalid", new String[] {url}));
      return ERROR;
    }

    addUrl(urlWrapped);

    return SUCCESS;
  }

  private String addFileSource() {
    // uploaded a new file. Is it compressed?
    // application/zip, application/x-gzip
    if (StringUtils.endsWithAny(fileContentType.toLowerCase(), "zip", "gzip", "compressed")) {
      try {
        File tmpDir = dataDir.tmpDir();
        // override auto-generated name
        String unzippedFileName = fileFileName != null
            ? fileFileName.substring(0, fileFileName.lastIndexOf(".")) : null;

        List<File> files = CompressionUtil.decompressFile(tmpDir, file, unzippedFileName);
        addActionMessage(getText("manage.source.compressed.files", new String[]{String.valueOf(files.size())}));

        // import each file
        for (File f : files) {
          addDataFile(f, f.getName());
        }
      } catch (IOException e) {
        LOG.error(e);
        addActionError(getText("manage.source.filesystem.error", new String[]{e.getMessage()}));
        return ERROR;
      } catch (UnsupportedCompressionType e) {
        LOG.error(e);
        addActionError(getText("manage.source.unsupported.compression.format"));
        return ERROR;
      } catch (InvalidFilenameException e) {
        LOG.error(e);
        addActionError(getText("manage.source.invalidFileName"));
        return ERROR;
      } catch (Exception e) {
        LOG.error(e);
        addActionError(getText("manage.source.upload.unexpectedException"));
        return ERROR;
      }
    } else {
      try {
        // treat as is - hopefully a simple text or Excel file
        addDataFile(file, fileFileName);
      } catch (InvalidFilenameException e) {
        LOG.error(e);
        addActionError(getText("manage.source.invalidFileName"));
        return ERROR;
      } catch (Exception e) {
        LOG.error(e);
        addActionError(getText("manage.source.upload.unexpectedException"));
        return ERROR;
      }
    }

    return SUCCESS;
  }

  private String addSqlSource() {
    if (StringUtils.isEmpty(sourceName)) {
      addActionError(getText("manage.source.name.empty"));
      return ERROR;
    }

    source = new SqlSource();
    source.setResource(resource);
    source.setName(sourceName);
    ((SqlSource) source).setRdbms(jdbcSupport.get("mysql"));

    try {
      resource.addSource(source, true);
    } catch (AlreadyExistingException e) {
      addActionError(getText("manage.source.existing"));
      return ERROR;
    }

    // set sources modified date
    resource.setSourcesModified(new Date());
    // save resource
    saveResource();
    id = source.getName();

    return SUCCESS;
  }

  private void addUrl(URI url) {
    Source existingSource = resource.getSource(sourceName);
    boolean replaced = existingSource != null;

    try {
      source = sourceManager.add(resource, url, sourceName);
      resource.setSourcesModified(new Date());
      saveResource();
      id = source.getName();

      if (replaced) {
        addActionMessage(getText("manage.source.replaced.existing", new String[] {source.getName()}));
        // alert user if the number of columns changed
        alertColumnNumberChange(resource.hasMappedSource(existingSource), source.getColumns(),
            existingSource.getColumns());
      } else {
        addActionMessage(getText("manage.source.added.new", new String[] {source.getName()}));
      }
    } catch (ImportException e) {
      // even though we have problems with this source we'll keep it for manual corrections
      LOG.error("Cannot add URL source " + url, e);
      addActionError(getText("manage.source.cannot.add", new String[]{sourceName, e.getMessage()}));
    }
  }

  /**
   * Add new file source to resource, replacing existing source if they have the exact same name.
   *
   * @param f file source
   * @param filename filename
   *
   * @throws InvalidFilenameException
   */
  private void addDataFile(File f, String filename) throws InvalidFilenameException {
    Source existingSource = resource.getSource(filename);
    boolean replaced = existingSource != null;
    try {
      source = sourceManager.add(resource, f, filename);
      // set sources modified date
      resource.setSourcesModified(new Date());
      // save resource
      saveResource();
      id = source.getName();
      if (replaced) {
        addActionMessage(getText("manage.source.replaced.existing", new String[] {source.getName()}));
        // alert user if the number of columns changed
        alertColumnNumberChange(resource.hasMappedSource(existingSource), source.getColumns(),
          existingSource.getColumns());
      } else {
        addActionMessage(getText("manage.source.added.new", new String[] {source.getName()}));
      }
    } catch (ImportException e) {
      // even though we have problems with this source we'll keep it for manual corrections
      LOG.error("Cannot add source " + filename + ": " + e.getMessage(), e);
      addActionError(getText("manage.source.cannot.add", new String[] {filename, e.getMessage()}));
    } catch (InvalidFilenameException e) {
      // clean session variables used for confirming file overwrite
      throw e;
    }
  }

  /**
   * Alert user if the number of columns changed, when the existing source has already been mapped.
   *
   * @param sourceIsMapped true if source is mapped, false otherwise
   * @param number         current number of columns in source
   * @param originalNumber original number of columns in source
   *
   * @return true if alert was sent, false otherwise
   */
  protected boolean alertColumnNumberChange(boolean sourceIsMapped, int number, int originalNumber) {
    if (sourceIsMapped) {
      if (originalNumber != number) {
        addActionWarning(getText("manage.source.numColumns.changed",
          new String[] {source.getName(), String.valueOf(originalNumber), String.valueOf(number)}));
      return true;
      }
    }
    return false;
  }

  @Override
  public String delete() {
    if (sourceManager.delete(resource, resource.getSource(id))) {
      addActionMessage(getText("manage.source.deleted", new String[] {id}));
      // set sources modified date
      resource.setSourcesModified(new Date());
      saveResource();
    } else {
      addActionMessage(getText("manage.source.deleted.couldnt", new String[] {id}));
    }
    return SUCCESS;
  }

  public List<String> getColumns() {
    return columns;
  }

  public TextFileSource getFileSource() {
    if (source instanceof TextFileSource) {
      return (TextFileSource) source;
    }
    return null;
  }

  public Map<String, String> getJdbcOptions() {
    return jdbcSupport.optionMap();
  }

  public boolean getLogExists() {
    return dataDir.sourceLogFile(resource.getShortname(), source.getName()).exists();
  }

  public List<String[]> getPeek() {
    return peek;
  }

  public int getPreviewSize() {
    return peekRows;
  }

  public String getProblem() {
    return problem;
  }

  public String getRdbms() {
    return rdbms;
  }

  public String getSqlSourcePassword() {
    return sqlSourcePassword;
  }

  public Source getSource() {
    return source;
  }

  public SqlSource getSqlSource() {
    if (source instanceof SqlSource) {
      return (SqlSource) source;
    }
    return null;
  }

  public String peek() {
    if (source == null) {
      return NOT_FOUND;
    }
    peek = sourceManager.peek(source, peekRows);
    columns = sourceManager.columns(source);
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    session.remove(Constants.SESSION_FILE_NUMBER_COLUMNS);
    if (id != null) {
      source = resource.getSource(id);
      if (source == null) {
        LOG.error("No source with id " + id + " found!");
        addActionError(getText("manage.source.cannot.load", new String[] {id}));
      } else {
        // store original number of columns, in case they change the user should be warned to update its mappings
        session.put(Constants.SESSION_FILE_NUMBER_COLUMNS, source.getColumns());
      }

      // we don't display password after saving, store password internally
      if (source instanceof SqlSource) {
        String pw = ((SqlSource) source).getPassword();
        if (StringUtils.isNotEmpty(pw)) {
          sqlSourcePasswordCache = pw;
        }
      }
    } else if (file == null) {
      // prepare a new, empty sql source
      source = new SqlSource();
      source.setResource(resource);
      ((SqlSource) source).setRdbms(jdbcSupport.get("mysql"));
    } else {
      // prepare a new, empty file source
      source = new TextFileSource();
      source.setResource(resource);
    }
  }

  @Override
  public String save() throws IOException {
    if (source != null) {
      source.setLastModified(new Date());
    }

    // treat jdbc special
    if (source != null && rdbms != null) {
      ((SqlSource) source).setRdbms(jdbcSupport.get(rdbms));
    }

    String result = INPUT;
    if (id != null && source != null) {
      if (this.analyze || !source.isReadable()) {
        problem = sourceManager.analyze(source);
      } else {
        result = SUCCESS;
      }
    }
    resource.setSourcesModified(new Date());
    // save resource
    saveResource();

    return result;
  }

  public void setAnalyze(String analyze) {
    if (StringUtils.trimToNull(analyze) != null) {
      this.analyze = true;
    }
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFileContentType(String fileContentType) {
    this.fileContentType = fileContentType;
  }

  public void setFileFileName(String fileFileName) {
    this.fileFileName = fileFileName;
  }

  public void setRdbms(String jdbc) {
    this.rdbms = jdbc;
    if (source != null && source instanceof SqlSource) {
      ((SqlSource) source).setRdbms(jdbcSupport.get(rdbms));
    }
  }

  public void setSqlSourcePassword(String sqlSourcePassword) {
    if (source != null && source instanceof SqlSource) {
      // ignore empty password
      if (StringUtils.isNotBlank(sqlSourcePassword)) {
        ((SqlSource) source).setPassword(sqlSourcePassword);
        // source should be re-analyzed after password update
        this.analyze = true;
      }
    }
  }

  public void setRows(int previewSize) {
    this.peekRows = previewSize > 0 ? previewSize : 10;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  @Override
  public void validateHttpPostOnly() {
    if (source != null) {
      // ALL SOURCES
      // check if title exists already as a source
      if (StringUtils.trimToEmpty(source.getName()).length() == 0) {
        addFieldError("source.name", getText("validation.required", new String[] {getText("source.name")}));
      } else if (StringUtils.trimToEmpty(source.getName()).length() < 3) {
        addFieldError("source.name", getText("validation.short", new String[] {getText("source.name"), "3"}));
      } else if (id == null && resource.getSources().contains(source)) {
        addFieldError("source.name", getText("manage.source.unique"));
      }
      if (source instanceof SqlSource) {
        // SQL SOURCE
        SqlSource src = (SqlSource) source;

        // restore password if it was not sent from UI
        if (StringUtils.isEmpty(src.getPassword()) && StringUtils.isNotEmpty(sqlSourcePasswordCache)) {
          ((SqlSource) source).setPassword(sqlSourcePasswordCache);
        }

        // pure ODBC connections need only a DSN, no server
        if (StringUtils.trimToEmpty(src.getHost()).length() == 0 && rdbms != null && !rdbms.equalsIgnoreCase("odbc")) {
          addFieldError("sqlSource.host", getText("validation.required", new String[] {getText("sqlSource.host")}));
        } else if (StringUtils.trimToEmpty(src.getHost()).length() < 2) {
          addFieldError("sqlSource.host", getText("validation.short", new String[] {getText("sqlSource.host"), "2"}));
        }
        if (StringUtils.trimToEmpty(src.getDatabase()).length() == 0) {
          addFieldError("sqlSource.database",
            getText("validation.required", new String[] {getText("sqlSource.database")}));
        } else if (StringUtils.trimToEmpty(src.getDatabase()).length() < 2) {
          addFieldError("sqlSource.database",
            getText("validation.short", new String[] {getText("sqlSource.database"), "2"}));
        }
      }
      // alert user if the number of columns changed
      Integer originalNumberColumns = (Integer) session.get(Constants.SESSION_FILE_NUMBER_COLUMNS);
      if (originalNumberColumns != null) {
        alertColumnNumberChange(resource.hasMappedSource(source), sourceManager.columns(source).size(),
          originalNumberColumns);
      }
    }
  }

  enum UiSourceType {
    SOURCE_FILE("source-file"),
    SOURCE_URL("source-url"),
    SOURCE_SQL("source-sql");

    final String value;

    UiSourceType(String value) {
      this.value = value;
    }
  }
}
