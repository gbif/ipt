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

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SourceTest {

  @Test
  public void testEquals() {
    Source src1 = new TextFileSource();
    src1.setName("Peter");
    Source src2 = new SqlSource();
    src2.setName(" peter");
    Source src3 = new TextFileSource();
    src3.setName("karl");

    assertEquals(src1, src2);
    assertFalse(src3.equals(src1));
    assertFalse(src3.equals(src2));
    assertTrue(SourceBase.class.isInstance(src1));
    assertTrue(SourceBase.class.isInstance(src2));
    assertTrue(SourceBase.class.isInstance(src3));
    assertFalse(TextFileSource.class.isInstance(src2));

    Set<Source> sources = new HashSet<>();
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
    // r.setKey(UUID.randomUUID());
    src1.setResource(r);
    src2.setResource(r);
    src3.setResource(r);

    assertEquals(src1, src2);
    assertFalse(src3.equals(src1));
    assertFalse(src3.equals(src2));
    assertTrue(SourceBase.class.isInstance(src1));
    assertTrue(SourceBase.class.isInstance(src2));
    assertTrue(SourceBase.class.isInstance(src3));
    assertFalse(TextFileSource.class.isInstance(src2));

    sources = new HashSet<>();
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
    Source src = new TextFileSource();
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

  @Test
  public void testNormaliseName() {
    assertEquals("filename", SourceBase.normaliseName("fileName.txt"));
    assertEquals("filename", SourceBase.normaliseName("FILENAME.txt"));
    assertEquals("filenametxt", SourceBase.normaliseName("filename txt"));
    assertEquals("filename", SourceBase.normaliseName("filename%*?/:.<>|"));
    assertEquals("filenametxt", SourceBase.normaliseName("filename\\/\"%*?/:<>|.txt.txt"));
    assertEquals("filename-copy", SourceBase.normaliseName("filename-copy%*?/:.<>|.txt"));
    assertEquals("filename", SourceBase.normaliseName("filename.pdf"));
    assertEquals("filename-copy-2011", SourceBase.normaliseName("filename-copy-2011.cvs"));
    assertEquals("filename", SourceBase.normaliseName("filename"));
    assertEquals("-1", SourceBase.normaliseName("-1"));
    assertEquals("", SourceBase.normaliseName(""));
    assertNull(SourceBase.normaliseName(null));
  }
}
