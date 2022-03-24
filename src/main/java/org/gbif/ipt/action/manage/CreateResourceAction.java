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

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class CreateResourceAction extends POSTAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(CreateResourceAction.class);

  private ResourceManager resourceManager;
  private DataDir dataDir;
  private File file;
  private String fileContentType;
  private String fileFileName;
  private String shortname;
  private String resourceType;
  private Map<String, String> types;
  private List<Organisation> organisations;
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
  public void prepare() {
    super.prepare();
    // load organisations able to host
    organisations = registrationManager.list();
  }

  @Override
  public String save() throws IOException {
    // validation already checked that shortname was not null and valid
    if (resourceManager.get(shortname) != null) {
      addFieldError("resource.shortname", getText("validation.resource.shortname.exists", new String[] {shortname}));
      return INPUT;
    }

    Date start = new Date();
    // 10 seconds subtracted to accommodate differences in file system date resolution (e.g. Mac HFS has 1s resolution)
    long startTimeInMs = start.getTime() - 10000;
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
      // remove resource and its resource folder from data directory
      cleanupResourceFolder(shortname, startTimeInMs);
      return INPUT;
    } catch (InvalidFilenameException e) {
      addActionError(getText("manage.source.invalidFileName"));
      return INPUT;
    }
    return SUCCESS;
  }

  /**
   * Delete resource and recursively delete its resource folder from data directory.
   * As a safeguard, the resource folder must have been created after the provided start time in milliseconds.
   *
   * @param shortname shortname of resource to delete
   * @param startTimeInMs date when resource creation started in milliseconds
   */
  protected void cleanupResourceFolder(String shortname, long startTimeInMs) {
    Objects.requireNonNull(shortname);

    Resource resource = resourceManager.get(shortname);
    File directory = new File(dataDir.dataFile(DataDir.RESOURCES_DIR), shortname);
    if (resource != null && directory.exists() && directory.isDirectory() && directory.lastModified() > startTimeInMs) {
      LOG.info("Deleting resource and its folder from data directory: " + directory);
      try {
        resourceManager.delete(resource, true);
      } catch (IOException e) {
        LOG.error("Deleting resource failed: " + e.getMessage(), e);
      } catch (DeletionNotAllowedException e) {
        LOG.error("Deleting resource not allowed", e);
      }
    }
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
    // write the file to the file specified
    try (InputStream input = new FileInputStream(file);
         OutputStream output = new FileOutputStream(tmpFile)) {
      IOUtils.copy(input, output);
      output.flush();
      LOG.debug("Uploaded file " + fileFileName + " with content-type " + fileContentType);
    } catch (IOException e) {
      LOG.error(e);
      throw new ImportException("Failed to upload file to tmp file", e);
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
    types = new LinkedHashMap<>();
    types.put("", getText("manage.resource.create.coreType.selection"));
    types.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_TYPE, getLocaleLanguage(), false));
    types = MapUtils.getMapWithLowercaseKeys(types);
    return types;
  }

  /**
   * @return list of organisations that can host
   */
  public List<Organisation> getOrganisations() {
    return organisations;
  }

  /**
   * @return DataDir
   */
  public DataDir getDataDir() {
    return dataDir;
  }
}
