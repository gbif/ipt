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
package org.gbif.provider.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class PointTest {

  @Test
  public void rangeLimit() {
    Point p1 = new Point(54.0, 123.0);
    Point p2 = new Point(31.24332, -13.9883321);
    p2.toStringShort(3);
    assertFalse(p1.equals(p2));
    p1.setLatitude(31.24332);
    p1.setLongitude(-13.9883321);
    assertTrue(p1.equals(p2));

    p1.setLatitude(null);
    boolean success = false;
    try {
      p1.setLatitude(121.243);
    } catch (IllegalArgumentException e) {
      success = true;
    }
    assertTrue(success);
    assertFalse(p1.isValid());
    assertTrue(p2.isValid());

    // test nulls
    Point p = new Point();
    Point pp = new Point(p);
    Double lon = p.getLongitude();
    Double lat = p.getLatitude();
    pp = new Point(lat, lon);
  }
}
