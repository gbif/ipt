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

import org.gbif.provider.model.Extension;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO: Documentation.
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class ExtensionManagerTest extends ContextAwareTestBase {
  @Autowired
  protected ExtensionManager extensionManager;

  @Test
  public void testGetConceptVocabularyString() {
    Extension dwc = extensionManager.get(1L);
    assertTrue(dwc != null);
    // System.out.println(dwc);
    // System.out.println(dwc.getProperties());
  }
}
