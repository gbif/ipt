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

import java.io.Serializable;

/**
 * This class can be used to encapsulate generic attribute information. Each
 * attribute has a category, a name, and a type within the context of a
 * {@link LocaleBundle}.
 * 
 * Note that this class is immuatable. New instances can be created using the
 * create method.
 * 
 */
public class Attribute implements Serializable {

  private static final long serialVersionUID = 8805087340650428951L;

  /**
   * Creates a new Attribute instance. Throws {@link NullPointerException} if
   * any of the arguments are null. Throws {@link IllegalArgumentException} if
   * category, name, or value arguments are the empty string.
   * 
   * @param category the category
   * @param localeBundle the locale bundle
   * @param name the name
   * @param value the value
   * @return new instance of Attribute
   */
  public static Attribute create(String category, LocaleBundle localeBundle,
      String name, String value) {
    checkNotNull(category, "Category was null");
    checkArgument(!category.isEmpty(), "Category was empty");
    checkNotNull(localeBundle, "LocaleBundle was null");
    checkNotNull(name, "Name was null");
    checkArgument(!name.isEmpty(), "Name was empty");
    checkNotNull(value, "Value was null");
    checkArgument(!value.isEmpty(), "Value was empty");
    return new Attribute(category, localeBundle, name, value);
  }

  private final String category;
  private final LocaleBundle localeBundle;
  private final String name;
  private final String value;

  private Attribute(String category, LocaleBundle localeBundle, String name,
      String value) {
    this.category = category;
    this.localeBundle = localeBundle;
    this.name = name;
    this.value = value;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Attribute)) {
      return false;
    }
    Attribute o = (Attribute) other;
    return equal(category, o.category) && equal(localeBundle, o.localeBundle)
        && equal(name, o.name) && equal(value, o.value);
  }

  public String getCategory() {
    return category;
  }

  public LocaleBundle getLocaleBundle() {
    return localeBundle;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(category, localeBundle, name, value);
  }

  @Override
  public String toString() {
    return String.format("Category=%s, LocaleBundle=%s, Name=%s, Value=%s",
        category, localeBundle, name, value);
  }
}
