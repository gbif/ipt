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
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.metadata.eml.Eml;

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
      res.setTitle("Möck rèşürçe wíŧħ ƒũñňÿ ćĥåřæċŧëŗş");
      res.setCreated(new Date());
      res.setSubtype("occurrence test");
      Eml eml = new Eml();
      eml.setAbstract("An IPT unit test resource that can be deleted. Testing unicode characters like ą ć ę ł ń  ś ź ż (for polish) ť ů ž č ď ě ň ř š (for czech) and other taken from http://www.alanflavell.org.uk/unicode/unidata.html  ᠀᠔ᡎᢥ(mongolian) ⅛  Ⅳ ⅸ ↂ (numbers) ∀ ∰ ⊇ ⋩ (maths) CJK Symbols and Punctuation U+3000 – U+303F (12288–12351) 々 〒 〣 〰 Hiragana U+3040 – U+309F (12352–12447) あ ぐ る ゞ Katakana U+30A0 – U+30FF (12448–12543) ア ヅ ヨ ヾ Bopomofo U+3100 – U+312F (12544–12591) ㄆ ㄓ ㄝ ㄩ Hangul Compatibility Jamo U+3130 – U+318F (12592–12687) ㄱ ㄸ ㅪ ㆍ Kanbun U+3190 – U+319F (12688–12703) ㆐ ㆕ ㆚ ㆟ Bopomofo Extended U+31A0 – U+32BF (12704–12735) ㆠ ㆧ ㆯ ㆷ Katakana Phonetic Extensions U+31F0 – U+31FF (12784–12799) ㇰ ㇵ ㇺ ㇿ Enclosed CJK Letters and Months U+3200 – U+32FF (12800–13055) ㈔ ㈲ ㊧ ㋮ CJK Compatibility U+3300 – U+33FF (13056–13311) ㌃ ㍻ ㎡ ㏵ CJK Unified Ideographs Extension A U+3400 – U+4DB5 (13312–19893) 㐅 㒅 㝬 㿜 Yijing Hexagram Symbols U+4DC0 – U+4DFF (19904–19967) ䷂ ䷫ ䷴ ䷾ CJK Unified Ideographs U+4E00 – U+9FFF (19968–40959) 一 憨 田 龥 Yi Syllables U+A000 – U+A48F (40960–42127) ꀀ ꅴ ꊩ ꒌ Yi Radicals U+A490 – U+A4CF (42128–42191) ꒐ ꒡ ꒰ ꓆ ");
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
