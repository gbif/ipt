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
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionPropertyManager;
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

  @Autowired
  private ExtensionManager extensionManager;

  /**
   * Test getting a property by {@link Exception} and qualified name.
   * 
   * void
   */
  @Test
  public void testGetPropertyByName() {
    Extension extension = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
    String name = "http://rs.tdwg.org/dwc/terms/scientificName";
    ExtensionProperty p = extensionPropertyManager.getProperty(extension, name);
    assertNotNull(p);
    assertEquals(p.getExtension(), extension);
  }
}
