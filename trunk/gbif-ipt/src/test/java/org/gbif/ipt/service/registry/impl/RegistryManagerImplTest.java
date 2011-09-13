/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.metadata.eml.Eml;

import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author markus
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
    RegistryManager man = new RegistryManagerImpl(cfg, dataDir, buildHttpClient(), buildSaxFactory());
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
      System.out.println("******************************");
      System.out.println(cfg.getRegistryUrl());
      System.out.println("******************************");
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
      Organisation tim = getTim();

      Ipt ipt = getIpt();
      ipt.setCreated(new Date());
      ipt.setDescription("a unit test mock IPT");
      ipt.setPrimaryContactEmail("gbif@mailinator.com");
      ipt.setPrimaryContactType("technical");
      ipt.setLanguage("en");
      ipt.setName("Mock IPT");

      // register IPT
      ipt.setKey(man.registerIPT(ipt, tim));

      // register resource
      Resource res = new Resource();
      User user = new User();
      user.setFirstname("Mock Name");
      user.setEmail("mocking@themock.org");
      res.setCreator(user);
      res.setShortname("mock");
      res.setTitle("MÃ¶ck rÃ¨ÅŸÃ¼rÃ§e wÃ­Å§Ä§ Æ’Å©Ã±ÅˆÃ¿ Ä‡Ä¥Ã¥Å™Ã¦Ä‹Å§Ã«Å—ÅŸ");
      res.setCreated(new Date());
      res.setSubtype("occurrence test");
      Eml eml = new Eml();
      eml
        .setAbstract("An IPT unit test resource that can be deleted. Testing unicode characters like Ä… Ä‡ Ä™ Å‚ Å„  Å› Åº Å¼ (for polish) Å¥ Å¯ Å¾ Ä� Ä� Ä› Åˆ Å™ Å¡ (for czech) and other taken from http://www.alanflavell.org.uk/unicode/unidata.html  á €á ”á¡Žá¢¥(mongolian) â…›  â…£ â…¸ â†‚ (numbers) âˆ€ âˆ° âŠ‡ â‹© (maths) CJK Symbols and Punctuation U+3000 â€“ U+303F (12288â€“12351) ã€… ã€’ ã€£ ã€° Hiragana U+3040 â€“ U+309F (12352â€“12447) ã�‚ ã�� ã‚‹ ã‚ž Katakana U+30A0 â€“ U+30FF (12448â€“12543) ã‚¢ ãƒ… ãƒ¨ ãƒ¾ Bopomofo U+3100 â€“ U+312F (12544â€“12591) ã„† ã„“ ã„� ã„© Hangul Compatibility Jamo U+3130 â€“ U+318F (12592â€“12687) ã„± ã„¸ ã…ª ã†� Kanbun U+3190 â€“ U+319F (12688â€“12703) ã†� ã†• ã†š ã†Ÿ Bopomofo Extended U+31A0 â€“ U+32BF (12704â€“12735) ã†  ã†§ ã†¯ ã†· Katakana Phonetic Extensions U+31F0 â€“ U+31FF (12784â€“12799) ã‡° ã‡µ ã‡º ã‡¿ Enclosed CJK Letters and Months U+3200 â€“ U+32FF (12800â€“13055) ãˆ” ãˆ² ãŠ§ ã‹® CJK Compatibility U+3300 â€“ U+33FF (13056â€“13311) ãŒƒ ã�» ãŽ¡ ã�µ CJK Unified Ideographs Extension A U+3400 â€“ U+4DB5 (13312â€“19893) ã�… ã’… ã�¬ ã¿œ Yijing Hexagram Symbols U+4DC0 â€“ U+4DFF (19904â€“19967) ä·‚ ä·« ä·´ ä·¾ CJK Unified Ideographs U+4E00 â€“ U+9FFF (19968â€“40959) ä¸€ æ†¨ ç”° é¾¥ Yi Syllables U+A000 â€“ U+A48F (40960â€“42127) ê€€ ê…´ êŠ© ê’Œ Yi Radicals U+A490 â€“ U+A4CF (42128â€“42191) ê’� ê’¡ ê’° ê“† ");
      res.setEml(eml);

      res.setLastPublished(new Date());
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
