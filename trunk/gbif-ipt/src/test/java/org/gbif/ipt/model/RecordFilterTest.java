/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.model;

import org.gbif.ipt.model.RecordFilter.Comparator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author markus
 */
public class RecordFilterTest {
  @Test
  public void testEquals() {
    RecordFilter f = new RecordFilter();
    f.setColumn(1);
    f.setComparator(Comparator.Equals);
    f.setParam("a");

    assertFalse(f.matches(new String[]{}, -1));
    assertFalse(f.matches(new String[]{"1", null, "2"}, -1));
    assertFalse(f.matches(new String[]{"1", "", "2"}, -1));
    assertFalse(f.matches(new String[]{"1", "  ", "2"}, -1));
    assertFalse(f.matches(new String[]{"1", "A", "2"}, -1));
    assertFalse(f.matches(new String[]{"1", "aa", "2"}, -1));
    assertTrue(f.matches(new String[]{"1", "a", "3"}, -1));
  }

  @Test
  public void testIsNotNull() {
    RecordFilter f = new RecordFilter();
    f.setColumn(1);
    f.setComparator(Comparator.IsNotNULL);

    assertFalse(f.matches(new String[]{}, -1));
    assertFalse(f.matches(new String[]{"1", null, "2"}, -1));
    assertFalse(f.matches(new String[]{"1", "", "2"}, -1));
    assertFalse(f.matches(new String[]{"1", "  ", "2"}, -1));
    assertTrue(f.matches(new String[]{"1", "2", "3"}, -1));
  }

  @Test
  public void testIsNull() {
    RecordFilter f = new RecordFilter();
    f.setColumn(1);
    f.setComparator(Comparator.IsNULL);

    assertTrue(f.matches(new String[]{}, -1));
    assertTrue(f.matches(new String[]{"1", null, "2"}, -1));
    assertTrue(f.matches(new String[]{"1", "", "2"}, -1));
    assertTrue(f.matches(new String[]{"1", "  ", "2"}, -1));
    assertFalse(f.matches(new String[]{"1", "2", "3"}, -1));
  }

  @Test
  public void testNotEquals() {
    RecordFilter f = new RecordFilter();
    f.setColumn(1);
    f.setComparator(Comparator.NotEquals);
    f.setParam("a");

    assertTrue(f.matches(new String[]{}, -1));
    assertTrue(f.matches(new String[]{"1", null, "2"}, -1));
    assertTrue(f.matches(new String[]{"1", "", "2"}, -1));
    assertTrue(f.matches(new String[]{"1", "  ", "2"}, -1));
    assertTrue(f.matches(new String[]{"1", "A", "2"}, -1));
    assertTrue(f.matches(new String[]{"1", "aa", "2"}, -1));
    assertFalse(f.matches(new String[]{"1", "a", "3"}, -1));
  }

}
