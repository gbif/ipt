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
import org.gbif.ipt.struts2.RequirePost;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptFileUtils;

import jakarta.inject.Inject;
import java.io.File;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;

import lombok.Getter;

public class AppearanceAction extends POSTAction implements UploadedFilesAware {

  @Serial
  private static final long serialVersionUID = 2001100185337026057L;

  private static final Logger LOG = LogManager.getLogger(AppearanceAction.class);

  private final DataDir dataDir;

  private IptColorScheme colorScheme;
  private List<UploadedFile> uploadedFiles = new ArrayList<>();
  @Getter
  private boolean removeLogo;

  @Inject
  public AppearanceAction(
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
    // a copy: the posted values must not reach the pages before they are checked and saved
    colorScheme = new IptColorScheme(getCfg().getColorSchemeConfig());
  }

  @Override
  public String save() {
    if (!validColours()) {
      return INPUT;
    }

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
   * Normalises each posted colour to #rrggbb; a field error for each one that isn't a hex colour.
   *
   * @return true if all colours are valid
   */
  private boolean validColours() {
    String primary = check("primaryColor", colorScheme.getPrimaryColor());
    String navbar = check("navbarColor", colorScheme.getNavbarColor());
    String navbarLink = check("navbarLinkColor", colorScheme.getNavbarLinkColor());
    String logoColour = check("navbarGbifLogoColor", colorScheme.getNavbarGbifLogoColor());
    String activeTab = check("navbarActiveTabColor", colorScheme.getNavbarActiveTabColor());
    String link = check("linkColor", colorScheme.getLinkColor());
    if (hasFieldErrors()) {
      return false;
    }
    colorScheme = new IptColorScheme(primary, navbar, navbarLink, logoColour, activeTab, link);
    return true;
  }

  private String check(String field, String value) {
    String hex = IptColorScheme.normalizeHex(value);
    if (hex == null) {
      addFieldError("colorScheme." + field, getText("admin.uiManagement.error.colour"));
    }
    return hex;
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

  /**
   * @return the colour schemes to start from, by key; the first is the default
   */
  public Map<String, IptColorScheme> getPresets() {
    return IptColorScheme.presets();
  }

  /**
   * @return the administrator's colours from before the light navigation bar became the default, or null
   */
  public IptColorScheme getPreviousColorScheme() {
    return getCfg().getPreviousColorScheme();
  }

  /**
   * @return true while the new default colours replace the administrator's own colours
   */
  public boolean isColorSchemeReplaced() {
    return getCfg().isColorSchemeReplaced();
  }

  /**
   * @return true when an administrator uploaded a logo, checked the way AppFileAction serves it
   */
  public boolean isCustomLogo() {
    return Arrays.stream(Constants.IMAGE_TYPES).anyMatch(type -> dataDir.appLogoFile(type).exists());
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
