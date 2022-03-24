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
package org.gbif.ipt.model;

import javax.annotation.Nullable;

public enum BiMonthEnum {
  JANUARY_JULY("january_july", 0),
  FEBRUARY_AUGUST("february_august", 1),
  MARCH_SEPTEMBER("march_september", 2),
  APRIL_OCTOBER("april_october", 3),
  MAY_NOVEMBER("may_november", 4),
  JUNE_DECEMBER("june_december", 5);

  private final String identifier;
  private final int biMonthId;

  BiMonthEnum(String identifier, int biMonthId) {
    this.identifier = identifier;
    this.biMonthId = biMonthId;
  }

  public String getIdentifier() {
    return identifier;
  }

  public int getBiMonthId() {
    return biMonthId;
  }

  public static BiMonthEnum findByIdentifier(@Nullable String id) {
    if (id != null) {
      BiMonthEnum[] var1 = values();
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; ++var3) {
        BiMonthEnum entry = var1[var3];
        if (entry.getIdentifier().toLowerCase().equals(id.trim().toLowerCase())) {
          return entry;
        }
      }
    }

    return null;
  }
}
