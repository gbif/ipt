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
package org.gbif.ipt.model.converter;

import org.gbif.ipt.model.ExcelFileSource;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.UrlSource;

import java.util.Map;

import com.thoughtworks.xstream.mapper.DefaultImplementationsMapper;
import com.thoughtworks.xstream.mapper.Mapper;

public class ExtendedXstreamMapper extends DefaultImplementationsMapper {

  // In case of only alias is present instead of a fully qualified name (old mappings)
  private static final Map<String, Class<?>> CUSTOM_CLASSES = Map.of(
      "filesource", FileSource.class,
      "excelsource", ExcelFileSource.class,
      "sqlsource", SqlSource.class,
      "urlsource", UrlSource.class,
      "sorted-set", java.util.TreeSet.class
  );

  public ExtendedXstreamMapper(Mapper wrapped) {
    super(wrapped);
  }

  @Override
  public Class<?> realClass(String elementName) {
    Class<?> mapped = CUSTOM_CLASSES.get(elementName);
    return mapped != null ? mapped : super.realClass(elementName);
  }
}