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

import java.nio.charset.Charset;
import java.util.Map;

import org.junit.Test;

/**
 * This class can be used for unit testing {@link PhysicalData}.
 * 
 */
public class PhysicalDataTest {

  @Test
  public final void testBuilder() {
    try {
      PhysicalData.builder(null, null);
      fail("Should fail with null args");
    } catch (Exception e) {
    }
    try {
      PhysicalData.builder(Charsets.UTF_8, null);
      fail("Should fail with null name");
    } catch (Exception e) {
    }
    try {
      PhysicalData.builder(Charsets.UTF_8, "");
      fail("Should fail with empty name");
    } catch (Exception e) {
    }
    try {
      PhysicalData.builder(null, "name");
      fail("Should fail with null charset");
    } catch (Exception e) {
    }
    PhysicalData.builder(Charsets.UTF_8, "name");
  }

  @Test
  public final void testEqualsObject() {
    assertEquals(PhysicalData.builder(Charsets.UTF_8, "name").build(),
        PhysicalData.builder(Charsets.UTF_8, "name").build());
  }

  @Test
  public final void testGetCharset() {
    Charset charset = Charsets.UTF_8;
    PhysicalData pd = PhysicalData.builder(charset, "name").build();
    assertEquals(charset.displayName(), pd.getCharset());
  }

  @Test
  public final void testGetDistributionUrl() {
    String distributionUrl = "du";
    PhysicalData pd = PhysicalData.builder(Charsets.UTF_8, "name").distributionUrl(
        distributionUrl).build();
    assertEquals(distributionUrl, pd.getDistributionUrl());
  }

  @Test
  public final void testGetFormat() {
    String format = "f";
    PhysicalData pd = PhysicalData.builder(Charsets.UTF_8, "name").format(
        format).build();
    assertEquals(format, pd.getFormat());
  }

  @Test
  public final void testGetFormatVersion() {
    String formatVersion = "fv";
    PhysicalData pd = PhysicalData.builder(Charsets.UTF_8, "name").formatVersion(
        formatVersion).build();
    assertEquals(formatVersion, pd.getFormatVersion());
  }

  @Test
  public final void testGetName() {
    String name = "name";
    PhysicalData pd = PhysicalData.builder(Charsets.UTF_8, name).build();
    assertEquals(name, pd.getName());
  }

  @Test
  public final void testHashCode() {
    PhysicalData pd = PhysicalData.builder(Charsets.UTF_8, "name").build();
    assertEquals(pd.hashCode(),
        PhysicalData.builder(Charsets.UTF_8, "name").build().hashCode());
    Map<PhysicalData, String> map = Maps.newHashMap();
    map.put(pd, "foo");
    assertTrue(map.containsKey(PhysicalData.builder(Charsets.UTF_8, "name").build()));
  }

  @Test
  public final void testToString() {
    PhysicalData pd = PhysicalData.builder(Charsets.UTF_8, "n").distributionUrl(
        "du").format("f").formatVersion("fv").build();
    assertEquals(
        "Charset=UTF-8, DistributionUrl=du, Format=f, FormatVersion=fv, Name=n",
        pd.toString());
  }
}
