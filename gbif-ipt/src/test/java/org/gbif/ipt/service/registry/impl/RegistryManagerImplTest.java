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

import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.IptMockBaseTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author markus
 * 
 */
public class RegistryManagerImplTest extends IptMockBaseTest {
  private static final String TIM_UUID = "3780d048-8e18-4c0c-afcd-cb6389df56de";
  private static final String TIM_PASSWORD = "password";

  /**
   * @return
   */
  private Ipt getIpt() {
    Ipt ipt = new Ipt();
    return ipt;
  }

  public RegistryManager getManager() throws ParserConfigurationException, SAXException {
    RegistryManager man = new RegistryManagerImpl(cfg, dataDir, buildHttpUtil(), buildSaxFactory());
    return man;
  }

  public Organisation getTim() {
    Organisation org = new Organisation();
    org.setKey(TIM_UUID);
    org.setName("Tim");
    org.setPassword(TIM_PASSWORD);
    return org;
  }

  @Test
  public void testBuild() {
    try {
      RegistryManager man = getManager();
      // test dev organisation "Tim"
      assertTrue(man.validateOrganisation(TIM_UUID, TIM_PASSWORD));
      assertFalse(man.validateOrganisation(TIM_UUID, "tim"));
      assertFalse(man.validateOrganisation("3780d048-8e18-4c0c-afcd-cb6389df56df", TIM_PASSWORD));

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testWriteReadResource() {
    try {
      RegistryManager man = getManager();
      Resource res = new Resource();
      Organisation tim = getTim();
      Ipt ipt = getIpt();
      ipt.setCreated(new Date());
      ipt.setDescription("a unit test mock IPT");
      ipt.setLanguage("en");
      ipt.setName("Mock IPT");

      // register IPT
      man.registerIPT(ipt, tim);

      // register resource
      UUID uuid = man.register(res, tim, ipt);
      assertTrue(uuid != null);

      // get resource and compare
      // TODO: no registry method for this yet
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
