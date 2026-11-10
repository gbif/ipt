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
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.voc.DataPackageMetadataSection;
import org.gbif.ipt.model.voc.FrictionlessMetadataSection;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.DataPackageMetadataValidator;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.Date;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

/**
 * Frictionless data package metadata (e.g. ColDP). Camtrap DP metadata is handled by {@link CamtrapMetadataAction}.
 */
public class DataPackageMetadataAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = -1669636958170716515L;

  private final DataPackageMetadataValidator metadataValidator;
  private final DataPackageMetadataSection section = FrictionlessMetadataSection.BASIC_SECTION;

  @Inject
  public DataPackageMetadataAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      DataPackageMetadataValidator metadataValidator) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.metadataValidator = metadataValidator;
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  @Override
  public void prepare() {
    super.prepare();
    // no user logged in, or no resource: RequireManagerInterceptor already returned the login page, a 400 or a 404,
    // so this only happens when the action runs without it, e.g. in tests
    if (session.get(Constants.SESSION_USER) == null || resource == null) {
      return;
    }

    // an EML resource, a Camtrap DP (it has its own pages), or a data package without Frictionless metadata (ColDP)
    DataPackageMetadata metadata = resource.getDataPackageMetadata();
    if (!(metadata instanceof FrictionlessMetadata) || metadata instanceof CamtrapMetadata) {
      notFound("manage.404.page.title", "manage.404.page.body", resource.getShortname());
      return;
    }

    if (isHttpPost()) {
      FrictionlessMetadata<?, ?, ?> frictionless = (FrictionlessMetadata<?, ?, ?>) metadata;
      frictionless.getContributors().clear();
      frictionless.getLicenses().clear();
      frictionless.getSources().clear();
    }
  }

  @Override
  public String save() throws Exception {
    // before saving, the minimum amount of mandatory metadata must have been provided, and ALL metadata sections must
    // be valid, otherwise an error is displayed
    if (metadataValidator.isSectionValid(this, resource, section)) {
      // Save metadata information (datapackage.json)
      resourceManager.saveDatapackageMetadata(resource);
      // save date metadata was last modified
      resource.setMetadataModified(new Date());
      // Alert user of successful save
      addActionMessage(getText("manage.success", new String[]{getText("submenu.datapackagemetadata." + section.getName())}));
      // Save resource information (resource.xml)
      resourceManager.save(resource);
    } else {
      return INPUT;
    }

    return SUCCESS;
  }

  public String getSection() {
    return section.getName();
  }

  @StrutsParameter(depth = 8)
  public DataPackageMetadata getMetadata() {
    return resource.getDataPackageMetadata();
  }
}
