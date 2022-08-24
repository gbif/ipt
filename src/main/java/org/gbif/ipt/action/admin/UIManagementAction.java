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
import org.gbif.ipt.model.IptColorScheme;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import com.google.inject.Inject;

import java.io.IOException;

public class UIManagementAction extends POSTAction {

  private static final long serialVersionUID = 2001100185337026057L;

  private IptColorScheme colorScheme;

  @Inject
  public UIManagementAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager) {
    super(textProvider, cfg, registrationManager);
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
    } catch (IOException e) {
      // TODO: 23/08/2022 handle IOException
    }
    return SUCCESS;
  }

  public IptColorScheme getColorScheme() {
    return colorScheme;
  }

  public void setColorScheme(IptColorScheme colorScheme) {
    this.colorScheme = colorScheme;
  }
}
