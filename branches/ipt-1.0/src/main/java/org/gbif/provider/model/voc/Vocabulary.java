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
package org.gbif.provider.model.voc;

/**
 * TODO: Documentation.
 * 
 */
public enum Vocabulary {
  Language("http://iso.org/639-1"), Country("http://iso.org/iso3166"), NomenclaturalStatus(
      "http://rs.tdwg.org/ontology/voc/NomenclaturalStatus"), DarwinCoreTypes(
      "http://rs.tdwg.org/dwc/dwctype/"), TaxonomicStatus(
      "http://rs.tdwg.org/ontology/voc/TaxonomicStatus"), ResourceType(
      "http://rs.gbif.org/gbrds/resourceType");

  public String uri;

  private Vocabulary(String vocabularyUri) {
    this.uri = vocabularyUri;
  }
}
