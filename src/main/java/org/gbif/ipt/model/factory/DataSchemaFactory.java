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
package org.gbif.ipt.model.factory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.gbif.ipt.model.DataSchemaFile;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DataSchemaFactory {

  private static final Logger LOG = LogManager.getLogger(DataSchemaFactory.class);

  /**
   * Builds a data schema from the supplied JSON file
   *
   * @param file JSON file
   *
   * @return data schema file
   */
  public DataSchemaFile build(File file) throws IOException {
    // TODO: 11/03/2022 bean?
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // TODO: 11/03/2022 process possible exceptions
    DataSchemaFile dataSchemaFile = objectMapper.readValue(file, DataSchemaFile.class);

//    LOG.error("Unable to access extension definition defined at " + urlAsString, e);
//    LOG.error("Unable to parse extension definition defined at " + urlAsString, e);

    return dataSchemaFile;
  }
}
