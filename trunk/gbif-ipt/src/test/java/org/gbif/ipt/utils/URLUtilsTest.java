package org.gbif.ipt.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;


/**
 * Test cases for class {@link URLUtils}.
 */
public class URLUtilsTest {

  @Test
  public void isLocalhostTest() throws MalformedURLException {
    Assert.assertTrue(URLUtils.isLocalhost(new URL("http://localhost:8080")));
    Assert.assertTrue(URLUtils.isLocalhost(new URL("http://127.0.0.1/test/tests")));
  }

  @Test
  public void getHostNameTest() {
    Assert.assertNotNull(URLUtils.getHostName());
  }

  @Test
  public void hasPortTest() {
    Assert.assertTrue(URLUtils.hasPort("http://localhost:8080"));
    Assert.assertTrue(URLUtils.hasPort("http://gbif.nothing.iii:9932"));
    Assert.assertFalse(URLUtils.hasPort("http://gbif.nothing.iii/eee/oo"));
  }

}
