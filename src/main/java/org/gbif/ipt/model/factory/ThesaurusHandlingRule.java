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

import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Inject;
import org.apache.commons.digester.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;

/**
 * This will call the root of the stack to find the url2thesaurus, and then set the appropriate thesaurus on the
 * extension. Namespaces are completely ignored. The "thesaurus" attribute is searched for and if found, the thesaurus
 * is set if found.
 */
public class ThesaurusHandlingRule extends Rule {

  public static final String ATTRIBUTE_THESAURUS = "thesaurus";
  private static final Logger LOG = LogManager.getLogger(ThesaurusHandlingRule.class);
  private final VocabulariesManager vocabManager;

  @Inject
  public ThesaurusHandlingRule(VocabulariesManager vocabManager) {
    this.vocabManager = vocabManager;
  }

  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {

    for (int i = 0; i < attributes.getLength(); i++) {
      if (ThesaurusHandlingRule.ATTRIBUTE_THESAURUS.equals(attributes.getQName(i))) {
        Vocabulary tv = null;
        try {
          URL url = new URL(attributes.getValue(i));
          tv = vocabManager.get(url);
          // install vocabulary if it's new
          if (tv == null) {
            LOG.warn("Installing new vocabulary with URL (" + attributes.getValue(i) + ")...");
            tv = vocabManager.install(url);
          }
        } catch (MalformedURLException e) {
          LOG.error("Thesaurus URL (" + attributes.getValue(i) + ") is malformed: " + e.getMessage(), e);
        }

        if (tv == null) {
          LOG.error("No Vocabulary object exists for the URL (" + attributes.getValue(i) + ") so cannot be set");
        } else {
          Object extensionPropertyAsObject = getDigester().peek();
          if (extensionPropertyAsObject instanceof ExtensionProperty) {
            ExtensionProperty eProperty = (ExtensionProperty) extensionPropertyAsObject;
            eProperty.setVocabulary(tv);
            LOG.debug("Vocabulary with URI[" + tv.getUriString() + "] added to extension property");
          }
        }

        break; // since we found the attribute
      }
    }
  }
}
