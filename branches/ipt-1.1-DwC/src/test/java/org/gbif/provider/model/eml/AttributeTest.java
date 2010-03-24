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
 * This class can be used for unit testing {@link Attribute}.
 * 
 */
public class AttributeTest {

  private static Attribute create(String category, LocaleBundle localeBundle,
      String name, String value) {
    return create(null, category, localeBundle, name, value);
  }

  private static Attribute create(String failMsg, String category,
      LocaleBundle localeBundle, String name, String value) {
    Attribute m = null;
    try {
      m = Attribute.create(category, localeBundle, name, value);
      if (failMsg != null) {
        fail(failMsg);
      } else {
        System.out.printf("Success as expected: create(%s)\n", m);
      }
    } catch (Exception e) {
      if (failMsg == null) {
        fail(e.getMessage());
      } else {
        System.out.printf("Failure as expected: %s\n", e.getMessage());
      }
    }
    return m;
  }

  @Test
  public final void testCreate() {
    LocaleBundle lb = LocaleBundle.create("EN", "USA", Charsets.UTF_8);
    create("Should fail with null params", null, null, null, null);
    create("Should fail with null category", null, lb, "n", "v");
    create("Should fail with empty category", "", lb, "n", "v");
    create("Should fail with null locale bundle", "c", null, "n", "v");
    create("Should fail with null name", "c", lb, null, "v");
    create("Should fail with empty name", "c", lb, "", "v");
    create("Should fail with null value", "c", lb, "n", null);
    create("Should fail with empty name", "c", lb, "n", "");
    create("c", lb, "n", "v");
  }

  @Test
  public final void testEqualsObject() {
    LocaleBundle lb = LocaleBundle.create("EN", "USA", Charsets.UTF_8);
    assertEquals(create("c", lb, "n", "v"), create("c", lb, "n", "v"));
  }

  @Test
  public final void testGetCategory() {
    LocaleBundle lb = LocaleBundle.create("EN", "USA", Charsets.UTF_8);
    String category = "c";
    assertEquals(category, create(category, lb, "n", "v").getCategory());
  }

  @Test
  public final void testGetLocaleBundle() {
    LocaleBundle lb = LocaleBundle.create("EN", "USA", Charsets.UTF_8);
    assertEquals(lb, create("c", lb, "n", "v").getLocaleBundle());
  }

  @Test
  public final void testGetName() {
    LocaleBundle lb = LocaleBundle.create("EN", "USA", Charsets.UTF_8);
    String name = "n";
    assertEquals(name, create("c", lb, name, "v").getName());
  }

  @Test
  public final void testGetValue() {
    LocaleBundle lb = LocaleBundle.create("EN", "USA", Charsets.UTF_8);
    String value = "v";
    assertEquals(value, create("c", lb, "n", value).getValue());
  }

  @Test
  public final void testHashCode() {
    LocaleBundle lb = LocaleBundle.create("EN", "USA", Charsets.UTF_8);
    Attribute a = create("c", lb, "n", "v");
    assertEquals(a.hashCode(), create("c", lb, "n", "v").hashCode());
    Map<Attribute, String> map = Maps.newHashMap();
    map.put(a, "foo");
    assertTrue(map.containsKey(create("c", lb, "n", "v")));
  }

  @Test
  public final void testToString() {
    LocaleBundle lb = LocaleBundle.create("EN", "USA", Charsets.UTF_8);
    Attribute a = create("c", lb, "n", "v");
    assertEquals(
        "Category=c, LocaleBundle=Country=USA, Language=en, Charset=UTF-8, Name=n, Value=v",
        a.toString());
  }
}
