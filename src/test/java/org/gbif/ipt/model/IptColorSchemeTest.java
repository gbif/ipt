/*
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
package org.gbif.ipt.model;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IptColorSchemeTest {

  @Test
  public void testNormalizeHex() {
    assertEquals("#3d7d25", IptColorScheme.normalizeHex("#3D7D25"));
    assertEquals("#3d7d25", IptColorScheme.normalizeHex(" 3d7d25 "));
    assertEquals("#aabbcc", IptColorScheme.normalizeHex("#abc"));
    assertNull(IptColorScheme.normalizeHex(null));
    assertNull(IptColorScheme.normalizeHex(""));
    assertNull(IptColorScheme.normalizeHex("#fff0"));
    assertNull(IptColorScheme.normalizeHex("green"));
    assertNull(IptColorScheme.normalizeHex("#12345g"));
  }

  @Test
  public void testToRgbFallsBackToDefault() {
    assertEquals("61, 125, 37", IptColorScheme.toRgb("#3d7d25", "#ffffff"));
    assertEquals("255, 255, 255", IptColorScheme.toRgb("not a colour", "#ffffff"));
    assertEquals("255, 255, 255", IptColorScheme.toRgb(null, "#fff"));
  }

  @Test
  public void testMalformedPropertiesGetDefaults() {
    Properties props = new Properties();
    props.setProperty(IptColorScheme.PRIMARY_COLOR_PROPERTY, "#ABC");
    props.setProperty(IptColorScheme.NAVBAR_COLOR_PROPERTY, "rgb(1,2,3)");
    IptColorScheme scheme = new IptColorScheme(props);
    assertEquals("#aabbcc", scheme.getPrimaryColor());
    assertEquals(IptColorScheme.NAVBAR_COLOR_DEFAULT_VALUE, scheme.getNavbarColor());
    assertEquals(IptColorScheme.LINK_COLOR_DEFAULT_VALUE, scheme.getLinkColor());
  }

  @Test
  public void testLegacyDefault() {
    assertTrue(IptColorScheme.legacyDefaults().isLegacyDefault());

    // the short-lived green link default counts as default too
    IptColorScheme greenLink = IptColorScheme.legacyDefaults();
    greenLink.setLinkColor("#61a861");
    assertTrue(greenLink.isLegacyDefault());

    IptColorScheme custom = IptColorScheme.legacyDefaults();
    custom.setNavbarColor("#003361");
    assertFalse(custom.isLegacyDefault());

    // missing values in an old file are the old defaults, not the new ones
    IptColorScheme fromEmptyFile = new IptColorScheme(new Properties(), IptColorScheme.legacyDefaults());
    assertTrue(fromEmptyFile.isLegacyDefault());
  }

  @Test
  public void testCopy() {
    IptColorScheme scheme = IptColorScheme.presets().get("blue");
    IptColorScheme copy = new IptColorScheme(scheme);
    assertNotSame(scheme, copy);
    assertEquals(scheme, copy);
    copy.setPrimaryColor("#000000");
    assertEquals(IptColorScheme.GBIF_BLUE, scheme.getPrimaryColor());
  }

  @Test
  public void testDefaultIsFirstPreset() {
    assertEquals(new IptColorScheme(), IptColorScheme.presets().values().iterator().next());
  }

  @Test
  public void testNavbarStripeColor() {
    // light bar: 6% darker
    assertEquals("#f0f0f0", new IptColorScheme().getNavbarStripeColor());
    // mid bar: 12% darker
    assertEquals("#006a9e", IptColorScheme.presets().get("blue").getNavbarStripeColor());
    // nearly black bar: 12% lighter
    assertEquals("#3d3a3b", IptColorScheme.presets().get("black").getNavbarStripeColor());
  }

  @Test
  public void testPresetsPassContrast() {
    for (IptColorScheme s : IptColorScheme.presets().values()) {
      String stripe = s.getNavbarStripeColor();
      assertTrue(contrast(s.getNavbarLinkColor(), s.getNavbarColor()) >= 4.5, s.toString());
      assertTrue(contrast(s.getNavbarLinkColor(), stripe) >= 4.5, s.toString());
      assertTrue(contrast(s.getNavbarGbifLogoColor(), s.getNavbarColor()) >= 3, s.toString());
      assertTrue(contrast(s.getNavbarGbifLogoColor(), stripe) >= 3, s.toString());
      assertTrue(contrast(s.getNavbarActiveTabColor(), s.getNavbarColor()) >= 3, s.toString());
      assertTrue(contrast(s.getNavbarActiveTabColor(), stripe) >= 3, s.toString());
      assertTrue(contrast(s.getPrimaryColor(), "#ffffff") >= 4.5, s.toString());
      assertTrue(contrast(s.getLinkColor(), "#ffffff") >= 4.5, s.toString());
    }
  }

  private static double contrast(String a, String b) {
    double la = luminance(a);
    double lb = luminance(b);
    return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
  }

  private static double luminance(String hex) {
    double[] l = new double[3];
    for (int i = 0; i < 3; i++) {
      double v = Integer.parseInt(hex.substring(1 + 2 * i, 3 + 2 * i), 16) / 255.0;
      l[i] = v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
    return 0.2126 * l[0] + 0.7152 * l[1] + 0.0722 * l[2];
  }
}
