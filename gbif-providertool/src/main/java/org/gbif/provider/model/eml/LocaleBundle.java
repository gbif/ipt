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

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * This class can be used to bundle a {@link Locale} with a {@link Charset}
 * encoding.
 * 
 * Note that this class is immutable. New instances can be created using the
 * static create method.
 * 
 */
public class LocaleBundle {

  /**
   * Contains constant definitions for the six standard {@link Charset}
   * instances, which are guaranteed to be supported by all Java platform
   * implementations.
   * 
   * @author Mike Bostock Copyright (C) 2007 Google Inc.
   * @since 2009.09.15 <b>tentative</b>
   */
  public final static class Charsets {
    /**
     * US-ASCII: seven-bit ASCII, a.k.a. ISO646-US, a.k.a the Basic Latin block
     * of the Unicode character set.
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    /**
     * ISO-8859-1. ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * UTF-8: eight-bit UCS Transformation Format.
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * UTF-16BE: sixteen-bit UCS Transformation Format, big-endian byte order.
     */
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");

    /**
     * UTF-16LE: sixteen-bit UCS Transformation Format, little-endian byte
     * order.
     */
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");

    /**
     * UTF-16: sixteen-bit UCS Transformation Format, byte order identified by
     * an optional byte-order mark.
     */
    public static final Charset UTF_16 = Charset.forName("UTF-16");

    private Charsets() {
    }
  }

  /**
   * Creates a new LocaleBundle instance. Throws {@link NullPointerException} or
   * {@link IllegalArgumentException} if any of the parameters are null or if
   * the language or country parameters are the empty string.
   * 
   * @param language the language
   * @param country the country
   * @param charset the character set
   * @return new instance of LocalBundle
   */
  public static LocaleBundle create(String language, String country,
      Charset charset) {
    checkNotNull(language, "Language was null");
    checkArgument(!language.isEmpty(), "Language was empty");
    checkNotNull(country, "Country was null");
    checkArgument(!country.isEmpty(), "Country was empty");
    checkNotNull(charset, "Charset was null");
    return new LocaleBundle(language, country, charset);
  }

  private final Locale locale;
  private final Charset charset;

  private LocaleBundle(String language, String country, Charset charset) {
    locale = new Locale(language, country);
    this.charset = charset;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof LocaleBundle)) {
      return false;
    }
    LocaleBundle lb = (LocaleBundle) other;
    return equal(locale, lb.locale) && equal(charset, lb.charset);
  }

  public String getCharset() {
    return charset.displayName();
  }

  public String getCountry() {
    return locale.getCountry();
  }

  public String getLanguage() {
    return locale.getLanguage();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(locale, charset);
  }

  public String toString() {
    return String.format("Country=%s, Language=%s, Charset=%s", getCountry(),
        getLanguage(), getCharset());
  }
}
