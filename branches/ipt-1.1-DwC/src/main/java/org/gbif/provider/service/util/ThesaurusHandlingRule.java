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
package org.gbif.provider.service.util;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ThesaurusVocabulary;

import org.apache.commons.digester.Rule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * This will call the root of the stack to find the url2thesaurus, and then set
 * the appropriate thesaurus on the extension. Namespaces are completely
 * ignored. The "thesaurus" attribute is searched for and if found, the
 * thesaurus is set if found.
 * 
 */
public class ThesaurusHandlingRule extends Rule {
  public static final String ATTRIBUTE_THESAURUS = "thesaurus";
  protected static Log log = LogFactory.getLog(ThesaurusHandlingRule.class);

  @SuppressWarnings("unchecked")
  @Override
  public void begin(String namespace, String name, Attributes attributes)
      throws Exception {

    Object mapAsObject = getDigester().getRoot();
    Map<String, ThesaurusVocabulary> url2ThesaurusMap = null;
    if (mapAsObject instanceof HashMap) {
      url2ThesaurusMap = (Map) mapAsObject;

      for (int i = 0; i < attributes.getLength(); i++) {
        if (ThesaurusHandlingRule.ATTRIBUTE_THESAURUS.equals(attributes.getLocalName(i))) {
          ThesaurusVocabulary tv = url2ThesaurusMap.get(attributes.getValue(i));
          log.info("Thesaurus [" + attributes.getValue(i)
              + "] has ThesaurusVocabulary[" + tv + "]");

          if (tv != null) {
            Object extensionPropertyAsObject = getDigester().peek();
            if (extensionPropertyAsObject instanceof ExtensionProperty) {
              log.info("Thesaurus [" + tv + "] successfully added");
              ((ExtensionProperty) extensionPropertyAsObject).setVocabulary(tv);
              log.info("Thesaurus with ID[" + tv.getId()
                  + "] added to property: " + tv.getTitle());
            }
          } else {
            log.warn("No Thesaurus Object exists for the URL["
                + attributes.getValue(i) + "] so cannot be set");
          }

          break; // since we found the attribute
        }
      }
    } else if (mapAsObject == null) {
      log.error("Root of SAX parse stack is NOT as expected.  Should be a Map<String, ThesaurusVocabulary> but is null.  No thesaurus can be set");
    } else {
      log.error("Root of SAX parse stack is NOT as expected.  Should be a Map<String, ThesaurusVocabulary> but is: "
          + mapAsObject.getClass() + ".  No thesaurus can be set");
    }
  }
}
