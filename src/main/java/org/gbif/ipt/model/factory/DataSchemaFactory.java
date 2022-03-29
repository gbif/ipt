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

import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.model.DataSubschema;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataSchemaFactory {

  private final Gson gson;

  public DataSchemaFactory() {
    this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
  }

  /**
   * Builds a data subschema from the supplied JSON file
   *
   * @param file JSON file
   *
   * @return data subschema
   */
  public DataSubschema buildSubschema(File file) throws IOException {
    DataSubschema result;
    try (FileReader fr = new FileReader(file)) {
      result = gson.fromJson(fr, DataSubschema.class);
    }
    return result;
  }

  /**
   * Builds a data schema from the supplied JSON file
   *
   * @param file JSON file
   *
   * @return data schema
   */
  public DataSchema buildSchema(File file) throws IOException {
    DataSchema result;
    try (FileReader fr = new FileReader(file)) {
      result = gson.fromJson(fr, DataSchema.class);
    }
    return result;
  }
}
