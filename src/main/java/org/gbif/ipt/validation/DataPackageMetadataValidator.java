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
package org.gbif.ipt.validation;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Taxonomic;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.voc.CamtrapMetadataSection;
import org.gbif.ipt.model.voc.DataPackageMetadataSection;
import org.gbif.ipt.model.voc.FrictionlessMetadataSection;
import org.gbif.ipt.service.InvalidMetadataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;

public class DataPackageMetadataValidator {

  private final Validator validator;

  private static final Pattern VERNACULAR_NAME_KEY_PATTERN = Pattern.compile("^[a-z]{3}$");

  public DataPackageMetadataValidator() {
    try (ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory()) {
      validator = validatorFactory.getValidator();
    }
  }

  public boolean isValid(Resource resource) {
    Set<ConstraintViolation<DataPackageMetadata>> violations
        = validator.validate(resource.getDataPackageMetadata(), BasicMetadata.class, GeographicScopeMetadata.class,
        TaxonomicScopeMetadata.class, TemporalScopeMetadata.class, KeywordsMetadata.class, ProjectMetadata.class,
        OtherMetadata.class);

    return violations.isEmpty();
  }

  /**
   * Validate that all metadata sections are valid. For the first section encountered that doesn't validate, an
   * error message will appear for that section only.
   *
   * @param action Action
   * @param metadata metadata
   */
  public void validate(BaseAction action, DataPackageMetadata metadata) throws InvalidMetadataException {
    boolean problemsEncountered = false;

    if (metadata instanceof CamtrapMetadata) {
      for (CamtrapMetadataSection section : CamtrapMetadataSection.values()) {
        validateCamtrap(action, (CamtrapMetadata) metadata, section);
        // only highlight first section has errors
        if ((action.hasActionErrors() || action.hasFieldErrors()) && !problemsEncountered) {
          problemsEncountered = true;
        }
      }
    } else {
      validateFrictionless(action, metadata, null);
      // only highlight first section has errors
      if ((action.hasActionErrors() || action.hasFieldErrors()) && !problemsEncountered) {
        problemsEncountered = true;
      }
    }

    if (problemsEncountered) {
      throw new InvalidMetadataException("Validation failed");
    }
  }

  public boolean isSectionValid(BaseAction action, Resource resource, DataPackageMetadataSection section) {
    boolean problemsEncountered = false;
    validate(action, resource, section);
    if ((action.hasActionErrors() || action.hasFieldErrors())) {
      action.addActionError(action.getText("manage.failed", new String[] {action.getText("submenu.datapackagemetadata.camtrap." + section.getName())}));
      problemsEncountered = true;
    }
    return !problemsEncountered;
  }

  /**
   * Validate Data Package metadata, optionally only a part of it.
   * </br>
   * For each section, validation only proceeds if at least one field in the section's form has been entered.
   *
   * @param action BaseAction
   * @param resource resource
   * @param section EML document section name
   */
  public void validate(BaseAction action, Resource resource, @Nullable DataPackageMetadataSection section) {
    if (resource != null) {
      DataPackageMetadata metadata = (resource.getDataPackageMetadata() == null) ? new FrictionlessMetadata() : resource.getDataPackageMetadata();

      if (CAMTRAP_DP.equals(resource.getCoreType())) {
        validateCamtrap(action, (CamtrapMetadata) metadata, ((CamtrapMetadataSection) section));
      } else {
        validateFrictionless(action, metadata, ((FrictionlessMetadataSection) section));
      }
    }
  }

  /**
   * Validate Camtrap metadata, optionally only a part of it.
   * </br>
   * For each section, validation only proceeds if at least one field in the section's form has been entered.
   *
   * @param action BaseAction
   * @param metadata data package metadata
   * @param section EML document section name
   */
  public void validateCamtrap(BaseAction action, CamtrapMetadata metadata, @Nullable CamtrapMetadataSection section) {
    // set default
    if (section == null) {
      section = CamtrapMetadataSection.BASIC_SECTION;
    }

    switch (section) {
      case BASIC_SECTION:
        Set<ConstraintViolation<DataPackageMetadata>> basicSectionViolations
            = validator.validate(metadata, BasicMetadata.class);

        for (ConstraintViolation<DataPackageMetadata> violation : basicSectionViolations) {
          if (StringUtils.equalsAny(violation.getPropertyPath().toString(), "licenses", "licenses.name", "contributors", "sources")) {
            action.addActionError(action.getText(violation.getMessage()));
          } else {
            if (violation.getMessage().equals("validation.datapackage.metadata.license.name.required")) {
              // remove all characters, we need the index
              String index = RegExUtils.removeAll(violation.getPropertyPath().toString(), "[a-zA-z.\\[\\]]*");
              action.addFieldError(
                  "metadata.licenses[" + index + "].name",
                  action.getText(action.getText(violation.getMessage()))
              );
            } else {
              addDefaultFieldError(action, violation);
            }
          }
        }

        break;

      case GEOGRAPHIC_SECTION:
        Set<ConstraintViolation<DataPackageMetadata>> geographicSectionViolations
            = validator.validate(metadata, GeographicScopeMetadata.class);

        for (ConstraintViolation<DataPackageMetadata> violation : geographicSectionViolations) {
          action.addActionError(action.getText(violation.getMessage()));
        }

        break;

      case TAXONOMIC_SECTION:
        Set<ConstraintViolation<DataPackageMetadata>> taxonomicSectionViolations
            = validator.validate(metadata, TaxonomicScopeMetadata.class);

        for (ConstraintViolation<DataPackageMetadata> violation : taxonomicSectionViolations) {
          addDefaultFieldError(action, violation);
        }

        validateVernacularNames(action, metadata);

        break;

      case TEMPORAL_SECTION:
        Set<ConstraintViolation<DataPackageMetadata>> temporalSectionViolations
            = validator.validate(metadata, TemporalScopeMetadata.class);

        for (ConstraintViolation<DataPackageMetadata> violation : temporalSectionViolations) {
          addDefaultFieldError(action, violation);
        }

        break;

      case KEYWORDS_SECTION:
        Set<ConstraintViolation<DataPackageMetadata>> keywordsSectionViolations
            = validator.validate(metadata, KeywordsMetadata.class);

        for (ConstraintViolation<DataPackageMetadata> violation : keywordsSectionViolations) {
          action.addActionError(action.getText(violation.getMessage()));
        }

        break;

      case PROJECT_SECTION:
        Set<ConstraintViolation<DataPackageMetadata>> projectSectionViolations
            = validator.validate(metadata, ProjectMetadata.class);

        for (ConstraintViolation<DataPackageMetadata> violation : projectSectionViolations) {
          addDefaultFieldError(action, violation);
        }

        break;

      case OTHER_SECTION:
        Set<ConstraintViolation<DataPackageMetadata>> otherMetadataSectionViolations
            = validator.validate(metadata, OtherMetadata.class);

        for (ConstraintViolation<DataPackageMetadata> violation : otherMetadataSectionViolations) {
          addDefaultFieldError(action, violation);
        }

        break;
    }
  }

