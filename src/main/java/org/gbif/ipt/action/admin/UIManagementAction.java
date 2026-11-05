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
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.IptColorScheme;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptFileUtils;

import jakarta.inject.Inject;
import java.io.File;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;

import lombok.Getter;

public class UIManagementAction extends POSTAction implements UploadedFilesAware {

  @Serial
  private static final long serialVersionUID = 2001100185337026057L;

  private static final Logger LOG = LogManager.getLogger(UIManagementAction.class);

  private final DataDir dataDir;

  private IptColorScheme colorScheme;
  private List<UploadedFile> uploadedFiles = new ArrayList<>();
  @Getter
  private boolean removeLogo;

  @Inject
  public UIManagementAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      DataDir dataDir) {
    super(textProvider, cfg, registrationManager);
    this.dataDir = dataDir;
  }

  @Override
  public void prepare() {
    super.prepare();
    colorScheme = getCfg().getColorSchemeConfig();
  }

  @Override
  public String save() {
    File logo = uploadedLogo();
    String logoType = logo == null ? null : IptFileUtils.imageType(logo);
    if (logo != null && logoType == null) {
      addFieldError("file", getText("validation.logo.type"));
      return INPUT;
    }

    try {
      getCfg().saveColorSchemeConfig(colorScheme);

      if (logo != null) {
        // a failed copy leaves the previous logo in place
        File logoFile = dataDir.appLogoFile(logoType);
        Files.createDirectories(logoFile.toPath().getParent());
        Files.copy(logo.toPath(), logoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        dataDir.removeLogoFile(logoFile);
      } else if (removeLogo) {
        dataDir.removeLogoFile();
      }
    } catch (Exception e) {
      LOG.error("Exception while trying to change IPT UI settings", e);
      addActionError(getText("admin.uiManagement.error.failedToSafe"));
      return INPUT;
    }

    addActionMessage(getText("admin.uiManagement.success"));
    return SUCCESS;
  }

  /**
   * @return the uploaded logo, or null if no file was chosen
   */
  private File uploadedLogo() {
    if (uploadedFiles == null) {
      return null;
    }
    return uploadedFiles.stream()
        .filter(Objects::nonNull)
        .filter(f -> f.getContent() instanceof File file && file.length() > 0)
        .map(f -> (File) f.getContent())
        .findFirst()
        .orElse(null);
  }

  @StrutsParameter(depth = 1)
  public IptColorScheme getColorScheme() {
    return colorScheme;
  }

  @Override
  public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
    this.uploadedFiles = uploadedFiles;
  }

  @StrutsParameter
  public void setRemoveLogo(boolean removeLogo) {
    this.removeLogo = removeLogo;
  }
}
