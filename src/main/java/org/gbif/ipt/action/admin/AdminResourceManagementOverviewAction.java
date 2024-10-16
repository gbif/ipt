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
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import com.google.inject.Inject;

public class AdminResourceManagementOverviewAction extends POSTAction {

  private static final long serialVersionUID = 8401862206351723866L;

  @Inject
  public AdminResourceManagementOverviewAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                                       ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
  }

  @Override
  public void prepare() {
    super.prepare();
  }

  @Override
  public String execute() {
    return SUCCESS;
  }
}
