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

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;
import lombok.Setter;

public class ManagerBaseAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = -7385261456013846954L;

  // the resourceManager session is populated by the resource interceptor and kept alive for an entire manager session
  protected final ResourceManager resourceManager;
  @Setter
  protected Resource resource;
  // shown on the 404 page instead of the generic "resource does not exist" text, e.g. for a missing source
  @Getter
  private String notFoundTitle;
  @Getter
  private String notFoundBody;

  @Inject
  public ManagerBaseAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
  }

  @Override
  public void prepare() {
    super.prepare();
    // the resource comes from the request parameter only, never from the session: a resource stored in the session
    // can belong to another tab or an earlier page. RequireManagerInterceptor returns a 400 without it, see
    // requiresResource().
    String res = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
    resource = res == null ? null : resourceManager.get(res);
    if (resource == null) {
      notFound = true;
    }
  }

  /**
   * Marks the request as not found, with a message for the 404 page saying what is missing.
   *
   * @param titleKey i18n key of the 404 page title
   * @param bodyKey i18n key of the 404 page text
   * @param args arguments of the text
   */
  protected void notFound(String titleKey, String bodyKey, String... args) {
    notFound = true;
    notFoundTitle = getText(titleKey);
    notFoundBody = getText(bodyKey, args);
  }

  /**
   * Whether the action works on a single resource, given by the r parameter. RequireManagerInterceptor returns
   * a 400 for such actions when the parameter is missing. Override for actions that don't need a resource.
   *
   * @return true if the r parameter is required, false otherwise
   */
  public boolean requiresResource() {
    return true;
  }

  protected void saveResource() {
    try {
      resourceManager.save(resource);
    } catch (InvalidConfigException e) {
      addActionError(e.getMessage());
    }
  }

  @StrutsParameter(depth = 8)
  public Resource getResource() {
    return resource;
  }
}
