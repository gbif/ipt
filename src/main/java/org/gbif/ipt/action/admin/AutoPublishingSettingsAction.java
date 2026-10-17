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
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;

/**
 * Admin-only action to enable or disable scheduled (auto) publishing for the whole IPT instance.
 * Per-resource schedules are never modified: turning the switch off only suppresses them,
 * so turning it back on restores everything.
 */
public class AutoPublishingSettingsAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LogManager.getLogger(AutoPublishingSettingsAction.class);

  private final ConfigManager configManager;

  @Getter
  private boolean autoPublishingEnabled;
  @Getter
  private boolean autoPublishingAdminOnly;

  @Inject
  public AutoPublishingSettingsAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager regManager,
      ConfigManager configManager) {
    super(textProvider, cfg, regManager);
    this.configManager = configManager;
  }

  @Override
  public void prepare() {
    super.prepare();
    // Only pre-fill on GET; on POST the submitted values must win
    if (!isHttpPost()) {
      autoPublishingEnabled = cfg.isAutoPublishingEnabled();
      autoPublishingAdminOnly = cfg.isAutoPublishingAdminOnly();
    }
  }

  @Override
  public String save() {
    boolean previousEnabled = cfg.isAutoPublishingEnabled();
    boolean previousAdminOnly = cfg.isAutoPublishingAdminOnly();

    // The admin-only checkbox is hidden while auto-publishing is off, so whatever it submits is not
    // a deliberate choice. Keep the stored preference so it survives switching auto-publishing off and on.
    if (!autoPublishingEnabled) {
      autoPublishingAdminOnly = previousAdminOnly;
    }

    try {
      configManager.setAutoPublishingSettings(autoPublishingEnabled, autoPublishingAdminOnly);
    } catch (InvalidConfigException e) {
      LOG.error("Failed to save auto-publishing settings", e);
      addActionError(getText("admin.autoPublishing.save.failed"));
      return INPUT;
    }

    // Simple audit trail: who changed what
    String who = getCurrentUser() == null ? "unknown" : getCurrentUser().getEmail();
    if (previousEnabled != autoPublishingEnabled) {
      LOG.info("Auto-publishing {} instance-wide by {}", autoPublishingEnabled ? "ENABLED" : "DISABLED", who);
    }
    if (previousAdminOnly != autoPublishingAdminOnly) {
      LOG.info("Auto-publishing restricted to administrators: {} (changed by {})", autoPublishingAdminOnly, who);
    }

    addActionMessage(getText(autoPublishingEnabled
        ? "admin.autoPublishing.saved.enabled"
        : "admin.autoPublishing.saved.disabled"));
    return SUCCESS;
  }

  @StrutsParameter
  public void setAutoPublishingEnabled(boolean autoPublishingEnabled) {
    this.autoPublishingEnabled = autoPublishingEnabled;
  }

  @StrutsParameter
  public void setAutoPublishingAdminOnly(boolean autoPublishingAdminOnly) {
    this.autoPublishingAdminOnly = autoPublishingAdminOnly;
  }
}
