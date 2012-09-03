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
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.JGTICuratorialUnit;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.PhysicalData;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.metadata.eml.TemporalCoverageType;

import java.net.URI;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

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
   * @return the URL formatted with the schema component
   */
  public static String formatURL(String url) {
    if (url != null) {
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
   * Validate an EML document, optionally only a part of it matching the individual forms on the metadata editor:
   * "basic","geocoverage","taxcoverage","tempcoverage", "keywords", "parties", "project", "methods", "citations",
   * "collections", "physical", "additional".
   *
   * @param action BaseAction
   * @param eml    EML
   * @param part   EML document part name
   */
  public void validate(BaseAction action, Eml eml, @Nullable String part) {
    if (eml != null) {
      if (part == null || part.equalsIgnoreCase("basic")) {

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

      } else if (part == null || part.equalsIgnoreCase("parties")) {
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
              action.addFieldError("eml.associatedParties[" + index + "].homepage",
                action.getText("validation.invalid", new String[] {action.getText("eml.associatedParties.homepage")}));
            } else {
              eml.getAssociatedParties().get(index)
                .setHomepage(formatURL(eml.getAssociatedParties().get(index).getHomepage()));
            }
          }
        }
      } else if (part == null || part.equalsIgnoreCase("geocoverage")) {
        Double coord = 0.0;
        for (int index = 0; index < eml.getGeospatialCoverages().size(); index++) {
          // The Bounding coordinates and description are mandatory.
          // If all fields are empty, the <coverage> label in eml.xml will be removed.
          if (eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMin().getLongitude() == null &&
              eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMax().getLongitude() == null &&
              eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMin().getLatitude() == null &&
              eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMax().getLatitude() == null &&
              eml.getGeospatialCoverages().get(index).getDescription().equals("")) {
            eml.getGeospatialCoverages().clear();
          }
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
            if (eml.getGeospatialCoverages().get(index).getDescription().length() == 0) {
              action.addFieldError("eml.geospatialCoverages[" + index + "].description", action
                .getText("validation.required", new String[] {action.getText("eml.geospatialCoverages.description")}));
            } else if (!exists(eml.getGeospatialCoverages().get(index).getDescription(), 2)) {
              action.addFieldError("eml.geospatialCoverages[" + index + "].description", action
                .getText("validation.short",
                  new String[] {action.getText("eml.geospatialCoverages.description"), "2"}));
            }
          }
        }
      } else if (part == null || part.equalsIgnoreCase("taxcoverage")) {
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
      } else if (part == null || part.equalsIgnoreCase("tempcoverage")) {
        int index = 0;
        for (TemporalCoverage tc : eml.getTemporalCoverages()) {
          if (tc.getType() == TemporalCoverageType.SINGLE_DATE && !exists(tc.getStartDate())) {
            action.addFieldError("eml.temporalCoverages[" + index + "].startDate",
              action.getText("validation.required", new String[] {action.getText("eml.temporalCoverages.startDate")}));
          }
          if (tc.getType() == TemporalCoverageType.DATE_RANGE) {
            if (!exists(tc.getStartDate())) {
              action.addFieldError("eml.temporalCoverages[" + index + "].startDate", action
                .getText("validation.required", new String[] {action.getText("eml.temporalCoverages.startDate")}));
            }
            if (!exists(tc.getEndDate())) {
              action.addFieldError("eml.temporalCoverages[" + index + "].endDate",
                action.getText("validation.required", new String[] {action.getText("eml.temporalCoverages.endDate")}));
            }
          }
          if (tc.getType() == TemporalCoverageType.FORMATION_PERIOD && !exists(tc.getFormationPeriod())) {
            action.addFieldError("eml.temporalCoverages[" + index + "].formationPeriod", action
              .getText("validation.required", new String[] {action.getText("eml.temporalCoverages.formationPeriod")}));
          }
          if (tc.getType() == TemporalCoverageType.LIVING_TIME_PERIOD && !exists(tc.getLivingTimePeriod())) {
            action.addFieldError("eml.temporalCoverages[" + index + "].livingTimePeriod", action
              .getText("validation.required", new String[] {action.getText("eml.temporalCoverages.livingTimePeriod")}));
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("project")) {
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

      } else if (part == null || part.equalsIgnoreCase("methods")) {

        boolean emptyFields = false;
        if (eml.getSampleDescription().length() == 0 && eml.getStudyExtent().length() == 0 &&
            eml.getQualityControl().length() == 0) {
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
          if (!(eml.getSampleDescription().length() == 0) && eml.getStudyExtent().length() == 0) {
            action.addFieldError("eml.studyExtent",
              action.getText("validation.required", new String[] {action.getText("eml.studyExtent")}));
          }
          if (!(eml.getStudyExtent().length() == 0) && eml.getSampleDescription().length() == 0) {
            action.addFieldError("eml.sampleDescription",
              action.getText("validation.required", new String[] {action.getText("eml.sampleDescription")}));
          }
        }
      } else if (part == null || part.equalsIgnoreCase("citations")) {
        // citation text is required, while identifier attribute is optional
        if (!(eml.getCitation().getIdentifier().length() == 0) && !exists(eml.getCitation().getIdentifier())) {
          action.addFieldError("eml.citation.identifier",
            action.getText("validation.field.blank", new String[] {action.getText("eml.citation.identifier")}));
        } else {
          eml.getCitation().setIdentifier(eml.getCitation().getIdentifier().trim());
        }

        if (exists(eml.getCitation().getIdentifier()) && !exists(eml.getCitation().getCitation())) {
          action.addFieldError("eml.citation.citation",
            action.getText("validation.required", new String[] {action.getText("eml.citation.citation")}));
        }

        int index = 0;
        for (Citation citation : eml.getBibliographicCitations()) {
          if (!(citation.getIdentifier().length() == 0) && !exists(citation.getIdentifier())) {
            action.addFieldError("eml.bibliographicCitationSet.bibliographicCitations[" + index + "].identifier", action
              .getText("validation.field.blank",
                new String[] {action.getText("eml.bibliographicCitationSet.bibliographicCitations.identifier")}));
          }
          if (!exists(citation.getCitation())) {
            action.addFieldError("eml.bibliographicCitationSet.bibliographicCitations[" + index + "].citation",
              action.getText("validation.required", new String[] {action.getText("validation.field.required")}));
          }
          index++;
        }

      } else if (part == null || part.equalsIgnoreCase("collections")) {
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
            action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].unitType",
              action.getText("validation.required", new String[] {action.getText("eml.jgtiCuratorialUnits.unitType")}));
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("physical")) {
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
            action.addFieldError("eml.physicalData[" + index + "].distributionUrl",
              action.getText("validation.required", new String[] {action.getText("eml.physicalData.distributionUrl")}));
          }
          // data format required
          if (!exists(pd.getFormat())) {
            action.addFieldError("eml.physicalData[" + index + "].format",
              action.getText("validation.required", new String[] {action.getText("eml.physicalData.format")}));
          }

          // data format version is optional - so skip

          /* Validate distribution URL form each Physical data */
          if (pd.getDistributionUrl() != null) {
            if (formatURL(pd.getDistributionUrl()) == null) {
              action.addFieldError("eml.physicalData[" + index + "].distributionUrl", action
                .getText("validation.invalid", new String[] {action.getText("eml.physicalData.distributionUrl")}));
            } else {
              pd.setDistributionUrl(formatURL(pd.getDistributionUrl()));
            }
          }
          index++;
        }
        /* Validate the distribution URL */
        if (eml.getDistributionUrl() != null) {
          if (formatURL(eml.getDistributionUrl()) == null) {
            action.addFieldError("eml.distributionUrl",
              action.getText("validation.invalid", new String[] {action.getText("eml.distributionUrl")}));
          } else {
            eml.setDistributionUrl(formatURL(eml.getDistributionUrl()));
          }
        }
      } else if (part == null || part.equalsIgnoreCase("keywords")) {
        int index = 0;
        for (KeywordSet ks : eml.getKeywords()) {
          if (!exists(ks.getKeywordsString())) {
            action.addFieldError("eml.keywords[" + index + "].keywordsString",
              action.getText("validation.required", new String[] {action.getText("eml.keywords.keywordsString")}));
          }
          if (!exists(ks.getKeywordThesaurus())) {
            action.addFieldError("eml.keywords[" + index + "].keywordThesaurus",
              action.getText("validation.required", new String[] {action.getText("eml.keywords.keywordThesaurus")}));
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("additional")) {
        if (eml.getPubDate() == null) {
          action.addFieldError("eml.pubDate",
            action.getText("validation.required", new String[] {action.getText("eml.pubDate")}));
        }
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
