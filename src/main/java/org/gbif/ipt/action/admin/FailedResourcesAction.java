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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceLoadFailure;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import jakarta.inject.Inject;
import java.io.File;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin-only list of resource folders that failed to load, with the reason, and of resources loaded with problems.
 * Resources that failed to load don't show anywhere else; they can be loaded again from here.
 */
public class FailedResourcesAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = 1L;

  private final ResourceManager resourceManager;
  private final DataDir dataDir;

  private List<ResourceLoadFailure> failures = new ArrayList<>();
  private List<Resource> resourcesWithNotices = new ArrayList<>();
  // shortname of the resource to load again
  private String r;

  @Inject
  public FailedResourcesAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      DataDir dataDir) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.dataDir = dataDir;
  }

  @Override
  public String execute() {
    failures = resourceManager.listLoadFailures();
    resourcesWithNotices = resourceManager.listWithLoadNotices();
    return SUCCESS;
  }

  /**
   * Loads a resource that failed to load again, e.g. after its files were fixed. POST only: it changes what the IPT
   * has loaded, and may install extensions.
   */
  public String retry() {
    if (!isHttpPost()) {
      return execute();
    }
    try {
      Resource resource = resourceManager.retryLoad(r, getCurrentUser());
      addActionMessage(getText("admin.failedResources.retry.success", new String[]{resource.getShortname()}));
    } catch (IllegalArgumentException e) {
      addActionError(getText("admin.failedResources.retry.notFound", new String[]{r}));
    } catch (InvalidConfigException e) {
      addActionError(getText("admin.failedResources.retry.failed", new String[]{r}));
    }
    return execute();
  }

  @StrutsParameter
  public void setR(String r) {
    this.r = r;
  }

  public List<ResourceLoadFailure> getFailures() {
    return failures;
  }

  public List<Resource> getResourcesWithNotices() {
    return resourcesWithNotices;
  }

  /**
   * @return the absolute path of the resources folder, so admins can find the failed resource folders
   */
  public String getResourcesDirPath() {
    File resourcesDir = dataDir.dataFile(DataDir.RESOURCES_DIR);
    return resourcesDir == null ? "" : resourcesDir.getAbsolutePath();
  }
}
