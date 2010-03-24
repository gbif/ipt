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
 * This class can be used for unit testing {@link Method}.
 * 
 */
public class MethodTest {

  private static Method create(String maintenance, String purpose,
      String qualityControl, String sampleDescription) {
    return create(null, maintenance, purpose, qualityControl, sampleDescription);
  }

  private static Method create(String failMsg, String maintenance,
      String purpose, String qualityControl, String sampleDescription) {
    Method m = null;
    try {
      m = Method.create(maintenance, purpose, qualityControl, sampleDescription);
      if (failMsg != null) {
        fail(failMsg);
      } else {
        System.out.printf("Success: create(%s)\n", m);
      }
    } catch (Exception e) {
      if (failMsg == null) {
        fail(e.getMessage());
      } else {
        System.out.printf("Fail: %s\n", e.getMessage());
      }
    }
    return m;
  }

  @Test
  public final void testCreate() {
    create("Should fail with null params", null, null, null, null);
    create("Should fail with null maintenance", null, "p", "qc", "sd");
    create("Should fail with empty maintenance", "", "p", "qc", "sd");
    create("Should fail with null purpose", "m", null, "qc", "sd");
    create("Should fail with empty purpose", "m", "", "qc", "sd");
    create("Should fail with null qualityControl", "m", "p", null, "sd");
    create("Should fail with empty qualityControl", "m", "p", "", "sd");
    create("Should fail with null sampleDescription", "m", "p", "qc", null);
    create("Should fail with empty sampleDescription", "m", "p", "qc", "");
    create("m", "p", "qc", "sd");
  }

  @Test
  public final void testEqualsObject() {
    assertEquals(create("m", "p", "qc", "sd"), create("m", "p", "qc", "sd"));
  }

  @Test
  public final void testGetMaintenance() {
    String maintenance = "m";
    assertEquals(maintenance,
        create(maintenance, "p", "qc", "sd").getMaintenance());
  }

  @Test
  public final void testGetPurpose() {
    String purpose = "p";
    assertEquals(purpose, create("m", purpose, "qc", "sd").getPurpose());
  }

  @Test
  public final void testGetQualityControl() {
    String qualityControl = "qc";
    assertEquals(qualityControl,
        create("m", "p", qualityControl, "sd").getQualityControl());
  }

  @Test
  public final void testGetSampleDescription() {
    String sampleDescription = "sd";
    assertEquals(sampleDescription,
        create("m", "p", "qc", sampleDescription).getSampleDescription());
  }

  @Test
  public final void testHashCode() {
    Method m = create("m", "p", "qc", "sd");
    assertEquals(m.hashCode(), create("m", "p", "qc", "sd").hashCode());
    Map<Method, String> map = Maps.newHashMap();
    map.put(m, "foo");
    assertTrue(map.containsKey(create("m", "p", "qc", "sd")));
  }

  @Test
  public final void testToString() {
    assertEquals(
        "Maintenance=m, Purpose=p, QualityControl=qc, SampleDescription=sd",
        create("m", "p", "qc", "sd").toString());
  }
}
