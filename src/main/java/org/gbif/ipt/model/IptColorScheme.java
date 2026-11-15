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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The colours of the IPT's pages, as #rrggbb. The defaults are the "White and green" scheme: a white navigation bar with
 * grey menu text, the GBIF green (#4c9c2e) for the logo and the active tab, and gbif.org's darker green (#3d7d25) for
 * buttons and links, dark enough for text on white (WCAG 4.5 : 1). The other colours are from the GBIF colour
 * palette (2025).
 */
public class IptColorScheme {

  public static final String PRIMARY_COLOR_PROPERTY = "primaryColor";
  public static final String NAVBAR_COLOR_PROPERTY = "navbarColor";
  public static final String NAVBAR_LINK_COLOR_PROPERTY = "navbarLinkColor";
  public static final String NAVBAR_GBIF_LOGO_COLOR_PROPERTY = "navbarGbifLogoColor";
  public static final String NAVBAR_ACTIVE_TAB_COLOR_PROPERTY = "navbarActiveTabColor";
  public static final String LINK_COLOR_PROPERTY = "linkColor";

  /** GBIF green from the GBIF colour palette; below 4.5 : 1 on white, so used for the logo and lines only. */
  public static final String GBIF_GREEN = "#4c9c2e";
  /** The darker GBIF green of gbif.org (--primary700, its link hover colour); 5 : 1 with white: buttons and links. */
  public static final String GBIF_GREEN_DARK = "#3d7d25";
  /** The IPT's header grey. */
  public static final String GBIF_GREY = "#4e565f";
  /** Matisse, from the GBIF colour palette. */
  public static final String GBIF_BLUE = "#0078b4";
  /** Solid black, from the GBIF colour palette. */
  public static final String GBIF_BLACK = "#231f20";
  public static final String WHITE = "#ffffff";

  public static final String PRIMARY_COLOR_DEFAULT_VALUE = GBIF_GREEN_DARK;
  public static final String NAVBAR_COLOR_DEFAULT_VALUE = WHITE;
  public static final String NAVBAR_LINK_COLOR_DEFAULT_VALUE = GBIF_GREY;
  public static final String NAVBAR_GBIF_LOGO_COLOR_DEFAULT_VALUE = GBIF_GREEN;
  public static final String NAVBAR_ACTIVE_TAB_COLOR_DEFAULT_VALUE = GBIF_GREEN;
  public static final String LINK_COLOR_DEFAULT_VALUE = GBIF_GREEN_DARK;

  private static final Pattern HEX = Pattern.compile("#?([0-9a-fA-F]{6}|[0-9a-fA-F]{3})");

  private String primaryColor = PRIMARY_COLOR_DEFAULT_VALUE;
  private String navbarColor = NAVBAR_COLOR_DEFAULT_VALUE;
  private String navbarLinkColor = NAVBAR_LINK_COLOR_DEFAULT_VALUE;
  private String navbarGbifLogoColor = NAVBAR_GBIF_LOGO_COLOR_DEFAULT_VALUE;
  private String navbarActiveTabColor = NAVBAR_ACTIVE_TAB_COLOR_DEFAULT_VALUE;
  private String linkColor = LINK_COLOR_DEFAULT_VALUE;

  /**
   * The default scheme ("GBIF light").
   */
  public IptColorScheme() {
  }

  public IptColorScheme(String primaryColor, String navbarColor, String navbarLinkColor, String navbarGbifLogoColor,
                        String navbarActiveTabColor, String linkColor) {
    this.primaryColor = primaryColor;
    this.navbarColor = navbarColor;
    this.navbarLinkColor = navbarLinkColor;
    this.navbarGbifLogoColor = navbarGbifLogoColor;
    this.navbarActiveTabColor = navbarActiveTabColor;
    this.linkColor = linkColor;
  }

  /**
   * Copy constructor.
   */
  public IptColorScheme(IptColorScheme other) {
    this(other.primaryColor, other.navbarColor, other.navbarLinkColor, other.navbarGbifLogoColor,
        other.navbarActiveTabColor, other.linkColor);
  }

  /**
   * Reads a scheme from properties. A missing or malformed value gets the default, so a hand-edited file can't break
   * the pages.
   */
  public IptColorScheme(Properties props) {
    this(props, new IptColorScheme());
  }

  /**
   * Reads a scheme from properties, taking missing or malformed values from {@code defaults}.
   */
  public IptColorScheme(Properties props, IptColorScheme defaults) {
    primaryColor = valueOrDefault(props.getProperty(PRIMARY_COLOR_PROPERTY), defaults.primaryColor);
    navbarColor = valueOrDefault(props.getProperty(NAVBAR_COLOR_PROPERTY), defaults.navbarColor);
    navbarLinkColor = valueOrDefault(props.getProperty(NAVBAR_LINK_COLOR_PROPERTY), defaults.navbarLinkColor);
    navbarGbifLogoColor = valueOrDefault(props.getProperty(NAVBAR_GBIF_LOGO_COLOR_PROPERTY), defaults.navbarGbifLogoColor);
    navbarActiveTabColor = valueOrDefault(props.getProperty(NAVBAR_ACTIVE_TAB_COLOR_PROPERTY), defaults.navbarActiveTabColor);
    linkColor = valueOrDefault(props.getProperty(LINK_COLOR_PROPERTY), defaults.linkColor);
  }

  /**
   * The schemes offered on the Appearance page, by key (admin.uiManagement.scheme.KEY); the first is the default.
   * Three on a white bar with a green, blue or black accent (the logo included), three on a bar of that colour. All of
   * them reach 4.5 : 1 for text and 3 : 1 for the logo and
   * the current page marker, also on the test-mode stripes.
   */
  public static Map<String, IptColorScheme> presets() {
    Map<String, IptColorScheme> presets = new LinkedHashMap<>();
    presets.put("whiteGreen", new IptColorScheme());
    presets.put("whiteBlue", new IptColorScheme(GBIF_BLUE, WHITE, GBIF_GREY, GBIF_BLUE, GBIF_BLUE, GBIF_BLUE));
    presets.put("whiteBlack", new IptColorScheme(GBIF_BLACK, WHITE, GBIF_GREY, GBIF_BLACK, GBIF_BLACK, GBIF_GREEN_DARK));
    presets.put("green", new IptColorScheme(GBIF_GREEN_DARK, GBIF_GREEN_DARK, WHITE, WHITE, WHITE, GBIF_GREEN_DARK));
    presets.put("blue", new IptColorScheme(GBIF_BLUE, GBIF_BLUE, WHITE, WHITE, WHITE, GBIF_BLUE));
    presets.put("black", new IptColorScheme(GBIF_GREEN_DARK, GBIF_BLACK, WHITE, GBIF_GREEN, GBIF_GREEN, GBIF_GREEN_DARK));
    return presets;
  }

  /**
   * The colours IPT 3.0 to 3.4 used by default: a green navigation bar with white text. Their file,
   * ipt-color-scheme.properties, is read only to offer an administrator's own colours again.
   */
  public static IptColorScheme legacyDefaults() {
    return new IptColorScheme("#61a861", "#78b578", WHITE, WHITE, WHITE, "#4ba2ce");
  }

  /**
   * @return true when this scheme equals the IPT 3.x defaults; the link colour may also be the primary green, the
   * link default for a short time before the new defaults
   */
  public boolean isLegacyDefault() {
    IptColorScheme legacy = legacyDefaults();
    if (Objects.equals(linkColor, legacy.primaryColor)) {
      legacy.linkColor = legacy.primaryColor;
    }
    return equals(legacy);
  }

  /**
   * @return the value as lower case #rrggbb (#abc is expanded), or null if it isn't a hex colour
   */
  public static String normalizeHex(String value) {
    if (value == null) {
      return null;
    }
    Matcher m = HEX.matcher(value.trim());
    if (!m.matches()) {
      return null;
    }
    String hex = m.group(1).toLowerCase(Locale.ROOT);
    if (hex.length() == 3) {
      hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
    }
    return "#" + hex;
  }

  /**
   * @return "r, g, b" for a CSS variable used as rgb(var(--x)) or rgba(var(--x), a); the default if the value isn't a
   * hex colour
   */
  public static String toRgb(String hex, String defaultHex) {
    String h = normalizeHex(hex);
    if (h == null) {
      h = normalizeHex(defaultHex);
    }
    return Integer.parseInt(h.substring(1, 3), 16) + ", "
        + Integer.parseInt(h.substring(3, 5), 16) + ", "
        + Integer.parseInt(h.substring(5, 7), 16);
  }

  /**
   * The second colour of the test-mode stripes on the navigation bar: the bar colour darkened by 12% (6% for a light
   * bar, so the GBIF green logo stays above 3 : 1 on white), or lightened by 12% when the bar is nearly black.
   * Darkening keeps white menu text readable on a dark bar. The same rule is in
   * js/appearance.js, which checks the menu text's contrast against both.
   */
  public String getNavbarStripeColor() {
    String h = Objects.requireNonNullElse(normalizeHex(navbarColor), NAVBAR_COLOR_DEFAULT_VALUE);
    int[] c = {
        Integer.parseInt(h.substring(1, 3), 16),
        Integer.parseInt(h.substring(3, 5), 16),
        Integer.parseInt(h.substring(5, 7), 16)};
    double luminance = luminance(c);
    boolean lighten = luminance < 0.05;
    double darken = luminance > 0.5 ? 0.94 : 0.88;
    StringBuilder sb = new StringBuilder("#");
    for (int v : c) {
      int mixed = lighten ? (int) Math.round(v + (255 - v) * 0.12) : (int) Math.round(v * darken);
      sb.append(String.format("%02x", mixed));
    }
    return sb.toString();
  }

  private static double luminance(int[] rgb) {
    double[] l = new double[3];
    for (int i = 0; i < 3; i++) {
      double v = rgb[i] / 255.0;
      l[i] = v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
    return 0.2126 * l[0] + 0.7152 * l[1] + 0.0722 * l[2];
  }

  private static String valueOrDefault(String value, String defaultValue) {
    return Objects.requireNonNullElse(normalizeHex(value), defaultValue);
  }

  public Properties toProperties() {
    Properties props = new Properties();
    props.setProperty(PRIMARY_COLOR_PROPERTY, primaryColor);
    props.setProperty(NAVBAR_COLOR_PROPERTY, navbarColor);
    props.setProperty(NAVBAR_LINK_COLOR_PROPERTY, navbarLinkColor);
    props.setProperty(NAVBAR_GBIF_LOGO_COLOR_PROPERTY, navbarGbifLogoColor);
    props.setProperty(NAVBAR_ACTIVE_TAB_COLOR_PROPERTY, navbarActiveTabColor);
    props.setProperty(LINK_COLOR_PROPERTY, linkColor);
    return props;
  }

  public String getPrimaryColor() {
    return primaryColor;
  }

  public void setPrimaryColor(String primaryColor) {
    this.primaryColor = primaryColor;
  }

  public String getNavbarColor() {
    return navbarColor;
  }

  public void setNavbarColor(String navbarColor) {
    this.navbarColor = navbarColor;
  }

  public String getNavbarLinkColor() {
    return navbarLinkColor;
  }

  public void setNavbarLinkColor(String navbarLinkColor) {
    this.navbarLinkColor = navbarLinkColor;
  }

  public String getNavbarGbifLogoColor() {
    return navbarGbifLogoColor;
  }

  public void setNavbarGbifLogoColor(String navbarGbifLogoColor) {
    this.navbarGbifLogoColor = navbarGbifLogoColor;
  }

  public String getNavbarActiveTabColor() {
    return navbarActiveTabColor;
  }

  public void setNavbarActiveTabColor(String navbarActiveTabColor) {
    this.navbarActiveTabColor = navbarActiveTabColor;
  }

  public String getLinkColor() {
    return linkColor;
  }

  public void setLinkColor(String linkColor) {
    this.linkColor = linkColor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IptColorScheme that = (IptColorScheme) o;
    return Objects.equals(primaryColor, that.primaryColor)
        && Objects.equals(navbarColor, that.navbarColor)
        && Objects.equals(navbarLinkColor, that.navbarLinkColor)
        && Objects.equals(navbarGbifLogoColor, that.navbarGbifLogoColor)
        && Objects.equals(navbarActiveTabColor, that.navbarActiveTabColor)
        && Objects.equals(linkColor, that.linkColor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(primaryColor, navbarColor, navbarGbifLogoColor, navbarActiveTabColor, navbarLinkColor, linkColor);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", IptColorScheme.class.getSimpleName() + "[", "]")
        .add("primaryColor='" + primaryColor + "'")
        .add("navbarColor='" + navbarColor + "'")
        .add("navbarLinkColor='" + navbarLinkColor + "'")
        .add("navbarGbifLogoColor='" + navbarGbifLogoColor + "'")
        .add("navbarActiveTabColor='" + navbarActiveTabColor + "'")
        .add("linkColor='" + linkColor + "'")
        .toString();
  }
}
