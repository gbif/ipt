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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;

import org.apache.commons.validator.routines.UrlValidator;

public class ValidURIValidator implements ConstraintValidator<ValidURI, URI> {

  private final UrlValidator urlValidator;

  public ValidURIValidator() {
    String[] schemes = {"http", "https"};
    this.urlValidator = new UrlValidator(schemes);
  }

  @Override
  public boolean isValid(URI value, ConstraintValidatorContext context) {
    return value == null || urlValidator.isValid(value.toString());
  }
}
