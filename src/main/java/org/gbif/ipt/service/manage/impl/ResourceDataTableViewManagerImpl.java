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
import java.util.stream.Collectors;

public class ResourceDataTableViewManagerImpl extends BaseManager implements ResourceDataTableViewManager {

  private static final Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareToIgnoreCase);
  private static final Comparator<Date> nullSafeDateComparator = Comparator.nullsFirst(Date::compareTo);
  private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

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
