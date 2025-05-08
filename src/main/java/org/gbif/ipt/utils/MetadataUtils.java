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
package org.gbif.ipt.utils;

import org.apache.commons.lang3.StringUtils;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;

public final class MetadataUtils {

  private MetadataUtils() {
  }

  public static Class<? extends DataPackageMetadata> metadataClassForType(String type) {
    if (CAMTRAP_DP.equals(type)) {
      return CamtrapMetadata.class;
    } else if (COL_DP.equals(type)) {
      return ColMetadata.class;
    } else {
      return FrictionlessMetadata.class;
    }
  }

  public static boolean isDataPackageType(String type) {
    return StringUtils.equalsAny(type, CAMTRAP_DP, COL_DP);
  }
}
