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

public enum MonthEnum {
  JANUARY("january", 0),
  FEBRUARY("february", 1),
  MARCH("march", 2),
  APRIL("april", 3),
  MAY("may", 4),
  JUNE("june", 5),
  JULY("july", 6),
  AUGUST("august", 7),
  SEPTEMBER("september", 8),
  OCTOBER("october", 9),
  NOVEMBER("november", 10),
  DECEMBER("december", 11);

  private final String identifier;
  private final int monthId;

  MonthEnum(String identifier, int monthId) {
    this.identifier = identifier;
    this.monthId = monthId;
  }

  public String getIdentifier() {
    return identifier;
  }

  public int getMonthId() {
    return monthId;
  }

  public static MonthEnum findByIdentifier(@Nullable String id) {
    if (id != null) {
      MonthEnum[] var1 = values();
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; ++var3) {
        MonthEnum entry = var1[var3];
        if (entry.getIdentifier().toLowerCase().equals(id.trim().toLowerCase())) {
          return entry;
        }
      }
    }

    return null;
  }
}
