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
import java.util.List;

/**
 * Implementation of {@link ValidCoordinates} validator.
 **/
public class ValidCoordinatesValidator implements ConstraintValidator<ValidCoordinates, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext ctx) {
    if (value == null) {
      return false;
    }

    // invalid input, just fail validation
    if (!(value instanceof List)) {
      return false;
    }

    @SuppressWarnings("unchecked")
    List<Double> values = ((List<Double>) value);

    // it's checked separately, just fail validation
    if (values.size() < 4) {
      return false;
    }

    Double coord1;
    Double coord2;

    coord1 = values.get(0);
    if (coord1 == null) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.west.required")
          .addConstraintViolation();

      return false;
    } else if (Double.isNaN(coord1)) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.west.invalid")
          .addConstraintViolation();

      return false;
    }

    coord2 = values.get(1);
    if (coord2 == null) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.east.required")
          .addConstraintViolation();

      return false;
    } else if (Double.isNaN(coord2)) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.east.invalid")
          .addConstraintViolation();

      return false;
    }

    if (coord1 > coord2) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.longitude.swapped")
          .addConstraintViolation();

      return false;
    }

    if (coord1 < -180 || coord1 > 180) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.west.value")
          .addConstraintViolation();

      return false;
    }

    if (coord2 < -180 || coord2 > 180) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.east.value")
          .addConstraintViolation();

      return false;
    }

    coord1 = values.get(2);
    if (coord1 == null) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.south.required")
          .addConstraintViolation();

      return false;
    } else if (Double.isNaN(coord1)) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.south.invalid")
          .addConstraintViolation();

      return false;
    }

    coord2 = values.get(3);
    if (coord2 == null) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.south.required")
          .addConstraintViolation();

      return false;
    } else if (Double.isNaN(coord2)) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.north.invalid")
          .addConstraintViolation();

      return false;
    }

    if (coord1 > coord2) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.latitude.swapped")
          .addConstraintViolation();

      return false;
    }

    if (coord1 < -90 || coord1 > 90) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.south.value")
          .addConstraintViolation();

      return false;
    }

    if (coord2 < -90 || coord2 > 90) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate("validation.camtrap.metadata.spatial.north.value")
          .addConstraintViolation();

      return false;
    }

    return true;
  }

}

