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

import org.junit.Before;
import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class BBoxTest {
  private Point p1;
  private Point p2;
  private Point p3;
  private Point p4;
  private BBox bbox;

  @Before
  public void setUp() throws Exception {
    p1 = new Point(54.0, 123.0);
    p2 = new Point(31.243, -13.9883);
    p3 = new Point(41.1, -3.9);
    p4 = new Point(41.1, -33.9);
    bbox = new BBox(p2, p1);
  }

  @Test
  public void testBoxRatio() {
    // System.out.println(bbox);
    bbox.expandToMapRatio();
    // System.out.println(bbox);
    bbox.expandBox(p4);
    // System.out.println(bbox);
    bbox.expandToMapRatio();
    // System.out.println(bbox);
    assertTrue(bbox.isValid());
  }

  @Test
  public void testContains() {
    assertTrue(bbox.contains(p1));
    assertTrue(bbox.contains(p3));
    assertFalse(bbox.contains(p4));
  }

  @Test
  public void testExpandBox() {
    bbox.expandBox(p1);
    bbox.expandBox(p3);
    assertTrue(bbox.contains(p3));

    bbox.expandBox(p4);
    assertTrue(bbox.contains(p4));

    // test with null values in bbox
    bbox = new BBox();
    assertFalse(bbox.contains(p4));
    bbox.expandBox(p4);
    assertTrue(bbox.contains(p4));
    bbox.expandBox(p3);
    assertTrue(bbox.contains(p3));
    assertTrue(bbox.contains(p4));
    bbox.expandBox(p1);
    assertTrue(bbox.contains(p1));
    assertTrue(bbox.contains(p3));
    assertTrue(bbox.contains(p4));
  }

  @Test
  public void testExpandBoxByPercentage() {
    bbox = BBox.newWorldInstance();
    System.out.println(bbox);
    bbox.resize(1.8f);
    System.out.println(bbox);
    bbox.resize(0.2f);
    System.out.println(bbox);
    assertTrue(bbox.isValid());
  }

  @Test
  public void testSurface() {
    bbox = new BBox(13.2, 4.0, 13.2, 4.0);
    System.out.println(bbox);
    System.out.println(bbox.surface());
    assertTrue(bbox.isValid());
    assertTrue(bbox.surface() == 0f);
  }

  @Test
  public void testWorldBox() {
    bbox = BBox.newWorldInstance();
    // System.out.println(bbox);
    assertTrue(bbox.isValid());
  }
}
