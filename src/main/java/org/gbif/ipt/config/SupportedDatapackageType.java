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

import static org.gbif.ipt.config.Constants.DATA_SCHEMA_CAMTRAP_DP_IDENTIFIER;
import static org.gbif.ipt.config.Constants.DATA_SCHEMA_COLDP_IDENTIFIER;
import static org.gbif.ipt.config.Constants.DATA_SCHEMA_MATERIAL_DP_IDENTIFIER;

public enum SupportedDatapackageType {

    CAMTRAP_DP(Constants.CAMTRAP_DP, DATA_SCHEMA_CAMTRAP_DP_IDENTIFIER, "1.0-rc.1"),
    COLDP(Constants.COL_DP, DATA_SCHEMA_COLDP_IDENTIFIER, "1.0"),
    MATERIAL_DP(Constants.MATERIAL_DP, DATA_SCHEMA_MATERIAL_DP_IDENTIFIER, "0.1");

    private final String name;
    private final String identifier;
    private final String supportedVersion;

    SupportedDatapackageType(String name, String identifier, String supportedVersion) {
        this.name = name;
        this.identifier = identifier;
        this.supportedVersion = supportedVersion;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getSupportedVersion() {
        return supportedVersion;
    }
}
