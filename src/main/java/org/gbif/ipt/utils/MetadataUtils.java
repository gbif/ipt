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

import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.commons.lang3.Strings;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.DWC_DP;

public final class MetadataUtils {

  private static final DateTimeFormatter CAMTRAP_TEMPORAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
    return Strings.CS.equalsAny(type, CAMTRAP_DP, COL_DP, DWC_DP);
  }

  /**
   * Formats the data package "created" timestamp as UTC with second precision, e.g. 2026-01-15T10:00:00Z,
   * regardless of the server time zone.
   */
  public static String formatDataPackageCreated(Instant instant) {
    return DateTimeFormatter.ISO_INSTANT.format(instant.truncatedTo(ChronoUnit.SECONDS));
  }

  /**
   * Formats a date of the Camtrap DP temporal scope (start/end), e.g. 2026-01-15, in the server time zone. Unlike a
   * shared SimpleDateFormat, it can be called from several threads at once (requests and publications).
   */
  public static String formatCamtrapTemporalDate(Date date) {
    return CAMTRAP_TEMPORAL_DATE_FORMAT.format(Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()));
  }
}
