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
package org.gbif.ipt.task;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatusReportTest {

  @Test
  public void testStepIsKept() {
    StatusReport report = new StatusReport(false, "Creating metadata files", "METADATA", List.of());

    assertEquals("METADATA", report.getStep());
    assertFalse(report.hasException());
  }

  @Test
  public void testStepStartsAreKeptInOrder() {
    Map<String, Long> starts = new LinkedHashMap<>();
    starts.put("STARTED", 1000L);
    starts.put("DATAFILES", 1500L);
    starts.put("METADATA", 9000L);
    StatusReport failed = new StatusReport(new Exception("boom"), "Failed", "METADATA", List.of())
        .withStepStarts(starts);

    assertEquals(List.of("STARTED", "DATAFILES", "METADATA"), List.copyOf(failed.getStepStarts().keySet()));
    assertEquals(1500L, failed.getStepStarts().get("DATAFILES"));
    // the rest of the report is unchanged
    assertEquals("METADATA", failed.getStep());
    assertTrue(failed.hasException());
    assertTrue(failed.isCompleted());
    // a copy: later steps of the task don't change a report already made
    starts.put("BUNDLING", 10000L);
    assertEquals(3, failed.getStepStarts().size());
    assertThrows(UnsupportedOperationException.class, () -> failed.getStepStarts().put("X", 1L));
  }

  @Test
  public void testCancelled() {
    assertTrue(new StatusReport(new InterruptedException("stop"), "Cancelled", "DATAFILES", List.of()).isCancelled());
    assertFalse(new StatusReport(new Exception("boom"), "Failed", "DATAFILES", List.of()).isCancelled());
    assertFalse(new StatusReport(true, "Done", List.of()).isCancelled());
  }

  @Test
  public void testNoStepStarts() {
    assertTrue(new StatusReport(true, "Done", List.of()).getStepStarts().isEmpty());
    assertTrue(new StatusReport(true, "Done", List.of()).withStepStarts(null).getStepStarts().isEmpty());
  }
}
