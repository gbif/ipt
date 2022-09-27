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
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.IptColorScheme;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class UIManagementAction extends POSTAction {

  private static final long serialVersionUID = 2001100185337026057L;

  private static final Logger LOG = LogManager.getLogger(UIManagementAction.class);

  private DataDir dataDir;

  private IptColorScheme colorScheme;

  private File file;
  private String fileContentType;
  private boolean removeLogo;

  @Inject
  public UIManagementAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
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
    try {
      getCfg().saveColorSchemeConfig(colorScheme);
      if (removeLogo) {
        dataDir.removeLogoFile();
      }
    } catch (IOException e) {
      LOG.error("Exception while trying to change IPT UI settings", e);
    }
    return SUCCESS;
  }

  public String uploadLogo() {
    if (file != null) {
      // remove any previous logo file
      for (String suffix : Constants.IMAGE_TYPES) {
        FileUtils.deleteQuietly(dataDir.appLogoFile(suffix));
      }
      // inspect file type
      String type = "jpeg";
      if (fileContentType != null) {
        type = StringUtils.substringAfterLast(fileContentType, "/");
      }
      File logoFile = dataDir.appLogoFile(type);
      try {
        FileUtils.copyFile(file, logoFile);
      } catch (IOException e) {
        LOG.warn(e.getMessage());
      }
    }
    return INPUT;
  }

  public IptColorScheme getColorScheme() {
    return colorScheme;
  }

  public void setColorScheme(IptColorScheme colorScheme) {
    this.colorScheme = colorScheme;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFileContentType(String fileContentType) {
    this.fileContentType = fileContentType;
  }

  public boolean isRemoveLogo() {
    return removeLogo;
  }

  public void setRemoveLogo(boolean removeLogo) {
    this.removeLogo = removeLogo;
  }
}
