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
import org.gbif.ipt.utils.URLUtils;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.CompressionUtil.UnsupportedCompressionType;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.activation.MimeTypeParseException;
import javax.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.ServletActionContext;

import lombok.Getter;

public class SourceAction extends ManagerBaseAction implements UploadedFilesAware {

  @Serial
  private static final long serialVersionUID = 3324051864626106131L;

  private static final Logger LOG = LogManager.getLogger(SourceAction.class);

  private SourceManager sourceManager;
  private JdbcSupport jdbcSupport;
  private DataDir dataDir;
  // config
  private Source source;
  @Getter
  private String rdbms;
  @Getter
  private String problem;
  @Getter
  private String sqlSourcePassword;
  // to store password internally
  private String sqlSourcePasswordCache;
  // URL
  private String url;
  private String sourceName;
  // file upload
  private List<UploadedFile> uploadedFiles = new ArrayList<>();
  private boolean analyze = false;
  // preview
  @Getter
  private List<String> columns;
  @Getter
  private List<String[]> peek;
  private int peekRows = 10;
  private int analyzeRows = 1000;

  private String sourceType;

  @Inject
  public SourceAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      SourceManager sourceManager,
      JdbcSupport jdbcSupport,
      DataDir dataDir) {
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
        LOG.error("Can't read URL {} : Not Found", url);
        addActionError(getText("manage.source.url.notFound", new String[]{url}));
        return ERROR;
      }

      // check a text file (or no extension)
      String extension = FilenameUtils.getExtension(url);
      boolean extensionNotAllowed = (!extension.isEmpty()
          && !Strings.CS.equalsAny(extension, "txt", "tsv", "csv", "zip"));

      if (extensionNotAllowed) {
        LOG.debug("No extension in the URL, checking content type.");
        String contentType = URLUtils.getUrlContentType(url);
        LOG.debug("Content type confirmed: {}", contentType);

        // check a mime type
        if (!URLUtils.VALID_CONTENT_TYPES.contains(contentType)) {
          LOG.error("Not allowed content type: {}", contentType);
          addActionError(getText("manage.source.url.invalidExtension", new String[]{url, extension}));
          return ERROR;
        }
      }
    } catch (IOException | MimeTypeParseException e) {
      LOG.error("Failed to create a URL source: {}", e.getMessage());
      addActionError(getText("manage.source.url.invalid", new String[]{url}));
      return ERROR;
    }

    addUrl(urlWrapped);

    return SUCCESS;
  }

  private String addFileSource() {
    if (uploadedFiles == null || uploadedFiles.isEmpty()) {
      addErrorHeader("manage.source.upload.unexpectedException");
      addActionError(getText("manage.source.upload.unexpectedException"));
      return ERROR;
    }

    List<String> createdSourceNames = new ArrayList<>();

    for (UploadedFile upload : uploadedFiles) {
      if (upload == null || upload.getContent() == null) {
        continue; // ignore empty slots, but don’t fail whole request
      }

      File content = (File) upload.getContent();
      String contentType = StringUtils.defaultString(upload.getContentType()).toLowerCase();
      String originalName = StringUtils.trimToNull(upload.getOriginalName());

      // uploaded a new file. Is it compressed?
      // application/zip, application/x-gzip
      if (Strings.CS.endsWithAny(contentType.toLowerCase(), "zip", "gzip", "compressed")) {
        try {
          File tmpDir = dataDir.tmpDir();
          // override auto-generated name
          String unzippedFileName = originalName != null && originalName.contains(".")
              ? originalName.substring(0, originalName.lastIndexOf("."))
              : null;

          List<File> files = CompressionUtil.decompressFile(tmpDir, content, unzippedFileName);
          addActionMessage(getText("manage.source.compressed.files", new String[]{String.valueOf(files.size())}));

          // import each file from the archive
          for (File f : files) {
            String created = addDataFileAndReturnSourceName(f, f.getName());
            if (created != null) {
              createdSourceNames.add(created);
            }
          }
        } catch (IOException e) {
          LOG.error(e);
          addErrorHeader("manage.source.filesystem.error");
          addActionError(getText("manage.source.filesystem.error", new String[]{e.getMessage()}));
          return ERROR;
        } catch (UnsupportedCompressionType e) {
          LOG.error(e);
          addErrorHeader("manage.source.unsupported.compression.format");
          addActionError(getText("manage.source.unsupported.compression.format"));
          return ERROR;
        } catch (InvalidFilenameException e) {
          LOG.error(e);
          addErrorHeader("manage.source.invalidFileName.archive");
          addActionError(getText("manage.source.invalidFileName.archive"));
          return ERROR;
        } catch (Exception e) {
          LOG.error(e);
          addErrorHeader("manage.source.upload.unexpectedException");
          addActionError(getText("manage.source.upload.unexpectedException"));
          return ERROR;
        }
      } else {
        try {
          // treat as is - hopefully a simple text or Excel file
          String created = addDataFileAndReturnSourceName(content, originalName);
          if (created != null) {
            createdSourceNames.add(created);
          }
        } catch (InvalidFilenameException e) {
          LOG.error(e);
          addErrorHeader("manage.source.invalidFileName");
          addActionError(getText("manage.source.invalidFileName"));
          return ERROR;
        } catch (Exception e) {
          LOG.error(e);
          addErrorHeader("manage.source.upload.unexpectedException");
          addActionError(getText("manage.source.upload.unexpectedException"));
          return ERROR;
        }
      }
    }

    if (createdSourceNames.isEmpty()) {
      addErrorHeader("manage.source.upload.unexpectedException");
      addActionError(getText("manage.source.upload.unexpectedException"));
      return ERROR;
    }

    if (createdSourceNames.size() == 1) {
      addActionMessage(getText("manage.overview.source.created", new String[]{createdSourceNames.get(0)}));
    } else {
      addActionMessage(getText("manage.overview.sources.created", new String[]{String.join(", ", createdSourceNames)}));
    }

    return SUCCESS;
  }

  private String addDataFileAndReturnSourceName(File f, String filename) throws InvalidFilenameException {
    Source existingSource = resource.getSource(filename);
    boolean replaced = existingSource != null;

    try {
      source = sourceManager.add(resource, f, filename);

      resource.setSourcesModified(new Date());
      saveResource();

      id = source.getName();

      if (replaced) {
        addActionMessage(getText("manage.source.replaced.existing", new String[]{source.getName()}));
        alertColumnNumberChange(resource.hasMappedSource(existingSource), source.getColumns(), existingSource.getColumns());
      } else {
        addActionMessage(getText("manage.source.added.new", new String[]{source.getName()}));
      }

      return source.getName();

    } catch (ImportException e) {
      LOG.error("Cannot add source {}: {}", filename, e.getMessage(), e);
      addActionError(getText("manage.source.cannot.add", new String[]{filename, e.getMessage()}));
      return null;
    }
  }

  private void addErrorHeader(String value) {
    HttpServletResponse response = ServletActionContext.getResponse();
    response.setHeader("X-Error-Message", getText(value));
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
      LOG.error("Cannot add URL source {}", url, e);
      addActionError(getText("manage.source.cannot.add", new String[]{sourceName, e.getMessage()}));
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

  public int getPreviewSize() {
    return peekRows;
  }

  @StrutsParameter(depth = 2)
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
        LOG.error("No source with id {} found!", id);
        addActionError(getText("manage.source.cannot.load", new String[] {id}));
      } else {
        // store the original number of columns, in case they change, the user should be warned to update its mappings
        session.put(Constants.SESSION_FILE_NUMBER_COLUMNS, source.getColumns());
      }

      // we don't display password after saving, store password internally
      if (source instanceof SqlSource sqlSrc) {
        String pw = sqlSrc.getPassword();
        if (StringUtils.isNotEmpty(pw)) {
          sqlSourcePasswordCache = pw;
        }
      }
    } else if (uploadedFiles == null || uploadedFiles.isEmpty()) {
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
        result = "analyze";
        if (problem == null) {
          if (source instanceof UrlSource) {
            addActionMessage(getText("manage.source.analyze.inProcess"));
          } else {
            addActionMessage(getText("manage.source.analyzed"));
          }
        } else {
          addActionError(getText("manage.source.analyzed.problem", new String[] {problem}));
        }
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

  @StrutsParameter
  public void setUrl(String url) {
    this.url = url;
  }

  @StrutsParameter
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  @StrutsParameter
  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  @StrutsParameter
  public void setRdbms(String jdbc) {
    this.rdbms = jdbc;
    if (source != null && source instanceof SqlSource) {
      ((SqlSource) source).setRdbms(jdbcSupport.get(rdbms));
    }
  }

  @StrutsParameter
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
      if (StringUtils.trimToEmpty(source.getName()).isEmpty()) {
        addFieldError("source.name", getText("validation.required", new String[] {getText("source.name")}));
      } else if (StringUtils.trimToEmpty(source.getName()).length() < 3) {
        addFieldError("source.name", getText("validation.short", new String[] {getText("source.name"), "3"}));
      } else if (id == null && resource.getSources().contains(source)) {
        addFieldError("source.name", getText("manage.source.unique"));
      }
      if (source instanceof SqlSource src) {
        // restore password if it was not sent from the UI
        if (StringUtils.isEmpty(src.getPassword()) && StringUtils.isNotEmpty(sqlSourcePasswordCache)) {
          src.setPassword(sqlSourcePasswordCache);
        }

        // pure ODBC connections need only a DSN, no server
        if (StringUtils.trimToEmpty(src.getHost()).isEmpty() && rdbms != null && !rdbms.equalsIgnoreCase("odbc")) {
          addFieldError("sqlSource.host", getText("validation.required", new String[] {getText("sqlSource.host")}));
        } else if (StringUtils.trimToEmpty(src.getHost()).length() < 2) {
          addFieldError("sqlSource.host", getText("validation.short", new String[] {getText("sqlSource.host"), "2"}));
        }
        if (StringUtils.trimToEmpty(src.getDatabase()).isEmpty()) {
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

  @Override
  public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
    this.uploadedFiles = uploadedFiles;
  }

  @StrutsParameter(depth = 2)
  public Source getSource() {
    return source;
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
