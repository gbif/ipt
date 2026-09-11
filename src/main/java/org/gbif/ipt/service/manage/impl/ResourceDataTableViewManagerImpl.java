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
package org.gbif.ipt.service.manage.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceDataTableViewManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.RequireManagerInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceDataTableViewManagerImpl extends BaseManager implements ResourceDataTableViewManager {

  private static final Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareToIgnoreCase);
  private static final Comparator<Date> nullSafeDateComparator = Comparator.nullsFirst(Date::compareTo);
  private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private final ResourceManager resourceManager;
  private final VocabulariesManager vocabManager;
  private final DataPackageSchemaManager schemaManager;
  private final SimpleTextProvider textProvider;

  public ResourceDataTableViewManagerImpl(AppConfig cfg, DataDir dataDir, ResourceManager resourceManager,
                                          VocabulariesManager vocabManager, DataPackageSchemaManager schemaManager,
                                          SimpleTextProvider textProvider) {
    super(cfg, dataDir);
    this.resourceManager = resourceManager;
    this.vocabManager = vocabManager;
    this.schemaManager = schemaManager;
    this.textProvider = textProvider;
  }

  @Override
  public DatatableResult listPublishedPublicResourceSummaries(DatatableRequest request) {
    List<ResourceSummaryView> resourceSummaryViews = resourceManager.listPublishedPublicResourceSummaries();

    List<ResourceSummaryView> filteredResources = resourceSummaryViews.stream()
        .filter(p -> matchesSearchString(p, request.getSearch()))
        .toList();

    Locale currentLocale = Locale.forLanguageTag(request.getLocale());

    Map<String, String> datasetTypes =
        MapUtils.getMapWithLowercaseKeys(
            vocabManager.getI18nDatasetTypesVocab(request.getLocale(), false));
    // add data packages
    List<DataPackageSchema> installedSchemas = schemaManager.list();
    for (DataPackageSchema installedSchema : installedSchemas) {
      datasetTypes.put(
          installedSchema.getName(),
          Optional.ofNullable(installedSchema.getShortTitle()).orElse(installedSchema.getName()));
    }

    Map<String, String> datasetSubtypes =
        MapUtils.getMapWithLowercaseKeys(
            vocabManager.getI18nDatasetSubtypesVocab(request.getLocale(), false));

    List<List<String>> data = filteredResources.stream()
        .sorted(resourceComparator(request.getSortFieldIndex(), request.getSortOrder()))
        .skip(request.getOffset())
        .limit(request.getLimit())
        .map(res -> toDatatableResourcePortalView(res, currentLocale, datasetTypes, datasetSubtypes))
        .collect(Collectors.toList());

    DatatableResult result = new DatatableResult();
    result.setTotalRecords(resourceSummaryViews.size());
    result.setTotalDisplayRecords(filteredResources.size());
    result.setData(data);

    return result;
  }

  @Override
  public DatatableResult list(User user, DatatableRequest request) {
    List<Resource> allResources = resourceManager.list();

    List<ResourceSummaryView> filteredResources = allResources.stream()
        .filter(res -> RequireManagerInterceptor.isAuthorized(user, res))
        .map(this::toSimplifiedResource)
        .filter(res -> matchesSearchString(res, request.getSearch()))
        .toList();

    Locale currentLocale = Locale.forLanguageTag(request.getLocale());

    Map<String, String> datasetTypes =
        MapUtils.getMapWithLowercaseKeys(
            vocabManager.getI18nDatasetTypesVocab(request.getLocale(), false));
    // add data packages
    List<DataPackageSchema> installedSchemas = schemaManager.list();
    for (DataPackageSchema installedSchema : installedSchemas) {
      datasetTypes.put(
          installedSchema.getName(),
          Optional.ofNullable(installedSchema.getShortTitle()).orElse(installedSchema.getName()));
    }
    Map<String, String> datasetSubtypes =
        MapUtils.getMapWithLowercaseKeys(
            vocabManager.getI18nDatasetSubtypesVocab(request.getLocale(), false));

    List<List<String>> data = filteredResources.stream()
        .sorted(resourceComparator(request.getSortFieldIndex(), request.getSortOrder()))
        .skip(request.getOffset())
        .limit(request.getLimit())
        .map(res -> toDatatableResourceManageView(res, currentLocale, datasetTypes, datasetSubtypes))
        .collect(Collectors.toList());

    DatatableResult result = new DatatableResult();
    result.setTotalRecords(allResources.size());
    result.setTotalDisplayRecords(filteredResources.size());
    result.setData(data);

    return result;
  }

  /**
   * Converts raw data to UI format.
   * Wraps lower case status into span to make it badge on UI.
   *
   * @param status        publication status
   * @param pendingStatus pending publication status
   * @return wrapped publication status (badge)
   */
  private String toUiStatus(PublicationStatus status, PublicationStatus pendingStatus, Locale locale) {
    PublicationStatus effectiveStatus;
    String localizedStatus;
    String translationPrefix;
    String icon;

    if (pendingStatus != null) {
      effectiveStatus = pendingStatus;
      translationPrefix = "manage.home.visible.pending.";
    } else {
      effectiveStatus = status;
      translationPrefix = "manage.home.visible.";
    }

    localizedStatus = textProvider.getText(
        locale,
        translationPrefix + effectiveStatus.name().toLowerCase(),
        effectiveStatus.name().toLowerCase(),
        Collections.emptyList());

    if (effectiveStatus == PublicationStatus.PUBLIC || effectiveStatus == PublicationStatus.PRIVATE) {
      icon = "<i class=\"bi bi-circle fs-smaller-2 me-1\"></i>";
    } else {
      icon = "<i class=\"bi bi-circle-fill fs-smaller-2 me-1\"></i>";
    }

    return "<span class=\"text-nowrap status-pill fs-smaller-2 status-" + effectiveStatus.name().toLowerCase() + "\">" +
        icon +
        "<span>" +
        localizedStatus +
        "</span>" +
        "</span>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps resource title or shortname into a link (home page)
   *
   * @param resource lightweight resource
   * @return link to resource (home page)
   */
  private String toResourceHomeLink(ResourceSummaryView resource) {
    String resourceName = StringUtils.defaultIfEmpty(resource.getTitle(), resource.getShortname());
    String resourceNameEscaped = escapeHtml(resourceName);
    return "<a class=\"resource-table-link\" href='" + cfg.getBaseUrl() + "/resource?r=" + resource.getShortname() + "'>" + resourceNameEscaped + "</a>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps resource title or shortname into a link (manage page)
   *
   * @param resource lightweight resource
   * @return link to resource (manage page)
   */
  private String toResourceManageLink(ResourceSummaryView resource) {
    String resourceName = StringUtils.defaultIfEmpty(resource.getTitle(), resource.getShortname());
    String resourceNameEscaped = escapeHtml(resourceName);
    return "<a class=\"resource-table-link\" href='" + cfg.getBaseUrl() + "/manage/resource?r=" + resource.getShortname() + "'>" + resourceNameEscaped + "</a>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps number of published records into a link and format number according to the locale.
   *
   * @param resource lightweight resource
   * @param locale   locale
   * @return link to records section
   */
  private String toUiRecordsPublished(ResourceSummaryView resource, Locale locale) {
    NumberFormat format = NumberFormat.getInstance(locale);

    if (resource.getLastPublished() == null && resource.getRecordsPublished() == 0) {
      return "<span>--</span>";
    }

    return "<a class=\"resource-table-link\" href='" + cfg.getBaseUrl() + "/resource?r=" + resource.getShortname() + "#anchor-dataRecords'>" + format.format(resource.getRecordsPublished()) + "</a>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps core type or subtype into span to make it badge on UI.
   *
   * @param type  core type or subtype
   * @param vocab vocabulary map
   * @return wrapped type (badge)
   */
  private String toTypeBadge(String type, Map<String, String> vocab) {
    if (type == null) {
      return "<span>--</span>";
    }
    return "<span class=\"fs-smaller-2 text-nowrap dt-content-link dt-content-pill type-" + type.toLowerCase() + "\">" + vocab.getOrDefault(type.toLowerCase(), "--") + "</span>";
  }

  /**
   * Converts raw data to UI format.
   * Logo URL or "--" if empty
   *
   * @param logoUrl logo URL
   * @return Logo URL or "--" if empty
   */
  private String toUiLogoUrl(String logoUrl) {
    if (logoUrl == null) {
      return "<span>--</span>";
    }
    return "<img class=\"resourceminilogo\" src=\"" + logoUrl + "\"/>";
  }

  /**
   * Converts raw data to UI format.
   * Organization alias or name or "--" if empty
   *
   * @param resource lightweight resource
   * @return alias or name or "--"
   */
  private String toUiOrganization(ResourceSummaryView resource) {
    String result = resource.getOrganizationAliasOrName();
    return result != null && !"No organization".equals(result) ? escapeHtml(result) : "--";
  }

  /**
   * Converts raw data (one simplified resource) to UI data for portal home page.
   * BEWARE! Order is crucial!
   *
   * @param resource        simplified resource
   * @param datasetTypes    dataset types vocabulary
   * @param datasetSubtypes dataset subtypes vocabulary
   * @return UI data (array)
   */
  private List<String> toDatatableResourcePortalView(
      ResourceSummaryView resource, Locale locale, Map<String, String> datasetTypes, Map<String, String> datasetSubtypes) {
    List<String> result = new ArrayList<>();
    result.add(toUiLogoUrl(resource.getLogoUrl()));
    result.add(toResourceHomeLink(resource));
    result.add(toUiOrganization(resource));
    result.add(toTypeBadge(resource.getCoreType(), datasetTypes));
    result.add(toTypeBadge(resource.getSubtype(), datasetSubtypes));
    result.add(toUiRecordsPublished(resource, locale));
    result.add(toUiDateTime(resource.getModified()));
    result.add(toUiDateTime(resource.getLastPublished()));
    result.add(toUiNextPublished(resource.getNextPublished()));
    result.add(toUiStatus(resource.getStatus(), resource.getPendingStatus(), locale));
    result.add(escapeHtml(resource.getCreatorName()));
    result.add(resource.getShortname());
    result.add(resource.getSubject() != null ? escapeHtml(resource.getSubject()) : "");

    return result;
  }

  /**
   * Converts raw data (one simplified resource) to UI data for manage home page.
   * BEWARE! Order is crucial!
   *
   * @param resource        simplified resource
   * @param datasetTypes    dataset types vocabulary
   * @param datasetSubtypes dataset subtypes vocabulary
   * @return UI data (array)
   */
  private List<String> toDatatableResourceManageView(
      ResourceSummaryView resource, Locale locale, Map<String, String> datasetTypes, Map<String, String> datasetSubtypes) {
    List<String> result = new ArrayList<>();
    result.add(toUiLogoUrl(resource.getLogoUrl()));
    result.add(toResourceManageLink(resource));
    result.add(toUiOrganization(resource));
    result.add(toTypeBadge(resource.getCoreType(), datasetTypes));
    result.add(toTypeBadge(resource.getSubtype(), datasetSubtypes));
    result.add(toUiRecordsPublished(resource, locale));
    result.add(toUiDateTime(resource.getModified()));
    result.add(toUiDateTime(resource.getLastPublished()));
    result.add(toUiNextPublished(resource.getNextPublished()));
    result.add(toUiStatus(resource.getStatus(), resource.getPendingStatus(), locale));
    result.add(escapeHtml(resource.getCreatorName()));
    result.add(resource.getShortname());
    result.add(resource.getSubject() != null ? escapeHtml(resource.getSubject()) : "");

    return result;
  }

  private String escapeHtml(String name) {
    return StringEscapeUtils.escapeHtml4(name);
  }

  /**
   * Converts raw data to UI format.
   * Date formatted as yyyy-MM-dd HH:mm:ss or "--" if empty.
   *
   * @param date date
   * @return formatted date
   */
  private String toUiDateTime(Date date) {
    if (date == null) {
      return "<span>--</span>";
    }
    return DATETIME_FORMAT.format(date);
  }

  /**
   * Converts raw data to UI format.
   * Next publication date formatted as yyyy-MM-dd HH:mm:ss or "--" if empty.
   * Next published date should never be before today's date, otherwise auto-publication must have failed.
   * In this case, highlight the row to bring the problem to the resource manager's attention.
   *
   * @param date next publication date
   * @return formatted date
   */
  private String toUiNextPublished(Date date) {
    if (date == null) {
      return "<span>--</span>";
    }

    Date now = new Date();

    // highlight if next published is before now (something wrong)
    return date.before(now)
        ? "<span class=\"text-gbif-danger\">" + DATETIME_FORMAT.format(date) + "</span>"
        : DATETIME_FORMAT.format(date);
  }

  /**
   * Produces comparator from the raw parameters.
   *
   * @param index field index (1 - title, 2 - organization, 3 - core type etc.)
   * @param order asc/desc
   * @return comparator
   */
  private Comparator<ResourceSummaryView> resourceComparator(int index, String order) {
    Comparator<ResourceSummaryView> comparator = SortField.fromIndex(index).comparator();
    return isDescendingOrder(order) ? comparator.reversed() : comparator;
  }

  /**
   * Check whether sort order is descending.
   *
   * @param order raw sort order string
   * @return true if descending, false otherwise
   */
  private boolean isDescendingOrder(String order) {
    return Strings.CI.equals(StringUtils.trimToEmpty(order), "desc");
  }

  /**
   * Check if the provided string is present in one of the searchable fields.
   *
   * @param resource lightweight resource
   * @param search   search string
   * @return true/false
   */
  private boolean matchesSearchString(ResourceSummaryView resource, String search) {
    if (StringUtils.isEmpty(search)) {
      return true;
    }

    return Strings.CI.contains(resource.getShortname(), search)
        || Strings.CI.contains(resource.getTitle(), search)
        || Strings.CI.contains(resource.getOrganisationAlias(), search)
        || Strings.CI.contains(resource.getOrganisationName(), search)
        || Strings.CI.contains(resource.getCoreType(), search)
        || Strings.CI.contains(resource.getSubtype(), search)
        || Strings.CI.contains(resource.getCreatorName(), search)
        || Strings.CI.contains(resource.getSubject(), search);
  }

  /**
   * Converts regular Resource to lightweight SimplifiedResource.
   *
   * @param resource regular Resource
   * @return simplified resource
   */
  private ResourceSummaryView toSimplifiedResource(Resource resource) {
    ResourceSummaryView result = new ResourceSummaryView();
    result.setShortname(resource.getShortname());
    result.setTitle(resource.getTitle());
    result.setPendingStatus(resource.getPendingStatus());
    result.setStatus(resource.getStatus());
    result.setRecordsPublished(resource.getRecordsPublished());
    result.setLogoUrl(resource.getLogoUrl());
    result.setSubject(resource.getSubject());
    if (resource.getOrganisation() != null) {
      result.setOrganisationKey(resource.getOrganisation().getKey());
      result.setOrganisationName(resource.getOrganisationName());
      result.setOrganisationAlias(resource.getOrganisationAlias());
    }
    result.setCoreType(resource.getCoreType());
    result.setSubtype(resource.getSubtype());
    result.setModified(resource.getModified());
    result.setPublished(resource.getLastPublished() != null);
    result.setLastPublished(resource.getLastPublished());
    result.setNextPublished(resource.getNextPublished());
    result.setCreatorName(resource.getCreatorName());
    result.setDataPackage(resource.isDataPackage());

    return result;
  }

  private enum SortField {
    TITLE(1, Comparator.comparing(ResourceSummaryView::getTitleOrShortname, nullSafeStringComparator)),
    ORGANIZATION(2, Comparator.comparing(ResourceSummaryView::getOrganizationAliasOrName, nullSafeStringComparator)),
    CORE_TYPE(3, Comparator.comparing(ResourceSummaryView::getCoreType, nullSafeStringComparator)),
    SUBTYPE(4, Comparator.comparing(ResourceSummaryView::getSubtype, nullSafeStringComparator)),
    RECORDS_PUBLISHED(5, Comparator.comparingInt(ResourceSummaryView::getRecordsPublished)
        .thenComparing(ResourceSummaryView::getLastPublished, nullSafeDateComparator)),
    MODIFIED(6, Comparator.comparing(ResourceSummaryView::getModified, nullSafeDateComparator)),
    LAST_PUBLISHED(7, Comparator.comparing(ResourceSummaryView::getLastPublished, nullSafeDateComparator)),
    NEXT_PUBLISHED(8, Comparator.comparing(ResourceSummaryView::getNextPublished, nullSafeDateComparator)),
    STATUS(9, Comparator.comparing(ResourceSummaryView::getStatus, Comparator.nullsFirst(PublicationStatus::compareTo))),
    CREATOR_NAME(10, Comparator.comparing(ResourceSummaryView::getCreatorName, nullSafeStringComparator)),
    SHORTNAME(-1, Comparator.comparing(ResourceSummaryView::getShortname, nullSafeStringComparator)); // default/fallback

    private final int index;
    private final Comparator<ResourceSummaryView> comparator;

    SortField(int index, Comparator<ResourceSummaryView> comparator) {
      this.index = index;
      this.comparator = comparator;
    }

    Comparator<ResourceSummaryView> comparator() {
      return comparator;
    }

    static SortField fromIndex(int index) {
      return Arrays.stream(values())
          .filter(f -> f.index == index)
          .findFirst()
          .orElse(SHORTNAME);
    }
  }
}
