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
package org.gbif.ipt.service.manage;

import org.gbif.ipt.service.manage.impl.JsonServiceImpl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import com.google.inject.ImplementedBy;

@ImplementedBy(JsonServiceImpl.class)
public interface JsonService {

  /**
   * Read JSON from file.
   */
  <T> T readValue(File src, Class<T> valueType) throws IOException;

  /**
   * Write JSON to file.
   */
  void writeValue(File resultFile, Object value) throws IOException;

  /**
   * Write JSON to writer.
   */
  void writeValue(Writer writer, Object value) throws IOException;
}
