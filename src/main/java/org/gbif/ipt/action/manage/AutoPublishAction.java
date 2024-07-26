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

import java.util.Optional;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.BiMonthEnum;
import org.gbif.ipt.model.DayEnum;
import org.gbif.ipt.model.MonthEnum;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.metadata.eml.ipt.model.MaintenanceUpdateFrequency;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class AutoPublishAction extends ManagerBaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(AutoPublishAction.class);

  private static final String OFF_FREQUENCY = "off";
  private static final String COLON = ":";
  private static final String DEFAULT_TIME = "12:00";

  private final VocabulariesManager vocabManager;

  private Map<String, String> frequencies;
  private Map<String, String> months;
  private Map<String, String> biMonths;
  private Map<Integer, String> days;
  private Map<String, String> daysOfWeek;

  @Inject
  public AutoPublishAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager, ResourceManager resourceManager,
                           VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
  }

  @Override
  public void prepare() {
    super.prepare();
    // populate frequencies map
    populateFrequencies();
    populateMonths();
    populateBiMonths();
    populateDays();
    populateDaysOfWeek();

    setServerTimeZone();
    setUpdateFrequencyTime();
  }

  @Override
  public String save() {
    String updateFrequency = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY);
    String updateFrequencyMonth = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_MONTH);
    String updateFrequencyBiMonth = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_BIMONTH);
    int updateFrequencyDay = Integer.parseInt(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_DAY));
    String updateFrequencyDayOfWeek = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_DAYOFWEEK);
    String updateFrequencyTime = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_TIME);
    String[] hoursAndMinutes = updateFrequencyTime != null && updateFrequencyTime.contains(COLON)
        ? updateFrequencyTime.split(COLON) : DEFAULT_TIME.split(COLON);
    int updateFrequencyHour = Integer.parseInt(hoursAndMinutes[0]);
    int updateFrequencyMinute = Integer.parseInt(hoursAndMinutes[1]);

    if (OFF_FREQUENCY.equals(updateFrequency)) {
      LOG.debug("Turning off auto-publishing for [{}]", resource.getShortname());
      resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_OFF);
      resource.clearAutoPublishingFrequency();
    } else if (MaintenanceUpdateFrequency.findByIdentifier(updateFrequency) != null) {
      LOG.debug("Updating auto-publishing for [{}] to: {}", resource.getShortname(),
        updateFrequency);
      resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);
      resource.setAutoPublishingFrequency(
        updateFrequency,
        updateFrequencyMonth,
        updateFrequencyBiMonth,
        updateFrequencyDay,
        updateFrequencyDayOfWeek,
        updateFrequencyHour,
        updateFrequencyMinute);
    } else {
      LOG.error("Cannot update auto-publishing setting for [{}]. Unknown frequency: {}",
        resource.getShortname(), updateFrequency);
      return ERROR;
    }

    // update next published date
    resourceManager.updatePublicationMode(resource);
    LOG.debug("Next published date updated for resource [{}]", resource.getShortname());

    // save entire resource config
    saveResource();
    LOG.debug("Resource [{}] saved with new auto-publishing setting", resource.getShortname());

    return SUCCESS;
  }

  public Map<String, String> getFrequencies() {
    return frequencies;
  }

  public Map<String, String> getMonths() {
    return months;
  }

  public Map<String, String> getBiMonths() {
    return biMonths;
  }

  public Map<Integer, String> getDays() {
    return days;
  }

  public Map<String, String> getDaysOfWeek() {
    return daysOfWeek;
  }

  /**
   * Populate frequencies map, representing the publishing interval choices uses have when configuring
   * auto-publishing. The frequencies list is derived from an XML vocabulary, and will contain values in the requested
   * locale, defaulting to English.
   */
  private void populateFrequencies() {
    frequencies = new LinkedHashMap<>();
    frequencies.put(OFF_FREQUENCY, getText("manage.autopublish.off"));

    // update frequencies list, that qualify for auto-publishing
    Map<String, String> filteredFrequencies =
      vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false);
    MapUtils.removeNonMatchingKeys(filteredFrequencies, MaintenanceUpdateFrequency.NON_ZERO_DAYS_UPDATE_PERIODS);
    frequencies.putAll(filteredFrequencies);
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

  private void populateDays() {
    days = new LinkedHashMap<>();
    for (int i = 1; i <= 31; i++) {
      days.put(i, ((i < 10) ? "0" : "") + i);
    }
  }

  private void populateDaysOfWeek() {
    daysOfWeek = new LinkedHashMap<>();
    for (DayEnum dayOfWeek : DayEnum.values()) {
      daysOfWeek.put(
          dayOfWeek.getIdentifier(), getText("manage.autopublish." + dayOfWeek.getIdentifier()));
    }
  }

  private void setServerTimeZone() {
    req.setAttribute("serverTimeZone", TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT));
  }

  private void setUpdateFrequencyTime() {
    // Retrieve the saved time and set it as a request attribute
    if (resource.getPublicationMode().equals(PublicationMode.AUTO_PUBLISH_ON)) {
      int savedHour = Optional.ofNullable(resource.getUpdateFrequencyHour()).orElse(12);
      int savedMinute = Optional.ofNullable(resource.getUpdateFrequencyMinute()).orElse(0);
      String savedTime = String.format("%02d:%02d", savedHour, savedMinute);
      req.setAttribute("updateFrequencyTime", savedTime);
    }
  }
}
