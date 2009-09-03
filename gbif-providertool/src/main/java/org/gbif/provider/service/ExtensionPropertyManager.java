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
package org.gbif.provider.service;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.tapir.ParseException;
import org.gbif.provider.tapir.filter.Filter;

import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
public interface ExtensionPropertyManager extends
    GenericManager<ExtensionProperty> {
  /**
   * Get a single ExtensionProeprty by its simple name. As there might be more
   * than one core extension using the same name, the ExtensionType narrows down
   * the search to just one core type. In case there are multiple matches a
   * Hibernate exception will be thrown. Extensions other than the core are not
   * being searched.
   * 
   * @param name
   * @param type
   * @return
   */
  ExtensionProperty getCorePropertyByName(String name);

  /**
   * Get a single ExtensionProeprty by its qualified concept name. As there
   * might be more than one core extension using the same qualified concept
   * name, the ExtensionType narrows down the search to just one core type.
   * Extensions other than the core are not being searched.
   * 
   * @param qName
   * @param type
   * @return
   */

  ExtensionProperty getCorePropertyByQualName(String qName);

  /**
   * Iterates through all ComparisonOperators and replaces the existing
   * ExtensionProperties with persistent properties looked up by their qualified
   * name and the type of resource
   * 
   * @param filter the filter to iterate through. Properties will be replaced in
   *          this object
   * @param type the type of (core) extension to narrow down qualified name to
   *          extension property "homonyms"
   * @return
   * @throws ParseException
   */
  Set<ExtensionProperty> lookupFilterCoreProperties(Filter filter)
      throws ParseException;
}
