package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.validation.ResourceValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class CreateResourceAction extends POSTAction {

  // logging
  private static final Logger LOG = Logger.getLogger(CreateResourceAction.class);

  private ResourceManager resourceManager;
  private DataDir dataDir;
  private File file;
  private String fileContentType;
  private String fileFileName;
  private String shortname;
  private String resourceType;
  private Map<String, String> types;
  private final VocabulariesManager vocabManager;
  private final ResourceValidator validator = new ResourceValidator();

  @Inject
  public CreateResourceAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, DataDir dataDir, VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.dataDir = dataDir;
    this.vocabManager = vocabManager;
  }

  public String getShortname() {
    return shortname;
  }

  @Override
  public String save() throws IOException {
    try {
      File tmpFile = uploadToTmp();
      if (tmpFile == null) {
        resourceManager.create(shortname, resourceType, getCurrentUser());
      } else {
        resourceManager.create(shortname, resourceType, tmpFile, getCurrentUser(), this);
      }
    } catch (AlreadyExistingException e) {
      addFieldError("resource.shortname", getText("validation.resource.shortname.exists", new String[] {shortname}));
      return INPUT;
    } catch (ImportException e) {
      LOG.error("Error importing the dwc archive: " + e.getMessage(), e);
      addActionError(getText("validation.resource.import.exception"));
      return INPUT;
    } catch (InvalidFilenameException e) {
      addActionError(getText("manage.source.invalidFileName"));
      return INPUT;
    }
    return SUCCESS;
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

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }

  /**
   * Upload file to temp file.
   *
   * @return uploaded file
   *
   * @throws ImportException if file type was invalid
   */
  private File uploadToTmp() throws ImportException {
    if (fileFileName == null) {
      return null;
    }
    // the file to upload to
    File tmpFile = dataDir.tmpFile(shortname, fileFileName);
    LOG.debug("Uploading dwc archive file for new resource " + shortname + " to " + tmpFile.getAbsolutePath());
    // retrieve the file data
    InputStream input = null;
    OutputStream output = null;
    try {
      input = new FileInputStream(file);
      // write the file to the file specified
      output = new FileOutputStream(tmpFile);
      IOUtils.copy(input, output);
      output.flush();
      LOG.debug("Uploaded file " + fileFileName + " with content-type " + fileContentType);
    } catch (IOException e) {
      LOG.error(e);
      throw new ImportException("Failed to upload file to tmp file", e);
    } finally {
      if (output != null) {
        IOUtils.closeQuietly(output);
      }
      if (input != null) {
        IOUtils.closeQuietly(input);
      }
    }
    return tmpFile;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      validator.validateShortname(this, shortname);
    }
  }

  /**
   * Resource (core) type.
   *
   * @return resource core type
   */
  @Nullable
  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * Get map of resource types to populate resource type selection.
   * Dataset core type list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
   *
   * @return map of resource types
   */
  public Map<String, String> getTypes() {
    types = new LinkedHashMap<String, String>();
    types.put("", getText("manage.resource.create.coreType.selection"));
    types.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_TYPE, getLocaleLanguage(), false));
    types = MapUtils.getMapWithLowercaseKeys(types);
    return types;
  }
}
