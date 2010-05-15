/*
 * Copyright 2010 Global Biodiversity Informatics Facility.
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

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.tapir.ParseException;
import org.gbif.provider.tapir.filter.Filter;

import java.util.Set;

/**
 * This class provides a service interface that extends {@link GenericManager}
 * for services that are specific to {@link ExtensionProperty} objects.
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

  /**
   * Gets a core {@link ExtensionProperty} by its qualified name.
   * 
   * @param qName the qualified name of the extension
   * @return ExtensionProperty
   */
  ExtensionProperty getCorePropertyByQualName(String qName);

  /**
   * Returns an {@link ExtensionProperty} for a given {@link Extension} and
   * qualified property name.
   * 
   * @param extension the extension to which the extension property belongs
   * @param name the qualified property name
   * @return ExtensionProperty
   */
  ExtensionProperty getProperty(Extension extension, String name);

  /**
   * Gets extension property by its name.
   * 
   * For example, for a given qualified name such as
   * 'http://rs.tdwg.org/dwc/terms/scientificName', the name would be
   * 'scientificName'.
   * 
   * @param name
   * @return ExtensionProperty
   */
  ExtensionProperty getPropertyByName(String name);

  /**
   * Gets an extension property by querying for its qualified name, such as
   * 'http://rs.tdwg.org/dwc/terms/scientificName'. It splits the qualified name
   * into name (scientificName) and namespace (http://rs.tdwg.org/dwc/terms/)
   * for the query.
   * 
   * @param qName
   * @return ExtensionProperty
   */
  ExtensionProperty getPropertyByQualName(String qName);

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
