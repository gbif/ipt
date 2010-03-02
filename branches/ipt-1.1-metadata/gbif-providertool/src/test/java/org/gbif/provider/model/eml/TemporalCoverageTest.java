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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;

import org.junit.Test;

/**
 * This class can be used for unit testing {@link TemporalCoverage}.
 * 
 */
public class TemporalCoverageTest {

  @Test
  public final void testCreate() {
    TemporalCoverage.create("d", new Date(), null, new Date());
    try {
      TemporalCoverage.create(null, new Date(), null, new Date());
      fail("Description is required");
    } catch (Exception e) {
    }
    try {
      TemporalCoverage.create("", new Date(), null, new Date());
      fail("Description is required");
    } catch (Exception e) {
    }
    try {
      TemporalCoverage.create("d", new Date(), null, null);
      fail("StartDate is required");
    } catch (Exception e) {
    }
    try {
      TemporalCoverage.create("d", null, null, new Date());
      fail("EndDate is required");
    } catch (Exception e) {
    }
  }

  @Test
  public final void testEqualsObject() {
    assertEquals(TemporalCoverage.create("d", new Date(), null, new Date()),
        TemporalCoverage.create("d", new Date(), null, new Date()));
  }

  @Test
  public final void testGetDescription() {
    assertEquals(
        "d",
        TemporalCoverage.create("d", new Date(), null, new Date()).getDescription());
  }

  @Test
  public final void testGetEndDate() {
    Date d = new Date();
    assertEquals(d,
        TemporalCoverage.create("d", d, null, new Date()).getEndDate());
  }

  @Test
  public final void testGetKeywords() {
    assertEquals(ImmutableSet.of(), TemporalCoverage.create("d", new Date(),
        null, new Date()).getKeywords());
  }

  @Test
  public final void testGetStartDate() {
    Date d = new Date();
    assertEquals(d,
        TemporalCoverage.create("d", new Date(), null, d).getStartDate());
  }

  @Test
  public final void testHashCode() {
    assertEquals(
        TemporalCoverage.create("d", new Date(), null, new Date()).hashCode(),
        TemporalCoverage.create("d", new Date(), null, new Date()).hashCode());
    Date d = new Date();
    TemporalCoverage tc = TemporalCoverage.create("d", d, null, d);
    Map<TemporalCoverage, String> map = Maps.newHashMap();
    map.put(tc, "foo");
    assertTrue(map.containsKey(TemporalCoverage.create("d", d, null, d)));
  }

  @Test
  public final void testToString() {
    Date d = new Date();
    assertEquals(String.format(
        "Descriptioin=d, EndDate=%s, Keywords=[], StartDate=%s", d, d),
        TemporalCoverage.create("d", d, null, d).toString());
  }
}