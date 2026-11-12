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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceDataTableViewManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;

import java.io.Serial;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.annotations.JSON;

import lombok.Getter;

public class HomeAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = -7395504502586148676L;

  private DatatableResult resources = new DatatableResult();

  private final ResourceDataTableViewManager resourceManager;
  /**
   * list of organisations that can host
   */
  @Getter
  private List<Organisation> organisations;
  /**
   * organisations of the resources the user can manage, for the organisation filter: name by key
   */
  @Getter
  private Map<String, String> resourceOrganisations = Collections.emptyMap();
  /**
   * whether the resources of this IPT belong to more than one organisation, so the table shows the organisation
   */
  @Getter
  private boolean showOrganisationColumn;

  @Inject
  public HomeAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceDataTableViewManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
  }

  @Override
  public String execute() {
    DatatableRequest dr = getRequestParameters(ServletActionContext.getRequest());
    resources = resourceManager.list(getCurrentUser(), dr);

    // load organisations able to host
    organisations = registrationManager.list();

    resourceOrganisations = organisationFilterOptions(resourceManager.listOrganisations(getCurrentUser()));
    showOrganisationColumn = resourceManager.hasSeveralOrganisations();

    return SUCCESS;
  }

  /**
   * The organisation filter's options: "Organization not set" (the default organisation) last.
   *
   * @param organisations name by key, sorted by name
   * @return name by key
   */
  private Map<String, String> organisationFilterOptions(Map<String, String> organisations) {
    String defaultKey = Constants.DEFAULT_ORG_KEY.toString();
    if (!organisations.containsKey(defaultKey)) {
      return organisations;
    }
    Map<String, String> result = new LinkedHashMap<>(organisations);
    result.remove(defaultKey);
    result.put(defaultKey, getText("manage.home.organisation.notSet"));
    return result;
  }

  /**
   * Constructs request object from HTTP request to get required resources.
   *
   * @param request HTTP request
   * @return resource request object
   */
  private DatatableRequest getRequestParameters(HttpServletRequest request) {
    DatatableRequest result = new DatatableRequest();
    result.setLocale(getLocaleLanguage());
    getRequestParameter(request, Constants.DATATABLE_SEARCH_PARAM)
        .map(StringUtils::trimToEmpty)
        .ifPresent(result::setSearch);
    getRequestParameter(request, Constants.DATATABLE_ORDER_DIRECTORY_PARAM)
        .ifPresent(result::setSortOrder);
    getRequestParameter(request, Constants.DATATABLE_ORDER_COLUMN_PARAM)
        .map(Integer::parseInt)
        .ifPresent(result::setSortFieldIndex);
    getRequestParameter(request, Constants.DATATABLE_START_PARAM)
        .map(Long::parseLong)
        .ifPresent(result::setOffset);
    getRequestParameter(request, Constants.DATATABLE_LENGTH_PARAM)
        .map(Integer::parseInt)
        .ifPresent(result::setLimit);
    // the manage page's organisation filter
    getRequestParameter(request, "organisation")
        .map(StringUtils::trimToEmpty)
        .ifPresent(result::setOrganisation);

    return result;
  }

  @JSON
  public DatatableResult getResources() {
    return resources;
  }

  /**
   * method for dealing with the action for a locked resource.
   * Does nothing but the regular home plus an error message
   */
  public String locked() {
    addActionError(getText("manage.home.resource.locked"));
    return execute();
  }

}
