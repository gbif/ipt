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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.voc.DataPackageMetadataSection;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class DataPackageMetadataAction extends ManagerBaseAction {

  private static final Logger LOG = LogManager.getLogger(DataPackageMetadataAction.class);

  private static final long serialVersionUID = -1669636958170716515L;

  private DataPackageMetadataSection section = DataPackageMetadataSection.BASIC_SECTION;
  private DataPackageMetadataSection next = DataPackageMetadataSection.GEOGRAPHIC_SECTION;

  @Inject
  public DataPackageMetadataAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                                   ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  // TODO: 13/10/2022 implement prepare
  @Override
  public void prepare() {
    super.prepare();
    if (session.get(Constants.SESSION_USER) == null) {
      return;
    }

    // take the section parameter from the requested url
    section = DataPackageMetadataSection.fromName(StringUtils.substringBetween(req.getRequestURI(), "datapackage-metadata-", "."));

    switch (section) {
      case BASIC_SECTION:
        break;

      case GEOGRAPHIC_SECTION:
        break;

      case TAXONOMIC_SECTION:
        break;

      case TEMPORAL_SECTION:
        break;

      case PROJECT_SECTION:
        break;

      case OTHER_SECTION:
        break;

      default: break;
    }
  }

  @Override
  public String save() throws Exception {
    // before saving, the minimum amount of mandatory metadata must have been provided, and ALL metadata sections must
    // be valid, otherwise an error is displayed
    // TODO: 13/10/2022 implement validation
    if (true) {
      // Save metadata information (datapackage.json)
      resourceManager.saveDatapackageMetadata(resource);
      // save date metadata was last modified
      resource.setMetadataModified(new Date());
      // Alert user of successful save
      addActionMessage(getText("manage.success", new String[] {getText("submenu." + section.getName())}));
      // Save resource information (resource.xml)
      resourceManager.save(resource);

      // progress to next section, since save succeeded
      switch (section) {
        case BASIC_SECTION:
          next = DataPackageMetadataSection.GEOGRAPHIC_SECTION;
          break;
        case GEOGRAPHIC_SECTION:
          next = DataPackageMetadataSection.TAXONOMIC_SECTION;
          break;
        case TAXONOMIC_SECTION:
          next = DataPackageMetadataSection.TEMPORAL_SECTION;
          break;
        case TEMPORAL_SECTION:
          next = DataPackageMetadataSection.PROJECT_SECTION;
          break;
        case PROJECT_SECTION:
          next = DataPackageMetadataSection.OTHER_SECTION;
          break;
        case OTHER_SECTION:
          next = DataPackageMetadataSection.BASIC_SECTION;
          break;
        default: break;
      }
    } else {
      // stay on the same section, since save failed
      next = section;
    }

    return SUCCESS;
  }

  public String getNext() {
    return next.getName();
  }

  public String getSection() {
    return section.getName();
  }

  public DataPackageMetadata getMetadata() {
    return resource.getDataPackageMetadata();
  }
}
