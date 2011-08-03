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

import com.google.inject.internal.Nullable;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * @author markus
 */
public class EmlValidator extends BaseValidator {
  protected static Pattern phonePattern = Pattern.compile("[0-9 ()/+-]+");

  /**
   * @param url
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
    if (phone == null) {
      return false;
    }
    return phonePattern.matcher(phone).matches();
  }

  public boolean isValid(Eml eml, @Nullable String part) {
    BaseAction action = new BaseAction(new SimpleTextProvider(), AppConfig.buildMock());
    validate(action, eml, part);
    if (action.hasActionErrors() || action.hasFieldErrors()) {
      return false;
    }
    return true;
  }

  /**
   * Validate an EML document, optionally only a part of it matching the infividual forms on the metadata editor:
   * "basic","parties","geocoverage","taxcoverage","tempcoverage","project","methods","citations","collections",
   * "physical","keywords","additional"
   * 
   * @param action
   * @param eml
   * @param part
   */
  public void validate(BaseAction action, Eml eml, @Nullable String part) {
    if (eml != null) {
      if (part == null || part.equalsIgnoreCase("basic")) {
        /* BASIC.FTL - XML Schema Documentation */

        /*
         * Principal Fields
         * <dataset>
         * <title>{eml.title}</title> - mandatory
         * <language>{eml.language}</language> - optional
         * <abstract> - mandatory
         * <para>{eml.description}</para> - mandatory
         * </abstract>
         * </dataset>
         */

        /* Title - mandatory */
        if (!exists(eml.getTitle()) || eml.getTitle().trim().equals("")) {
          action.addFieldError("eml.title",
              action.getText("validation.required", new String[]{action.getText("eml.title")}));
        }
        /* languaje - optional */

        /* description - mandatory and greater than 5 chars */
        if (!exists(eml.getDescription())) {
          action.addFieldError("eml.description",
              action.getText("validation.required", new String[]{action.getText("eml.description")}));
        } else if (!exists(eml.getDescription(), 5)) {
          action.addFieldError("eml.description",
              action.getText("validation.short", new String[]{action.getText("eml.description"), "5"}));
        }

        /*
         * RESOURCE CONTACT
         * <dataset>
         * <contact> - mandatory
         * <organizationName>{eml.contact.organisation}</organizationName> |
         * <individualName> |
         * <givenName>{eml.contact.firstName}</givenName> - optional | - mandatory (at least one of them)
         * <surName>{eml.contact.lastName}</surName> - mandatory |
         * </individualName> |
         * <positionName>{eml.contact.position}</positionName> - optional |
         * <address> - optional
         * <deliveryPoint>{eml.contact.address.address}</deliveryPoint> - optional
         * <city>{eml.contact.address.city}</city> - optional
         * <administrativeArea>{eml.contact.address.province}</administrativeArea> - optional
         * <postalCode>{eml.contact.address.postalCode}</postalCode> - optional
         * <country>{eml.contact.country}</country> - optional
         * </address>
         * <phone>{eml.creator.phone}</phone> - optional
         * <electronicMailAddress>{eml.creator.email}</electronicMailAddress> - optional
         * <onlineUrl>{eml.creator.homepage}</onlineUrl> - optional
         * </contact>
         * </dataset>
         */

        /* At least have to exist an organisation or a lastName (or both) */
        if (!exists(eml.getContact().getOrganisation()) && !exists(eml.getContact().getLastName())
            && !exists(eml.getContact().getPosition())) {
          if (!action.getActionErrors().contains(action.getText("validation.lastname.organisation.position"))) {
            action.addActionError(action.getText("validation.lastname.organisation.position"));
          }
          action.addFieldError("eml.contact.organisation",
              action.getText("validation.required", new String[]{action.getText("eml.contact.organisation")}));
          action.addFieldError("eml.contact.lastName",
              action.getText("validation.required", new String[]{action.getText("eml.contact.lastName")}));
          action.addFieldError("eml.contact.position",
              action.getText("validation.required", new String[]{action.getText("eml.contact.position")}));
        } else {
          /* firstName - optional. But if firstName exists, lastName have to exist. */
          if (exists(eml.getContact().getFirstName()) && !exists(eml.getContact().getLastName())) {
            action.addFieldError("eml.contact.lastName", action.getText("validation.firstname.lastname"));
          }
        }

        /* address and all its sub-elements are optional */

        /* email is optional. But if exists, should be valid. */
        if (exists(eml.getContact().getEmail()) && !isValidEmail(eml.getContact().getEmail())) {
          action.addFieldError("eml.contact.email",
              action.getText("validation.invalid", new String[]{action.getText("eml.contact.email")}));
        }

        /* phone is optional. But if it exists, should match the pattern */
        if (exists(eml.getContact().getPhone())) {
          if (!isValidPhoneNumber(eml.getContact().getPhone())) {
            action.addFieldError("eml.contact.phone",
                action.getText("validation.invalid", new String[]{action.getText("eml.contact.phone")}));
          }
        }

        /* Validate the homepage URL form resource contact */
        if (eml.getContact().getHomepage() != null) {
          if (formatURL(eml.getContact().getHomepage()) == null) {
            action.addFieldError("eml.contact.homepage",
                action.getText("validation.invalid", new String[]{action.getText("eml.contact.homepage")}));
          } else {
            eml.getContact().setHomepage(formatURL(eml.getContact().getHomepage()));
          }
        }

        /*
         * RESOURCE CREATOR
         * <dataset>
         * <creator> - mandatory
         * <organizationName>{eml.creator.organisation}</organizationName> |
         * <individualName> - mandatory |
         * <givenName>{eml.creator.firstName}</givenName> - optional | - mandatory (at least one of them)
         * <surName>{eml.creator.lastName}</surName> - mandatory |
         * </individualName> |
         * <positionName>{eml.creator.position}</positionName> - optional
         * <address> - optional
         * <deliveryPoint>{eml.creator.address.address}</deliveryPoint> - optional
         * <city>{eml.creator.address.city}</city> - optional
         * <administrativeArea>{eml.creator.address.province}</administrativeArea> - optional
         * <postalCode>{eml.creator.address.postalCode}</postalCode> - optional
         * <country>{eml.creator.country}</country> - optional
         * </address>
         * <phone>{eml.creator.phone}</phone> - optional
         * </creator>
         * </dataset>
         */

        /* At least have to exist an organisation, a lastName or a position */
        if (!exists(eml.getResourceCreator().getOrganisation()) && !exists(eml.getResourceCreator().getLastName())
            && !exists(eml.getResourceCreator().getPosition())) {
          if (!action.getActionErrors().contains(action.getText("validation.lastname.organisation.position"))) {
            action.addActionError(action.getText("validation.lastname.organisation.position"));
          }
          action.addFieldError("eml.resourceCreator.organisation",
              action.getText("validation.required", new String[]{action.getText("eml.resourceCreator.organisation")}));
          action.addFieldError("eml.resourceCreator.lastName",
              action.getText("validation.required", new String[]{action.getText("eml.resourceCreator.lastName")}));
          action.addFieldError("eml.resourceCreator.position",
              action.getText("validation.required", new String[]{action.getText("eml.resourceCreator.position")}));
        } else {
          /* firstName - optional. But if firstName exists, lastName have to exist too. */
          if (exists(eml.getResourceCreator().getFirstName()) && !exists(eml.getResourceCreator().getLastName())) {
            action.addFieldError("eml.resourceCreator.lastName", action.getText("validation.firstname.lastname"));
          }
        }

        /* address and all its sub-elements are optional */

        /* email is optional. But if it exists, should be a valid email address */
        if (exists(eml.getResourceCreator().getEmail()) && !isValidEmail(eml.getResourceCreator().getEmail())) {
          action.addFieldError("eml.resourceCreator.email",
              action.getText("validation.invalid", new String[]{action.getText("eml.resourceCreator.email")}));
        }

        /* phone is optional. But if it exists, should match the pattern */
        if (exists(eml.getResourceCreator().getPhone())) {
          if (!isValidPhoneNumber(eml.getResourceCreator().getPhone())) {
            action.addFieldError("eml.resourceCreator.phone",
                action.getText("validation.invalid", new String[]{action.getText("eml.resourceCreator.phone")}));
          }
        }

        /* Validate the homepage URL from resource creator */
        if (eml.getResourceCreator().getHomepage() != null) {
          if (formatURL(eml.getResourceCreator().getHomepage()) == null) {
            action.addFieldError("eml.resourceCreator.homepage",
                action.getText("validation.invalid", new String[]{action.getText("eml.resourceCreator.homepage")}));
          } else {
            eml.getResourceCreator().setHomepage(formatURL(eml.getResourceCreator().getHomepage()));
          }
        }

        /*
         * METADATA PROVIDER
         * <dataset>
         * <metadataProvider> - mandatory
         * <organizationName>{eml.metadataProvider.organisation}</organizationName> |
         * <individualName> - mandatory |
         * <givenName>{eml.metadataProvider.firstName}</givenName> - optional | - mandatory (at least one of them)
         * <surName>{eml.metadataProvider.lastName}</surName> - mandatory |
         * </individualName> |
         * <positionName>{eml.metadataProvider.position}</positionName> - optional |
         * <address> - optional
         * <deliveryPoint>{eml.metadataProvider.address.address}</deliveryPoint> - optional
         * <city>{eml.metadataProvider.address.city}</city> - optional
         * <administrativeArea>{eml.metadataProvider.address.province}</administrativeArea> - optional
         * <postalCode>{eml.metadataProvider.address.postalCode}</postalCode> - optional
         * <country>{eml.metadataProvider.address.country}</country> - optional
         * </address>
         * <phone>{eml.metadataProvider.phone}</phone> - optional
         * <electronicMailAddress>{eml.metadataProvider.email}</electronicMailAddress> - optional
         * <onlineUrl>{eml.metadataProvider.homepage}</onlineUrl> - optional
         * </metadataProvider>
         * </dataset>
         */

        /* At least have to exist an organisation, a lastName or a position */
        if (!exists(eml.getMetadataProvider().getOrganisation()) && !exists(eml.getMetadataProvider().getLastName())
            && !exists(eml.getMetadataProvider().getPosition())) {
          if (!action.getActionErrors().contains(action.getText("validation.lastname.organisation.position"))) {
            action.addActionError(action.getText("validation.lastname.organisation.position"));
          }
          action.addFieldError("eml.metadataProvider.organisation",
              action.getText("validation.required", new String[]{action.getText("eml.metadataProvider.organisation")}));
          action.addFieldError("eml.metadataProvider.lastName",
              action.getText("validation.required", new String[]{action.getText("eml.metadataProvider.lastName")}));
          action.addFieldError("eml.metadataProvider.position",
              action.getText("validation.required", new String[]{action.getText("eml.metadataProvider.position")}));
        } else {
          /* firstName - optional. But if firstName exists, lastName have to exist too. */
          if (exists(eml.getMetadataProvider().getFirstName()) && !exists(eml.getMetadataProvider().getLastName())) {
            action.addFieldError("eml.metadataProvider.lastName", action.getText("validation.firstname.lastname"));
          }
        }

        /* address is optional. */

        /* email is optional. But if it exists, should be a valid email address */
        if (exists(eml.getMetadataProvider().getEmail()) && !isValidEmail(eml.getMetadataProvider().getEmail())) {
          action.addFieldError("eml.metadataProvider.email",
              action.getText("validation.invalid", new String[]{action.getText("eml.metadataProvider.email")}));
        }

        /* phone is optional. But if it exists, should match the pattern */
        if (exists(eml.getMetadataProvider().getPhone())) {
          if (!isValidPhoneNumber(eml.getMetadataProvider().getPhone())) {
            action.addFieldError("eml.metadataProvider.phone",
                action.getText("validation.invalid", new String[]{action.getText("eml.metadataProvider.phone")}));
          }
        }

        /* Validate the homepage URL from metadata provider */
        if (eml.getMetadataProvider().getHomepage() != null) {
          if (formatURL(eml.getMetadataProvider().getHomepage()) == null) {
            action.addFieldError("eml.metadataProvider.homepage",
                action.getText("validation.invalid", new String[]{action.getText("eml.metadataProvider.homepage")}));
          } else {
            eml.getMetadataProvider().setHomepage(formatURL(eml.getMetadataProvider().getHomepage()));
          }
        }

      } else if (part == null || part.equalsIgnoreCase("parties")) {
        /*
         * PARTIES.FTL - XML Schema Documentation
         * <dataset>
         * <associatedParty> - optional - many
         * <organizationName>{eml.associatedParties[i].organisation}</organizationName> |
         * <individualName> |
         * <givenName>{eml.associatedParties[i].firstName}</givenName> | - mandatory (at least one of them)
         * <surName>{eml.associatedParties[i].lastName}</surName> |
         * </individualName> |
         * <positionName>{eml.associatedParties[i].position}</positionName> |
         * <address> - optional
         * <deliveryPoint>{eml.associatedParties[i].address.address}</deliveryPoint> - optional
         * <city>{eml.associatedParties[i].address.city}</city> - optional
         * <administrativeArea>{eml.associatedParties[i].address.province}</administrativeArea> - optional
         * <postalCode>{eml.associatedParties[i].address.postalCode}</postalCode> - optional
         * <country>{eml.associatedParties[i].address.country}</country> - optional
         * </address>
         * <phone>{eml.associatedParties[i].phone}</phone> - optional
         * <electronicMailAddress>{eml.associatedParties[i].email}</electronicMailAddress> - optional - valid format
         * <onlineUrl>{eml.associatedParties[i].homePage}</onlineUrl> - optional
         * <role>eml.associatedParties[i].role</role> - optional
         * </associatedParty>
         * </dataset>
         */
        for (int index = 0; index < eml.getAssociatedParties().size(); index++) {
          /* firstName - optional. But if firstName exists, lastName have to exist */
          if (exists(eml.getAssociatedParties().get(index).getFirstName())
              && !exists(eml.getAssociatedParties().get(index).getLastName())) {
            action.addFieldError("eml.associatedParties[" + index + "].lastName",
                action.getText("validation.firstname.lastname"));
          }

          /* At least have to exist an organisation or a lastName (or both) */
          if (!exists(eml.getAssociatedParties().get(index).getOrganisation())
              && !exists(eml.getAssociatedParties().get(index).getLastName())) {
            action.addActionError(action.getText("validation.lastname.organisation.position"));
            action.addFieldError("eml.associatedParties[" + index + "].organisation", action.getText(
                "validation.required", new String[]{action.getText("eml.associatedParties.organisation")}));
            action.addFieldError("eml.associatedParties[" + index + "].lastName",
                action.getText("validation.required", new String[]{action.getText("eml.associatedParties.lastName")}));
            action.addFieldError("eml.associatedParties[" + index + "].position",
                action.getText("validation.required", new String[]{action.getText("eml.associatedParties.position")}));
          }

          /* email is optional. But if it exists, should be a valid email address */
          if (exists(eml.getAssociatedParties().get(index).getEmail())
              && !isValidEmail(eml.getAssociatedParties().get(index).getEmail())) {
            action.addFieldError("eml.associatedParties[" + index + "].email",
                action.getText("validation.invalid", new String[]{action.getText("eml.associatedParties.email")}));
          }

          /* phone is optional. But if it exists, should match the pattern */
          if (exists(eml.getAssociatedParties().get(index).getPhone())) {
            if (!isValidPhoneNumber(eml.getAssociatedParties().get(index).getPhone())) {
              action.addFieldError("eml.associatedParties[" + index + "].phone",
                  action.getText("validation.invalid", new String[]{action.getText("eml.associatedParties.phone")}));
            }
          }

          /* Validate the homepage URL from each associated parties */
          if (eml.getAssociatedParties().get(index).getHomepage() != null) {
            if (formatURL(eml.getAssociatedParties().get(index).getHomepage()) == null) {
              action.addFieldError("eml.associatedParties[" + index + "].homepage",
                  action.getText("validation.invalid", new String[]{action.getText("eml.associatedParties.homepage")}));
            } else {
              eml.getAssociatedParties().get(index).setHomepage(
                  formatURL(eml.getAssociatedParties().get(index).getHomepage()));
            }
          }

        }
      } else if (part == null || part.equalsIgnoreCase("geocoverage")) {
        /*
         * GEOCOVERAGE.FTL - XML Schema Documentation
         * <dataset>
         * <coverage>
         * <geographicCoverage> - mandatory - many
         * <geographicDescription>{geocoverage.description}</geographicDescription> - optional
         * <boundingCoordinates> - mandatory
         * <westBoundingCoordinate>{geocoverage.boundingCoordinates.min.longitude}</westBoundingCoordinate> - mandatory
         * <eastBoundingCoordinate>{geocoverage.boundingCoordinates.max.longitude}</eastBoundingCoordinate> - mandatory
         * <northBoundingCoordinate>{geocoverage.boundingCoordinates.max.latitude}</northBoundingCoordinate> - mandatory
         * <southBoundingCoordinate>{geocoverage.boundingCoordinates.min.latitude}</southBoundingCoordinate> - mandatory
         * </boundingCoordinates>
         * </geographicCoverage>
         * </coverage>
         * </dataset>
         */
        Double coord = 0.0;
        for (int index = 0; index < eml.getGeospatialCoverages().size(); index++) {
          boolean descriptionNeeded = true;
          coord = eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMin().getLongitude();
          if (coord == null || Double.isNaN(coord)) {
            descriptionNeeded = false;
            action.addFieldError(
                "eml.geospatialCoverages[" + index + "].boundingCoordinates.min.longitude",
                action.getText("validation.invalid",
                    new String[]{action.getText("eml.geospatialCoverages.boundingCoordinates.min.longitude")}));
          }
          coord = eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMax().getLongitude();
          if (coord == null || Double.isNaN(coord)) {
            descriptionNeeded = false;
            action.addFieldError(
                "eml.geospatialCoverages[" + index + "].boundingCoordinates.max.longitude",
                action.getText("validation.invalid",
                    new String[]{action.getText("eml.geospatialCoverages.boundingCoordinates.max.longitude")}));
          }
          coord = eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMax().getLatitude();
          if (coord == null || Double.isNaN(coord)) {
            descriptionNeeded = false;
            action.addFieldError(
                "eml.geospatialCoverages[" + index + "].boundingCoordinates.max.latitude",
                action.getText("validation.invalid",
                    new String[]{action.getText("eml.geospatialCoverages.boundingCoordinates.max.latitude")}));
          }
          coord = eml.getGeospatialCoverages().get(index).getBoundingCoordinates().getMin().getLatitude();
          if (coord == null || Double.isNaN(coord)) {
            descriptionNeeded = false;
            action.addFieldError(
                "eml.geospatialCoverages[" + index + "].boundingCoordinates.min.latitude",
                action.getText("validation.invalid",
                    new String[]{action.getText("eml.geospatialCoverages.boundingCoordinates.min.latitude")}));
          }
          if (descriptionNeeded && !exists(eml.getGeospatialCoverages().get(index).getDescription())) {
            action.addFieldError(
                "eml.geospatialCoverages[" + index + "].description",
                action.getText("validation.required",
                    new String[]{action.getText("eml.geospatialCoverages.description")}));
          }
        }

      } else if (part == null || part.equalsIgnoreCase("taxcoverage")) {
        /*
         * TAXCOVERAGE.FTL - XML Schema Documentation
         * <dataset>
         * <coverage> - optional
         * <taxonomicCoverage> - mandatory - many
         * <generalTaxonomicCoverage>{eml.taxonomicCoverages[i].description}</generalTaxonomicCoverage> - optional
         * <taxonomicClassification> - mandatory - many
         * <taxonRankName>{eml.taxonomicCoverages[i].taxonKeywords[j].scientificName}</taxonRankName> - optional
         * <taxonRankValue>{eml.taxonomicCoverages[i].taxonKeywords[j].rank}</taxonRankValue> - mandatory
         * <commonName>{eml.taxonomicCoverages[i].taxonKeywords[j].commonName}</commonName> - optional
         * </taxonomicClassification>
         * </taxonomicCoverage>
         * </coverage>
         * </dataset>
         */
        int index = 0;
        for (TaxonomicCoverage tc : eml.getTaxonomicCoverages()) {
          int kw = 0;
          for (TaxonKeyword k : tc.getTaxonKeywords()) {
            if (!exists(k.getScientificName())) {
              action.addFieldError(
                  "eml.taxonomicCoverages[" + index + "].taxonKeywords[" + kw + "].scientificName",
                  action.getText("validation.required",
                      new String[]{action.getText("eml.taxonomicCoverages.taxonKeyword.scientificName")}));
            }
            kw++;
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("tempcoverage")) {
        /*
         * TEMPCOVERAGE.FTL - XML Schema Documentation
         * <dataset>
         * <coverage> - optional
         * <temporalCoverage> - mandatory - many
         * <rangeOfDates> - mandatory |
         * <beginDate><calendarDate>{eml.temporalCoverages[i].startDate}</calendarDate></beginDate> - mandatory |
         * <endDate><calendarDate>{eml.temporalCoverages[i].endDate}</calendarDate></endDate> - mandatory | - mandatory
         * (but only should appear one of them)
         * </rangeOfDates> | "rangeOfDates or singleDateTime"
         * <singleDateTime><calendarDate>{eml.temporalCoverages[i].startDate}</calendarDate></singleDateTime> -
         * mandatory |
         * </temporalCoverage>
         * </coverage>
         * </dataset>
         * <additionalMetadata> - optional
         * <metadata> - mandatory
         * <gbif> - mandatory
         * <formationPeriod>{eml.temporalCoverages[i].formationPeriod}</formationPeriod> - optional - many
         * <livingTimePeriod>{eml.temporalCoverages[i].livingTimePeriod}</livingTimePeriod> - optional - many
         * </gbif>
         * </metadata>
         * </additionalMetadata>
         */
        int index = 0;
        for (TemporalCoverage tc : eml.getTemporalCoverages()) {
          if (tc.getType().equals(TemporalCoverageType.SINGLE_DATE)) {
            if (!exists(tc.getStartDate())) {
              action.addFieldError(
                  "eml.temporalCoverages[" + index + "].startDate",
                  action.getText("validation.required", new String[]{action.getText("eml.temporalCoverages.startDate")}));
            }
          }
          if (tc.getType().equals(TemporalCoverageType.DATE_RANGE)) {
            if (!exists(tc.getStartDate())) {
              action.addFieldError(
                  "eml.temporalCoverages[" + index + "].startDate",
                  action.getText("validation.required", new String[]{action.getText("eml.temporalCoverages.startDate")}));
            }
            if (!exists(tc.getEndDate())) {
              action.addFieldError("eml.temporalCoverages[" + index + "].endDate",
                  action.getText("validation.required", new String[]{action.getText("eml.temporalCoverages.endDate")}));
            }
          }
          if (tc.getType().equals(TemporalCoverageType.FORMATION_PERIOD)) {
            if (!exists(tc.getFormationPeriod())) {
              action.addFieldError(
                  "eml.temporalCoverages[" + index + "].formationPeriod",
                  action.getText("validation.required",
                      new String[]{action.getText("eml.temporalCoverages.formationPeriod")}));
            }
          }
          if (tc.getType().equals(TemporalCoverageType.LIVING_TIME_PERIOD)) {
            if (!exists(tc.getLivingTimePeriod())) {
              action.addFieldError(
                  "eml.temporalCoverages[" + index + "].livingTimePeriod",
                  action.getText("validation.required",
                      new String[]{action.getText("eml.temporalCoverages.livingTimePeriod")}));
            }
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("project")) {
        /*
         * PROJECT.FTL - XML Schema Documentation
         * <dataset>
         * <project> - optional
         * <title>{eml.project.title}</title> - mandatory
         * <personnel> - mandatory
         * <individualName> - mandatory
         * <givenName>{eml.project.personnel.firstName}</givenName> - optional
         * <surName>{eml.project.personnel.lastName}</surName> - mandatory
         * </individualName>
         * <role>{eml.project.personnel.role}</role> - mandatory
         * </personnel>
         * <funding> - mandatory
         * <para>{eml.project.funding}</para> - mandatory
         * </funding>
         * <studyAreaDescription> - mandatory
         * <descriptor name="generic" citableClassificationSystem="false"> - mandatory
         * <descriptorValue>{eml.project.studyAreaDescription.descriptorValue}</descriptorValue> - mandatory
         * </descriptor>
         * </studyAreaDescription>
         * <designDescription> - mandatory
         * <description> - mandatory
         * <para>{eml.project.designDescription}</para> - optional
         * </description>
         * </designDescription>
         * </project>
         * </dataset>
         */

        /* First Name is optional but if exists, last name must to exist */
        if (exists(eml.getProject().getPersonnel().getFirstName())
            && !exists(eml.getProject().getPersonnel().getLastName())) {
          action.addFieldError("eml.project.personnel.lastName", action.getText("validation.firstname.lastname"));
        } else if (!exists(eml.getProject().getPersonnel().getLastName())) {
          action.addFieldError("eml.project.personnel.lastName",
              action.getText("validation.required", new String[]{action.getText("eml.project.personnel.lastName")}));
        }

      } else if (part == null || part.equalsIgnoreCase("methods")) {
        /*
         * METHODS.FTL - XML Schema Documentation
         * <dataset>
         * <methods> - optional
         * <methodStep> - mandatory - many
         * <description> - mandatory
         * <para>{eml.methodSteps[i]}</para> - mandatory
         * </description>
         * </methodStep>
         * <qualityControl> - mandatory
         * <description> - mandatory
         * <para>{eml.qualityControl}</para> - mandatory
         * </description>
         * </qualityControl>
         * <sampling> - mandatory
         * <studyExtent> - mandatory
         * <description> - mandatory
         * <para>{eml.studyExtent}</para> - mandatory
         * </description>
         * </studyExtent>
         * <samplingDescription> - mandatory
         * <para>{eml.sampleDescription}</para> - mandatory
         * </samplingDescription>
         * </sampling>
         * </methods>
         * </dataset>
         */
        int index = 0;
        for (String method : eml.getMethodSteps()) {
          if (method.trim().equals("")) {
            action.addFieldError("eml.methodSteps[" + index + "]",
                action.getText("validation.required", new String[]{action.getText("validation.field.required")}));
          }
          index++;
        }

      } else if (part == null || part.equalsIgnoreCase("citations")) {
        /*
         * CITATIONS.FTL - XML Schema Documentation
         * <additionalMetadata>
         * <metadata>
         * <gbif>
         * <citation>{eml.citation}</citation> - optional
         * <bibliography> - optional
         * <citation>{eml.bibliographicCitationSet.bibliographicCitations[i]}</citation> - mandatory - many
         * </bibliography>
         * </gbif>
         * </metadata>
         * </additionalMetadata>
         */
        if (!eml.getCitation().getIdentifier().equals("") && !exists(eml.getCitation().getIdentifier())) {
          action.addFieldError("eml.citation.identifier",
              action.getText("validation.field.blank", new String[]{action.getText("eml.citation.identifier")}));
        } else {
          eml.getCitation().setIdentifier(eml.getCitation().getIdentifier().trim());
        }

        if (exists(eml.getCitation().getIdentifier()) && !exists(eml.getCitation().getCitation())) {
          action.addFieldError("eml.citation.citation",
              action.getText("validation.required", new String[]{action.getText("eml.citation.citation")}));
        }

        int index = 0;
        for (Citation citation : eml.getBibliographicCitations()) {
          if (!citation.getIdentifier().equals("") && !exists(citation.getIdentifier())) {
            action.addFieldError(
                "eml.bibliographicCitationSet.bibliographicCitations[" + index + "].identifier",
                action.getText("validation.field.blank",
                    new String[]{action.getText("eml.bibliographicCitationSet.bibliographicCitations.identifier")}));
          }
          if (!exists(citation.getCitation())) {
            action.addFieldError("eml.bibliographicCitationSet.bibliographicCitations[" + index + "].citation",
                action.getText("validation.required", new String[]{action.getText("validation.field.required")}));
          }
          index++;
        }

      } else if (part == null || part.equalsIgnoreCase("collections")) {
        /*
         * COLLECTIONS.FTL - XML Schema Documentation
         * <metadata>
         * <gbif>
         * <collection> - optional
         * <parentCollectionIdentifier>{eml.parentCollectionId}</parentCollectionIdentifier> - mandatory
         * <collectionIdentifier>{eml.collectionId}</collectionIdentifier> - mandatory
         * <collectionName>{eml.collectionName}</collectionName> - mandatory
         * </collection>
         * <jgtiCuratorialUnit> - optional - many
         * <jgtiUnitType>{eml.jgtiCuratorialUnits[i].unitType}</jgtiUnitType> - optional
         * <jgtiUnits
         * uncertaintyMeasure="{eml.jgtiCuratorialUnits[i].uncertaintyMeasure}">{eml.jgtiCuratorialUnits[i].rangeMean
         * }</jgtiUnits> - mandatory (xs:integer) | <jgtiUnitRange> - mandatory |
         * <beginRange>{eml.jgtiCuratorialUnits[i].rangeStart}</beginRange> - mandatory (xs:integer) | It has to be only
         * one of them. <jgtiUnits> or <jgtiUnitRange>.
         * <endRange>{eml.jgtiCuratorialUnits[i].rangeEnd}</endRange> - mandatory (xs:integer) |
         * </jgtiUnitType>
         * </jgtiCuratorialUnit>
         * </gbif>
         * </metadata>
         */
        if (!exists(eml.getParentCollectionId())) {
          action.addFieldError("eml.parentCollectionId",
              action.getText("validation.required", new String[]{action.getText("eml.parentCollectionId")}));
        }
        if (!exists(eml.getCollectionId())) {
          action.addFieldError("eml.collectionId",
              action.getText("validation.required", new String[]{action.getText("eml.collectionId")}));
        }
        if (!exists(eml.getCollectionName())) {
          action.addFieldError("eml.collectionName",
              action.getText("validation.required", new String[]{action.getText("eml.collectionName")}));
        }
        int index = 0;
        for (JGTICuratorialUnit jcu : eml.getJgtiCuratorialUnits()) {
          if (jcu.getType().equals(JGTICuratorialUnitType.COUNT_RANGE)) {
            if (!exists(jcu.getRangeStart())) {
              action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeStart",
                  action.getText("validation.required", new String[]{action.getText("validation.field.required")}));
            }
            if (!exists(jcu.getRangeEnd())) {
              action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeEnd",
                  action.getText("validation.required", new String[]{action.getText("validation.field.required")}));
            }
          }
          if (jcu.getType().equals(JGTICuratorialUnitType.COUNT_WITH_UNCERTAINTY)) {
            if (!exists(jcu.getRangeMean())) {
              action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeMean",
                  action.getText("validation.required", new String[]{action.getText("validation.field.required")}));
            }
            if (!exists(jcu.getUncertaintyMeasure())) {
              action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].uncertaintyMeasure",
                  action.getText("validation.required", new String[]{action.getText("validation.field.required")}));
            }
          }
          if (!exists(jcu.getUnitType())) {
            action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].unitType",
                action.getText("validation.required", new String[]{action.getText("eml.jgtiCuratorialUnits.unitType")}));
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("physical")) {
        /*
         * PHYSICAL.FTL - XML Schema Documentation
         */
        int index = 0;
        for (PhysicalData pd : eml.getPhysicalData()) {
          if (!exists(pd.getName())) {
            action.addFieldError("eml.physicalData[" + index + "].name", action.getText("validation.required"));
          }
          /* Validate distribution URL form each Physical data */
          if (pd.getDistributionUrl() != null) {
            if (formatURL(pd.getDistributionUrl()) == null) {
              action.addFieldError(
                  "eml.physicalData[" + index + "].distributionUrl",
                  action.getText("validation.invalid", new String[]{action.getText("eml.physicalData.distributionUrl")}));
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
                action.getText("validation.invalid", new String[]{action.getText("eml.distributionUrl")}));
          } else {
            eml.setDistributionUrl(formatURL(eml.getDistributionUrl()));
          }
        }
      } else if (part == null || part.equalsIgnoreCase("keywords")) {
        int index = 0;
        for (KeywordSet ks : eml.getKeywords()) {
          if (!exists(ks.getKeywordsString())) {
            action.addFieldError("eml.keywords[" + index + "].keywordsString", action.getText("validation.required"));
          }
          if (!exists(ks.getKeywordThesaurus())) {
            action.addFieldError("eml.keywords[" + index + "].keywordThesaurus", action.getText("validation.required"));
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("additional")) {
        if (eml.getPubDate() == null) {
          action.addFieldError("eml.pubDate",
              action.getText("validation.required", new String[]{action.getText("eml.pubDate")}));
        }
        int index = 0;
        for (String ai : eml.getAlternateIdentifiers()) {
          if (!exists(ai)) {
            action.addFieldError("eml.alternateIdentifiers[" + index + "]",
                action.getText("validation.required", new String[]{action.getText("eml.alternateIdentifier")}));
          }
          index++;
        }
      }
    }
  }
}
