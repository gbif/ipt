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
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;
import org.gbif.ipt.model.datatable.HomeSearchRequest;
import org.gbif.ipt.model.datatable.HomeSearchResult;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceDataTableViewManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.struts2.RequireManagerInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;

import jakarta.inject.Inject;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ResourceDataTableViewManagerImpl extends BaseManager implements ResourceDataTableViewManager {

  private static final Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareToIgnoreCase);
  private static final Comparator<Date> nullSafeDateComparator = Comparator.nullsFirst(Date::compareTo);
  private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  // home page search: the filters, and the licences GBIF supports with their short names
  static final String FACET_TYPES = "types";
  static final String FACET_ORGANISATIONS = "organisations";
  static final String FACET_LICENCES = "licences";
  static final String FACET_UPDATED = "updated";
  static final String HOME_LICENCE_OTHER = "other";
  private static final Map<String, String> HOME_LICENCES;
  static {
    Map<String, String> licences = new LinkedHashMap<>();
    licences.put("CC0-1.0", "CC0 1.0");
    licences.put("CC-BY-4.0", "CC BY 4.0");
    licences.put("CC-BY-NC-4.0", "CC BY-NC 4.0");
    HOME_LICENCES = Collections.unmodifiableMap(licences);
  }
  private static final int HOME_MAX_LIMIT = 100;

  private final ResourceManager resourceManager;
  private final VocabulariesManager vocabManager;
  private final DataPackageSchemaManager schemaManager;
  private final SimpleTextProvider textProvider;
  private final ResourcePublicationManager resourcePublicationManager;

  @Inject
  public ResourceDataTableViewManagerImpl(
      AppConfig cfg,
      DataDir dataDir,
      ResourceManager resourceManager,
      VocabulariesManager vocabManager,
      DataPackageSchemaManager schemaManager,
      SimpleTextProvider textProvider,
      ResourcePublicationManager resourcePublicationManager) {
    super(cfg, dataDir);
    this.resourceManager = resourceManager;
    this.vocabManager = vocabManager;
    this.schemaManager = schemaManager;
    this.textProvider = textProvider;
    this.resourcePublicationManager = resourcePublicationManager;
  }

  @Override
  public DatatableResult listPublishedPublicResourceSummaries(DatatableRequest request) {
    List<ResourceSummaryView> resourceSummaryViews = resourceManager.listPublishedPublicResourceSummaries();

    List<ResourceSummaryView> filteredResources = resourceSummaryViews.stream()
        .filter(p -> matchesSearchString(p, request.getSearch()))
        .toList();

    Locale currentLocale = Locale.forLanguageTag(request.getLocale());

    Map<String, String> datasetTypes = datasetTypesVocab(request.getLocale(), currentLocale);

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
  public HomeSearchResult searchPublishedPublicResources(HomeSearchRequest request) {
    List<ResourceSummaryView> all = resourceManager.listPublishedPublicResourceSummaries();
    List<ResourceSummaryView> searched = all.stream()
        .filter(res -> matchesHomeSearch(res, request.getSearch()))
        .toList();
    Date now = new Date();
    List<ResourceSummaryView> matched = searched.stream()
        .filter(res -> matchesHomeFilters(res, request, null, now))
        .toList();

    Locale locale = Locale.forLanguageTag(request.getLocale());
    Map<String, String> datasetTypes = datasetTypesVocab(request.getLocale(), locale);
    Map<String, String> datasetSubtypes =
        MapUtils.getMapWithLowercaseKeys(vocabManager.getI18nDatasetSubtypesVocab(request.getLocale(), false));
    NumberFormat numberFormat = NumberFormat.getInstance(locale);

    HomeSearchResult result = new HomeSearchResult();
    result.setTotal(all.size());
    result.setMatched(matched.size());
    result.setItems(matched.stream()
        .sorted(homeComparator(request.getSort()))
        .skip(Math.max(0, request.getOffset()))
        .limit(Math.min(Math.max(request.getLimit(), 1), HOME_MAX_LIMIT))
        .map(res -> toHomeItem(res, datasetTypes, datasetSubtypes, numberFormat))
        .toList());

    // the values come from all public resources, so a value whose count drops to 0 stays in its place
    Map<String, String> typeLabels = all.stream()
        .map(ResourceSummaryView::getCoreType)
        .filter(Objects::nonNull)
        .map(type -> type.toLowerCase(Locale.ROOT))
        .distinct()
        .collect(Collectors.toMap(type -> type, type -> datasetTypes.getOrDefault(type, type)));
    result.getFacets().put(FACET_TYPES, facet(searched, request, FACET_TYPES, now, sortedByLabel(typeLabels, null)));

    Map<String, String> organisationLabels = all.stream()
        .collect(Collectors.toMap(
            ResourceDataTableViewManagerImpl::homeOrganisationKey,
            res -> StringUtils.defaultIfBlank(homeOrganisationName(res),
                text(locale, "manage.home.organisation.notSet", "Organization not set")),
            (first, second) -> first));
    result.getFacets().put(FACET_ORGANISATIONS, facet(searched, request, FACET_ORGANISATIONS, now,
        sortedByLabel(organisationLabels, Constants.DEFAULT_ORG_KEY.toString())));

    Map<String, String> licenceLabels = new LinkedHashMap<>();
    Set<String> licencesInUse = all.stream().map(ResourceDataTableViewManagerImpl::homeLicenceKey).collect(Collectors.toSet());
    HOME_LICENCES.forEach((code, label) -> {
      if (licencesInUse.contains(code)) {
        licenceLabels.put(code, label);
      }
    });
    if (licencesInUse.contains(HOME_LICENCE_OTHER)) {
      licenceLabels.put(HOME_LICENCE_OTHER, text(locale, "portal.home.licence.other", "Other"));
    }
    result.getFacets().put(FACET_LICENCES, facet(searched, request, FACET_LICENCES, now, licenceLabels));

    Map<String, String> updatedLabels = new LinkedHashMap<>();
    updatedLabels.put(HomeSearchRequest.UPDATED_MONTH, text(locale, "portal.home.updated.month", "Last month"));
    updatedLabels.put(HomeSearchRequest.UPDATED_YEAR, text(locale, "portal.home.updated.year", "Last year"));
    updatedLabels.put(HomeSearchRequest.UPDATED_OLDER, text(locale, "portal.home.updated.older", "Older"));
    result.getFacets().put(FACET_UPDATED, facet(searched, request, FACET_UPDATED, now, updatedLabels));

    return result;
  }

  /**
   * The values of a filter with their counts: the resources matching the search and the other filters that have the
   * value.
   */
  private List<HomeSearchResult.FacetValue> facet(
      List<ResourceSummaryView> searched, HomeSearchRequest request, String facet, Date now, Map<String, String> labels) {
    List<ResourceSummaryView> others = searched.stream()
        .filter(res -> matchesHomeFilters(res, request, facet, now))
        .toList();
    return labels.entrySet().stream()
        .map(e -> new HomeSearchResult.FacetValue(e.getKey(), e.getValue(),
            (int) others.stream().filter(res -> hasFacetValue(res, facet, e.getKey(), now)).count()))
        .toList();
  }

  /**
   * Labels sorted alphabetically, with the value {@code last} (if any) at the end.
   */
  private static Map<String, String> sortedByLabel(Map<String, String> labels, String last) {
    return labels.entrySet().stream()
        .sorted(Comparator.<Map.Entry<String, String>, Boolean>comparing(e -> e.getKey().equals(last))
            .thenComparing(Map.Entry::getValue, String.CASE_INSENSITIVE_ORDER))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first, LinkedHashMap::new));
  }

  /**
   * Whether a resource matches the filters of a home page search, leaving out one filter (to count its values) or none.
   */
  private static boolean matchesHomeFilters(ResourceSummaryView res, HomeSearchRequest request, String skip, Date now) {
    return matchesFacet(res, FACET_TYPES, request.getTypes(), skip, now)
        && matchesFacet(res, FACET_ORGANISATIONS, request.getOrganisations(), skip, now)
        && matchesFacet(res, FACET_LICENCES, request.getLicences(), skip, now)
        && (FACET_UPDATED.equals(skip) || StringUtils.isBlank(request.getUpdated())
            || hasFacetValue(res, FACET_UPDATED, request.getUpdated(), now));
  }

  private static boolean matchesFacet(ResourceSummaryView res, String facet, Set<String> values, String skip, Date now) {
    return facet.equals(skip) || values == null || values.isEmpty()
        || values.stream().anyMatch(value -> hasFacetValue(res, facet, value, now));
  }

  private static boolean hasFacetValue(ResourceSummaryView res, String facet, String value, Date now) {
    return switch (facet) {
      case FACET_TYPES -> res.getCoreType() != null && res.getCoreType().equalsIgnoreCase(value);
      case FACET_ORGANISATIONS -> homeOrganisationKey(res).equals(value);
      case FACET_LICENCES -> homeLicenceKey(res).equals(value);
      case FACET_UPDATED -> {
        // never published (can't be on the home page) counts as older
        long days = res.getLastPublished() != null
            ? TimeUnit.MILLISECONDS.toDays(now.getTime() - res.getLastPublished().getTime())
            : Long.MAX_VALUE;
        yield switch (value) {
          case HomeSearchRequest.UPDATED_MONTH -> days <= 31;
          case HomeSearchRequest.UPDATED_YEAR -> days <= 366;
          case HomeSearchRequest.UPDATED_OLDER -> days > 366;
          default -> true;
        };
      }
      default -> false;
    };
  }

  /**
   * The organisation key of a public resource, the default organisation's key ("No organization") when it has none.
   */
  private static String homeOrganisationKey(ResourceSummaryView res) {
    return res.getOrganisationKey() != null ? res.getOrganisationKey().toString() : Constants.DEFAULT_ORG_KEY.toString();
  }

  /**
   * The organisation alias or name of a public resource, null when it has none or it's the default organisation.
   */
  private static String homeOrganisationName(ResourceSummaryView res) {
    if (res.getOrganisationKey() == null || Constants.DEFAULT_ORG_KEY.equals(res.getOrganisationKey())) {
      return null;
    }
    return StringUtils.trimToNull(res.getOrganizationAliasOrName());
  }

  private static String homeLicenceKey(ResourceSummaryView res) {
    return res.getLicence() != null && HOME_LICENCES.containsKey(res.getLicence()) ? res.getLicence() : HOME_LICENCE_OTHER;
  }

  /**
   * As the search of the resources table, and in the description too.
   */
  private boolean matchesHomeSearch(ResourceSummaryView res, String search) {
    return StringUtils.isBlank(search)
        || matchesSearchString(res, search.trim())
        || Strings.CI.contains(res.getDescription(), search.trim());
  }

  /**
   * Home page sort: most recently published first (the default), title A-Z, or most records first.
   */
  private static Comparator<ResourceSummaryView> homeComparator(String sort) {
    Comparator<ResourceSummaryView> byTitle =
        Comparator.comparing(ResourceSummaryView::getTitleOrShortname, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    if (HomeSearchRequest.SORT_TITLE.equals(sort)) {
      return byTitle;
    }
    if (HomeSearchRequest.SORT_RECORDS.equals(sort)) {
      return Comparator.comparingLong(ResourceSummaryView::getRecordsPublished).reversed().thenComparing(byTitle);
    }
    return Comparator.comparing(ResourceSummaryView::getLastPublished, Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(byTitle);
  }

  private HomeSearchResult.Item toHomeItem(
      ResourceSummaryView res, Map<String, String> datasetTypes, Map<String, String> datasetSubtypes,
      NumberFormat numberFormat) {
    HomeSearchResult.Item item = new HomeSearchResult.Item();
    item.setShortname(res.getShortname());
    item.setTitle(res.getTitleOrShortname());
    item.setLogoUrl(StringUtils.trimToNull(res.getLogoUrl()));
    item.setOrganisation(homeOrganisationName(res));
    if (res.getCoreType() != null) {
      String type = res.getCoreType().toLowerCase(Locale.ROOT);
      item.setType(type);
      item.setTypeLabel(datasetTypes.getOrDefault(type, res.getCoreType()));
    }
    if (res.getSubtype() != null) {
      item.setSubtypeLabel(datasetSubtypes.get(res.getSubtype().toLowerCase(Locale.ROOT)));
    }
    item.setDescription(res.getDescription());
    item.setRecords(res.getRecordsPublished());
    item.setRecordsLabel(numberFormat.format(res.getRecordsPublished()));
    item.setMetadataOnly(res.getCoreType() == null
        || Constants.DATASET_TYPE_METADATA_IDENTIFIER.equalsIgnoreCase(res.getCoreType()));
    if (res.getLastPublished() != null) {
      item.setLastPublished(DATE_FORMAT.format(res.getLastPublished().toInstant()));
      item.setLastPublishedTime(DATETIME_FORMATTER.format(res.getLastPublished().toInstant()));
    }
    if (res.getLicence() != null && HOME_LICENCES.containsKey(res.getLicence())) {
      item.setLicence(res.getLicence());
      item.setLicenceLabel(HOME_LICENCES.get(res.getLicence()));
    }
    item.setDoi(res.getDoi());
    item.setDoiUrl(res.getDoiUrl());
    if (res.getGbifKey() != null) {
      item.setGbifUrl(cfg.getPortalUrl() + "/dataset/" + res.getGbifKey());
    }
    return item;
  }

  @Override
  public DatatableResult list(User user, DatatableRequest request) {
    List<Resource> inOrganisation = resourceManager.list().stream()
        .filter(res -> RequireManagerInterceptor.isAuthorized(user, res))
        .filter(res -> matchesOrganisation(res, request.getOrganisation()))
        .toList();

    List<ResourceSummaryView> filteredResources = inOrganisation.stream()
        .map(this::toManageSummary)
        .filter(res -> matchesSearchString(res, request.getSearch()))
        .toList();

    Locale currentLocale = Locale.forLanguageTag(request.getLocale());

    Map<String, String> datasetTypes = datasetTypesVocab(request.getLocale(), currentLocale);
    Map<String, String> frequencies =
        vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, request.getLocale(), false);

    List<List<String>> data = filteredResources.stream()
        .sorted(manageResourceComparator(request.getSortFieldIndex(), request.getSortOrder()))
        .skip(request.getOffset())
        .limit(request.getLimit())
        .map(res -> toDatatableResourceManageView(res, currentLocale, datasetTypes, frequencies))
        .collect(Collectors.toList());

    DatatableResult result = new DatatableResult();
    result.setTotalRecords(inOrganisation.size());
    result.setTotalDisplayRecords(filteredResources.size());
    result.setData(data);

    return result;
  }

  @Override
  public Map<String, String> listOrganisations(User user) {
    // resources without an organisation are listed under "No organization" (the default organisation)
    Map<String, String> namesByKey = resourceManager.list().stream()
        .filter(res -> RequireManagerInterceptor.isAuthorized(user, res))
        .collect(Collectors.toMap(
            ResourceDataTableViewManagerImpl::organisationKey,
            res -> hasOrganisation(res)
                ? StringUtils.defaultIfBlank(
                    StringUtils.defaultIfBlank(res.getOrganisationAlias(), res.getOrganisationName()), organisationKey(res))
                : "No organization",
            (first, second) -> first));
    return namesByKey.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first, LinkedHashMap::new));
  }

  @Override
  public boolean hasSeveralOrganisations() {
    // "No organization" (the default organisation) isn't one
    return resourceManager.list().stream()
        .map(res -> res.getOrganisation() != null ? res.getOrganisation().getKey() : null)
        .filter(Objects::nonNull)
        .filter(key -> !Constants.DEFAULT_ORG_KEY.equals(key))
        .distinct()
        .limit(2)
        .count() > 1;
  }

  /**
   * Whether a resource belongs to the organisation with this key; an empty key matches every resource.
   */
  static boolean matchesOrganisation(Resource resource, String organisationKey) {
    return StringUtils.isBlank(organisationKey) || organisationKey.equals(organisationKey(resource));
  }

  /**
   * Whether a resource has an organisation: one is set, and it isn't "No organization" (the default organisation).
   */
  private static boolean hasOrganisation(Resource resource) {
    return resource.getOrganisation() != null
        && resource.getOrganisation().getKey() != null
        && !Constants.DEFAULT_ORG_KEY.equals(resource.getOrganisation().getKey());
  }

  /**
   * The key of a resource's organisation, the default organisation's key ("No organization") when it has none.
   */
  private static String organisationKey(Resource resource) {
    return hasOrganisation(resource)
        ? resource.getOrganisation().getKey().toString()
        : Constants.DEFAULT_ORG_KEY.toString();
  }

  /**
   * Converts raw data to UI format: the visibility as a status pill, and what it becomes on the next publication when
   * that is pending (as in the resource overview heading).
   *
   * @param status        publication status
   * @param pendingStatus pending publication status
   * @return status pill
   */
  private String toUiStatus(PublicationStatus status, PublicationStatus pendingStatus, Locale locale) {
    String name = status.name().toLowerCase();
    boolean pending = pendingStatus != null && pendingStatus != status;

    StringBuilder pill = new StringBuilder("<span class=\"text-nowrap text-discreet fs-smaller-2 status-pill status-")
        .append(name).append("\"");
    if (pending) {
      pill.append(" title=\"")
          .append(escapeHtml(text(locale, "manage.overview.changed.publication.status.republication", "")))
          .append("\"");
    }
    pill.append(">")
        .append(icon(status == PublicationStatus.PUBLIC || status == PublicationStatus.PRIVATE ? "radio_button_unchecked" : "circle", "fs-smaller-2"))
        .append("<span>").append(text(locale, "manage.home.visible." + name, name)).append("</span>");
    if (pending) {
      String pendingName = pendingStatus.name().toLowerCase();
      pill.append(icon("arrow_forward", "fs-smaller-2"))
          .append("<span>").append(text(locale, "resource.status." + pendingName, pendingName)).append("</span>");
    }
    return pill.append("</span>").toString();
  }

  /**
   * One line under the status pill on the manage page: what needs doing, worst first. Nothing for a deleted resource or
   * one that needs nothing.
   *
   * @param resource lightweight resource
   * @param locale   locale
   * @return the line, or an empty string
   */
  private String toUiAttention(ResourceSummaryView resource, Locale locale) {
    if (resource.getStatus() == PublicationStatus.DELETED) {
      return "";
    }
    if (resource.isPublishing()) {
      return attentionLine("info", "", text(locale, "manage.home.attention.publishing", "Publishing..."));
    }
    // suspended first: it follows failed publications, so the last one has failed too
    if (resource.isAutoPublishingSuspended()) {
      return attentionLine("warning", icon("warning", ""), text(locale, "manage.overview.attention.autopublishSuspended", "Auto-publishing suspended"));
    }
    if (resource.isLastPublicationFailed()) {
      return attentionLine("danger", icon("error", ""), text(locale, "manage.home.attention.failed", "Last publication failed"));
    }
    if (resource.getReservedDoi() != null) {
      return attentionLine("info", "",
          textProvider.getText(locale, "manage.home.attention.doiReserved", "DOI {0} reserved",
              List.of(escapeHtml(resource.getReservedDoi()))));
    }
    return "";
  }

  private String attentionLine(String level, String icon, String text) {
    return "<div class=\"mh-attention mh-attention-" + level + "\">" + icon + "<span>" + text + "</span></div>";
  }

  /**
   * A Material Icon from the sprite, as the {@code <@icon>} macro writes it (macros/icon.ftl).
   *
   * @param name     icon name, listed in ICONS in scripts/vendor/vendor.mjs
   * @param cssClass extra CSS classes, or an empty string
   * @return SVG element, hidden from screen readers
   */
  private String icon(String name, String cssClass) {
    return "<svg class=\"material-icon" + (cssClass.isEmpty() ? "" : " " + cssClass) + "\" aria-hidden=\"true\" focusable=\"false\">"
        + "<use href=\"" + cfg.getBaseUrl() + "/images/icons/material-icons.svg#" + name + "\"/></svg>";
  }

  private String text(Locale locale, String key, String defaultValue) {
    return textProvider.getText(locale, key, defaultValue, Collections.emptyList());
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
   * Dataset types vocabulary with the installed data package schemas added, named the same as on the resource overview.
   *
   * @param lang   language of the vocabulary
   * @param locale locale of the data package names
   * @return dataset types by lowercase type
   */
  private Map<String, String> datasetTypesVocab(String lang, Locale locale) {
    Map<String, String> datasetTypes =
        MapUtils.getMapWithLowercaseKeys(vocabManager.getI18nDatasetTypesVocab(lang, false));
    for (DataPackageSchema installedSchema : schemaManager.list()) {
      datasetTypes.put(
          installedSchema.getName(),
          textProvider.getText(
              locale,
              "portal.resource.type." + installedSchema.getName(),
              Optional.ofNullable(installedSchema.getShortTitle()).orElse(installedSchema.getName()),
              Collections.emptyList()));
    }
    return datasetTypes;
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
   * BEWARE! Order is crucial! It's the column order of manage/home.ftl and of {@link SortField#manageIndex}:
   * 0 name, 1 status, 2 organisation, 3 type, 4 records, 5 last published, 6 next published, 7 modified (hidden, the
   * default sort).
   *
   * @param resource     simplified resource
   * @param datasetTypes dataset types vocabulary
   * @param frequencies  update frequencies vocabulary
   * @return UI data (array)
   */
  private List<String> toDatatableResourceManageView(
      ResourceSummaryView resource, Locale locale, Map<String, String> datasetTypes, Map<String, String> frequencies) {
    List<String> result = new ArrayList<>();
    result.add(toManageNameCell(resource));
    result.add(toUiStatus(resource.getStatus(), resource.getPendingStatus(), locale) + toUiAttention(resource, locale));
    result.add(toManageOrganisationCell(resource, locale));
    result.add(toTypeBadge(resource.getCoreType(), datasetTypes));
    // never published: say so in records and last publication, rather than "--"
    String notPublishedYet = resource.isPublished() ? null
        : "<span class=\"mh-muted\">" + escapeHtml(text(locale, "manage.home.notPublishedYet", "not published yet")) + "</span>";
    result.add(notPublishedYet != null ? notPublishedYet : toUiRecordsPublished(resource, locale));
    result.add(notPublishedYet != null ? notPublishedYet : toUiDate(resource.getLastPublished()));
    result.add(toManageNextPublished(resource, locale, frequencies));
    result.add(toUiDateTime(resource.getModified()));

    return result;
  }

  /**
   * The resource's title, linked to its overview, with the shortname underneath.
   */
  private String toManageNameCell(ResourceSummaryView resource) {
    return toResourceManageLink(resource)
        + "<div class=\"mh-shortname\">" + escapeHtml(resource.getShortname()) + "</div>";
  }

  /**
   * The organisation on one line, cut by CSS when it's long, with the whole name in the title; "not set" when there
   * is none (or it's the default organisation).
   */
  private String toManageOrganisationCell(ResourceSummaryView resource, Locale locale) {
    if (resource.getOrganisationKey() == null || Constants.DEFAULT_ORG_KEY.equals(resource.getOrganisationKey())) {
      return "<span class=\"mh-muted\">" + escapeHtml(text(locale, "manage.home.noOrganisation", "not set")) + "</span>";
    }
    String name = toUiOrganization(resource);
    return "<div class=\"mh-organisation\" title=\"" + name + "\">" + name + "</div>";
  }

  /**
   * Next publication: the date with the auto-publishing icon and frequency ("2026-11-30 ↻ monthly"), red when it's in
   * the past (auto-publishing must have failed); "stopped" when auto-publishing is suspended, "manual" without
   * auto-publishing.
   */
  private String toManageNextPublished(ResourceSummaryView resource, Locale locale, Map<String, String> frequencies) {
    if (resource.isAutoPublishingSuspended()) {
      return "<span class=\"mh-muted\">" + escapeHtml(text(locale, "manage.home.next.stopped", "stopped")) + "</span>";
    }
    Date next = resource.getNextPublished();
    if (next == null) {
      return "<span class=\"mh-muted\">" + escapeHtml(text(locale, "manage.home.next.manual", "manual")) + "</span>";
    }
    String frequency = resource.getUpdateFrequency() != null
        ? frequencies.getOrDefault(resource.getUpdateFrequency(), resource.getUpdateFrequency())
        : "";
    String title = escapeHtml(text(locale, "manage.home.autoPublishing", "Auto-publishing"));
    return "<span class=\"text-nowrap" + (next.before(new Date()) ? " text-gbif-danger" : "") + "\" title=\""
        + DATETIME_FORMATTER.format(next.toInstant()) + "\">" + DATE_FORMAT.format(next.toInstant()) + "</span> "
        + "<span class=\"mh-muted text-nowrap\" title=\"" + title + "\">" + icon("autorenew", "")
        + (frequency.isEmpty() ? "" : " " + escapeHtml(frequency.toLowerCase(locale))) + "</span>";
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
   * Converts raw data to UI format: the date only (yyyy-MM-dd), with date and time in the title; "--" if empty.
   *
   * @param date date
   * @return formatted date
   */
  private String toUiDate(Date date) {
    if (date == null) {
      return "<span>--</span>";
    }
    return "<span class=\"text-nowrap\" title=\"" + DATETIME_FORMATTER.format(date.toInstant()) + "\">"
        + DATE_FORMAT.format(date.toInstant()) + "</span>";
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
   * Comparator for a column of the manage table (see {@link #toDatatableResourceManageView}).
   *
   * @param index column index in the manage table
   * @param order asc/desc
   * @return comparator
   */
  private Comparator<ResourceSummaryView> manageResourceComparator(int index, String order) {
    Comparator<ResourceSummaryView> comparator = SortField.fromManageIndex(index).comparator();
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
    result.setNextPublished(effectiveNextPublished(resource));
    result.setCreatorName(resource.getCreatorName());
    result.setDataPackage(resource.isDataPackage());

    return result;
  }

  /**
   * A lightweight resource for the manage page: the summary plus what needs doing. Reads the publication state without
   * side effects ({@code isLocked} and {@code status} would finish a completed publication).
   *
   * @param resource regular Resource
   * @return simplified resource
   */
  private ResourceSummaryView toManageSummary(Resource resource) {
    ResourceSummaryView result = toSimplifiedResource(resource);
    if (!hasOrganisation(resource)) {
      // the default organisation ("No organization") is none: "not set", sorted with the resources without one
      result.setOrganisationKey(null);
      result.setOrganisationName(null);
      result.setOrganisationAlias(null);
    }
    String shortname = resource.getShortname();

    var future = resourcePublicationManager.getProcessFutures().get(shortname);
    result.setPublishing(future != null && !future.isDone());

    StatusReport report = resourcePublicationManager.getProcessReports().get(shortname);
    result.setLastPublicationFailed(!result.isPublishing() && report != null && report.isCompleted() && report.hasException());

    result.setAutoPublishingSuspended(cfg.isAutoPublishingEnabled() && resource.usesAutoPublishing()
        && resourcePublicationManager.hasMaxProcessFailures(resource, false));
    if (cfg.isAutoPublishingEnabled() && resource.usesAutoPublishing()) {
      result.setUpdateFrequency(resource.getUpdateFrequency().getIdentifier());
    }

    if (resource.getIdentifierStatus() == IdentifierStatus.PUBLIC_PENDING_PUBLICATION && resource.getDoi() != null) {
      result.setReservedDoi(resource.getDoi().toString());
    }
    return result;
  }

  /**
   * Returns the resource's next scheduled publication date, or null when auto-publishing is disabled
   * instance-wide. Mirrors the overview page and {@code PublishingMonitor}, which both ignore existing
   * schedules while the global switch is off, so the manage home table does not display misleading dates.
   *
   * @param resource resource
   * @return next scheduled publication date, or null when auto-publishing is disabled instance-wide
   */
  private Date effectiveNextPublished(Resource resource) {
    return cfg.isAutoPublishingEnabled() ? resource.getNextPublished() : null;
  }

  private enum SortField {
    TITLE(1, 0, Comparator.comparing(ResourceSummaryView::getTitleOrShortname, nullSafeStringComparator)),
    ORGANIZATION(2, 2, Comparator.comparing(ResourceSummaryView::getOrganizationAliasOrName, nullSafeStringComparator)),
    CORE_TYPE(3, 3, Comparator.comparing(ResourceSummaryView::getCoreType, nullSafeStringComparator)),
    SUBTYPE(4, -1, Comparator.comparing(ResourceSummaryView::getSubtype, nullSafeStringComparator)),
    RECORDS_PUBLISHED(5, 4, Comparator.comparingLong(ResourceSummaryView::getRecordsPublished)
        .thenComparing(ResourceSummaryView::getLastPublished, nullSafeDateComparator)),
    MODIFIED(6, 7, Comparator.comparing(ResourceSummaryView::getModified, nullSafeDateComparator)),
    LAST_PUBLISHED(7, 5, Comparator.comparing(ResourceSummaryView::getLastPublished, nullSafeDateComparator)),
    NEXT_PUBLISHED(8, 6, Comparator.comparing(ResourceSummaryView::getNextPublished, nullSafeDateComparator)),
    STATUS(9, 1, Comparator.comparing(ResourceSummaryView::getStatus, Comparator.nullsFirst(PublicationStatus::compareTo))),
    CREATOR_NAME(10, -1, Comparator.comparing(ResourceSummaryView::getCreatorName, nullSafeStringComparator)),
    SHORTNAME(-1, -1, Comparator.comparing(ResourceSummaryView::getShortname, nullSafeStringComparator)); // default/fallback

    // column index in the public home table, and in the manage table
    private final int index;
    private final int manageIndex;
    private final Comparator<ResourceSummaryView> comparator;

    SortField(int index, int manageIndex, Comparator<ResourceSummaryView> comparator) {
      this.index = index;
      this.manageIndex = manageIndex;
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

    static SortField fromManageIndex(int index) {
      return Arrays.stream(values())
          .filter(f -> f.manageIndex >= 0 && f.manageIndex == index)
          .findFirst()
          .orElse(SHORTNAME);
    }
  }
}
