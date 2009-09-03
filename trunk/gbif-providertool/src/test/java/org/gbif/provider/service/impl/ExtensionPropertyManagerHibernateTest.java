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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.tapir.filter.Filter;
import org.gbif.provider.tapir.filter.Like;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class ExtensionPropertyManagerHibernateTest extends ContextAwareTestBase {
  @Autowired
  private ExtensionPropertyManager extensionPropertyManager;

  @Test
  public void testFindProperty() throws Exception {
    ExtensionProperty p = extensionPropertyManager.getCorePropertyByQualName(Constants.SCIENTIFIC_NAME_QUALNAME);
    assertTrue(p != null);

    p = extensionPropertyManager.getCorePropertyByName("ScientificName");
    assertTrue(p != null);
  }

  @Test
  public void testPropertyReplace() throws Exception {
    Filter f = new Filter();
    Like like = new Like();
    like.setProperty(Constants.SCIENTIFIC_NAME_QUALNAME);
    like.setValue("Abies*");
    f.setRoot(like);
    extensionPropertyManager.lookupFilterCoreProperties(f);
    System.out.println(f);
  }

}
