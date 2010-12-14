/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model.factory;

import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.admin.VocabulariesManager;

import com.google.inject.Inject;

import org.apache.commons.digester.Rule;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import java.net.URL;

/**
 * This will call the root of the stack to find the url2thesaurus, and then set the appropriate thesaurus on the
 * extension. Namespaces are completely ignored. The "thesaurus" attribute is searched for and if found, the thesaurus
 * is set if found.
 */
public class ThesaurusHandlingRule extends Rule {
  public static final String ATTRIBUTE_THESAURUS = "thesaurus";
  protected static Logger log = Logger.getLogger(ThesaurusHandlingRule.class);
  private VocabulariesManager vocabManager;

  @Inject
  public ThesaurusHandlingRule(VocabulariesManager vocabManager) {
    super();
    this.vocabManager = vocabManager;
  }

  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {

    for (int i = 0; i < attributes.getLength(); i++) {
      if (ThesaurusHandlingRule.ATTRIBUTE_THESAURUS.equals(attributes.getQName(i))) {
        Vocabulary tv = null;
        try {
          URL vocabURL = new URL(attributes.getValue(i));
          tv = vocabManager.get(vocabURL);
        } catch (Exception e) {
          log.error("Vocabulary with location " + attributes.getValue(i) + " couldnt get hold of: " + e.getMessage(), e);
        }

        if (tv != null) {
          Object extensionPropertyAsObject = getDigester().peek();
          if (extensionPropertyAsObject instanceof ExtensionProperty) {
            ExtensionProperty eProperty = (ExtensionProperty) extensionPropertyAsObject;
            eProperty.setVocabulary(tv);
            log.debug("Vocabulary with URI[" + tv.getUri() + "] added to extension property");
          }
        } else {
          log.warn("No Vocabulary object exists for the URL[" + attributes.getValue(i) + "] so cannot be set");
        }

        break; // since we found the attribute
      }
    }
  }
}
