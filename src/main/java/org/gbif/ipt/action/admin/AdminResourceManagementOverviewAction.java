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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import lombok.Getter;

public class AdminResourceManagementOverviewAction extends POSTAction {

  private static final long serialVersionUID = 8401862206351723866L;

  private final ResourceManager resourceManager;

  @Getter
  private String shortname = null;
  @Getter
  private Resource resource;
  @Getter
  private boolean resourceSuccessfullyLoaded = false;
  @Getter
  private List<File> resourceFiles = new ArrayList<>();
  @Getter
  private File resourceFile;
  @Getter
  private final List<File> sourceFiles = new ArrayList<>();
  @Getter
  private final List<File> metadataFiles = new ArrayList<>();
  @Getter
  private final List<File> generatedArchives = new ArrayList<>();
  @Getter
  private final List<File> otherFiles = new ArrayList<>();

  @Inject
  public AdminResourceManagementOverviewAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                                       ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
  }

  @Override
  public void prepare() {
    super.prepare();

    // look for resource parameter
    shortname = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
    if (shortname == null) {
      // try session instead
      try {
        shortname = (String) session.get(Constants.SESSION_RESOURCE);
      } catch (Exception e) {
        // swallow. if session is not yet opened we get an exception here...
      }
    }

    if (shortname != null) {
      resourceFiles = resourceManager.listAllResourceFiles(shortname);

      for (File file : resourceFiles) {
        if (file.getName().equals("resource.xml")) {
          resourceFile = file;
        } else if (file.getName().startsWith("metadata")
            || file.getName().startsWith("eml")
            || file.getName().startsWith("inferredMetadata")) {
          metadataFiles.add(file);
        } else if (file.getName().startsWith("sources")) {
          sourceFiles.add(file);
        } else if (file.getName().startsWith("dwca") || file.getName().startsWith("datapackage")) {
          generatedArchives.add(file);
        } else {
          otherFiles.add(file);
        }
      }
    }

    resource = resourceManager.get(shortname);

    if (resource != null) {
      resourceSuccessfullyLoaded = true;
    } else {
      resource = resourceManager.getFailed(shortname);
    }
  }

  @Override
  public String execute() {
    return SUCCESS;
  }

  public String formattedFileSize(long fileSize, String locale) {
    return FileUtils.formatSize(fileSize, 1, locale, true);
  }

  public String formattedFileSizeSimplified(long fileSize) {
    return FileUtils.formatSize(fileSize, 1, "en", true);
  }
}
