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

import org.gbif.ipt.model.datapackage.metadata.camtrap.Geojson;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geojson.GeoJsonObject;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ValidGeojsonValidator implements ConstraintValidator<ValidGeojson, Geojson> {

  private static final Logger LOG = LogManager.getLogger(ValidGeojsonValidator.class);

  private final ObjectMapper objectMapper;

  public ValidGeojsonValidator() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JtsModule());
  }

  @Override
  public boolean isValid(Geojson geojson, ConstraintValidatorContext ctx) {
    try {
      String geojsonString = objectMapper.writeValueAsString(geojson);
      objectMapper.readValue(geojsonString, GeoJsonObject.class);

      return true;
    } catch (IOException e) {
      LOG.error("GeoJSON validation failed: {}", e.getMessage());
      return false;
    }
  }
}
