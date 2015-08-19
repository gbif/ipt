package org.gbif.ipt.xss;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class XSSUtilTest {
  // decoded request, header, or parameter
  private String value;
  // true if request contains XSS, or false otherwise
  private boolean containsXSS;

  public XSSUtilTest(String value, boolean containsXSS) {
    this.value = value;
    this.containsXSS = containsXSS;
  }

  /**
   * Additional requests to test can be added here.
   */
  @Parameterized.Parameters
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
      // contain xss
      {"http://localhost:8090/ipt/about.do?request_locale=\"'><script>alert(6227)</script>&email=\"'><script>alert(6227)</script>&password=\"'><script>alert(6227)</script>&login-submit=\"'><script>alert(6227)</script>", true},
      {"http://www.gbif.org/dataset/search?q=\"><script>prompt('XSSPOSED')</script>", true},
      {"http://www.gbif.org/index.php?name=<script>window.onload = function() {var link=document.getElementsByTagName(\"a\");link[0].href=\"http://not-real-xssattackexamples.com/\";}</script>", true},
      // do not contain xss
      {"http://www.gbif.org/ipt", false},
      {"http://api.gbif.org/v1/occurrence/search?year=1800", false}});
  }

  @Test
  public void testContainsXSS() {
    assertEquals(containsXSS, XSSUtil.containsXSS(value));
  }
}
