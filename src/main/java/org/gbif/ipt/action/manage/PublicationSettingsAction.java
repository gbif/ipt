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
import org.gbif.ipt.model.BiMonthEnum;
import org.gbif.ipt.model.DayEnum;
import org.gbif.ipt.model.MonthEnum;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.utils.PublicationFailureEmailUtils;
import org.gbif.metadata.eml.ipt.model.MaintenanceUpdateFrequency;

import jakarta.inject.Inject;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

/**
 * The publishing settings of a resource on one page: the publishing organization and the auto-publishing schedule
 * with its options. Everything is checked before anything is changed, and the resource is saved once.
 */
public class PublicationSettingsAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = 7675406287200699071L;

  private static final Logger LOG = LogManager.getLogger(PublicationSettingsAction.class);

  static final String OFF_FREQUENCY = "off";
  private static final String COLON = ":";
  private static final int DEFAULT_HOUR = 12;
  private static final int DEFAULT_THRESHOLD = 10;
  private static final String EMAIL_SEPARATOR = ",";
  // Same validator as BaseValidator/the registry; accepts multi-label domains such as mail.huji.ac.il
  private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

  private final VocabulariesManager vocabManager;
  private final ResourcePublicationManager resourcePublicationManager;

  @Getter
  private Map<String, String> organisations;
  /** Frequencies that qualify for auto-publishing, without "off". */
  @Getter
  private Map<String, String> frequencies;
  @Getter
  private Map<String, String> months;
  @Getter
  private Map<String, String> biMonths;
  @Getter
  private Map<String, String> daysOfWeek;

  // the form's values: the saved ones, or the posted ones after a save that failed
  @Getter
  private String frequency = OFF_FREQUENCY;
  @Getter
  private String frequencyMonth;
  @Getter
  private String frequencyBiMonth;
  @Getter
  private int frequencyDay = 1;
  @Getter
  private String frequencyDayOfWeek;
  @Getter
  private String frequencyTime = String.format("%02d:00", DEFAULT_HOUR);
  @Getter
  private boolean skipUnchanged;
  @Getter
  private boolean skipDrop;
  @Getter
  private int recordsDropThreshold = DEFAULT_THRESHOLD;
  @Getter
  private boolean notifyFailure;
  @Getter
  private String failureEmails = "";

  @Inject
  public PublicationSettingsAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      VocabulariesManager vocabManager,
      ResourcePublicationManager resourcePublicationManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
    this.resourcePublicationManager = resourcePublicationManager;
  }

  @Override
  public void prepare() {
    super.prepare();
    loadOrganisations();
    populateFrequencies();
    populateMonths();
    populateBiMonths();
    populateDaysOfWeek();
    loadSavedSchedule();
  }

  private void loadSavedSchedule() {
    if (resource.usesAutoPublishing() && resource.getUpdateFrequency() != null) {
      frequency = resource.getUpdateFrequency().getIdentifier();
    }
    if (resource.getUpdateFrequencyMonth() != null) {
      frequencyMonth = resource.getUpdateFrequencyMonth().getIdentifier();
    }
    if (resource.getUpdateFrequencyBiMonth() != null) {
      frequencyBiMonth = resource.getUpdateFrequencyBiMonth().getIdentifier();
    }
    if (resource.getUpdateFrequencyDay() != null) {
      frequencyDay = resource.getUpdateFrequencyDay();
    }
    if (resource.getUpdateFrequencyDayOfWeek() != null) {
      frequencyDayOfWeek = resource.getUpdateFrequencyDayOfWeek().getIdentifier();
    }
    if (resource.getUpdateFrequencyHour() != null) {
      int minute = Optional.ofNullable(resource.getUpdateFrequencyMinute()).orElse(0);
      frequencyTime = String.format("%02d:%02d", resource.getUpdateFrequencyHour(), minute);
    }
    skipUnchanged = resource.isSkipPublicationIfNotChanged();
    skipDrop = resource.isSkipPublicationIfRecordsDrop();
    recordsDropThreshold = resource.getRecordsDropThreshold();
    notifyFailure = resource.isNotifyPublicationFailure();
    failureEmails = resource.getPublicationFailureEmails() == null
        ? "" : String.join(", ", resource.getPublicationFailureEmails());
  }

  @Override
  public String save() {
    // 1. publishing organisation - mandatory
    Organisation organisation = null;
    if (StringUtils.isEmpty(id)) {
      addFieldError("id", getText("validation.required", new String[]{getText("portal.home.organisation")}));
    } else {
      organisation = registrationManager.get(id);
      if (organisation == null) {
        addFieldError("id", getText("eml.publishingOrganisation.notFound", id));
      }
    }

    // 2. auto-publishing: read and check what was posted, without changing the resource yet
    String postedFrequency = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY));
    boolean scheduleChange = readAutoPublishing(postedFrequency);

    if (hasFieldErrors() || hasActionErrors()) {
      return INPUT;
    }

    // 3. apply
    boolean organisationChanged = resource.getOrganisation() == null
        || !Objects.equals(resource.getOrganisation().getKey(), organisation.getKey());
    resource.setOrganisation(organisation);

    if (scheduleChange) {
      applyAutoPublishing();
    }

    saveResource();

    if (organisationChanged && (resource.isAlreadyAssignedDoi() || resource.isRegistered())) {
      addActionMessage(
          getText(
              "manage.publicationSettings.success.registered",
              new String[]{"<a href=\"mailto:helpdesk@gbif.org\">helpdesk@gbif.org</a>"}));
    } else {
      addActionMessage(getText("manage.publicationSettings.saved"));
    }
    if (scheduleChange && resource.usesAutoPublishing() && notifyFailure && !isPublicationFailureEmailConfigured()) {
      addActionWarning(getText("manage.autopublish.options.notifyFailure.notConfigured"));
    }

    return SUCCESS;
  }

  /**
   * Reads the posted auto-publishing fields into the form values and checks them.
   *
   * @return true if the schedule or its options are to be changed
   */
  private boolean readAutoPublishing(String postedFrequency) {
    if (!isAutoPublishingConfigurable()) {
      // Auto-publishing is off instance-wide, or restricted to administrators and the user is not one. The only
      // change allowed is turning an existing schedule off; a schedule left as it is posts no frequency.
      if (postedFrequency == null || postedFrequency.equals(frequency)) {
        return false;
      }
      if (!OFF_FREQUENCY.equals(postedFrequency) || !resource.usesAutoPublishing()) {
        addActionError(getText(cfg.isAutoPublishingEnabled()
            ? "manage.overview.autopublish.notAllowed" : "manage.overview.autopublish.disabledByAdmin"));
        LOG.warn("Rejected auto-publishing change for [{}]: not allowed for this user", resource.getShortname());
        return false;
      }
      // only the frequency: the options (skip-if-unchanged, failure notifications, ...) survive the schedule
      // being switched off
      frequency = OFF_FREQUENCY;
      return true;
    }

    if (postedFrequency == null) {
      return false;
    }
    if (!OFF_FREQUENCY.equals(postedFrequency) && MaintenanceUpdateFrequency.findByIdentifier(postedFrequency) == null) {
      addActionError(getText("manage.autopublish.message.error"));
      LOG.error("Cannot update auto-publishing setting for [{}]. Unknown frequency: {}",
          resource.getShortname(), postedFrequency);
      return false;
    }
    frequency = postedFrequency;
    if (OFF_FREQUENCY.equals(frequency)) {
      // the options aren't posted with the schedule off, and stay as they are
      return true;
    }

    frequencyMonth = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_MONTH);
    frequencyBiMonth = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_BIMONTH);
    frequencyDayOfWeek = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_DAYOFWEEK);
    frequencyDay = Math.max(1, Math.min(31,
        NumberUtils.toInt(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_DAY), 1)));
    frequencyTime = normaliseTime(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_TIME));

    skipUnchanged = Boolean.parseBoolean(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_SKIP_UNCHANGED));
    skipDrop = Boolean.parseBoolean(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_SKIP_DROP));
    notifyFailure = Boolean.parseBoolean(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_NOTIFY_FAILURE));

    String thresholdRaw = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_DROP_THRESHOLD));
    if (thresholdRaw != null) {
      int threshold = NumberUtils.toInt(thresholdRaw, -1);
      if (threshold < 0 || threshold > 100) {
        addFieldError(Constants.REQ_PARAM_AUTO_PUBLISH_DROP_THRESHOLD,
            getText("manage.autopublish.options.dropThreshold.invalid"));
      } else {
        recordsDropThreshold = threshold;
      }
    }

    failureEmails = StringUtils.trimToEmpty(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FAILURE_EMAILS));
    if (notifyFailure) {
      List<String> emails = parseFailureEmails(failureEmails);
      List<String> invalidEmails = findInvalidEmails(emails);
      if (emails.isEmpty()) {
        addFieldError(Constants.REQ_PARAM_AUTO_PUBLISH_FAILURE_EMAILS,
            getText("manage.autopublish.options.notifyFailure.emails.required"));
      } else if (!invalidEmails.isEmpty()) {
        addFieldError(Constants.REQ_PARAM_AUTO_PUBLISH_FAILURE_EMAILS,
            getText("manage.autopublish.options.notifyFailure.emails.invalid",
                new String[]{String.join(", ", invalidEmails)}));
      }
    }
    return true;
  }

  /** Applies the form values that passed the checks to the resource, and works out the next publication date. */
  private void applyAutoPublishing() {
    if (OFF_FREQUENCY.equals(frequency)) {
      LOG.debug("Turning off auto-publishing for [{}]", resource.getShortname());
      resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_OFF);
      resource.clearAutoPublishingFrequency();
    } else {
      LOG.debug("Updating auto-publishing for [{}] to: {}", resource.getShortname(), frequency);
      String[] hoursAndMinutes = frequencyTime.split(COLON);
      resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);
      resource.setAutoPublishingFrequency(
          frequency,
          frequencyMonth,
          frequencyBiMonth,
          frequencyDay,
          frequencyDayOfWeek,
          Integer.parseInt(hoursAndMinutes[0]),
          Integer.parseInt(hoursAndMinutes[1]));
      resource.setSkipPublicationIfNotChanged(skipUnchanged);
      resource.setSkipPublicationIfRecordsDrop(skipDrop);
      resource.setRecordsDropThreshold(recordsDropThreshold);
      resource.setNotifyPublicationFailure(notifyFailure);
      resource.setPublicationFailureEmails(notifyFailure ? parseFailureEmails(failureEmails) : new ArrayList<>());
    }
    resourcePublicationManager.updatePublicationMode(resource);
  }

  /** "HH:mm" from a time input, or the default time if it isn't one. */
  static String normaliseTime(String raw) {
    if (raw != null && raw.contains(COLON)) {
      String[] parts = raw.trim().split(COLON);
      int hour = NumberUtils.toInt(parts[0], -1);
      int minute = parts.length > 1 ? NumberUtils.toInt(parts[1], -1) : -1;
      if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
        return String.format("%02d:%02d", hour, minute);
      }
    }
    return String.format("%02d:00", DEFAULT_HOUR);
  }

  /**
   * Parses a raw comma-separated string of email addresses into a trimmed, de-duplicated, non-blank list.
   */
  static List<String> parseFailureEmails(String raw) {
    if (StringUtils.isBlank(raw)) {
      return Collections.emptyList();
    }
    return Arrays.stream(raw.split(EMAIL_SEPARATOR))
        .map(StringUtils::trimToNull)
        .filter(StringUtils::isNotBlank)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Returns the addresses that are not valid emails, in their original order.
   */
  static List<String> findInvalidEmails(List<String> emails) {
    return emails.stream()
        .filter(email -> !EMAIL_VALIDATOR.isValid(email))
        .collect(Collectors.toList());
  }

  /** Can the current user enable or change auto-publication? */
  public boolean isAutoPublishingConfigurable() {
    if (!cfg.isAutoPublishingEnabled()) {
      return false;
    }
    return !cfg.isAutoPublishingAdminOnly()
        || (getCurrentUser() != null && getCurrentUser().hasAdminRights());
  }

  public boolean hasMaxProcessFailures() {
    return resourcePublicationManager.hasMaxProcessFailures(resource, false);
  }

  public boolean isPublicationFailureEmailConfigured() {
    return PublicationFailureEmailUtils.isConfigured(cfg);
  }

  /** The server's time zone, which the schedule runs in. */
  public String getServerTimeZone() {
    return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
  }

  /** The server's wall-clock time now, so the page can work out the next publication in the server's time zone. */
  public String getServerNow() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
  }

  /**
   * Populate organisations dropdown options/list, with placeholder option, followed by list of organisations able to
   * host resources. There must be more than the default organisation "No organisation" in order to include the
   * placeholder option.
   */
  private void loadOrganisations() {
    List<Organisation> associatedOrganisations = registrationManager.list();
    organisations = new LinkedHashMap<>();
    if (!associatedOrganisations.isEmpty()) {
      organisations.put("", getText("admin.organisation.name.select"));

      // add default organisation "No organisation" as first option
      Organisation noOrganisation = getDefaultOrganisation();
      if (noOrganisation != null) {
        organisations.put(noOrganisation.getKey().toString(), getText("eml.publishingOrganisation.none"));
      }

      // then add remaining organisations in the order they have been sorted, excluding the default organisation
      for (Organisation o : associatedOrganisations) {
        if (!Constants.DEFAULT_ORG_KEY.equals(o.getKey())) {
          organisations.put(o.getKey().toString(), o.getName());
        }
      }
    }
  }

  private void populateFrequencies() {
    frequencies = new LinkedHashMap<>();
    Map<String, String> filteredFrequencies =
        vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false);
    MapUtils.removeNonMatchingKeys(filteredFrequencies, MaintenanceUpdateFrequency.NON_ZERO_DAYS_UPDATE_PERIODS);
    // shortest period first: every day, week, month, twice a year, once a year
    MaintenanceUpdateFrequency.NON_ZERO_DAYS_UPDATE_PERIODS.stream()
        .map(f -> MaintenanceUpdateFrequency.findByIdentifier(f))
        .filter(Objects::nonNull)
        .sorted((a, b) -> Integer.compare(a.getPeriodInDays(), b.getPeriodInDays()))
        .map(MaintenanceUpdateFrequency::getIdentifier)
        .filter(filteredFrequencies::containsKey)
        .forEach(f -> frequencies.put(f, filteredFrequencies.get(f)));
  }

  private void populateMonths() {
    months = new LinkedHashMap<>();
    for (MonthEnum month : MonthEnum.values()) {
      months.put(month.getIdentifier(), getText("manage.autopublish." + month.getIdentifier()));
    }
  }

  private void populateBiMonths() {
    biMonths = new LinkedHashMap<>();
    for (BiMonthEnum biMonth : BiMonthEnum.values()) {
      biMonths.put(biMonth.getIdentifier(), getText("manage.autopublish." + biMonth.getIdentifier()));
    }
  }

  private void populateDaysOfWeek() {
    daysOfWeek = new LinkedHashMap<>();
    for (DayEnum dayOfWeek : DayEnum.values()) {
      daysOfWeek.put(dayOfWeek.getIdentifier(), getText("manage.autopublish." + dayOfWeek.getIdentifier()));
    }
  }
}