  /**
   * Validate Frictionless metadata, optionally only a part of it.
   * </br>
   * For each section, validation only proceeds if at least one field in the section's form has been entered.
   *
   * @param action BaseAction
   * @param metadata data package metadata
   * @param section EML document section name
   */
  public void validateFrictionless(BaseAction action, DataPackageMetadata metadata, @Nullable FrictionlessMetadataSection section) {
    // set default
    if (section == null) {
      section = FrictionlessMetadataSection.BASIC_SECTION;
    }

    if (section == FrictionlessMetadataSection.BASIC_SECTION) {
      Set<ConstraintViolation<DataPackageMetadata>> basicSectionViolations
        = validator.validate(metadata, BasicMetadata.class);

      for (ConstraintViolation<DataPackageMetadata> violation : basicSectionViolations) {
        if (StringUtils.equalsAny(violation.getPropertyPath().toString(), "licenses", "licenses.name", "contributors", "sources")) {
          action.addActionError(action.getText(violation.getMessage()));
        } else {
          if (violation.getMessage().equals("validation.datapackage.metadata.license.name.required")) {
            // remove all characters, we need the index
            String index = RegExUtils.removeAll(violation.getPropertyPath().toString(), "[a-zA-z.\\[\\]]*");
            action.addFieldError(
              "metadata.licenses[" + index + "].name",
              action.getText(action.getText(violation.getMessage()))
            );
          } else {
            addDefaultFieldError(action, violation);
          }
        }
      }
    }
  }

  /**
   * Validate ColMetadata.
   *
   * @param action BaseAction
   * @param metadata col metadata
   */
  public void validateColMetadata(BaseAction action, ColMetadata metadata) {
    Set<ConstraintViolation<ColMetadata>> violations = validator.validate(metadata);
    String violationsSquashed = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("\n", "-", ""));
    action.addActionError("Validation failed:\n" + violationsSquashed);
  }

  private boolean isValidationProperty(String message) {
    return message.startsWith("validation.");
  }

  private void addDefaultFieldError(BaseAction action, ConstraintViolation<DataPackageMetadata> violation) {
    if (isValidationProperty(violation.getMessage())) {
      action.addFieldError(
          "metadata." + violation.getPropertyPath(),
          action.getText(violation.getMessage(), new String[]{violation.getPropertyPath().toString()}));
    } else {
      action.addFieldError(
          "metadata." + violation.getPropertyPath(),
          violation.getMessage());
    }
  }

  private void validateVernacularNames(BaseAction action, DataPackageMetadata metadata) {
    if (metadata instanceof CamtrapMetadata) {
      CamtrapMetadata camtrapMetadata = (CamtrapMetadata) metadata;

      List<Taxonomic> taxonomics = camtrapMetadata.getTaxonomic();

      for (int taxonomicIndex = 0; taxonomicIndex < taxonomics.size(); taxonomicIndex++) {
        Taxonomic taxonomic = taxonomics.get(taxonomicIndex);
        Map<String, String> vernacularNames = taxonomic.getVernacularNames();

        Set<String> vernacularNamesKeys = vernacularNames.keySet();

        List<String> listKeys = new ArrayList<>(vernacularNamesKeys);

        for (int vernacularNameIndex = 0; vernacularNameIndex < listKeys.size(); vernacularNameIndex++) {
          if (!VERNACULAR_NAME_KEY_PATTERN.matcher(listKeys.get(vernacularNameIndex)).matches()) {
            action.addFieldError(
              "vernacularNames-key-" + taxonomicIndex + "-" + vernacularNameIndex,
              action.getText("validation.camtrap.metadata.taxonomic.vernacularNames"));
          }
        }
      }
    }
  }
}
