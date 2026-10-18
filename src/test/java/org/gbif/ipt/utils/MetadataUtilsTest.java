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

import java.time.Instant;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetadataUtilsTest {

  /**
   * The data package "created" timestamp ends with "Z", so it must be in UTC regardless of the server time zone.
   */
  @Test
  public void testFormatDataPackageCreatedIsUtcRegardlessOfDefaultTimeZone() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("Europe/Copenhagen"));
      assertEquals("2026-01-15T10:00:00Z",
          MetadataUtils.formatDataPackageCreated(Instant.parse("2026-01-15T10:00:00.123Z")));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
    }
  }
}
