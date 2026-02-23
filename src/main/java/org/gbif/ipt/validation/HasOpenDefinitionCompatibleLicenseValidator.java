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

import org.gbif.ipt.model.datapackage.metadata.FrictionlessLicense;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapLicense;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

import org.apache.commons.lang3.Strings;

public class HasOpenDefinitionCompatibleLicenseValidator implements ConstraintValidator<HasOpenDefinitionCompatibleLicense, List<? extends FrictionlessLicense>> {

  private static final String[] OPEN_DEFINITION_COMPATIBLE_LICENSES = {"AAL", "AFL-3.0", "AGPL-3.0", "APL-1.0",
      "APSL-2.0", "Against-DRM", "Apache-1.1", "Apache-2.0", "Artistic-2.0", "BSD-2-Clause", "BSD-3-Clause", "BSL-1.0",
      "BitTorrent-1.1", "CATOSL-1.1", "CC-BY-4.0", "CC-BY-NC-4.0", "CC-BY-NC-ND-4.0", "CC-BY-NC-SA-4.0", "CC-BY-ND-4.0",
      "CC-BY-SA-4.0", "CC0-1.0", "CDDL-1.0", "CECILL-2.1", "CNRI-Python", "CPAL-1.0", "CUA-OPL-1.0", "DSL", "ECL-2.0",
      "EFL-2.0", "EPL-1.0", "EPL-2.0", "EUDatagrid", "EUPL-1.1", "Entessa", "FAL-1.3", "Fair", "Frameworx-1.0",
      "GFDL-1.3-no-cover-texts-no-invariant-sections", "GPL-2.0", "GPL-3.0", "HPND", "IPA", "IPL-1.0", "ISC", "Intel",
      "LGPL-2.1", "LGPL-3.0", "LO-FR-2.0", "LPL-1.0", "LPL-1.02", "LPPL-1.3c", "MIT", "MPL-1.0", "MPL-1.1", "MPL-2.0",
      "MS-PL", "MS-RL", "MirOS", "Motosoto", "Multics", "NASA-1.3", "NCSA", "NGPL", "NPOSL-3.0", "NTP", "Naumen",
      "Nokia", "OCLC-2.0", "ODC-BY-1.0", "ODbL-1.0", "OFL-1.1", "OGL-Canada-2.0", "OGL-UK-1.0", "OGL-UK-2.0",
      "OGL-UK-3.0", "OGTSL", "OSL-3.0", "PDDL-1.0", "PHP-3.0", "PostgreSQL", "Python-2.0", "QPL-1.0", "RPL-1.5",
      "RPSL-1.0", "RSCPL", "SISSL", "SPL-1.0", "SimPL-2.0", "Sleepycat", "Talis", "Unlicense", "VSL-1.0", "W3C",
      "WXwindows", "Watcom-1.0", "Xnet", "ZPL-2.0", "Zlib", "dli-model-use", "geogratis", "hesa-withrights",
      "localauth-withrights", "met-office-cp", "mitre", "notspecified", "other-at", "other-closed", "other-nc",
      "other-open", "other-pd", "ukclickusepsi", "ukcrown", "ukcrown-withrights", "ukpsi"};

  @Override
  public boolean isValid(List<? extends FrictionlessLicense> value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    boolean isValid = true;

    for (FrictionlessLicense license : value) {
      CamtrapLicense camtrapLicense;
      if (license instanceof CamtrapLicense) {
        camtrapLicense = (CamtrapLicense) license;
      } else {
        continue;
      }

      if (isMediaLicense(camtrapLicense) && !isValid(camtrapLicense)) {
        isValid = false;

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addConstraintViolation();
      }
    }

    return isValid;
  }

  private boolean isMediaLicense(CamtrapLicense license) {
    return license.getScope() == CamtrapLicense.Scope.MEDIA;
  }

  private boolean isValid(CamtrapLicense license) {
    return Strings.CS.equalsAny(license.getName(), OPEN_DEFINITION_COMPATIBLE_LICENSES);
  }

}
