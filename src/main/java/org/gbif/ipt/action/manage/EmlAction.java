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
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;

import com.google.inject.Inject;

public class EmlAction extends ManagerBaseAction  {

  private File emlFile;

  @Inject
  public EmlAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                        ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
  }

  public void setEmlFile(File emlFile) {
    this.emlFile = emlFile;
  }

  public String replaceEml() {
    try {
      resourceManager.replaceEml(resource, emlFile);
      addActionMessage(getText("manage.overview.success.replace.eml"));
      return SUCCESS;
    }
    catch(ImportException e) {
      addActionError(getText("manage.overview.failed.replace.eml"));
      return ERROR;
    }
  }
}
