package org.gbif.ipt.utils;

import java.io.IOException;
import java.math.BigDecimal;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResourceUtilsTest {

  /**
   * @see org.gbif.ipt.service.manage.impl.ResourceManagerImplTest#testReconstructVersion()
   */
  @Ignore("See ResourceManagerImplTest#testReconstructVersion()")
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
