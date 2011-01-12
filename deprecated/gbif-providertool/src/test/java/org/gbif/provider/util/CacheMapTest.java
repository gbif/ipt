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
package org.gbif.provider.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class CacheMapTest {

  @Test
  public void testAll() {
    CacheMap lm = new CacheMap<Integer, Integer>(4);
    Integer[] vals = {1, 2, 3, 2, 3, 54, 34, 3, 4, 5, 32, 3};
    for (Integer v : vals) {
      lm.put(v, v * v);
      assertTrue(lm.size() < 5);
      assertEquals(lm.get(v), v * v);
      // System.out.println(lm.keySet());
    }
  }
}
