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
package org.gbif.ipt.action.portal;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import javax.inject.Inject;

public class FallbackAction extends BaseAction {

  private static final long serialVersionUID = 5075091500212058562L;

  @Inject
  public FallbackAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager) {
    super(textProvider, cfg, registrationManager);
  }

  @Override
  public void prepare() {
    super.prepare();
  }
}
