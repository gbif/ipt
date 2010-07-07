/*
 * Copyright 2010 Regents of the University of California, University of Kansas.
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
package org.gbif.registry.api.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * This class encapsulates a GBIF Thesaurus returned from the GBRDS.
 * 
 * @see http://gbrds.gbif.org/registry/ipt/extensions.json
 * 
 */
@SuppressWarnings("serial")
public class GbrdsThesaurus implements Serializable {

  /**
   * Creates a new {@link GbrdsThesaurus}. A {@link NullPointerException} is
   * thrown if any arguments are null.
   * 
   * @param id
   * @param title
   * @param language
   * @param url
   * @return GbrdsThesaurus
   */
  public static GbrdsThesaurus create(Integer id, String title,
      String language, String url) {
    checkNotNull(id);
    checkNotNull(title);
    checkNotNull(language);
    checkNotNull(url);
    return new GbrdsThesaurus(id, title, language, url);
  }

  private final int id;
  private final String title;
  private final String language;
  private final String url;

  GbrdsThesaurus() {
    this(-1, null, null, null);
  }

  private GbrdsThesaurus(int id, String title, String language, String url) {
    this.id = id;
    this.title = title;
    this.language = language;
    this.url = url;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GbrdsThesaurus)) {
      return false;
    }
    GbrdsThesaurus o = (GbrdsThesaurus) other;
    return Objects.equal(id, o.id) && Objects.equal(title, o.title)
        && Objects.equal(language, o.language) && Objects.equal(url, o.url);
  }

  public int getId() {
    return id;
  };

  public String getLanguage() {
    return language;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, title, language, url);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("Id", id).add("Title", title).add(
        "Language", language).add("URL", url).toString();
  }
}