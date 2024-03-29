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

import org.gbif.ipt.model.datapackage.metadata.License;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapLicense;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

public class HasGbifCompatibleLicenseValidator implements ConstraintValidator<HasGbifCompatibleLicense, List<License>> {

  private static final String[] GBIF_COMPATIBLE_LICENSES = {"CC0-1.0", "CC-BY-4.0", "CC-BY-NC-4.0"};

  @Override
  public boolean isValid(List<License> value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    boolean isValid = true;

    for (License license : value) {
      CamtrapLicense camtrapLicense;
      if (license instanceof CamtrapLicense) {
        camtrapLicense = (CamtrapLicense) license;
      } else {
        continue;
      }

      if (isDataLicense(camtrapLicense) && !isValid(camtrapLicense)) {
        isValid = false;

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addConstraintViolation();
      }
    }

    return isValid;
  }

  private boolean isDataLicense(CamtrapLicense license) {
    return license.getScope() == CamtrapLicense.Scope.DATA;
  }

  private boolean isValid(CamtrapLicense license) {
    return StringUtils.equalsAny(license.getName(), GBIF_COMPATIBLE_LICENSES);
  }
}
