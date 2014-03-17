/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***************************************************************************/

package org.gbif.ipt.validation;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.Address;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.BBox;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.GeospatialCoverage;
import org.gbif.metadata.eml.JGTICuratorialUnit;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.PhysicalData;
import org.gbif.metadata.eml.Point;
import org.gbif.metadata.eml.Project;
import org.gbif.metadata.eml.StudyAreaDescription;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.metadata.eml.TemporalCoverageType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

public class EmlValidator extends BaseValidator {

  protected static Pattern phonePattern = Pattern.compile("[\\w ()/+-\\.]+");
  private AppConfig cfg;
  private RegistrationManager regManager;
  private SimpleTextProvider simpleTextProvider;
  protected static final String BASIC_SECTION = "basic";
  protected static final String GEOCOVERAGE_SECTION = "geocoverage";
  protected static final String TAXCOVERAGE_SECTION = "taxcoverage";
  protected static final String TEMPCOVERAGE_SECTION = "tempcoverage";
  protected static final String PROJECT_SECTION = "project";
  protected static final String METHODS_SECTION = "methods";
  protected static final String CITATIONS_SECTION = "citations";
  protected static final String COLLECTIONS_SECTION = "collections";
  protected static final String PHYSICAL_SECTION = "physical";
  protected static final String KEYWORDS_SECTION = "keywords";
  protected static final String ADDITIONAL_SECTION = "additional";
  protected static final String PARTIES_SECTION = "parties";
  private static final List<String> SECTIONS_LIST = new ImmutableList.Builder<String>().add(BASIC_SECTION)
    .add(GEOCOVERAGE_SECTION).add(TAXCOVERAGE_SECTION).add(TEMPCOVERAGE_SECTION).add(KEYWORDS_SECTION)
    .add(PARTIES_SECTION).add(PROJECT_SECTION).add(METHODS_SECTION).add(CITATIONS_SECTION).add(COLLECTIONS_SECTION)
    .add(PHYSICAL_SECTION).add(ADDITIONAL_SECTION).build();

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
    if (!Strings.isNullOrEmpty(url)) {
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

  public boolean isValid(Eml eml, @Nullable String part) {
    BaseAction action = new BaseAction(simpleTextProvider, cfg, regManager);
    validate(action, eml, part);
    return !(action.hasActionErrors() || action.hasFieldErrors());
  }

  /**
   * Validate if all metadata sections are valid. For the first section encountered that doesn't validate, an
   * error message will appear for that section only.
   * 
   * @param action Action
   * @param eml EML
   * @return whether all sections validated or not
   */
  public boolean areAllSectionsValid(BaseAction action, Eml eml) {
    boolean problemsEncountered = false;
    for (String section : SECTIONS_LIST) {
      validate(action, eml, section);
      // only highlight first section has errors
      if ((action.hasActionErrors() || action.hasFieldErrors()) && !problemsEncountered) {
        action.addActionError(action.getText("manage.failed", new String[] {action.getText("submenu." + section)}));
        problemsEncountered = true;
      }
    }
    return !problemsEncountered;
  }

  /**
   * Validate an EML document, optionally only a part of it matching the individual sections on the metadata editor:
   * "basic","geocoverage","taxcoverage","tempcoverage", "keywords", "parties", "project", "methods", "citations",
   * "collections", "physical", "additional".
   * </p>
   * For each section, validation only proceeds if at least one field has been entered.
   * 
   * @param action BaseAction
   * @param eml EML
   * @param part EML document part name
   */
  public void validate(BaseAction action, Eml eml, @Nullable String part) {
    if (eml != null) {
      if (part == null || part.equalsIgnoreCase(BASIC_SECTION)) {

        // can't bypass the basic metadata page - it is absolutely mandatory

        // Title - mandatory
        if (!exists(eml.getTitle()) || eml.getTitle().trim().length() == 0) {
          action.addFieldError("eml.title",
            action.getText("validation.required", new String[] {action.getText("eml.title")}));
        }

        // description - mandatory and greater than 5 chars
        if (!exists(eml.getDescription())) {
          action.addFieldError("eml.description",
            action.getText("validation.required", new String[] {action.getText("eml.description")}));
        } else if (!exists(eml.getDescription(), 5)) {
          action.addFieldError("eml.description",
            action.getText("validation.short", new String[] {action.getText("eml.description"), "5"}));
        }

        // Contact: At least have to exist an organisation or a lastName (or both)
        if (!exists(eml.getContact().getOrganisation()) && !exists(eml.getContact().getLastName()) &&
          !exists(eml.getContact().getPosition())) {
          if (!action.getActionErrors().contains(action.getText("validation.lastname.organisation.position"))) {
            action.addActionError(action.getText("validation.lastname.organisation.position"));
          }
          action.addFieldError("eml.contact.organisation",
            action.getText("validation.required", new String[] {action.getText("eml.contact.organisation")}));
          action.addFieldError("eml.contact.lastName",
            action.getText("validation.required", new String[] {action.getText("eml.contact.lastName")}));
          action.addFieldError("eml.contact.position",
            action.getText("validation.required", new String[] {action.getText("eml.contact.position")}));
        } else {
          // firstName - optional. But if firstName exists, lastName have to exist
          if (exists(eml.getContact().getFirstName()) && !exists(eml.getContact().getLastName())) {
            action.addFieldError("eml.contact.lastName", action.getText("validation.firstname.lastname"));
          }
        }

        // email is optional. But if exists, should be valid
        if (exists(eml.getContact().getEmail()) && !isValidEmail(eml.getContact().getEmail())) {
          action.addFieldError("eml.contact.email",
            action.getText("validation.invalid", new String[] {action.getText("eml.contact.email")}));
        }

        // phone is optional. But if it exists, should match the pattern
        if (exists(eml.getContact().getPhone()) && !isValidPhoneNumber(eml.getContact().getPhone())) {
          action.addFieldError("eml.contact.phone",
            action.getText("validation.invalid", new String[] {action.getText("eml.contact.phone")}));
        }

        // Validate the homepage URL form resource contact
        if (eml.getContact().getHomepage() != null) {
          if (formatURL(eml.getContact().getHomepage()) == null) {
            action.addFieldError("eml.contact.homepage",
              action.getText("validation.invalid", new String[] {action.getText("eml.contact.homepage")}));
          } else {
            eml.getContact().setHomepage(formatURL(eml.getContact().getHomepage()));
          }
        }

        // Creator: at least have to exist an organisation, a lastName or a position
        if (!exists(eml.getResourceCreator().getOrganisation()) && !exists(eml.getResourceCreator().getLastName()) &&
          !exists(eml.getResourceCreator().getPosition())) {
          if (!action.getActionErrors().contains(action.getText("validation.lastname.organisation.position"))) {
            action.addActionError(action.getText("validation.lastname.organisation.position"));
          }
          action.addFieldError("eml.resourceCreator.organisation",
            action.getText("validation.required", new String[] {action.getText("eml.resourceCreator.organisation")}));
          action.addFieldError("eml.resourceCreator.lastName",
            action.getText("validation.required", new String[] {action.getText("eml.resourceCreator.lastName")}));
          action.addFieldError("eml.resourceCreator.position",
            action.getText("validation.required", new String[] {action.getText("eml.resourceCreator.position")}));
        } else {
          // firstName - optional. But if firstName exists, lastName have to exist too
          if (exists(eml.getResourceCreator().getFirstName()) && !exists(eml.getResourceCreator().getLastName())) {
            action.addFieldError("eml.resourceCreator.lastName", action.getText("validation.firstname.lastname"));
          }
        }

        // email is optional. But if it exists, should be a valid email address
        if (exists(eml.getResourceCreator().getEmail()) && !isValidEmail(eml.getResourceCreator().getEmail())) {
          action.addFieldError("eml.resourceCreator.email",
            action.getText("validation.invalid", new String[] {action.getText("eml.resourceCreator.email")}));
        }

        // phone is optional. But if it exists, should match the pattern
        if (exists(eml.getResourceCreator().getPhone()) && !isValidPhoneNumber(eml.getResourceCreator().getPhone())) {
          action.addFieldError("eml.resourceCreator.phone",
            action.getText("validation.invalid", new String[] {action.getText("eml.resourceCreator.phone")}));
        }

        // Validate the homepage URL from resource creator
        if (eml.getResourceCreator().getHomepage() != null) {
          if (formatURL(eml.getResourceCreator().getHomepage()) == null) {
            action.addFieldError("eml.resourceCreator.homepage",
              action.getText("validation.invalid", new String[] {action.getText("eml.resourceCreator.homepage")}));
          } else {
            eml.getResourceCreator().setHomepage(formatURL(eml.getResourceCreator().getHomepage()));
          }
        }

        // Metadata provider: at least have to exist an organisation, a lastName or a position
        if (!exists(eml.getMetadataProvider().getOrganisation()) && !exists(eml.getMetadataProvider().getLastName()) &&
          !exists(eml.getMetadataProvider().getPosition())) {
          if (!action.getActionErrors().contains(action.getText("validation.lastname.organisation.position"))) {
            action.addActionError(action.getText("validation.lastname.organisation.position"));
          }
          action.addFieldError("eml.metadataProvider.organisation",
            action.getText("validation.required", new String[] {action.getText("eml.metadataProvider.organisation")}));
          action.addFieldError("eml.metadataProvider.lastName",
            action.getText("validation.required", new String[] {action.getText("eml.metadataProvider.lastName")}));
          action.addFieldError("eml.metadataProvider.position",
            action.getText("validation.required", new String[] {action.getText("eml.metadataProvider.position")}));
        } else {
          // firstName - optional. But if firstName exists, lastName have to exist too
          if (exists(eml.getMetadataProvider().getFirstName()) && !exists(eml.getMetadataProvider().getLastName())) {
            action.addFieldError("eml.metadataProvider.lastName", action.getText("validation.firstname.lastname"));
          }
        }

        // email is optional. But if it exists, should be a valid email address
        if (exists(eml.getMetadataProvider().getEmail()) && !isValidEmail(eml.getMetadataProvider().getEmail())) {
          action.addFieldError("eml.metadataProvider.email",
            action.getText("validation.invalid", new String[] {action.getText("eml.metadataProvider.email")}));
        }

        /* phone is optional. But if it exists, should match the pattern */
        if (exists(eml.getMetadataProvider().getPhone()) && !isValidPhoneNumber(eml.getMetadataProvider().getPhone())) {
          action.addFieldError("eml.metadataProvider.phone",
            action.getText("validation.invalid", new String[] {action.getText("eml.metadataProvider.phone")}));
        }

        /* Validate the homepage URL from metadata provider */
        if (eml.getMetadataProvider().getHomepage() != null) {
          if (formatURL(eml.getMetadataProvider().getHomepage()) == null) {
            action.addFieldError("eml.metadataProvider.homepage",
              action.getText("validation.invalid", new String[] {action.getText("eml.metadataProvider.homepage")}));
          } else {
            eml.getMetadataProvider().setHomepage(formatURL(eml.getMetadataProvider().getHomepage()));
          }
        }
      } else if (part == null || part.equalsIgnoreCase(PARTIES_SECTION)) {
        // at least one field has to have had data entered into it to qualify for validation
        if (!isPartiesPageEmpty(eml)) {
          for (int index = 0; index < eml.getAssociatedParties().size(); index++) {
            /* firstName - optional. But if firstName exists, lastName have to exist */
            if (exists(eml.getAssociatedParties().get(index).getFirstName()) &&
              !exists(eml.getAssociatedParties().get(index).getLastName())) {
              action.addFieldError("eml.associatedParties[" + index + "].lastName",
                action.getText("validation.firstname.lastname"));
            }

            // At least one of organisation, position, or a lastName have to exist
            if (!exists(eml.getAssociatedParties().get(index).getOrganisation()) &&
              !exists(eml.getAssociatedParties().get(index).getLastName()) &&
              !exists(eml.getAssociatedParties().get(index).getPosition())) {
              action.addActionError(action.getText("validation.lastname.organisation.position"));
              action.addFieldError("eml.associatedParties[" + index + "].organisation", action
                .getText("validation.required", new String[] {action.getText("eml.associatedParties.organisation")}));
              action.addFieldError("eml.associatedParties[" + index + "].lastName",
                action.getText("validation.required", new String[] {action.getText("eml.associatedParties.lastName")}));
              action.addFieldError("eml.associatedParties[" + index + "].position",
                action.getText("validation.required", new String[] {action.getText("eml.associatedParties.position")}));
            }

            /* email is optional. But if it exists, should be a valid email address */
            if (exists(eml.getAssociatedParties().get(index).getEmail()) &&
              !isValidEmail(eml.getAssociatedParties().get(index).getEmail())) {
              action.addFieldError("eml.associatedParties[" + index + "].email",
                action.getText("validation.invalid", new String[] {action.getText("eml.associatedParties.email")}));
            }

            /* phone is optional. But if it exists, should match the pattern */
            if (exists(eml.getAssociatedParties().get(index).getPhone()) &&
              !isValidPhoneNumber(eml.getAssociatedParties().get(index).getPhone())) {
              action.addFieldError("eml.associatedParties[" + index + "].phone",
                action.getText("validation.invalid", new String[] {action.getText("eml.associatedParties.phone")}));
            }

            /* Validate the homepage URL from each associated parties */
            if (eml.getAssociatedParties().get(index).getHomepage() != null) {
              if (formatURL(eml.getAssociatedParties().get(index).getHomepage()) == null) {
                action
                  .addFieldError("eml.associatedParties[" + index + "].homepage",
                    action.getText("validation.invalid",
                      new String[] {action.getText("eml.associatedParties.homepage")}));
              } else {
                eml.getAssociatedParties().get(index)
                  .setHomepage(formatURL(eml.getAssociatedParties().get(index).getHomepage()));
              }
            }
          }
        }
      } else if (part == null || part.equalsIgnoreCase(GEOCOVERAGE_SECTION)) {

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
              if (Strings.isNullOrEmpty(eml.getGeospatialCoverages().get(index).getDescription())) {
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
      } else if (part == null || part.equalsIgnoreCase(TAXCOVERAGE_SECTION)) {

        // at least one field has to have had data entered into it to qualify for validation
        if (!isTaxonomicPageEmpty(eml)) {
          // scientific name is required
          int index = 0;
          for (TaxonomicCoverage tc : eml.getTaxonomicCoverages()) {
            int kw = 0;
            for (TaxonKeyword k : tc.getTaxonKeywords()) {
              if (!exists(k.getScientificName())) {
                action.addFieldError("eml.taxonomicCoverages[" + index + "].taxonKeywords[" + kw + "].scientificName",
                  action.getText("validation.required",
                    new String[] {action.getText("eml.taxonomicCoverages.taxonKeyword.scientificName")}));
              }
              kw++;
            }
            index++;
          }
        }
      } else if (part == null || part.equalsIgnoreCase(TEMPCOVERAGE_SECTION)) {

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
              action.addFieldError(
                "eml.temporalCoverages[" + index + "].livingTimePeriod",
                action
                  .getText("validation.required",
                    new String[] {action.getText("eml.temporalCoverages.livingTimePeriod")}));
            }
            index++;
          }
        }
      } else if (part == null || part.equalsIgnoreCase(PROJECT_SECTION)) {

        // at least one field has to have had data entered into it to qualify for validation
        if (!isProjectPageEmpty(eml)) {

          // title is required
          if (!exists(eml.getProject().getTitle()) || eml.getProject().getTitle().trim().length() == 0) {
            action.addFieldError("eml.project.title",
              action.getText("validation.required", new String[] {action.getText("eml.project.title")}));
          }

          // First Name is optional but if exists, last name must to exist, and so too must the role
          if (exists(eml.getProject().getPersonnel().getFirstName()) &&
            !exists(eml.getProject().getPersonnel().getLastName())) {
            action.addFieldError("eml.project.personnel.lastName", action.getText("validation.firstname.lastname"));
          } else if (!exists(eml.getProject().getPersonnel().getLastName())) {
            action.addFieldError("eml.project.personnel.lastName",
              action.getText("validation.required", new String[] {action.getText("eml.project.personnel.lastName")}));
          }
        }

      } else if (part == null || part.equalsIgnoreCase(METHODS_SECTION)) {

        // at least one field has to have had data entered into it to qualify for validation
        if (!isMethodsPageEmpty(eml)) {

          boolean emptyFields = false;
          if (Strings.isNullOrEmpty(eml.getSampleDescription()) && Strings.isNullOrEmpty(eml.getStudyExtent()) &&
            Strings.isNullOrEmpty(eml.getQualityControl())) {
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
            if (!Strings.isNullOrEmpty(eml.getSampleDescription()) && Strings.isNullOrEmpty(eml.getStudyExtent())) {
              action.addFieldError("eml.studyExtent",
                action.getText("validation.required", new String[] {action.getText("eml.studyExtent")}));
            }
            if (!Strings.isNullOrEmpty(eml.getStudyExtent()) && Strings.isNullOrEmpty(eml.getSampleDescription())) {
              action.addFieldError("eml.sampleDescription",
                action.getText("validation.required", new String[] {action.getText("eml.sampleDescription")}));
            }
          }
        }

      } else if (part == null || part.equalsIgnoreCase(CITATIONS_SECTION)) {

        // at least one field has to have had data entered into it to qualify for validation
        if (!isCitationsPageEmpty(eml)) {
          // evaluate Citation first
          if (eml.getCitation() != null) {
            // citation text is required, while identifier attribute is optional
            if (!Strings.isNullOrEmpty(eml.getCitation().getIdentifier()) && !exists(eml.getCitation().getIdentifier())) {
              action.addFieldError("eml.citation.identifier",
                action.getText("validation.field.blank", new String[] {action.getText("eml.citation.identifier")}));
            }

            if (exists(eml.getCitation().getIdentifier()) && !exists(eml.getCitation().getCitation())) {
              action.addFieldError("eml.citation.citation",
                action.getText("validation.required", new String[] {action.getText("eml.citation.citation")}));
            }
          }

          int index = 0;
          for (Citation citation : eml.getBibliographicCitations()) {
            if (!Strings.isNullOrEmpty(citation.getIdentifier()) && !exists(citation.getIdentifier())) {
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

      } else if (part == null || part.equalsIgnoreCase(COLLECTIONS_SECTION)) {

        // at least one field has to have had data entered into it to qualify for validation
        if (!isCollectionsPageEmpty(eml)) {
          // collection id, parent collection id, and collection name are all required
          if (!exists(eml.getParentCollectionId())) {
            action.addFieldError("eml.parentCollectionId",
              action.getText("validation.required", new String[] {action.getText("eml.parentCollectionId")}));
          }
          if (!exists(eml.getCollectionId())) {
            action.addFieldError("eml.collectionId",
              action.getText("validation.required", new String[] {action.getText("eml.collectionId")}));
          }
          if (!exists(eml.getCollectionName())) {
            action.addFieldError("eml.collectionName",
              action.getText("validation.required", new String[] {action.getText("eml.collectionName")}));
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
      } else if (part == null || part.equalsIgnoreCase(PHYSICAL_SECTION)) {

        // at least one field has to have had data entered into it to qualify for validation
        if (!isPhysicalPageEmpty(eml)) {
          // null or empty URLs bypass validation
          if (!Strings.isNullOrEmpty(eml.getDistributionUrl())) {
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
      } else if (part == null || part.equalsIgnoreCase(KEYWORDS_SECTION)) {

        // at least one field has to have had data entered into it to qualify for validation
        if (!isKeywordsPageEmpty(eml)) {
          int index = 0;
          for (KeywordSet ks : eml.getKeywords()) {
            // TODO: remove check for "null" below after upgrading gbif-metadata-profile to 1.0.2.4 (est for IPT v2.1.1)
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
      } else if (part == null || part.equalsIgnoreCase(ADDITIONAL_SECTION)) {

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
      Agent personnel = project.getPersonnel();
      StudyAreaDescription area = project.getStudyAreaDescription();

      // total of 7 fields on page
      String title = project.getTitle();
      String funding = project.getFunding();
      String design = project.getDesignDescription();
      String desc = area.getDescriptorValue();
      String first = personnel.getFirstName();
      String last = personnel.getLastName();

      return (Strings.isNullOrEmpty(title) &&
        Strings.isNullOrEmpty(funding) &&
        Strings.isNullOrEmpty(design) &&
        Strings.isNullOrEmpty(desc) &&
        Strings.isNullOrEmpty(first) && Strings.isNullOrEmpty(last));
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
      if (!Strings.isNullOrEmpty(method)) {
        return false;
      }
    }

    return (Strings.isNullOrEmpty(studyExtent) &&
      Strings.isNullOrEmpty(sample) && Strings.isNullOrEmpty(quality));
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

      return (Strings.isNullOrEmpty(citationId) && Strings.isNullOrEmpty(citationText));
    }
    return true;
  }

  /**
   * Determine if the Collections page is empty. In other words, the user hasn't entered any information for a single
   * field yet. There is a total of 8 fields on this page. The curatorial section can be multiple.
   * 
   * @param eml EML
   * @return whether the Collections page is empty or not.
   */
  private boolean isCollectionsPageEmpty(Eml eml) {
    // total of 8 fields on page
    String collectionName = eml.getCollectionName();
    String collectionId = eml.getCollectionId();
    String parentCollectionId = eml.getParentCollectionId();
    String preservation = eml.getSpecimenPreservationMethod();

    // check whether all curatorial units are empty
    for (JGTICuratorialUnit unit : eml.getJgtiCuratorialUnits()) {
      boolean isUnitEmpty = isJGTICuratorialUnitEmpty(unit);
      if (!isUnitEmpty) {
        return false;
      }
    }

    return (Strings.isNullOrEmpty(collectionName) &&
      Strings.isNullOrEmpty(collectionId) &&
      Strings.isNullOrEmpty(parentCollectionId) && Strings.isNullOrEmpty(preservation));
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

      return (Strings.isNullOrEmpty(unitType) &&
        rangeEnd == 0 &&
        rangeStart == 0 &&
        uncertainty == 0 && mean == 0);
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
    return Strings.isNullOrEmpty(homepageUrl);
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

      return (Strings.isNullOrEmpty(charset) &&
        Strings.isNullOrEmpty(format) &&
        Strings.isNullOrEmpty(formatVersion) &&
        Strings.isNullOrEmpty(distributionUrl) && Strings.isNullOrEmpty(name));
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
      return Strings.isNullOrEmpty(set1.getKeywordsString()) && Strings.isNullOrEmpty(set1.getKeywordThesaurus());
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
      if (!Strings.isNullOrEmpty(id)) {
        return false;
      }
    }

    return (Strings.isNullOrEmpty(logo) &&
      Strings.isNullOrEmpty(rights) &&
      Strings.isNullOrEmpty(info) && Strings.isNullOrEmpty(purpose));

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

      return (Strings.isNullOrEmpty(formationPeriod) &&
        end == null &&
        Strings.isNullOrEmpty(period) && start == null);
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
      return Strings.isNullOrEmpty(description);
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
      return Strings.isNullOrEmpty(scientificName) && Strings.isNullOrEmpty(common) && Strings.isNullOrEmpty(rank);
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

        return (lat1 == null && lon1 == null && lat2 == null && lon2 == null && Strings.isNullOrEmpty(description));
      } else {
        return Strings.isNullOrEmpty(description);
      }
    }
    return true;
  }

  /**
   * Determine if the Agent is empty. In other words, the user hasn't entered any information for a single
   * field yet.
   * 
   * @param agent Agent
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

      Address address = agent.getAddress();

      if (address != null) {
        String city = address.getCity();
        String street = address.getAddress();
        String country = address.getCountry();
        String code = address.getPostalCode();
        String province = address.getProvince();

        return (Strings.isNullOrEmpty(city) &&
          Strings.isNullOrEmpty(street) &&
          Strings.isNullOrEmpty(country) &&
          Strings.isNullOrEmpty(code) &&
          Strings.isNullOrEmpty(province) &&
          Strings.isNullOrEmpty(first) &&
          Strings.isNullOrEmpty(last) &&
          Strings.isNullOrEmpty(email) &&
          Strings.isNullOrEmpty(home) &&
          Strings.isNullOrEmpty(org) &&
          Strings.isNullOrEmpty(phone) && Strings.isNullOrEmpty(position));
      } else {
        return (Strings.isNullOrEmpty(first) &&
          Strings.isNullOrEmpty(last) &&
          Strings.isNullOrEmpty(email) &&
          Strings.isNullOrEmpty(home) &&
          Strings.isNullOrEmpty(org) &&
          Strings.isNullOrEmpty(phone) && Strings.isNullOrEmpty(position));
      }
    }
    return true;
  }

  /**
   * Determine if the Parties page is empty. In other words, the user hasn't entered any information for a single
   * field yet. The party can be multiple.
   * 
   * @param eml EML
   * @return whether the Parties page is empty or not.
   */
  private boolean isPartiesPageEmpty(Eml eml) {
    List<Agent> parties = eml.getAssociatedParties();

    for (Agent party : parties) {
      boolean isEmpty = isAgentEmpty(party);
      // we are interested in finding a non-empty party
      if (!isEmpty) {
        return false;
      }
    }
    return true;
  }
}
