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

import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapLicense;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Geojson;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Project;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.util.StrutsTypeConverter;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Taxonomic;

public class DataPackageMetadataEnumConverter extends StrutsTypeConverter {

  @Override
  public Object convertFromString(Map context, String[] values, Class toClass) {
    if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
      return null;
    }

    Object result = null;

    if (toClass == Project.ClassificationLevel.class) {
      result = Project.ClassificationLevel.fromValue(values[0]);
    } else if (toClass == Project.SamplingDesign.class) {
      result = Project.SamplingDesign.fromValue(values[0]);
    } else if (toClass == Taxonomic.TaxonRank.class) {
      result = Taxonomic.TaxonRank.fromValue(values[0]);
    } else if (toClass == Geojson.Type.class) {
      result = Geojson.Type.fromValue(values[0]);
    } else if (toClass == CamtrapLicense.Scope.class) {
      result = CamtrapLicense.Scope.fromValue(values[0]);
    }

    return result;
  }

  @Override
  public String convertToString(Map context, Object o) {
    if (o instanceof Project.ClassificationLevel) {
      return ((Project.ClassificationLevel) o).value();
    } else if (o instanceof Project.SamplingDesign) {
      return ((Project.SamplingDesign) o).value();
    } else if (o instanceof Taxonomic.TaxonRank) {
      return ((Taxonomic.TaxonRank) o).value();
    } else if (o instanceof Geojson.Type) {
      return ((Geojson.Type) o).value();
    } else if (o instanceof CamtrapLicense.Scope) {
      return ((CamtrapLicense.Scope) o).value();
    }

    return null;
  }
}
