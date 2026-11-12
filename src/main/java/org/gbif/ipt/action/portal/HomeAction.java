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
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;
import org.gbif.ipt.model.datatable.HomeSearchRequest;
import org.gbif.ipt.model.datatable.HomeSearchResult;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceDataTableViewManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.annotations.JSON;

import java.io.Serial;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = 8504283506326312600L;

  private final ResourceDataTableViewManager resourceManager;
  private DatatableResult resources = new DatatableResult();
  private HomeSearchResult searchResult = new HomeSearchResult();

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
    resources = resourceManager.listPublishedPublicResourceSummaries(dr);

    return SUCCESS;
  }

  /**
   * The public resources for the home page: one page matching the search and filters, with the filter values counted.
   * Parameters: q, type, organisation and licence (each repeatable), updated (month, year or older), sort (published,
   * title or records), offset and limit.
   */
  public String search() {
    HttpServletRequest request = ServletActionContext.getRequest();
    HomeSearchRequest hsr = new HomeSearchRequest();
    hsr.setLocale(getLocaleLanguage());
    hsr.setSearch(StringUtils.trimToEmpty(request.getParameter("q")));
    hsr.setTypes(parameterValues(request, "type"));
    hsr.setOrganisations(parameterValues(request, "organisation"));
    hsr.setLicences(parameterValues(request, "licence"));
    hsr.setUpdated(StringUtils.trimToEmpty(request.getParameter("updated")));
    hsr.setSort(StringUtils.defaultIfBlank(request.getParameter("sort"), HomeSearchRequest.SORT_PUBLISHED));
    hsr.setOffset(NumberUtils.toLong(request.getParameter("offset"), 0));
    hsr.setLimit(NumberUtils.toInt(request.getParameter("limit"), 10));
    searchResult = resourceManager.searchPublishedPublicResources(hsr);
    return SUCCESS;
  }

  private static Set<String> parameterValues(HttpServletRequest request, String name) {
    String[] values = request.getParameterValues(name);
    return values == null ? new LinkedHashSet<>() : Arrays.stream(values)
        .map(StringUtils::trimToNull)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
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

    return result;
  }

  /**
   * Ensure resource table shows properties coming from last published versions of resources where applicable (e.g.
   * resource title should be the title assigned to last published version, not the title of current
   * (unpublished) version that may still be under editing.
   */
  @Override
  public void prepare() {
    super.prepare();
  }

  /**
   * A list of last published versions for all public or registered resources.
   *
   * @return a list of resources
   */
  @JSON
  public DatatableResult getResources() {
    return resources;
  }

  /**
   * The result of {@link #search()}.
   */
  @JSON
  public HomeSearchResult getSearchResult() {
    return searchResult;
  }

  public int getResourcesSize() {
    return resources != null ? resources.getTotalRecords() : 0;
  }
}
