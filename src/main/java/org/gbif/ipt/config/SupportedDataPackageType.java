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
package org.gbif.ipt.config;

import lombok.Getter;

import static org.gbif.ipt.config.Constants.DATA_PACKAGE_CAMTRAP_DP_IDENTIFIER;
import static org.gbif.ipt.config.Constants.DATA_PACKAGE_COLDP_IDENTIFIER;
import static org.gbif.ipt.config.Constants.DATA_PACKAGE_DWC_DP_IDENTIFIER;
import static org.gbif.ipt.config.Constants.DATA_PACKAGE_INTERACTION_DP_IDENTIFIER;
import static org.gbif.ipt.config.Constants.DATA_PACKAGE_MATERIAL_DP_IDENTIFIER;

@Getter
public enum SupportedDataPackageType {

  CAMTRAP_DP(Constants.CAMTRAP_DP, DATA_PACKAGE_CAMTRAP_DP_IDENTIFIER, "1.0", "prod"),
  COLDP(Constants.COL_DP, DATA_PACKAGE_COLDP_IDENTIFIER, "1.1", "prod"),
  MATERIAL_DP(Constants.MATERIAL_DP, DATA_PACKAGE_MATERIAL_DP_IDENTIFIER, "0.1", "dev"),
  INTERACTION_DP(Constants.INTERACTION_DP, DATA_PACKAGE_INTERACTION_DP_IDENTIFIER, "0.1", "dev"),
  DWC_DP(Constants.DWC_DP, DATA_PACKAGE_DWC_DP_IDENTIFIER, "0.1", "dev");

  private final String name;
  private final String identifier;
  private final String supportedVersion;
  private final String env;

  SupportedDataPackageType(String name, String identifier, String supportedVersion, String env) {
    this.name = name;
    this.identifier = identifier;
    this.supportedVersion = supportedVersion;
    this.env = env;
  }

  public boolean isProductionType() {
    return "prod".equals(env);
  }
}
