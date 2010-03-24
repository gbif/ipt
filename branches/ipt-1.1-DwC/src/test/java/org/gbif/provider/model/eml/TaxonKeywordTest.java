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

import com.google.common.collect.Maps;

import java.util.Map;

import org.junit.Test;

/**
 * This class can be used for unit testing {@link TaxonKeyword}.
 * 
 */
public class TaxonKeywordTest {

  @Test
  public final void testCreate() {
    TaxonKeyword.create(null, null, null);
    TaxonKeyword.create("sn", "r", "cn");
  }

  @Test
  public final void testEqualsObject() {
    assertEquals(TaxonKeyword.create("sn", "r", "cn"), TaxonKeyword.create(
        "sn", "r", "cn"));
  }

  @Test
  public final void testGetCommonName() {
    assertEquals("cn", TaxonKeyword.create("sn", "r", "cn").getCommonName());
  }

  @Test
  public final void testGetRank() {
    assertEquals("r", TaxonKeyword.create("sn", "r", "cn").getRank());
  }

  @Test
  public final void testGetScientificName() {
    assertEquals("sn", TaxonKeyword.create("sn", "r", "cn").getScientificName());
  }

  @Test
  public final void testHashCode() {
    assertEquals(TaxonKeyword.create("sn", "r", "cn").hashCode(),
        TaxonKeyword.create("sn", "r", "cn").hashCode());
    TaxonKeyword tk = TaxonKeyword.create("sn", "r", "cn");
    Map<TaxonKeyword, String> map = Maps.newHashMap();
    map.put(tk, "foo");
    assertTrue(map.containsKey(TaxonKeyword.create("sn", "r", "cn")));
  }

  @Test
  public final void testToString() {
    assertEquals("ScientificName=sn, Rank=r, CommonName=cn",
        TaxonKeyword.create("sn", "r", "cn").toString());
  }
}
