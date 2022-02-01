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
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

/**
 * Action that allows a editing a resource version's change summary.
 */
public class VersionHistoryAction extends ManagerBaseAction {

  private static final Logger LOG = LogManager.getLogger(VersionHistoryAction.class);

  protected BigDecimal version;
  private String summary;

  @Inject
  public VersionHistoryAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
  }

  @Override
  public void prepare() {
    super.prepare();

    // look for version parameter
    String v = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_VERSION));
    if (StringUtils.isNotBlank(v)) {
      try {
        setVersion(new BigDecimal(v));
      } catch (NumberFormatException e) {
        LOG.error("Parameter version (v) was not a valid number: " + String.valueOf(v));
      }
    }
  }

  @Override
  public String save() {
    if (getVersion() != null && getResource() != null && getSummary() != null) {
      VersionHistory history = resource.findVersionHistory(getVersion());
      if (history != null) {
        history.setChangeSummary(getSummary());

        // update who is responsible for update
        User user = getCurrentUser();
        if (user != null) {
          history.setModifiedBy(user);
        }

        // save resource
        saveResource();
        return SUCCESS;
      } else {
        addActionError("The version history for version " + getVersion() + " does not exist!");
      }
    }
    addActionError("The change summary for this version could not be updated. Please refresh the page and try again.");
    return ERROR;
  }

  public String back() {
    return SUCCESS;
  }

  /**
   * Return the version number requested. Null version is equal to the latest published version.
   *
   * @return the version number requested
   */
  @Nullable
  public BigDecimal getVersion() {
    return version;
  }

  /**
   * Version number of the resource requested. Null version is equal to the latest published version.
   *
   * @param version version number requested
   */
  public void setVersion(BigDecimal version) {
    this.version = version;
  }

  /**
   * Return the version number requested. Null version is equal to the latest published version.
   *
   * @return the version number requested
   */
  public String getVersionString() {
    return version.toPlainString();
  }

  /**
   * @return the change summary (for resource version) as it was entered by the user in the form
   */
  public String getSummary() {
    return summary;
  }

  /**
   * @param summary the change summary (for version) entered by the user in the form, defaulting to empty string
   */
  public void setSummary(String summary) {
    this.summary = StringUtils.trimToEmpty(summary);
  }
}
