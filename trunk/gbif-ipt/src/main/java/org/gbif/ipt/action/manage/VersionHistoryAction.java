package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.math.BigDecimal;
import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Action that allows a editing the change summaries in a resource's VersionHistory.
 */
public class VersionHistoryAction extends ManagerBaseAction {

  private static final Logger LOG = Logger.getLogger(VersionHistoryAction.class);

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
   * TODO
   * @return
   */
  public String getSummary() {
    return summary;
  }

  /**
   * TODO
   * @param summary
   */
  public void setSummary(String summary) {
    this.summary = StringUtils.trimToNull(summary);
  }
}
