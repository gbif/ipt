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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.action.ParameterNameAware;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;
import lombok.Setter;

public class ManagerBaseAction extends POSTAction implements ParameterNameAware {

  @Serial
  private static final long serialVersionUID = -7385261456013846954L;

  protected final ResourceManager resourceManager;
  // the resource of the r parameter, set by prepare()
  @Setter
  protected Resource resource;
  // shown on the 404 page instead of the generic "resource does not exist" text, e.g. for a missing source
  @Getter
  private String notFoundTitle;
  @Getter
  private String notFoundBody;
  // shown on the 400 page instead of the generic "missing resource parameter" text
  @Getter
  private String badRequestBody;

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
   * Sets the text the 400 page shows instead of the default one, which is about a missing resource parameter.
   *
   * @param bodyKey i18n key of the 400 page text
   * @param args arguments of the text
   * @return the 400 result
   */
  protected String badRequest(String bodyKey, String... args) {
    badRequestBody = getText(bodyKey, args);
    return BAD_REQUEST;
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

  @StrutsParameter(depth = 1)
  public Resource getResource() {
    return resource;
  }

  /**
   * The resource.* parameters this action's forms submit. The resource is the live object held by the resource
   * manager, so any other resource.* parameter is refused. None by default.
   *
   * @return the accepted resource.* parameter names
   */
  protected Set<String> resourceParameters() {
    return Set.of();
  }

  /**
   * Nested parameters (e.g. resource.coreType, source.name, eml.title) set fields of the live resource and its
   * objects, so they are only accepted with a POST request, which is CSRF-checked. A GET request, including the AJAX
   * ones (peek, report), sets only simple parameters such as r, id and mid. Of the resource, only
   * {@link #resourceParameters()} are accepted.
   */
  @Override
  public boolean acceptableParameterName(String parameterName) {
    if (!isHttpPost() && isNestedParameter(parameterName)) {
      return false;
    }
    return !isPropertyOf(parameterName, "resource") || resourceParameters().contains(parameterName);
  }

  /**
   * @return true if the parameter name sets something on an object rather than a simple action field, e.g.
   * "resource.title", "eml['title']" or "tmap(x)"
   */
  protected static boolean isNestedParameter(String parameterName) {
    return parameterName != null && StringUtils.containsAny(parameterName, '.', '[', '(');
  }

  /**
   * @return true if the parameter name sets something on the given action property, e.g. "resource.title" or
   * "resource['title']" for "resource". Compared ignoring case.
   */
  protected static boolean isPropertyOf(String parameterName, String property) {
    if (parameterName == null || !parameterName.regionMatches(true, 0, property, 0, property.length())) {
      return false;
    }
    if (parameterName.length() == property.length()) {
      return true;
    }
    char next = parameterName.charAt(property.length());
    return next == '.' || next == '[' || next == '(';
  }

}
