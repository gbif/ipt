/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***************************************************************************/

package org.gbif.ipt.validation;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.JGTICuratorialUnit;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.PhysicalData;
import org.gbif.metadata.eml.TaxonomicCoverage;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.metadata.eml.TemporalCoverageType;

import com.google.inject.internal.Nullable;

import java.util.regex.Pattern;

/**
 * @author markus
 * 
 */
public class EmlValidator extends BaseValidator {
  protected static Pattern phonePattern = Pattern.compile("[0-9 ()/+-]+");

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
        if (!exists(eml.getTitle())) {
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
              action.getText("validation.short", new String[]{action.getText("eml.title"), "5"}));
        }

        /*
         * RESOURCE CREATOR
         * <dataset>
         * <creator>
         * <organizationName>{eml.creator.organisation}</organizationName> |
         * <individualName> - mandatory |
         * <givenName>{eml.creator.firstName}</givenName> - optional | - mandatory (at least one of them)
         * <surName>{eml.creator.lastName}</surName> - mandatory |
         * </individualName> |
         * <positionName>{eml.creator.position}</positionName> - optional
         * <address> - optional
         * <deliveryPoint>{eml.creator.address.address}</deliveryPoint> - optional
         * <city>{eml.creator.address.city}</city> - mandatory
         * <administrativeArea>{eml.creator.address.province}</administrativeArea> - mandatory
         * <postalCode>{eml.creator.address.postalCode}</postalCode> - mandatory
         * <country>{eml.creator.country}</country> - mandatory
         * </address>
         * </creator>
         * </dataset>
         */

        /* At least have to exist an organisation or a lastName (or both) */
        if (!exists(eml.getResourceCreator().getOrganisation()) && !exists(eml.getResourceCreator().getLastName())) {
          action.addActionError(action.getText("validation.lastname.organisation"));
          action.addFieldError("eml.resourceCreator.organisation",
              action.getText("validation.required", new String[]{action.getText("eml.resourceCreator.organisation")}));
          action.addFieldError("eml.resourceCreator.lastName",
              action.getText("validation.required", new String[]{action.getText("eml.resourceCreator.lastName")}));
        } else {
          /* firstName - optional. But if firstName exists, lastName have to exist. */
          if (exists(eml.getResourceCreator().getFirstName()) && !exists(eml.getResourceCreator().getLastName())) {
            action.addFieldError("eml.resourceCreator.lastName", action.getText("validation.firstname.lastname"));
          }
        }

        /* positionName is optional */

        /* address is optional */

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

        /*
         * METADATA PROVIDER
         * <dataset>
         * <metadataProvider> - mandatory
         * <individualName> - mandatory
         * <givenName>{eml.metadataProvider.firstName}</givenName> - optional
         * <surName>{eml.metadataProvider.lastName}</surName> - mandatory
         * </individualName>
         * <address> - optional
         * <deliveryPoint>{eml.metadataProvider.address.address}</deliveryPoint> - optional
         * <city>{eml.metadataProvider.address.city}</city> - mandatory
         * <administrativeArea>{eml.metadataProvider.address.province}</administrativeArea> - mandatory
         * <postalCode>{eml.metadataProvider.address.postalCode}</postalCode> - mandatory
         * <country>{eml.metadataProvider.address.country}</country> - mandatory
         * </address>
         * <electronicMailAddress>{eml.metadataProvider.email}</electronicMailAddress> - optional
         * </metadataProvider>
         * </dataset>
         */

        /* firstName - optional. But if firstName exists, lastName have to exist. */
        if (exists(eml.getMetadataProvider().getFirstName()) && !exists(eml.getMetadataProvider().getLastName())) {
          action.addFieldError("eml.metadataProvider.lastName", action.getText("validation.firstname.lastname"));
        }

        /*
         * address is optional.
         */

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

        /*
         * RESOURCE CONTACT
         * <dataset>
         * <contact>
         * <organizationName>{eml.contact.organisation}</organizationName> |
         * <individualName> - mandatory |
         * <givenName>{eml.contact.firstName}</givenName> - optional | - mandatory (at least one of them)
         * <surName>{eml.contact.lastName}</surName> - mandatory |
         * </individualName> |
         * <positionName>{eml.contact.position}</positionName> - optional
         * <address> - optional
         * <deliveryPoint>{eml.contact.address.address}</deliveryPoint> - optional
         * <city>{eml.contact.address.city}</city> - mandatory
         * <administrativeArea>{eml.contact.address.province}</administrativeArea> - mandatory
         * <postalCode>{eml.contact.address.postalCode}</postalCode> - mandatory
         * <country>{eml.contact.country}</country> - mandatory
         * </address>
         * <phone>{eml.creator.phone}</phone> - optional
         * <electronicMailAddress>{eml.creator.email}</electronicMailAddress> - optional
         * <onlineUrl>{eml.creator.homepage}</onlineUrl> - optional
         * </contact>
         * </dataset>
         */

        /* At least have to exist an organisation or a lastName (or both) */
        if (!exists(eml.getContact().getOrganisation()) && !exists(eml.getContact().getLastName())) {
          if (!action.getActionErrors().contains(action.getText("validation.lastname.organisation"))) {
            action.addActionError(action.getText("validation.lastname.organisation"));
          }
          action.addFieldError("eml.contact.organisation",
              action.getText("validation.required", new String[]{action.getText("eml.contact.organisation")}));
          action.addFieldError("eml.contact.lastName",
              action.getText("validation.required", new String[]{action.getText("eml.contact.lastName")}));
        } else {
          /* firstName - optional. But if firstName exists, lastName have to exist. */
          if (exists(eml.getContact().getFirstName()) && !exists(eml.getContact().getLastName())) {
            action.addFieldError("eml.contact.lastName", action.getText("validation.firstname.lastname"));
          }
        }

        /* positionName is optional */

        /* address is optional. */

        /* email is optional. But if it exists, should be a valid email address */
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
         * <address> - optional
         * <deliveryPoin>{eml.associatedParties[i].address.address}</deliveryPoint> - optional
         * <city>{eml.associatedParties[i].address.city}</city> - mandatory
         * <administrativeArea>{eml.associatedParties[i].address.province}</administrativeArea> - mandatory
         * <postalCode>{eml.associatedParties[i].address.postalCode}</postalCode> - mandatory - integer
         * <country>{eml.associatedParties[i].address.country}</country> - mandatory
         * </address>
         * <phone>{eml.associatedParties[i].phone}</phone> - mandatory - integer
         * <electronicMailAddress>{eml.associatedParties[i].email}</electronicMailAddress> - optional - valid format
         * <onlineUrl>{eml.associatedParties[i].homePage}</onlineUrl> - optional
         * <role>eml.associatedParties[i].role</role> - mandatory
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
            action.addActionError(action.getText("validation.lastname.organisation"));
          }

          /*
           * address is optional. But if someone fill some of this fields (city, province, postal code or country),
           * the user is obligated to fill all the address information.
           */
          if (exists(eml.getAssociatedParties().get(index).getAddress().getAddress())
              || exists(eml.getAssociatedParties().get(index).getAddress().getCity())
              || exists(eml.getAssociatedParties().get(index).getAddress().getProvince())
              || exists(eml.getAssociatedParties().get(index).getAddress().getPostalCode())
              || exists(eml.getAssociatedParties().get(index).getAddress().getCountry())) {
            if (!exists(eml.getAssociatedParties().get(index).getAddress().getCity())) {
              action.addFieldError(
                  "eml.associatedParties[" + index + "].address.city",
                  action.getText("validation.required",
                      new String[]{action.getText("eml.associatedParties.address.city")}));
            }
            if (!exists(eml.getAssociatedParties().get(index).getAddress().getProvince())) {
              action.addFieldError(
                  "eml.associatedParties[" + index + "].address.province",
                  action.getText("validation.required",
                      new String[]{action.getText("eml.associatedParties.address.province")}));
            }
            /* postal code should be a number */
            if (!exists(eml.getAssociatedParties().get(index).getAddress().getPostalCode())) {
              action.addFieldError(
                  "eml.associatedParties[" + index + "].address.postalCode",
                  action.getText("validation.required",
                      new String[]{action.getText("eml.associatedParties.address.postalCode")}));
            } else {
              try {
                Integer.parseInt(eml.getAssociatedParties().get(index).getAddress().getPostalCode());
              } catch (NumberFormatException e) {
                action.addFieldError(
                    "eml.associatedParties[" + index + "].address.postalCode",
                    action.getText("validation.invalid",
                        new String[]{action.getText("eml.associatedParties.address.postalCode")}));
              }
            }
            if (!exists(eml.getAssociatedParties().get(index).getAddress().getCountry())) {
              action.addFieldError(
                  "eml.associatedParties[" + index + "].address.country",
                  action.getText("validation.required",
                      new String[]{action.getText("eml.associatedParties.address.country")}));
            }
          }

          /* email is optional. But if it exists, should be a valid email address */
          if (exists(eml.getAssociatedParties().get(index).getEmail())
              && !isValidEmail(eml.getAssociatedParties().get(index).getEmail())) {
            action.addFieldError("eml.associatedParties[" + index + "].email",
                action.getText("validation.invalid", new String[]{action.getText("eml.associatedParties.email")}));
          }

          /* phone is optional. But if it exists, should be a number */
          if (exists(eml.getAssociatedParties().get(index).getPhone())) {
            try {
              Integer.parseInt(eml.getAssociatedParties().get(index).getPhone());
            } catch (NumberFormatException e) {
              action.addFieldError("eml.associatedParties[" + index + "].phone",
                  action.getText("validation.invalid", new String[]{action.getText("eml.associatedParties.phone")}));
            }
          }
        }
      } else if (part == null || part.equalsIgnoreCase("geocoverage")) {
      } else if (part == null || part.equalsIgnoreCase("taxcoverage")) {
        int cont = 0;
        for (TaxonomicCoverage tc : eml.getTaxonomicCoverages()) {
          if (!exists(tc.getDescription(), 5)) {
            action.addFieldError("eml.taxonomicCoverages[" + cont + "].description",
                action.getText("validation.required"));
          }
          if (!exists(tc.getTaxonKeyword().getScientificName())) {
            action.addFieldError("eml.taxonomicCoverages[" + cont + "].taxonKeyword.scientificName",
                action.getText("validation.required"));
          }
          if (!exists(tc.getTaxonKeyword().getCommonName())) {
            action.addFieldError("eml.taxonomicCoverages[" + cont + "].taxonKeyword.commonName",
                action.getText("validation.required"));
          }
          cont++;
        }
      } else if (part == null || part.equalsIgnoreCase("tempcoverage")) {
        int index = 0;
        for (TemporalCoverage tc : eml.getTemporalCoverages()) {

          if (tc.getType().equals(TemporalCoverageType.SINGLE_DATE)) {
            if (!exists(tc.getStartDate())) {
              action.addFieldError("eml.temporalCoverages[" + index + "].startDate",
                  action.getText("validation.required"));
            }
          }
          if (tc.getType().equals(TemporalCoverageType.DATE_RANGE)) {
            if (!exists(tc.getStartDate())) {
              action.addFieldError("eml.temporalCoverages[" + index + "].startDate",
                  action.getText("validation.required"));
            }
            if (!exists(tc.getEndDate())) {
              action.addFieldError("eml.temporalCoverages[" + index + "].endDate",
                  action.getText("validation.required"));
            }
          }
          if (tc.getType().equals(TemporalCoverageType.FORMATION_PERIOD)) {
            if (!exists(tc.getFormationPeriod())) {
              action.addFieldError("eml.temporalCoverages[" + index + "].formationPeriod",
                  action.getText("validation.required"));
            }
          }
          if (tc.getType().equals(TemporalCoverageType.LIVING_TIME_PERIOD)) {
            if (!exists(tc.getLivingTimePeriod())) {
              action.addFieldError("eml.temporalCoverages[" + index + "].livingTimePeriod",
                  action.getText("validation.required"));
            }
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("project")) {
        if (!exists(eml.getProject().getPersonnel().getFirstName())) {
          action.addFieldError("eml.project.personnel.firstName", action.getText("validation.required"));
        }
        if (!exists(eml.getProject().getPersonnel().getLastName())) {
          action.addFieldError("eml.project.personnel.lastName", action.getText("validation.required"));
        }

      } else if (part == null || part.equalsIgnoreCase("methods")) {
        /*
         * for(int index=0;index<eml.getMethodSteps().size();index++) {
         * if(!exists(eml.getMethodSteps().get(index), 5)) {
         * action.addFieldError("eml.methodSteps["+index+"]", action.getText("validation.required"));
         * }
         * }
         */
      } else if (part == null || part.equalsIgnoreCase("citations")) {
        if (!exists(eml.getCitation())) {
          action.addFieldError("eml.citation", action.getText("validation.required"));
        }
        for (int index = 0; index < eml.getBibliographicCitations().size(); index++) {
          if (!exists(eml.getBibliographicCitations().get(index))) {
            action.addFieldError("eml.bibliographicCitationSet.bibliographicCitations[" + index + "]",
                action.getText("validation.required"));
          }
        }
      } else if (part == null || part.equalsIgnoreCase("collections")) {
        int index = 0;
        for (JGTICuratorialUnit jcu : eml.getJgtiCuratorialUnits()) {
          if (jcu.getType().equals(JGTICuratorialUnitType.COUNT_RANGE)) {
            if (!exists(jcu.getRangeStart())) {
              action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeStart",
                  action.getText("validation.required"));
            }
            if (!exists(jcu.getRangeEnd().toString())) {
              action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeEnd",
                  action.getText("validation.required"));
            }
          }
          if (jcu.getType().equals(JGTICuratorialUnitType.COUNT_WITH_UNCERTAINTY)) {
            if (!exists(jcu.getRangeMean())) {
              action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].rangeMean",
                  action.getText("validation.required"));
            }
            if (!exists(jcu.getUncertaintyMeasure())) {
              action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].uncertaintyMeasure",
                  action.getText("validation.required"));
            }
          }
          if (!exists(jcu.getUnitType())) {
            action.addFieldError("eml.jgtiCuratorialUnits[" + index + "].unitType",
                action.getText("validation.required"));
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("physical")) {
        int index = 0;
        for (PhysicalData pd : eml.getPhysicalData()) {
          if (!exists(pd.getName())) {
            action.addFieldError("eml.physicalData[" + index + "].name", action.getText("validation.required"));
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("keywords")) {
        int index = 0;
        for (KeywordSet ks : eml.getKeywords()) {
          if (!exists(ks.getKeywordsString())) {
            action.addFieldError("eml.keywords[" + index + "].keywordsString", action.getText("validation.required"));
          }
          index++;
        }
      } else if (part == null || part.equalsIgnoreCase("additional")) {
        if (!exists(eml.getDistributionUrl(), 5)) {
          if (!exists(eml.getDistributionUrl())) {
            action.addFieldError("eml.distributionUrl", action.getText("validation.required"));
          } else {
            action.addFieldError("eml.distributionUrl",
                action.getText("validation.short", new String[]{action.getText("eml.distributionUrl"), "5"}));
          }
        }
      }
    }
  }

}
