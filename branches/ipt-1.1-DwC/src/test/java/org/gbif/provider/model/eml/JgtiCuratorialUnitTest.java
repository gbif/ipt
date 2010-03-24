/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model.eml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Maps;

import java.util.Map;

import org.junit.Test;

/**
 * This class can be used for unit testing {@link JgtiCuratorialUnit}.
 * 
 */
public class JgtiCuratorialUnitTest {

  private static JgtiCuratorialUnit create(String rangeEnd, String rangeStart,
      String uncertaintyMeasure, Integer unit) {
    return create(null, rangeEnd, rangeStart, uncertaintyMeasure, unit);
  }

  private static JgtiCuratorialUnit create(String failMsg, String rangeEnd,
      String rangeStart, String uncertaintyMeasure, Integer unit) {
    JgtiCuratorialUnit jcu = null;
    try {
      jcu = JgtiCuratorialUnit.create(rangeStart, rangeEnd, uncertaintyMeasure,
          unit);
      if (failMsg != null) {
        fail(failMsg);
      } else {
        System.out.printf("Success: create(%s)\n", jcu);
      }
    } catch (Exception e) {
      if (failMsg == null) {
        fail(e.getMessage());
      } else {
        System.out.printf("Fail: %s\n", e.getMessage());
      }
    }
    return jcu;
  }

  @Test
  public final void testCreate() {
    create("Should fail with null params", null, null, null, null);
    create("Should fail with null rangeEnd", null, "rs", "um", 0);
    create("Should fail with empty rangeEnd", "", "rs", "um", 0);
    create("Should fail with null rangeStart", "re", null, "um", 0);
    create("Should fail with empty rangeStart", "re", "", "um", 0);
    create("Should fail with null uncertaintyMeasurement", "re", "rs", null, 0);
    create("Should fail with empty uncertaintyMeasurement", "re", "rs", "", 0);
    create("Should fail with null unit", "rs", "re", "um", null);
    create("re", "re", "um", 0);
  }

  @Test
  public final void testEqualsObject() {
    assertEquals(create("re", "re", "um", 0), create("re", "re", "um", 0));
  }

  @Test
  public final void testGetRangeEnd() {
    String rangeEnd = "re";
    assertEquals(rangeEnd, create(rangeEnd, "rs", "um", 0).getRangeEnd());
  }

  @Test
  public final void testGetRangeStart() {
    String rangeStart = "rs";
    assertEquals(rangeStart, create("re", rangeStart, "um", 0).getRangeStart());
  }

  @Test
  public final void testGetUncertaintyMeasure() {
    String uncertaintyMeasurement = "um";
    assertEquals(uncertaintyMeasurement, create("re", "rs",
        uncertaintyMeasurement, 0).getUncertaintyMeasure());
  }

  @Test
  public final void testGetUnit() {
    Integer unit = 0;
    assertEquals(unit, create("re", "rs", "um", unit).getUnit());
  }

  @Test
  public final void testHashCode() {
    JgtiCuratorialUnit jcu = create("re", "rs", "um", 0);
    assertEquals(jcu.hashCode(), create("re", "rs", "um", 0).hashCode());
    Map<JgtiCuratorialUnit, String> map = Maps.newHashMap();
    map.put(jcu, "foo");
    assertTrue(map.containsKey(create("re", "rs", "um", 0)));
  }

  @Test
  public final void testToString() {
    assertEquals("RangeEnd=re, RangeStart=rs, UncertaintyMeasure=um, Unit=0",
        create("re", "rs", "um", 0).toString());
  }
}
