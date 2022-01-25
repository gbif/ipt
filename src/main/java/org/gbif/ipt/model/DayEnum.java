/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

public enum DayEnum {
  MONDAY("monday", 2),
  TUESDAY("tuesday", 3),
  WEDNESDAY("wednesday", 4),
  THURSDAY("thursday", 5),
  FRIDAY("friday", 6),
  SATURDAY("saturday", 7),
  SUNDAY("sunday", 1);

  private final String identifier;
  private final int dayId;

  DayEnum(String identifier, int dayId) {
    this.identifier = identifier;
    this.dayId = dayId;
  }

  public String getIdentifier() {
    return identifier;
  }

  public int getDayId() {
    return dayId;
  }

  public static DayEnum findByIdentifier(@Nullable String id) {
    if (id != null) {
      DayEnum[] var1 = values();
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; ++var3) {
        DayEnum entry = var1[var3];
        if (entry.getIdentifier().toLowerCase().equals(id.trim().toLowerCase())) {
          return entry;
        }
      }
    }

    return null;
  }
}
