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
package org.gbif.ipt.struts2.converter;

import org.gbif.ipt.model.datapackage.metadata.camtrap.CaptureMethod;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.util.StrutsTypeConverter;

public class UriConverter extends StrutsTypeConverter {

  @Override
  public Object convertFromString(Map context, String[] values, Class toClass) {
    if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
      return null;
    }

    URI result = null;

    if (toClass == URI.class) {
      result = URI.create(values[0]);
    }

    return result;
  }

  @Override
  public String convertToString(Map context, Object o) {
    if (o instanceof URI) {
      return o.toString();
    }

    return null;
  }
}
