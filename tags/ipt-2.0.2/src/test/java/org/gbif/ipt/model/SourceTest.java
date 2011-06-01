/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.model;

import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author markus
 * 
 */
public class SourceTest {
  @Test
  public void testEquals() {
    Source src1 = new FileSource();
    src1.setName("Peter");
    Source src2 = new SqlSource();
    src2.setName(" peter");
    Source src3 = new FileSource();
    src3.setName("karl");

    assertEquals(src1, src2);
    assertFalse(src3.equals(src1));
    assertFalse(src3.equals(src2));
    assertTrue(Source.class.isInstance(src1));
    assertTrue(Source.class.isInstance(src2));
    assertTrue(Source.class.isInstance(src3));
    assertFalse(FileSource.class.isInstance(src2));

    Set<Source> sources = new HashSet<Source>();
    sources.add(src1);
    sources.add(src2);
    sources.add(src3);

    assertEquals(2, sources.size());
    assertTrue(sources.contains(src1));
    assertTrue(sources.contains(src2));
    assertTrue(sources.contains(src3));
    Source s = new SqlSource();
    s.setName("karlos");
    assertFalse(sources.contains(s));
    s.setName("karl");
    assertTrue(sources.contains(s));
    sources.remove(s);
    assertEquals(1, sources.size());

    // with resources:
    Resource r = new Resource();
    r.setTitle("Peterchen");
//    r.setKey(UUID.randomUUID());
    src1.setResource(r);
    src2.setResource(r);
    src3.setResource(r);

    assertEquals(src1, src2);
    assertFalse(src3.equals(src1));
    assertFalse(src3.equals(src2));
    assertTrue(Source.class.isInstance(src1));
    assertTrue(Source.class.isInstance(src2));
    assertTrue(Source.class.isInstance(src3));
    assertFalse(FileSource.class.isInstance(src2));

    sources = new HashSet<Source>();
    sources.add(src1);
    sources.add(src2);
    sources.add(src3);

    assertEquals(2, sources.size());
    assertTrue(sources.contains(src1));
    assertTrue(sources.contains(src2));
    assertTrue(sources.contains(src3));
    s = new SqlSource();
    s.setName("karlos");
    assertFalse(sources.contains(s));
    s.setName("karl");
    assertTrue(sources.contains(s));
    s.setResource(r);
    assertTrue(sources.contains(s));

    sources.remove(s);
    assertEquals(1, sources.size());
  }

  @Test
  public void testName() {
    Source src = new FileSource();
    src.setName("Peter");
    assertEquals("peter", src.getName());

    src.setName(" Peter nice");
    assertEquals("peternice", src.getName());

    src.setName("verNAcUl.:s");
    assertEquals("vernacul", src.getName());

    src.setName("veraculars.txt");
    assertEquals("veraculars", src.getName());

    src.setName("veraculars.my.txt");
    assertEquals("veracularsmy", src.getName());
  }
}
