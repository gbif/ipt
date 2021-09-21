package org.gbif.ipt.service.registry.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.metadata.eml.Eml;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests hitting sandbox registry (legacy) web services.
 */
@Ignore("These require live UAT webservice and should therefore only run when manually triggered")
public class RegistryManagerImplIT extends IptMockBaseTest {

  // logging
  private static final Logger LOG = LogManager.getLogger(RegistryManagerImplTest.class);

  // organisation below must exist in sandbox registry!
  private static final String ORGANISATION_UUID = "62922b92-69d1-4c4b-831c-b23d5412a124";
  private static final String ORGANISATION_PASSWORD = "password";
  private static final String ORGANISATION_NAME = "Test Organisation Jenkins";

  private RegistryManager manager;
  private ResourceManager resourceManager;

  @Before
  public void setup() throws SAXException, ParserConfigurationException {
    ConfigWarnings mockConfigWarnings = mock(ConfigWarnings.class);
    SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    resourceManager = mock(ResourceManager.class);

    // manager that issues real http requests
    manager =
      new RegistryManagerImpl(cfg, dataDir, buildHttpClient(), buildSaxFactory(), mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager, resourceManager);
  }

  @Test
  public void testRegisterResource() {
    try {
      // construct organisation
      Organisation organisation = new Organisation();
      organisation.setKey(ORGANISATION_UUID);
      organisation.setName(ORGANISATION_NAME);
      organisation.setPassword(ORGANISATION_PASSWORD);

      // construct IPT
      Ipt ipt = new Ipt();
      ipt.setCreated(new Date());
      ipt.setDescription("a unit test mock IPT");
      ipt.setPrimaryContactName("Ms. IPT Admin");
      ipt.setPrimaryContactEmail("gbif@mailinator.com");
      ipt.setPrimaryContactType("technical");
      ipt.setLanguage("en");
      ipt.setName("Mock IPT");
      ipt.setWsPassword("wsPassword");

      // register IPT
      String iptKey = manager.registerIPT(ipt, organisation);
      LOG.info("IPT registered successfully, key=" + iptKey);
      ipt.setKey(iptKey);
      ipt.setOrganisationKey(ORGANISATION_UUID);

      // construct resource
      Resource res = new Resource();
      User user = new User();
      user.setFirstname("Mock Name");
      user.setEmail("mocking@themock.org");
      res.setCreator(user);
      res.setShortname("mock");
      res.setTitle("Möck rèşürçe wíŧħ ƒũñňÿ ćĥåřæċŧëŗş");
      res.setCreated(new Date());
      res.setSubtype("occurrence test");

      // mock assigning a DOI to the resource. To be assigned/registered, the last published version DOI must be public
      VersionHistory history = new VersionHistory(new BigDecimal("1.1"), new Date(), PublicationStatus.PUBLIC);
      history.setModifiedBy(user);
      DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, "10.5072");
      history.setDoi(doi);
      history.setStatus(IdentifierStatus.PUBLIC);
      res.setDoi(doi);
      res.setIdentifierStatus(IdentifierStatus.PUBLIC);
      res.addVersionHistory(history);
      assertTrue(res.isAlreadyAssignedDoi());
      res.setLastPublished(new Date());

      Eml eml = new Eml();
      List<String> description = new ArrayList<>();
      description.add(
        "An IPT unit test resource that can be deleted. Testing unicode characters like ą ć ę ł ń  ś ź ż (for polish) ť ů ž č ď ě ň ř š (for czech) and other taken from http://www.alanflavell.org.uk/unicode/unidata.html  ᠀᠔ᡎᢥ(mongolian) ⅛  Ⅳ ⅸ ↂ (numbers) ∀ ∰ ⊇ ⋩ (maths) CJK Symbols and Punctuation U+3000 – U+303F (12288–12351) 々 〒 〣 〰 Hiragana U+3040 – U+309F (12352–12447) あ ぐ る ゞ Katakana U+30A0 – U+30FF (12448–12543) ア ヅ ヨ ヾ Bopomofo U+3100 – U+312F (12544–12591) ㄆ ㄓ ㄝ ㄩ Hangul Compatibility Jamo U+3130 – U+318F (12592–12687) ㄱ ㄸ ㅪ ㆍ Kanbun U+3190 – U+319F (12688–12703) ㆐ ㆕ ㆚ ㆟ Bopomofo Extended U+31A0 – U+32BF (12704–12735) ㆠ ㆧ ㆯ ㆷ Katakana Phonetic Extensions U+31F0 – U+31FF (12784–12799) ㇰ ㇵ ㇺ ㇿ Enclosed CJK Letters and Months U+3200 – U+32FF (12800–13055) ㈔ ㈲ ㊧ ㋮ CJK Compatibility U+3300 – U+33FF (13056–13311) ㌃ ㍻ ㎡ ㏵ CJK Unified Ideographs Extension A U+3400 – U+4DB5 (13312–19893) 㐅 㒅 㝬 㿜 Yijing Hexagram Symbols U+4DC0 – U+4DFF (19904–19967) ䷂ ䷫ ䷴ ䷾ CJK Unified Ideographs U+4E00 – U+9FFF (19968–40959) 一 憨 田 龥 Yi Syllables U+A000 – U+A48F (40960–42127) ꀀ ꅴ ꊩ ꒌ Yi Radicals U+A490 – U+A4CF (42128–42191) ꒐ ꒡ ꒰ ꓆ ");
      eml.setAbstract(description);
      res.setEml(eml);

      // register resource
      UUID uuid = manager.register(res, organisation, ipt);
      assertNotNull(uuid);
      LOG.info("Resource registered successfully, key=" + uuid.toString());

      // apply result of registration
      res.setKey(uuid);
      res.setStatus(PublicationStatus.REGISTERED);

      // update resource
      assertTrue(res.isRegistered());
      manager.updateResource(res, iptKey);

      // mock resourceManager returning registered resource in list of resources
      List<Resource> registeredResources = new ArrayList<>();
      registeredResources.add(res);
      when(resourceManager.list(PublicationStatus.REGISTERED)).thenReturn(registeredResources);

      // update IPT, which updates IPT registration and all registered resources registrations also
      manager.updateIpt(ipt);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testValidateOrganisation() {
    try {
      // validate organisation "Test Organisation Jenkins" exists in sandbox registry
      assertTrue(manager.validateOrganisation(ORGANISATION_UUID, ORGANISATION_PASSWORD));
      // ensure validate using invalid key or invalid password works as expected
      assertFalse(manager.validateOrganisation("INVALID92-69d1-4c4b-831c-b23d5412a124", ORGANISATION_PASSWORD));
      assertFalse(manager.validateOrganisation(ORGANISATION_UUID, "INVALID"));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
