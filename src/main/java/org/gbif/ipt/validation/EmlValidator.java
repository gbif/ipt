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
package org.gbif.ipt.validation;

import org.gbif.api.vocabulary.Language;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.MetadataSection;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.Address;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.BBox;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Collection;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.GeospatialCoverage;
import org.gbif.metadata.eml.JGTICuratorialUnit;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.MaintenanceUpdateFrequency;
import org.gbif.metadata.eml.PhysicalData;
import org.gbif.metadata.eml.Point;
import org.gbif.metadata.eml.Project;
import org.gbif.metadata.eml.StudyAreaDescription;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.metadata.eml.TemporalCoverageType;
import org.gbif.metadata.eml.UserId;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class EmlValidator extends BaseValidator {

  protected static Pattern phonePattern = Pattern.compile("[\\w ()/+-\\.]+");
  private AppConfig cfg;
  private RegistrationManager regManager;
  private SimpleTextProvider simpleTextProvider;

  @Inject
  public EmlValidator(AppConfig cfg, RegistrationManager registrationManager, SimpleTextProvider simpleTextProvider) {
    this.cfg = cfg;
    this.regManager = registrationManager;
    this.simpleTextProvider = simpleTextProvider;
  }

  /**
   * Returns a formatted URL string, prefixing it with a default scheme component if its not an absolute URL.
   * 
   * @return the URL always having a scheme component, or null if incoming URL string was null or empty
   */
  public static String formatURL(String url) {
    if (StringUtils.isNotBlank(url)) {
      try {
        URI uri = URI.create(url);
        if (uri.isAbsolute()) {
          return url;
        } else {
          // adding a default scheme component
          return "http://" + url;
        }
      } catch (IllegalArgumentException e) {
        return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Checks if the incoming string representing a URL, is in fact a well-formed URI.
   * 
   * @return true if the string is a well-formed URI
   */
  public static boolean isWellFormedURI(String url) {
    if (url != null) {
      try {
        new URI(url);
        return true;
      } catch (URISyntaxException e) {
        return false;
      }
    } else {
      return false;
    }
  }

  public static boolean isValidInteger(String integer) {
    try {
      Integer.parseInt(integer);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean isValidPhoneNumber(String phone) {
    return phone != null && phonePattern.matcher(phone).matches();
  }

  public boolean isValid(Resource resource, @Nullable MetadataSection section) {
    BaseAction action = new BaseAction(simpleTextProvider, cfg, regManager);
    validate(action, resource, section);
    return !(action.hasActionErrors() || action.hasFieldErrors());
  }

  /**
   * Validate if all metadata sections are valid. For the first section encountered that doesn't validate, an
   * error message will appear for that section only.
   * 
   * @param action Action
   * @param resource resource
   * @return whether all sections validated or not
   */
  public boolean areAllSectionsValid(BaseAction action, Resource resource) {
    boolean problemsEncountered = false;
    for (MetadataSection section : MetadataSection.values()) {
      validate(action, resource, section);
      // only highlight first section has errors
      if ((action.hasActionErrors() || action.hasFieldErrors()) && !problemsEncountered) {
        action.addActionError(action.getText("manage.failed", new String[] {action.getText("submenu." + section.getName())}));
        problemsEncountered = true;
      }
    }
    return !problemsEncountered;
  }

  /**
   * Validate an EML document, optionally only a part of it.
   * </br>
   * For each section, validation only proceeds if at least one field in the section's form has been entered.
   * 
   * @param action BaseAction
   * @param resource resource
   * @param section EML document section name
   */
  public void validate(BaseAction action, Resource resource, @Nullable MetadataSection section) {
    if (resource != null) {

      Eml eml = (resource.getEml() == null) ? new Eml() : resource.getEml();

      // set default
      if (section == null) {
        section = MetadataSection.BASIC_SECTION;
      }

      switch (section) {
        case BASIC_SECTION:
          // can't bypass the basic metadata page - it is absolutely mandatory

          // Title - mandatory
          if (StringUtils.isBlank(eml.getTitle())) {
            action.addFieldError("eml.title",
              action.getText("validation.required", new String[] {action.getText("eml.title")}));
          }

          // description - mandatory and greater than 5 chars
          if (eml.getDescription().isEmpty()) {
            action
              .addActionError(action.getText("validation.required", new String[] {action.getText("eml.description")}));
          } else {
            // ensure each description is longer than min length
            int index = 0;
            for (String d : eml.getDescription()) {
              if (!exists(d, 5)) {
                action.addFieldError("eml.description[" + index + "]",
                  action.getText("validation.short", new String[] {action.getText("eml.description"), "5"}));
              }
              index++;
            }
          }

          // intellectual rights - mandatory
          if (StringUtils.isBlank(eml.getIntellectualRights())) {
            action.addFieldError("eml.intellectualRights.license",
              action.getText("validation.required", new String[] {action.getText("eml.intellectualRights.license")}));
          }

          // publishing organisation - mandatory
          if (resource.getOrganisation() == null) {
            action.addFieldError("id",
              action.getText("validation.required", new String[] {action.getText("portal.home.organisation")}));
          } else if (regManager.get(resource.getOrganisation().getKey()) == null) {
            action.addFieldError("id",
              action.getText("eml.publishingOrganisation.notFound", new String[] {resource.getOrganisation().getKey().toString()}));
          }

          // type - mandatory
          if (StringUtils.isBlank(resource.getCoreType())) {
            action.addFieldError("resource.coreType",
              action.getText("validation.required", new String[] {action.getText("resource.coreType")}));
          }

          // 3 Mandatory fields with default values set: metadata language, data language, and update frequency

          // metadata language - mandatory (defaults to 3 letter ISO code for English)
          if (StringUtils.isBlank(eml.getMetadataLanguage())) {
            action.addActionWarning(action.getText("eml.metadataLanguage.default"));
            eml.setMetadataLanguage(Language.ENGLISH.getIso3LetterCode());
          }

          // data language - mandatory unless resource is metadata-only (defaults to English)
          if (StringUtils.isBlank(eml.getLanguage()) && resource.getCoreType() != null &&
              !resource.getCoreType().equalsIgnoreCase(Resource.CoreRowType.METADATA.toString())) {
            action.addActionWarning(action.getText("eml.language.default"));
            eml.setLanguage(Language.ENGLISH.getIso3LetterCode());
          }

          // update frequency - mandatory (defaults to Unknown)
          if (eml.getUpdateFrequency()==null) {
            if (resource.getUpdateFrequency() != null) {
              eml.setUpdateFrequency(resource.getUpdateFrequency().getIdentifier());
              action.addActionWarning(action.getText("eml.updateFrequency.default.interval", new String[] {resource.getUpdateFrequency().getIdentifier()}));
            } else {
              action.addActionWarning(action.getText("eml.updateFrequency.default"));
              eml.setUpdateFrequency(MaintenanceUpdateFrequency.UNKOWN.getIdentifier());
            }
          }

          // Contacts list: at least one field has to have had data entered into it to qualify for validation
          if (isAgentsListEmpty(eml.getContacts())) {
            action.addActionError(action.getText("eml.contact.required"));
          } else {
            for (int index = 0; index < eml.getContacts().size(); index++) {
              Agent c = eml.getContacts().get(index);

              // firstName - optional. But if firstName exists, lastName have to exist
              if (exists(c.getFirstName()) && !exists(c.getLastName())) {
                action.addFieldError("eml.contacts[" + index + "].lastName",
                  action.getText("validation.firstname.lastname"));
              }

              // directory and personnel id both required (if either is supplied)
              if (!c.getUserIds().isEmpty()) {
                if (exists(c.getUserIds().get(0).getDirectory()) && !exists(c.getUserIds().get(0).getIdentifier())) {
                  action.addFieldError("eml.contacts[" + index + "].userIds[0].identifier",
                    action.getText("validation.personnel"));
                } else if (!exists(c.getUserIds().get(0).getDirectory()) && exists(c.getUserIds().get(0).getIdentifier())) {
                  action.addFieldError("eml.contacts[" + index + "].userIds[0].directory",
                    action.getText("validation.directory"));
                }
              }

              // At least one of organisation, position, or a lastName have to exist
              if (!exists(c.getOrganisation()) && !exists(c.getLastName()) && !exists(c.getPosition())) {
                action.addActionError(action.getText("validation.lastname.organisation.position"));
                action.addFieldError("eml.contacts[" + index + "].organisation", action
                  .getText("validation.required", new String[] {action.getText("eml.contact.organisation")}));
                action.addFieldError("eml.contacts[" + index + "].lastName",
                  action.getText("validation.required", new String[] {action.getText("eml.contact.lastName")}));
                action.addFieldError("eml.contacts[" + index + "].position",
                  action.getText("validation.required", new String[] {action.getText("eml.contact.position")}));
              }

            /* email is optional. But if it exists, should be a valid email address */
              if (exists(c.getEmail()) && !isValidEmail(c.getEmail())) {
                action.addFieldError("eml.contacts[" + index + "].email",
                  action.getText("validation.invalid", new String[] {action.getText("eml.contact.email")}));
              }

            /* phone is optional. But if it exists, should match the pattern */
              if (exists(c.getPhone()) && !isValidPhoneNumber(c.getPhone())) {
                action.addFieldError("eml.contacts[" + index + "].phone",
                  action.getText("validation.invalid", new String[] {action.getText("eml.contact.phone")}));
              }

            /* Validate the homepage URL from each contact */
              if (c.getHomepage() != null) {
                if (formatURL(c.getHomepage()) == null) {
                  action.addFieldError("eml.contacts[" + index + "].homepage",
                    action.getText("validation.invalid",
                      new String[] {action.getText("eml.contact.homepage")}));
                } else {
                  c.setHomepage(formatURL(c.getHomepage()));
                }
              }
            }
          }

          // Creators list: at least one contact is required, and
          // at least one field has to have had data entered into it to qualify for validation
          if (isAgentsListEmpty(eml.getCreators())) {
            action.addActionError(action.getText("eml.resourceCreator.required"));
          } else {
            for (int index = 0; index < eml.getCreators().size(); index++) {
              Agent c = eml.getCreators().get(index);

              // firstName - optional. But if firstName exists, lastName have to exist
              if (exists(c.getFirstName()) && !exists(c.getLastName())) {
                action.addFieldError("eml.creators[" + index + "].lastName",
                  action.getText("validation.firstname.lastname"));
              }

              // directory and personnel id both required (if either is supplied)
              if (!c.getUserIds().isEmpty()) {
                if (exists(c.getUserIds().get(0).getDirectory()) && !exists(c.getUserIds().get(0).getIdentifier())) {
                  action.addFieldError("eml.creators[" + index + "].userIds[0].identifier",
                    action.getText("validation.personnel"));
                } else if (!exists(c.getUserIds().get(0).getDirectory()) && exists(c.getUserIds().get(0).getIdentifier())) {
                  action.addFieldError("eml.creators[" + index + "].userIds[0].directory",
                    action.getText("validation.directory"));
                }
              }

              // At least one of organisation, position, or a lastName have to exist
              if (!exists(c.getOrganisation()) && !exists(c.getLastName()) && !exists(c.getPosition())) {
                action.addActionError(action.getText("validation.lastname.organisation.position"));
                action.addFieldError("eml.creators[" + index + "].organisation", action
                  .getText("validation.required", new String[] {action.getText("eml.resourceCreator.organisation")}));
                action.addFieldError("eml.creators[" + index + "].lastName",
                  action.getText("validation.required", new String[] {action.getText("eml.resourceCreator.lastName")}));
                action.addFieldError("eml.creators[" + index + "].position",
                  action.getText("validation.required", new String[] {action.getText("eml.resourceCreator.position")}));
              }

            /* email is optional. But if it exists, should be a valid email address */
              if (exists(c.getEmail()) && !isValidEmail(c.getEmail())) {
                action.addFieldError("eml.creators[" + index + "].email",
                  action.getText("validation.invalid", new String[] {action.getText("eml.resourceCreator.email")}));
              }

            /* phone is optional. But if it exists, should match the pattern */
              if (exists(c.getPhone()) && !isValidPhoneNumber(c.getPhone())) {
                action.addFieldError("eml.creators[" + index + "].phone",
                  action.getText("validation.invalid", new String[] {action.getText("eml.resourceCreator.phone")}));
              }

            /* Validate the homepage URL from each contact */
              if (c.getHomepage() != null) {
                if (formatURL(c.getHomepage()) == null) {
                  action.addFieldError("eml.creators[" + index + "].homepage",
                    action.getText("validation.invalid",
                      new String[] {action.getText("eml.resourceCreator.homepage")}));
                } else {
                  c.setHomepage(formatURL(c.getHomepage()));
                }
              }
            }
          }

          // MetadataProviders list: at least one field has to have had data entered into it to qualify for validation
          if (isAgentsListEmpty(eml.getMetadataProviders())) {
            action.addActionError(action.getText("eml.metadataProvider.required"));
          } else {
            for (int index = 0; index < eml.getMetadataProviders().size(); index++) {
              Agent c = eml.getMetadataProviders().get(index);

              // firstName - optional. But if firstName exists, lastName have to exist
              if (exists(c.getFirstName()) && !exists(c.getLastName())) {
                action.addFieldError("eml.metadataProviders[" + index + "].lastName",
                  action.getText("validation.firstname.lastname"));
              }

              // directory and personnel id both required (if either is supplied)
              if (!c.getUserIds().isEmpty()) {
                if (exists(c.getUserIds().get(0).getDirectory()) && !exists(c.getUserIds().get(0).getIdentifier())) {
                  action.addFieldError("eml.metadataProviders[" + index + "].userIds[0].identifier",
                    action.getText("validation.personnel"));
                } else if (!exists(c.getUserIds().get(0).getDirectory()) && exists(c.getUserIds().get(0).getIdentifier())) {
                  action.addFieldError("eml.metadataProviders[" + index + "].userIds[0].directory",
                    action.getText("validation.directory"));
                }
              }

              // At least one of organisation, position, or a lastName have to exist
              if (!exists(c.getOrganisation()) && !exists(c.getLastName()) && !exists(c.getPosition())) {
                action.addActionError(action.getText("validation.lastname.organisation.position"));
                action.addFieldError("eml.metadataProviders[" + index + "].organisation", action
                  .getText("validation.required", new String[] {action.getText("eml.metadataProvider.organisation")}));
                action.addFieldError("eml.metadataProviders[" + index + "].lastName",
                  action.getText("validation.required", new String[] {action.getText("eml.metadataProvider.lastName")}));
                action.addFieldError("eml.metadataProviders[" + index + "].position",
                  action.getText("validation.required", new String[] {action.getText("eml.metadataProvider.position")}));
              }

            /* email is optional. But if it exists, should be a valid email address */
              if (exists(c.getEmail()) && !isValidEmail(c.getEmail())) {
                action.addFieldError("eml.metadataProviders[" + index + "].email",
                  action.getText("validation.invalid", new String[] {action.getText("eml.metadataProvider.email")}));
              }

            /* phone is optional. But if it exists, should match the pattern */
              if (exists(c.getPhone()) && !isValidPhoneNumber(c.getPhone())) {
                action.addFieldError("eml.metadataProviders[" + index + "].phone",
                  action.getText("validation.invalid", new String[] {action.getText("eml.metadataProvider.phone")}));
              }

            /* Validate the homepage URL from each contact */
              if (c.getHomepage() != null) {
                if (formatURL(c.getHomepage()) == null) {
                  action.addFieldError("eml.metadataProviders[" + index + "].homepage",
                    action.getText("validation.invalid",
                      new String[] {action.getText("eml.metadataProvider.homepage")}));
                } else {
                  c.setHomepage(formatURL(c.getHomepage()));
                }
              }
            }
          }
          break;

        case GEOGRAPHIC_COVERAGE_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isGeoPageEmpty(eml)) {
            Double coord = 0.0;
            for (int index = 0; index < eml.getGeospatialCoverages().size(); index++) {
              // The Bounding coordinates and description are mandatory.
              if (!eml.getGeospatialCoverages().isEmpty()) {
                coord = eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMin().getLongitude();
                if (coord == null) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].boundingCoordinates.min.longitude", action
                    .getText("validation.required",
                      new String[] {action.getText("eml.geospatialCoverages.boundingCoordinates.min.longitude")}));
                } else if (Double.isNaN(coord)) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].boundingCoordinates.min.longitude", action
                    .getText("validation.invalid",
                      new String[] {action.getText("eml.geospatialCoverages.boundingCoordinates.min.longitude")}));
                }
                coord = eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMax().getLongitude();
                if (coord == null) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].boundingCoordinates.max.longitude", action
                    .getText("validation.required",
                      new String[] {action.getText("eml.geospatialCoverages.boundingCoordinates.max.longitude")}));
                } else if (Double.isNaN(coord)) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].boundingCoordinates.max.longitude", action
                    .getText("validation.invalid",
                      new String[] {action.getText("eml.geospatialCoverages.boundingCoordinates.max.longitude")}));
                }
                coord = eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMax().getLatitude();
                if (coord == null) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].boundingCoordinates.max.latitude", action
                    .getText("validation.required",
                      new String[] {action.getText("eml.geospatialCoverages.boundingCoordinates.max.latitude")}));
                } else if (Double.isNaN(coord)) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].boundingCoordinates.max.latitude", action
                    .getText("validation.invalid",
                      new String[] {action.getText("eml.geospatialCoverages.boundingCoordinates.max.latitude")}));
                }
                coord = eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMin().getLatitude();
                if (coord == null) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].boundingCoordinates.min.latitude", action
                    .getText("validation.required",
                      new String[] {action.getText("eml.geospatialCoverages.boundingCoordinates.min.latitude")}));
                } else if (Double.isNaN(coord)) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].boundingCoordinates.min.latitude", action
                    .getText("validation.invalid",
                      new String[] {action.getText("eml.geospatialCoverages.boundingCoordinates.min.latitude")}));
                }
              /* description - mandatory and greater than 2 chars */
                if (StringUtils.isBlank(eml.getGeospatialCoverages().get(index).getDescription())) {
                  action
                    .addFieldError(
                      "eml.geospatialCoverages[" + index + "].description",
                      action
                        .getText("validation.required",
                          new String[] {action.getText("eml.geospatialCoverages.description")}));
                } else if (!exists(eml.getGeospatialCoverages().get(index).getDescription(), 2)) {
                  action.addFieldError("eml.geospatialCoverages[" + index + "].description", action
                    .getText("validation.short",
                      new String[] {action.getText("eml.geospatialCoverages.description"), "2"}));
                }
              }
            }
          } else {
            // If all fields are empty, the <coverage> label in eml.xml will be removed.
            eml.getGeospatialCoverages().clear();
          }

          break;

        case TAXANOMIC_COVERAGE_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isTaxonomicPageEmpty(eml)) {
            // scientific name is required
            int index = 0;
            for (TaxonomicCoverage tc : eml.getTaxonomicCoverages()) {
              int kw = 0;
              if (tc != null) {
                for (TaxonKeyword k : tc.getTaxonKeywords()) {
                  if (!exists(k.getScientificName())) {
                    action.addFieldError("eml.taxonomicCoverages[" + index + "].taxonKeywords[" + kw + "].scientificName",
                        action.getText("validation.required",
                            new String[]{action.getText("eml.taxonomicCoverages.taxonKeyword.scientificName")}));
                  }
                  kw++;
                }
              }
              index++;
            }
          }

          break;

        case TEMPORAL_COVERAGE_SECTION:

          // at least one field has to have had data entered into it to qualify for validation
          if (!isTemporalPageEmpty(eml)) {
            int index = 0;
            for (TemporalCoverage tc : eml.getTemporalCoverages()) {
              if (tc.getType() == TemporalCoverageType.SINGLE_DATE && !exists(tc.getStartDate())) {
                action
                  .addFieldError("eml.temporalCoverages[" + index + "].startDate",
                    action.getText("validation.required",
                      new String[] {action.getText("eml.temporalCoverages.startDate")}));
              }
              if (tc.getType() == TemporalCoverageType.DATE_RANGE) {
                if (!exists(tc.getStartDate())) {
                  action.addFieldError("eml.temporalCoverages[" + index + "].startDate", action
                    .getText("validation.required", new String[] {action.getText("eml.temporalCoverages.startDate")}));
                }
                if (!exists(tc.getEndDate())) {
                  action
                    .addFieldError("eml.temporalCoverages[" + index + "].endDate",
                      action.getText("validation.required",
                        new String[] {action.getText("eml.temporalCoverages.endDate")}));
                }
              }
              if (tc.getType() == TemporalCoverageType.FORMATION_PERIOD && !exists(tc.getFormationPeriod())) {
                action
                  .addFieldError(
                    "eml.temporalCoverages[" + index + "].formationPeriod",
                    action
                      .getText("validation.required",
                        new String[] {action.getText("eml.temporalCoverages.formationPeriod")}));
              }
              if (tc.getType() == TemporalCoverageType.LIVING_TIME_PERIOD && !exists(tc.getLivingTimePeriod())) {
                action.addFieldError("eml.temporalCoverages[" + index + "].livingTimePeriod", action
                  .getText("validation.required",
                    new String[] {action.getText("eml.temporalCoverages.livingTimePeriod")}));
              }
              index++;
            }
          }

          break;

        case KEYWORDS_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isKeywordsPageEmpty(eml)) {
            int index = 0;
            for (KeywordSet ks : eml.getKeywords()) {
              // TODO: remove check for "null" after fixing problem in gbif-metadata-profile
              if (!exists(ks.getKeywordsString()) || ks.getKeywordsString().equalsIgnoreCase("null")) {
                action.addFieldError("eml.keywords[" + index + "].keywordsString",
                  action.getText("validation.required", new String[] {action.getText("eml.keywords.keywordsString")}));
              }
              if (!exists(ks.getKeywordThesaurus())) {
                action.addFieldError("eml.keywords[" + index + "].keywordThesaurus",
                  action.getText("validation.required", new String[] {action.getText("eml.keywords.keywordThesaurus")}));
              }
              index++;
            }
          }
          break;

        case PARTIES_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isAgentsListEmpty(eml.getAssociatedParties())) {
            for (int index = 0; index < eml.getAssociatedParties().size(); index++) {
              Agent ap = eml.getAssociatedParties().get(index);

              // firstName - optional. But if firstName exists, lastName have to exist
              if (exists(ap.getFirstName()) && !exists(ap.getLastName())) {
                action.addFieldError("eml.associatedParties[" + index + "].lastName",
                  action.getText("validation.firstname.lastname"));
              }

              // directory and personnel id both required (if either is supplied)
              if (!ap.getUserIds().isEmpty()) {
                if (exists(ap.getUserIds().get(0).getDirectory()) && !exists(ap.getUserIds().get(0).getIdentifier())) {
                  action.addFieldError("eml.associatedParties[" + index + "].userIds[0].identifier",
                    action.getText("validation.personnel"));
                } else if (!exists(ap.getUserIds().get(0).getDirectory()) && exists(ap.getUserIds().get(0).getIdentifier())) {
                  action.addFieldError("eml.associatedParties[" + index + "].userIds[0].directory",
                    action.getText("validation.directory"));
                }
              }

              // At least one of organisation, position, or a lastName have to exist
              if (!exists(ap.getOrganisation()) && !exists(ap.getLastName()) && !exists(ap.getPosition())) {
                action.addActionError(action.getText("validation.lastname.organisation.position"));
                action.addFieldError("eml.associatedParties[" + index + "].organisation", action
                  .getText("validation.required", new String[] {action.getText("eml.associatedParties.organisation")}));
                action.addFieldError("eml.associatedParties[" + index + "].lastName",
                  action.getText("validation.required", new String[] {action.getText("eml.associatedParties.lastName")}));
                action.addFieldError("eml.associatedParties[" + index + "].position",
                  action.getText("validation.required", new String[] {action.getText("eml.associatedParties.position")}));
              }

            /* email is optional. But if it exists, should be a valid email address */
              if (exists(ap.getEmail()) && !isValidEmail(ap.getEmail())) {
                action.addFieldError("eml.associatedParties[" + index + "].email",
                  action.getText("validation.invalid", new String[] {action.getText("eml.associatedParties.email")}));
              }

            /* phone is optional. But if it exists, should match the pattern */
              if (exists(ap.getPhone()) && !isValidPhoneNumber(ap.getPhone())) {
                action.addFieldError("eml.associatedParties[" + index + "].phone",
                  action.getText("validation.invalid", new String[] {action.getText("eml.associatedParties.phone")}));
              }

            /* Validate the homepage URL from each associated parties */
              if (ap.getHomepage() != null) {
                if (formatURL(ap.getHomepage()) == null) {
                  action.addFieldError("eml.associatedParties[" + index + "].homepage",
                    action.getText("validation.invalid",
                      new String[] {action.getText("eml.associatedParties.homepage")}));
                } else {
                  ap.setHomepage(formatURL(ap.getHomepage()));
                }
              }
            }
          }

          break;

        case PROJECT_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isProjectPageEmpty(eml)) {

            // title is required
            if (!exists(eml.getProject().getTitle()) || eml.getProject().getTitle().trim().length() == 0) {
              action.addFieldError("eml.project.title",
                action.getText("validation.required", new String[] {action.getText("eml.project.title")}));
            }

            // Personnel list: at least one field has to have had data entered into it to qualify for validation
            if (isAgentsListEmpty(eml.getProject().getPersonnel())) {
              action.addActionError(action.getText("eml.project.personnel.required"));
            } else {
              for (int index = 0; index < eml.getProject().getPersonnel().size(); index++) {
                Agent p = eml.getProject().getPersonnel().get(index);

                // firstName - optional. But if firstName exists, lastName have to exist
                if (exists(p.getFirstName()) && !exists(p.getLastName())) {
                  action.addFieldError("eml.project.personnel[" + index + "].lastName",
                    action.getText("validation.firstname.lastname"));
                }
                // At least a lastName has to exist
                else if (!exists(p.getLastName())) {
                  action.addFieldError("eml.project.personnel[" + index + "].lastName",
                    action.getText("validation.required", new String[] {action.getText("eml.project.personnel.lastName")}));
                }

                // directory and personnel id both required (if either is supplied)
                if (!p.getUserIds().isEmpty()) {
                  if (exists(p.getUserIds().get(0).getDirectory()) && !exists(p.getUserIds().get(0).getIdentifier())) {
                    action.addFieldError("eml.project.personnel[" + index + "].userIds[0].identifier",
                      action.getText("validation.personnel"));
                  } else if (!exists(p.getUserIds().get(0).getDirectory()) && exists(p.getUserIds().get(0).getIdentifier())) {
                    action.addFieldError("eml.project.personnel[" + index + "].userIds[0].directory",
                      action.getText("validation.directory"));
                  }
                }
              }
            }
          }
          break;

        case METHODS_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isMethodsPageEmpty(eml)) {

            boolean emptyFields = false;
            if (StringUtils.isBlank(eml.getSampleDescription()) && StringUtils.isBlank(eml.getStudyExtent()) &&
                StringUtils.isBlank(eml.getQualityControl())) {
              eml.setSampleDescription(null);
              eml.setStudyExtent(null);
              eml.setQualityControl(null);
              emptyFields = true;
            }
            // method step required
            int index = 0;
            for (String method : eml.getMethodSteps()) {
              if (method.trim().length() == 0) {
                if (emptyFields && index == 0) {
                  eml.getMethodSteps().clear();
                  break;
                } else {
                  action.addFieldError("eml.methodSteps[" + index + "]",
                    action.getText("validation.required", new String[] {action.getText("validation.field.required")}));
                }
              }
              index++;
            }
            // both study extent and sampling description required if either one is present
            if (!emptyFields) {
              if (StringUtils.isNotBlank(eml.getSampleDescription()) && StringUtils.isBlank(eml.getStudyExtent())) {
                action.addFieldError("eml.studyExtent",
                  action.getText("validation.required", new String[] {action.getText("eml.studyExtent")}));
              }
              if (StringUtils.isNotBlank(eml.getStudyExtent()) && StringUtils.isBlank(eml.getSampleDescription())) {
                action.addFieldError("eml.sampleDescription",
                  action.getText("validation.required", new String[] {action.getText("eml.sampleDescription")}));
              }
            }
          }
          break;

        case CITATIONS_SECTION:
          // at least one field has to have had data entered into it to qualify for validation, unless auto-generation
          // of citation is turned on
          if (!isCitationsPageEmpty(eml) || resource.isCitationAutoGenerated()) {

            // recreate the auto-generated citation if auto-generation is turned on
            if (resource.isCitationAutoGenerated()) {
              // resource homepage is potential citation identifier
              URI homepage = cfg.getResourceVersionUri(resource.getShortname(), resource.getNextVersion());
              String citation = resource.generateResourceCitation(resource.getNextVersion(), homepage);
              eml.getCitation().setCitation(citation);
            }

            // evaluate Citation
            if (eml.getCitation() != null) {
              // citation identifier must be between 2 and 200 characters long
              if (StringUtils.isNotBlank(eml.getCitation().getIdentifier())
                  && !existsInRange(eml.getCitation().getIdentifier(), 2, 200)) {
                action.addFieldError("eml.citation.identifier",
                  action.getText("validation.field.invalidSize", new String[]
                    {action.getText("eml.citation.identifier"), "2", "200"}));
              }
              // citation text is required, while identifier attribute is optional
              if (exists(eml.getCitation().getIdentifier()) && !exists(eml.getCitation().getCitation())) {
                action.addFieldError("eml.citation.citation",
                  action.getText("validation.required", new String[] {action.getText("eml.citation.citation")}));
              }
            }

            int index = 0;
            for (Citation citation : eml.getBibliographicCitations()) {
              if (StringUtils.isNotBlank(citation.getIdentifier()) && !exists(citation.getIdentifier())) {
                action.addFieldError("eml.bibliographicCitationSet.bibliographicCitations[" + index + "].identifier",
                  action
                    .getText("validation.field.blank",
                      new String[] {action.getText("eml.bibliographicCitationSet.bibliographicCitations.identifier")}));
              }
              if (!exists(citation.getCitation())) {
                action.addFieldError("eml.bibliographicCitationSet.bibliographicCitations[" + index + "].citation",
                  action.getText("validation.required", new String[] {action.getText("validation.field.required")}));
              }
              index++;
            }
          }
          break;

        case COLLECTIONS_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isCollectionsPageEmpty(eml)) {

            // at least one collection with name filled in is required
            if (eml.getCollections().isEmpty()) {
              action.addActionError(action.getText("eml.collection.required"));
            }

            for (int index = 0; index < eml.getCollections().size(); index++) {
              Collection c = eml.getCollections().get(index);

              // collection name is required, collection id and parent collection id are NOT required
              if (!exists(c.getCollectionName())) {
                action.addFieldError("eml.collections[" + index + "].collectionName",
                  action.getText("validation.required", new String[] {action.getText("eml.collectionName")}));
              }
            }

            // TODO ensure this validation works with drop down, and bypasses default
            for (int index = 0; index < eml.getSpecimenPreservationMethods().size(); index++) {
              String preservationMethod = eml.getSpecimenPreservationMethods().get(index);

              // collection name is required, collection id and parent collection id are NOT required
              if (StringUtils.isBlank(preservationMethod)) {
                action.addFieldError("eml.specimenPreservationMethods[" + index + "]",
                  action.getText("validation.required", new String[] {action.getText("eml.specimenPreservationMethod")}));
              }
            }

            int index = 0;
            for (JGTICuratorialUnit jcu : eml.getJgtiCuratorialUnits()) {
              if (jcu.getType() == JGTICuratorialUnitType.COUNT_RANGE) {
                if (!exists(jcu.getRangeStart())) {
                  action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeStart",
                    action.getText("validation.required", new String[] {action.getText("validation.field.required")}));
                }
                if (!exists(jcu.getRangeEnd())) {
                  action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeEnd",
                    action.getText("validation.required", new String[] {action.getText("validation.field.required")}));
                }
              }
              if (jcu.getType() == JGTICuratorialUnitType.COUNT_WITH_UNCERTAINTY) {
                if (!exists(jcu.getRangeMean())) {
                  action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeMean",
                    action.getText("validation.required", new String[] {action.getText("validation.field.required")}));
                }
                if (!exists(jcu.getUncertaintyMeasure())) {
                  action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].uncertaintyMeasure",
                    action.getText("validation.required", new String[] {action.getText("validation.field.required")}));
                }
              }
              if (!exists(jcu.getUnitType())) {
                action
                  .addFieldError(
                    "eml.jgtiCuratorialUnits[" + index + "].unitType",
                    action.getText("validation.required",
                      new String[] {action.getText("eml.jgtiCuratorialUnits.unitType")}));
              }
              index++;
            }
          }
          break;

        case PHYSICAL_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isPhysicalPageEmpty(eml)) {
            // null or empty URLs bypass validation
            if (StringUtils.isNotBlank(eml.getDistributionUrl())) {
              // retrieve a formatted homepage URL including scheme component
              String formattedUrl = formatURL(eml.getDistributionUrl());
              if (formattedUrl == null || !isWellFormedURI(formattedUrl)) {
                action.addFieldError("eml.distributionUrl",
                  action.getText("validation.invalid", new String[] {action.getText("eml.distributionUrl")}));
              } else {
                eml.setDistributionUrl(formattedUrl);
              }
            }

            // character set, download URL, and data format are required
            int index = 0;
            for (PhysicalData pd : eml.getPhysicalData()) {
              // name required
              if (!exists(pd.getName())) {
                action.addFieldError("eml.physicalData[" + index + "].name",
                  action.getText("validation.required", new String[] {action.getText("eml.physicalData.name")}));
              }
              // character set required
              if (!exists(pd.getCharset())) {
                action.addFieldError("eml.physicalData[" + index + "].charset",
                  action.getText("validation.required", new String[] {action.getText("eml.physicalData.charset")}));
              }
              // download URL required
              if (!exists(pd.getDistributionUrl())) {
                action.addFieldError("eml.physicalData[" + index + "].distributionUrl", action
                  .getText("validation.required", new String[] {action.getText("eml.physicalData.distributionUrl")}));
              }
              // data format required
              if (!exists(pd.getFormat())) {
                action.addFieldError("eml.physicalData[" + index + "].format",
                  action.getText("validation.required", new String[] {action.getText("eml.physicalData.format")}));
              }

              // data format version is optional - so skip

            /* Validate distribution URL form each Physical data */
              String formattedDistributionUrl = formatURL(pd.getDistributionUrl());
              if (formattedDistributionUrl == null || !isWellFormedURI(formattedDistributionUrl)) {
                action.addFieldError("eml.physicalData[" + index + "].distributionUrl", action
                  .getText("validation.invalid", new String[] {action.getText("eml.physicalData.distributionUrl")}));
              } else {
                pd.setDistributionUrl(formattedDistributionUrl);
              }
              index++;
            }
          }
          break;

        case ADDITIONAL_SECTION:
          // at least one field has to have had data entered into it to qualify for validation
          if (!isAdditionalPageEmpty(eml)) {
            // ensure each alternative id is longer than min length
            int index = 0;
            for (String ai : eml.getAlternateIdentifiers()) {
              if (!exists(ai)) {
                action.addFieldError("eml.alternateIdentifiers[" + index + "]",
                  action.getText("validation.required", new String[] {action.getText("eml.alternateIdentifier")}));
              }
              index++;
            }
          }
          break;

        default: break;
      }
    }
  }


  /**
   * Determine if the Project page is empty. In other words, the user hasn't entered any information for a single field
   * yet. There is a total of 7 fields on this page.
   * 
   * @param eml EML
   * @return whether the Project page is empty or not.
   */
  private boolean isProjectPageEmpty(Eml eml) {
    if (eml.getProject() != null) {
      Project project = eml.getProject();
      List<Agent> personnelList = project.getPersonnel();
      StudyAreaDescription area = project.getStudyAreaDescription();

      // total of 4 fields on page, plus multiple personnel composed of first name, last name, and role
      String title = project.getTitle();
      String funding = project.getFunding();
      String design = project.getDesignDescription();
      String desc = area.getDescriptorValue();

      return (StringUtils.isBlank(title) &&
        StringUtils.isBlank(funding) &&
        StringUtils.isBlank(design) &&
        StringUtils.isBlank(desc) &&
        isAgentsListEmpty(personnelList));
    }
    return false;
  }

  /**
   * Determine if the Methods page is empty. In other words, the user hasn't entered any information for a single field
   * yet. There is a total of 4 fields on this page. The step description can be multiple.
   * 
   * @param eml EML
   * @return whether the Methods page is empty or not.
   */
  private boolean isMethodsPageEmpty(Eml eml) {
    // total of 4 fields on page
    String studyExtent = eml.getStudyExtent();
    String sample = eml.getSampleDescription();
    String quality = eml.getQualityControl();
    List<String> methods = eml.getMethodSteps();

    // there must be absolutely nothing entered for any method steps
    for (String method : methods) {
      if (StringUtils.isNotBlank(method)) {
        return false;
      }
    }

    return (StringUtils.isBlank(studyExtent) &&
      StringUtils.isBlank(sample) && StringUtils.isBlank(quality));
  }

  /**
   * Determine if the Citations page is empty. In other words, the user hasn't entered any information for a single
   * field yet. There is a total of 4 fields on this page. The bibliographic citation can be multiple.
   * 
   * @param eml EML
   * @return whether the Citations page is empty or not.
   */
  private boolean isCitationsPageEmpty(Eml eml) {
    // is the Citation empty?
    boolean citationIsEmpty = isCitationEmpty(eml.getCitation());
    if (!citationIsEmpty) {
      return false;
    }
    // are all the bibliographic citations empty?
    for (Citation bibCitation : eml.getBibliographicCitations()) {
      boolean isBibCitationEmpty = isCitationEmpty(bibCitation);
      if (!isBibCitationEmpty) {
        return false;
      }
    }
    // otherwise
    return true;
  }

  /**
   * Determine if a Citation is empty. In other words, the user hasn't entered any information for a single
   * field yet. There is a total of 2 fields.
   * 
   * @param citation citation
   * @return whether the Citation is empty or not.
   */
  private boolean isCitationEmpty(Citation citation) {

    if (citation != null) {
      String citationId = citation.getIdentifier();
      String citationText = citation.getCitation();

      return (StringUtils.isBlank(citationId) && StringUtils.isBlank(citationText));
    }
    return true;
  }

  /**
   * Determine if the Collections page is empty. In other words, the user hasn't entered any information for a single
   * field yet. The curatorial section and collection section can be multiple.
   * 
   * @param eml EML
   * @return whether the Collections page is empty or not.
   */
  private boolean isCollectionsPageEmpty(Eml eml) {
    // check whether all specimen preservation methods are empty
    for (String preservationMethod: eml.getSpecimenPreservationMethods()) {
      if (StringUtils.isNotBlank(preservationMethod)) {
        return false;
      }
    }

    // check whether all collection are empty
    for (Collection collection: eml.getCollections()) {
      boolean isCollectionEmpty = isCollectionEmpty(collection);
      if (!isCollectionEmpty) {
        return false;
      }
    }

    // check whether all curatorial units are empty
    for (JGTICuratorialUnit unit : eml.getJgtiCuratorialUnits()) {
      boolean isUnitEmpty = isJGTICuratorialUnitEmpty(unit);
      if (!isUnitEmpty) {
        return false;
      }
    }

    return true;
  }

  /**
   * Determine if a JGTICuratorialUnit is empty. In other words, the user hasn't entered any information for a
   * single field yet.
   * 
   * @param unit JGTICuratorialUnit
   * @return whether the JGTICuratorialUnit page is empty or not.
   */
  private boolean isJGTICuratorialUnitEmpty(JGTICuratorialUnit unit) {
    if (unit != null) {
      String unitType = unit.getUnitType();
      int rangeEnd = (unit.getRangeEnd() == null) ? 0 : unit.getRangeEnd();
      int rangeStart = (unit.getRangeStart() == null) ? 0 : unit.getRangeStart();
      int uncertainty = (unit.getUncertaintyMeasure() == null) ? 0 : unit.getUncertaintyMeasure();
      int mean = (unit.getRangeMean() == null) ? 0 : unit.getRangeMean();

      return (StringUtils.isBlank(unitType) &&
        rangeEnd == 0 &&
        rangeStart == 0 &&
        uncertainty == 0 && mean == 0);
    }
    return true;
  }

  /**
   * Determine if a Collection is empty. In other words, the user hasn't entered any information for a
   * single field yet.
   *
   * @param collection collection
   * @return whether the Collection section empty or not.
   */
  private boolean isCollectionEmpty(Collection collection) {
    if (collection != null) {
      String collectionName = collection.getCollectionName();
      String collectionId = collection.getCollectionId();
      String parentCollectionId = collection.getParentCollectionId();

      return (StringUtils.isBlank(collectionName) &&
              StringUtils.isBlank(collectionId) &&
              StringUtils.isBlank(parentCollectionId));
    }
    return true;
  }

  /**
   * Determine if the Physical page is empty. In other words, the user hasn't entered any information for a single
   * field yet. There is a total of 6 fields on this page. The link section can be multiple.
   * 
   * @param eml EML
   * @return whether the Physical page is empty or not.
   */
  private boolean isPhysicalPageEmpty(Eml eml) {
    // total of 8 fields on page
    String homepageUrl = eml.getDistributionUrl();

    // check all external links
    for (PhysicalData data : eml.getPhysicalData()) {
      boolean isLinkEmpty = isExternalLinkEmpty(data);
      if (!isLinkEmpty) {
        return false;
      }
    }
    // otherwise it all comes down to the homepage URL
    return StringUtils.isBlank(homepageUrl);
  }

  /**
   * Determine if a PhysicalData is empty. In other words, the user hasn't entered any information for a single
   * field yet.
   * 
   * @param data PhysicalData
   * @return whether the PhysicalData is empty or not.
   */
  private boolean isExternalLinkEmpty(PhysicalData data) {
    if (data != null) {
      String charset = data.getCharset();
      String format = data.getFormat();
      String formatVersion = data.getFormatVersion();
      String distributionUrl = data.getDistributionUrl();
      String name = data.getName();

      return (StringUtils.isBlank(charset) &&
        StringUtils.isBlank(format) &&
        StringUtils.isBlank(formatVersion) &&
        StringUtils.isBlank(distributionUrl) && StringUtils.isBlank(name));
    }
    return true;
  }

  /**
   * Determine if the Keywords page is empty. In other words, the user hasn't entered any information for a single
   * field yet. There is a total of 2 fields on this page. The 2 fields together can be multiple.
   * 
   * @param eml EML
   * @return whether the Keywords page is empty or not.
   */
  private boolean isKeywordsPageEmpty(Eml eml) {
    // total of 2 fields on page
    if (!eml.getKeywords().isEmpty()) {
      KeywordSet set1 = eml.getKeywords().get(0);
      return StringUtils.isBlank(set1.getKeywordsString()) && StringUtils.isBlank(set1.getKeywordThesaurus());
    }
    return true;
  }

  /**
   * Determine if the Additional page is empty. In other words, the user hasn't entered any information for a single
   * field yet. The alternate identifier can be multiple.
   * 
   * @param eml EML
   * @return whether the Additional page is empty or not.
   */
  private boolean isAdditionalPageEmpty(Eml eml) {
    // total of 5 editable fields on page, including repeating alt. id
    String logo = eml.getLogoUrl();
    String rights = eml.getRights();
    String info = eml.getAdditionalInfo();
    String purpose = eml.getPurpose();
    // skip pubDate - it's auto-set
    // skip hierarchy - it's auto-set

    for (String id : eml.getAlternateIdentifiers()) {
      if (StringUtils.isNotBlank(id)) {
        return false;
      }
    }

    return (StringUtils.isBlank(logo) &&
      StringUtils.isBlank(rights) &&
      StringUtils.isBlank(info) && StringUtils.isBlank(purpose));

  }

  /**
   * Determine if the Temporal page is empty. In other words, the user hasn't entered any information for a single
   * field yet. The temporal coverages can be multiple.
   * 
   * @param eml EML
   * @return whether the Temporal page is empty or not.
   */
  private boolean isTemporalPageEmpty(Eml eml) {
    // total of 1 editable repeatable section on page
    List<TemporalCoverage> coverages = eml.getTemporalCoverages();
    // iterate through them, they all must be empty, otherwise they all get validated
    for (TemporalCoverage coverage : coverages) {
      boolean isEmtpy = isTemporalCoverageEmpty(coverage);
      // have we found a non-empty coverage?
      if (!isEmtpy) {
        return false;
      }
    }
    // otherwise, at least one coverage was not empty, and they must all be validated
    return true;
  }

  /**
   * Determine if a single TemporalCoverage is empty. In other words, the user hasn't entered any information for a
   * single field yet.
   * 
   * @param cov TemporalCoverage
   * @return whether the TemporalCoverage is empty or not.
   */
  private boolean isTemporalCoverageEmpty(TemporalCoverage cov) {
    if (cov != null) {

      String formationPeriod = cov.getFormationPeriod();
      Date end = cov.getEndDate();
      String period = cov.getLivingTimePeriod();
      Date start = cov.getStartDate();

      return (StringUtils.isBlank(formationPeriod) &&
        end == null &&
        StringUtils.isBlank(period) && start == null);
    }
    return true;
  }

  /**
   * Determine if the Taxonomic page is empty. In other words, the user hasn't entered any information for a single
   * field yet. The taxonomic coverages can be multiple.
   * 
   * @param eml EML
   * @return whether the Taxonomic page is empty or not.
   */
  private boolean isTaxonomicPageEmpty(Eml eml) {
    // total of 1 editable repeatable section on page
    for (TaxonomicCoverage cov : eml.getTaxonomicCoverages()) {
      boolean isTaxonomicCoverageEmpty = isTaxonomicCoverageEmpty(cov);
      if (!isTaxonomicCoverageEmpty) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determine if a TaxonomicCoverage is empty. In other words, the user hasn't entered any information for a single
   * field yet.
   * 
   * @param cov TaxonomicCoverage
   * @return whether the TaxonomicCoverage is empty or not.
   */
  private boolean isTaxonomicCoverageEmpty(TaxonomicCoverage cov) {
    if (cov != null) {
      String description = cov.getDescription();
      // check all TaxonKeyword are empty
      for (TaxonKeyword word : cov.getTaxonKeywords()) {
        boolean isTaxonKeywordEmpty = isTaxonKeywordEmpty(word);
        if (!isTaxonKeywordEmpty) {
          return false;
        }
      }
      // gotten here means all TaxonKeyword were empty, therefore the only thing left to check is the description
      return StringUtils.isBlank(description);
    }
    return true;
  }

  /**
   * Determine if a TaxonKeyword is empty. In other words, the user hasn't entered any information for a single
   * field yet.
   * 
   * @param word TaxonKeyword
   * @return whether the TaxonKeyword is empty or not.
   */
  private boolean isTaxonKeywordEmpty(TaxonKeyword word) {
    if (word != null) {
      String scientificName = word.getScientificName();
      String common = word.getCommonName();
      String rank = word.getRank();
      return StringUtils.isBlank(scientificName) && StringUtils.isBlank(common) && StringUtils.isBlank(rank);
    }
    return true;
  }

  /**
   * Determine if the Geo page is empty. In other words, the user hasn't entered any information for a single
   * field yet. The geo coverages can be multiple.
   * 
   * @param eml EML
   * @return whether the Geo page is empty or not.
   */
  private boolean isGeoPageEmpty(Eml eml) {
    if (!eml.getGeospatialCoverages().isEmpty()) {
      GeospatialCoverage cov1 = eml.getGeospatialCoverages().get(0);
      String description = cov1.getDescription();
      BBox bbox = cov1.getBoundingCoordinates();
      Point p1 = bbox.getMin();
      Point p2 = bbox.getMax();

      if (p1 != null && p2 != null) {
        Double lat1 = p1.getLatitude();
        Double lon1 = p1.getLongitude();
        Double lat2 = p2.getLatitude();
        Double lon2 = p2.getLongitude();

        return (lat1 == null && lon1 == null && lat2 == null && lon2 == null && StringUtils.isBlank(description));
      } else {
        return StringUtils.isBlank(description);
      }
    }
    return true;
  }

  /**
   * Determine if the Agent is empty. In other words, the user hasn't entered any information for a single
   * field yet.
   *
   * @param agent Agent
   *
   * @return whether the Agent is empty or not.
   */
  private boolean isAgentEmpty(Agent agent) {
    if (agent != null) {
      String first = agent.getFirstName();
      String last = agent.getLastName();
      String email = agent.getEmail();
      String home = agent.getHomepage();
      String org = agent.getOrganisation();
      String phone = agent.getPhone();
      String position = agent.getPosition();

      String city = null;
      String street = null;
      String country = null;
      String code = null;
      String province = null;
      Address address = agent.getAddress();
      if (address != null) {
        city = address.getCity();
        street = address.getAddress();
        country = address.getCountry();
        code = address.getPostalCode();
        province = address.getProvince();
      }

      // only one userId supported
      String directory = null;
      String identifier = null;
      List<UserId> userIds = agent.getUserIds();
      if (!userIds.isEmpty()) {
        UserId userId = userIds.get(0);
        directory = userId.getDirectory();
        identifier = userId.getIdentifier();
      }

      return (StringUtils.isBlank(city) &&
              StringUtils.isBlank(street) &&
              StringUtils.isBlank(country) &&
              StringUtils.isBlank(code) &&
              StringUtils.isBlank(province) &&
              StringUtils.isBlank(first) &&
              StringUtils.isBlank(last) &&
              StringUtils.isBlank(email) &&
              StringUtils.isBlank(home) &&
              StringUtils.isBlank(org) &&
              StringUtils.isBlank(phone) &&
              StringUtils.isBlank(position) &&
              StringUtils.isBlank(directory) &&
              StringUtils.isBlank(identifier));
    }
    return true;
  }

  /**
   * Determine if the Agent list is empty (there isn't a single non-empty agent). In other words, the user hasn't
   * entered any information for a single field yet.
   *
   * @param agents list
   * @return false if a non-empty agent was encountered, or true otherwise
   */
  private boolean isAgentsListEmpty(List<Agent> agents) {
    for (Agent agent : agents) {
      boolean isEmpty = isAgentEmpty(agent);
      // we are interested in finding a non-empty agent
      if (!isEmpty) {
        return false;
      }
    }
    return true;
  }
}
