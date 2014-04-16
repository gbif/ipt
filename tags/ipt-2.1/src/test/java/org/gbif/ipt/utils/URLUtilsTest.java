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
    Assert.assertTrue(URLUtils.hasPort("http://gbif.nothing.org:9932"));
    Assert.assertFalse(URLUtils.hasPort("http://gbif.nothing.org/eee/oo"));
  }


  @Test
  public void isURLValidTest() {
    Assert.assertTrue(URLUtils.isURLValid("http://localhost:8080"));
    Assert.assertTrue(URLUtils.isURLValid("https://gbif.nothing.com:9932"));
    Assert.assertTrue(URLUtils.isURLValid("http://gbif.nothing.org/eee/oo"));
    Assert.assertFalse(URLUtils.isURLValid("ftp://gbif.nothing.org/eee/oo"));
    Assert.assertFalse(URLUtils.isURLValid("file://gbif.nothing.org/eee/oo"));
    Assert.assertFalse(URLUtils.isURLValid("//gbif.nothing.org/eee/oo"));
    Assert.assertFalse(URLUtils.isURLValid("nothing.com/eee/oo"));
  }

}
