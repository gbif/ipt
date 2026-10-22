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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

  @Test
  public void testFormatCamtrapTemporalDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(2026, Calendar.JANUARY, 15, 23, 59, 59);

    assertEquals("2026-01-15", MetadataUtils.formatCamtrapTemporalDate(calendar.getTime()));
  }

  @Test
  public void testFormatCamtrapTemporalDateOfSqlDate() {
    assertEquals("2026-01-15", MetadataUtils.formatCamtrapTemporalDate(java.sql.Date.valueOf("2026-01-15")));
  }

  /**
   * Formatted by requests and publications at the same time; a shared SimpleDateFormat mixes up dates.
   */
  @Test
  public void testFormatCamtrapTemporalDateConcurrently() throws Exception {
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      List<Future<List<String>>> results = new ArrayList<>();
      for (int t = 0; t < threads; t++) {
        int year = 2000 + t;
        results.add(pool.submit(() -> {
          List<String> wrong = new ArrayList<>();
          Calendar calendar = Calendar.getInstance();
          for (int day = 1; day <= 365; day++) {
            calendar.clear();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.DAY_OF_YEAR, day);
            String expected = String.format("%d-%02d-%02d", year, calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
            String formatted = MetadataUtils.formatCamtrapTemporalDate(calendar.getTime());
            if (!expected.equals(formatted)) {
              wrong.add(expected + " formatted as " + formatted);
            }
          }
          return wrong;
        }));
      }
      for (Future<List<String>> result : results) {
        assertEquals(List.of(), result.get(30, TimeUnit.SECONDS));
      }
    } finally {
      pool.shutdownNow();
    }
  }
}
