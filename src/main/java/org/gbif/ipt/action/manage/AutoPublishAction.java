/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.metadata.eml.MaintenanceUpdateFrequency;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class AutoPublishAction extends ManagerBaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(AutoPublishAction.class);

  private static String OFF_FREQUENCY = "off";

  private final VocabulariesManager vocabManager;

  private Map<String, String> frequencies;
  private Map<String, String> months;
  private Map<String, String> biMonths;
  private Map<Integer, String> days;
  private Map<String, String> daysOfWeek;
  private Map<Integer, String> hours;
  private Map<Integer, String> minutes;

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
    populateHours();
    populateMinutes();
  }

  @Override
  public String save() {
    String updateFrequency = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY);
    String updateFrequencyMonth = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_MONTH);
    String updateFrequencyBiMonth = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_BIMONTH);
    int updateFrequencyDay = Integer.parseInt(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_DAY));
    String updateFrequencyDayOfWeek = req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_DAYOFWEEK);
    int updateFrequencyHour = Integer.parseInt(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_HOUR));
    int updateFrequencyMinute = Integer.parseInt(req.getParameter(Constants.REQ_PARAM_AUTO_PUBLISH_FREQUENCY_MINUTE));

    if (OFF_FREQUENCY.equals(updateFrequency)) {
      LOG.debug("Turning off auto-publishing for [" + resource.getShortname() + "]");
      resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_OFF);
      resource.clearAutoPublishingFrequency();
    } else if (MaintenanceUpdateFrequency.findByIdentifier(updateFrequency) != null) {
      LOG.debug("Updating auto-publishing for [" + resource.getShortname() + "] to: " + updateFrequency);
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
      LOG.error("Cannot update auto-publishing setting for [" + resource.getShortname() + "]. Unkown frequency: " + updateFrequency);
      return ERROR;
    }

    // update next published date
    resourceManager.updatePublicationMode(resource);
    LOG.debug("Next published date updated for resource [" + resource.getShortname() + "]");

    // save entire resource config
    saveResource();
    LOG.debug("Resource [" + resource.getShortname() + "] saved with new auto-publishing setting");

    return SUCCESS;
  }

  public String cancel() {
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

  public Map<Integer, String> getHours() {
    return hours;
  }

  public Map<Integer, String> getMinutes() {
    return minutes;
  }

  /**
   * Populate frequencies map, representing the publishing interval choices uses have when configuring
   * auto-publishing. The frequencies list is derived from an XML vocabulary, and will contain values in the requested
   * locale, defaulting to English.
   */
  private void populateFrequencies() {
    frequencies = new LinkedHashMap<String, String>();
    frequencies.put(OFF_FREQUENCY, getText("manage.autopublish.off"));

    // update frequencies list, that qualify for auto-publishing
    Map<String, String> filteredFrequencies =
      vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false);
    MapUtils.removeNonMatchingKeys(filteredFrequencies, MaintenanceUpdateFrequency.NON_ZERO_DAYS_UPDATE_PERIODS);
    frequencies.putAll(filteredFrequencies);
  }

  private void populateMonths() {
    months = new LinkedHashMap<String, String>();
    for (MonthEnum month : MonthEnum.values()) {
      months.put(month.getIdentifier(), getText("manage.autopublish." + month.getIdentifier()));
    }
  }

  private void populateBiMonths() {
    biMonths = new LinkedHashMap<String, String>();
    for (BiMonthEnum biMonth : BiMonthEnum.values()) {
      biMonths.put(biMonth.getIdentifier(), getText("manage.autopublish." + biMonth.getIdentifier()));
    }
  }

  private void populateDays() {
    days = new LinkedHashMap<Integer, String>();
    for (int i = 1; i <= 31; i++) {
      days.put(i, ((i < 10) ? "0" : "") + i);
    }
  }

  private void populateDaysOfWeek() {
    daysOfWeek = new LinkedHashMap<String, String>();
    for (DayEnum dayOfWeek : DayEnum.values()) {
      daysOfWeek.put(dayOfWeek.getIdentifier(), getText("manage.autopublish." + dayOfWeek.getIdentifier()));
    }
  }

  private void populateHours() {
    hours = new LinkedHashMap<Integer, String>();
    for (int i = 0; i <= 23; i++) {
      hours.put(i, ((i < 10) ? "0" : "") + i);
    }
  }

  private void populateMinutes() {
    minutes = new LinkedHashMap<Integer, String>();
    for (int i = 0; i <= 59; i++) {
      minutes.put(i, ((i < 10) ? "0" : "") + i);
    }
  }

}
