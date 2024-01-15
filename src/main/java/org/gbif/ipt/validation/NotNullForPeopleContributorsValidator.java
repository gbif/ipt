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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class NotNullForPeopleContributorsValidator implements ConstraintValidator<NotNullLastNameForPeopleContributors, Object> {

  private static final List<String> ROLES = Arrays.asList("contact", "principalInvestigator", "contributor");

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext ctx) {
    try {
      String roleValue = BeanUtils.getProperty(value, "role");
      String lastName = BeanUtils.getProperty(value, "lastName");

      // last name must not be null for the roles
      if (ROLES.contains(roleValue) && StringUtils.isEmpty(lastName)) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
            .addPropertyNode("lastName")
            .addConstraintViolation();

        return false;
      }

    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
      throw new RuntimeException(ex);
    }

    return true;
  }

}