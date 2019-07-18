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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    if (!Strings.isNullOrEmpty(v)) {
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
