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
package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.UUID;

import com.google.inject.Inject;

public class AboutAction extends BaseAction {

  private static final long serialVersionUID = -471175839075190159L;

  private final AppConfig cfg;
  private String title;
  private UUID iptKey;
  private String iptDescription;
  private String hostingOrganisationName;

  @Inject
  public AboutAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager) {
    super(textProvider, cfg, registrationManager);
    this.cfg = cfg;
  }

  public String getTitle() {
    return title;
  }

  public String getPortalUrl() {
    return cfg.getPortalUrl();
  }

  @Override
  public void prepare() {
    Ipt ipt = registrationManager.getIpt();
    Organisation org = registrationManager.getHostingOrganisation();

    if (ipt != null) {
      iptKey = ipt.getKey();
      iptDescription = ipt.getDescription();
    }

    if (org != null) {
      hostingOrganisationName = org.getName();
    }

    // if registered - get title from registration data
    // if not - try to get title form ipt.properties
    // otherwise, just use default value
    if (ipt != null && ipt.getName() != null) {
      title = ipt.getName();
    } else if (cfg.getProperty("about.title") != null) {
      title = cfg.getProperty("about.title");
    } else {
      title = getText("about.title");
    }
  }

  public UUID getIptKey() {
    return iptKey;
  }

  public String getIptDescription() {
    return iptDescription;
  }

  public String getHostingOrganisationName() {
    return hostingOrganisationName;
  }
}
