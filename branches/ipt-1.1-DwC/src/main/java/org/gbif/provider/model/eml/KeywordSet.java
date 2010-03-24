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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

/**
 * This class can be used to associate multiple keywords with a single
 * thesaurus. It cannot hold duplicate thesaurus-keyword pairs. Adding a
 * thesaurus-keyword pair that's already in the KeywordSet has no effect.
 * 
 * Usage example:
 * 
 * <pre>
 * Builder b = KeywordSet.builder();
 * b.add("Thesaurus1", "Insect").add("Thesaurus1", "Fly");
 * b.add("Thesaurus2", "Spider").add("Thesaurus2", "Bee");
 * b.add("Thesaurus2", "Spider"); // Adding a duplicate has no effect.
 * KeywordSet ks = b.build(); 
 * ks.getKeywords("Thesaurus1"); // ["Insect", "Fly"]
 * ks.getKeywords("Thesaurus2"); // ["Spider", "Bee"]
 * ks.getAllKeywords(); // ["Insect", "Fly", "Spider", "Bee"]
 * ks.getAllThesauri(); // ["Thesaurus1", "Thesaurus2"]
 * </pre>
 * 
 * Note that this class is immutable. Instances can be created using the builder
 * pattern via the builder method.
 * 
 */
public class KeywordSet {

  /**
   * This class can be used to build a KeywordSet instance using the builder
   * pattern. Instances of this class are created using KeywordSet's builder
   * method.
   * 
   * Usage example:
   * 
   * KeywordSet ks = KeywordSet.builder().add("T1", "X").add("T2", "Z").build();
   * 
   */
  public static class Builder {
    private final ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();

    private Builder() {
    }

    public Builder add(String thesaurus, String keyword) {
      builder.put(thesaurus, keyword);
      return this;
    }

    public Builder addAll(Multimap<String, String> multimap) {
      builder.putAll(multimap);
      return this;
    }

    public Builder addAll(String thesaurus, Iterable<String> keywords) {
      builder.putAll(thesaurus, keywords);
      return this;
    }

    public Builder addAll(String thesaurus, String... keywords) {
      builder.putAll(thesaurus, keywords);
      return this;
    }

    public KeywordSet build() {
      return new KeywordSet(builder.build());
    }
  }

  /**
   * Returns a KeywordSet builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  private final ImmutableSetMultimap<String, String> multimap;

  private KeywordSet(ImmutableSetMultimap<String, String> multimap) {
    this.multimap = multimap;
  }

  /**
   * Returns an immutable collection of all keywords in this KeyWord set.
   */
  public ImmutableCollection<String> getAllKeywords() {
    return multimap.values();
  }

  /**
   * Returns an immutable set of all thesauri in this keyword set.
   */
  public ImmutableSet<String> getAllThesauri() {
    return multimap.keySet();
  }

  /**
   * Returns an immutable set of keywords for a given thesauri.
   * 
   * @param thesauri the thesauri
   * @return immutable set of keywords for the thesauri
   */
  public ImmutableSet<String> getKeywords(String thesauri) {
    return multimap.get(thesauri);
  }
}