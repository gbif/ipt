package org.gbif.ipt.config;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class IPTContextListenerTest {
  Pattern regex = Pattern.compile(IPTContextListener.ALL_BUT_AUTHENTICATED);

  /**
   * Make sure the regex used to apply the xss filter only fires on all urls not having manage or admin in the path.
   */
  @Test
  public void testRegex() {
    assertTrue(matches(""));
    assertTrue(matches("/"));
    assertTrue(matches("/aha"));
    assertTrue(matches("/manage"));
    assertTrue(matches("/my-manager/678"));
    assertTrue(matches("/admin"));
    assertTrue(matches("/administrator/67483"));
    assertTrue(matches("/fhdjs/administrator/67483?fd"));

    assertFalse(matches("/manage/resource.do"));
    assertFalse(matches("/admin/resource.do"));
    assertFalse(matches("/root/manage/resource.do"));
  }

  private boolean matches(String path) {
    return regex.matcher(path).matches();
  }
}