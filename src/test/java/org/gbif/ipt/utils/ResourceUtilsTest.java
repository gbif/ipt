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
package org.gbif.ipt.utils;

import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceUtilsTest {

  /**
   * @see org.gbif.ipt.service.manage.impl.ResourceManagerImplTest#testReconstructVersion()
   */
  @Disabled("See ResourceManagerImplTest#testReconstructVersion()")
  public void testReconstructVersion() throws Exception {
  }

  @Test
  public void testAssertVersionOrder() throws ParserConfigurationException, SAXException, IOException {
    // true cases
    BigDecimal b = new BigDecimal("1.10");
    BigDecimal a = new BigDecimal("1.9");
    assertTrue(ResourceUtils.assertVersionOrder(b, a));

    b = new BigDecimal("2.0");
    a = new BigDecimal("1.300");
    assertTrue(ResourceUtils.assertVersionOrder(b, a));

    b = new BigDecimal("2.10");
    a = new BigDecimal("1.9999");
    assertTrue(ResourceUtils.assertVersionOrder(b, a));

    // false cases
    b = new BigDecimal("1.0");
    a = new BigDecimal("1.0");
    assertFalse(ResourceUtils.assertVersionOrder(b, a));

    b = new BigDecimal("1.0");
    a = new BigDecimal("1.11");
    assertFalse(ResourceUtils.assertVersionOrder(b, a));

    b = new BigDecimal("1.900");
    a = new BigDecimal("2.0");
    assertFalse(ResourceUtils.assertVersionOrder(b, a));
  }
}
