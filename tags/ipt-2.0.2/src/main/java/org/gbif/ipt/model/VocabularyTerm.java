/*
 * Copyright 2009 GBIF.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.ipt.model;

import org.gbif.ipt.utils.LangUtils;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * A single literal representation of a vocabulary concept in a given language
 * 
 */
public class VocabularyTerm implements Serializable {
  private static final long serialVersionUID = 9000999000012L;
  private String title;
  private String lang;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof VocabularyTerm)) {
      return false;
    }
    VocabularyTerm o = (VocabularyTerm) other;
    return equal(title, o.title) && equal(lang, o.lang);
  }

  public String getLang() {
    return lang;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(title, lang);
  }

  public void setLang(String lang) {
    this.lang = LangUtils.iso2(lang);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return String.format("%s [%s]", title, lang);
  }

}
