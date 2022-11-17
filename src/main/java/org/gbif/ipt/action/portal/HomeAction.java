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
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.annotations.JSON;

import com.google.inject.Inject;

public class HomeAction extends BaseAction {

  private final ResourceManager resourceManager;
  private final VocabulariesManager vocabManager;

  private DatatableResult resources;
  private Map<String, String> types;
  private Map<String, String> datasetSubtypes;

  @Inject
  public HomeAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.vocabManager = vocabManager;
  }

  @Override
  public String execute() {
    DatatableRequest dr = getRequestParameters(ServletActionContext.getRequest());
    resources = resourceManager.listPublishedPublicVersionsSimplified(dr);

    return SUCCESS;
  }

  /**
   * Constructs request object from HTTP request to get required resources.
   *
   * @param request HTTP request
   * @return resource request object
   */
  private DatatableRequest getRequestParameters(HttpServletRequest request) {
    DatatableRequest result = new DatatableRequest();
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

    // Dataset core type list, derived from XML vocabulary
    types = new LinkedHashMap<>();
    types.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_TYPE, getLocaleLanguage(), false));
    types = MapUtils.getMapWithLowercaseKeys(types);

    // Dataset Subtypes list, derived from XML vocabulary
    datasetSubtypes = new LinkedHashMap<>();
    datasetSubtypes.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_SUBTYPES, getLocaleLanguage(), false));
    datasetSubtypes = MapUtils.getMapWithLowercaseKeys(datasetSubtypes);
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

  public int getResourcesSize() {
    return resources != null ? resources.getTotalRecords() : 0;
  }

  /**
   * A map of dataset types keys to internationalized values.
   *
   * @return map of dataset subtypes
   */
  public Map<String, String> getTypes() {
    return types;
  }

  /**
   * A map of dataset subtypes keys to internationalized values.
   *
   * @return map of dataset subtypes
   */
  public Map<String, String> getDatasetSubtypes() {
    return datasetSubtypes;
  }
}
