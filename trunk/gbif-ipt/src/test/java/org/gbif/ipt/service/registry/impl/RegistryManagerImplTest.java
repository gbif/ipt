/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.service.registry.impl;

import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.IptMockBaseTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author markus
 * 
 */
public class RegistryManagerImplTest extends IptMockBaseTest {
  public RegistryManager getManager() throws ParserConfigurationException, SAXException {
    RegistryManager man = new RegistryManagerImpl(cfg, dataDir, buildHttpUtil(), buildSaxFactory());
    return man;
  }

  @Test
  public void testBuild() {
    try {
      RegistryManager man = getManager();
      // test dev organisation "Tim"
      assertTrue(man.validateOrganisation("3780d048-8e18-4c0c-afcd-cb6389df56de", "password"));
      assertFalse(man.validateOrganisation("3780d048-8e18-4c0c-afcd-cb6389df56de", "tim"));
      assertFalse(man.validateOrganisation("3780d048-8e18-4c0c-afcd-cb6389df56df", "password"));

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
