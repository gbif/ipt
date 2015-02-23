package org.gbif.ipt.action.portal;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceActionTest {

  private ResourceAction action;
  private Resource resource;
  private static final String RESOURCE_SHORT_NAME = "test_resource";
  private static final BigDecimal LATEST_RESOURCE_VERSION = new BigDecimal("3.0");
  private static final BigDecimal RESOURCE_VERSION_TWO = new BigDecimal("2.0");
  private static final BigDecimal RESOURCE_VERSION_ONE = new BigDecimal("1.0");

  private static User MANAGER;

  @Before
  public void setup() throws IOException, TemplateException {
    SimpleTextProvider textProvider = new SimpleTextProvider();
    AppConfig mockCfg = mock(AppConfig.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    VocabulariesManager mockVocabManager = mock(VocabulariesManager.class);
    DataDir mockDataDir = mock(DataDir.class);

    // mock: vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, Locale.getDefault().getLanguage(), false);
    Map<String, String> ranks = new HashMap<String, String>();
    ranks.put("Class", "http://rs.gbif.org/vocabulary/gbif/rank/class");
    ranks.put("kingdom", "http://rs.gbif.org/vocabulary/gbif/rank/kingdom");
    when(mockVocabManager.getI18nVocab(anyString(), anyString(), anyBoolean())).thenReturn(ranks);

    // setup Resource with TaxonomicCoverage with 3 TaxonKeyword
    resource = new Resource();
    resource.setShortname(RESOURCE_SHORT_NAME);
    resource.setEmlVersion(LATEST_RESOURCE_VERSION);

    // setup manager as resource creator
    MANAGER = new User();
    MANAGER.setEmail("jc@gbif.org");
    MANAGER.setLastname("Costa");
    MANAGER.setFirstname("Jose");
    MANAGER.setRole(User.Role.Manager);
    resource.setCreator(MANAGER);

    // add three published versions to version history, all published by manager, some private, other public
    VersionHistory v1 = new VersionHistory(RESOURCE_VERSION_ONE, new Date(), PublicationStatus.PRIVATE);
    resource.addVersionHistory(v1);
    VersionHistory v2 = new VersionHistory(RESOURCE_VERSION_TWO, new Date(), PublicationStatus.PUBLIC);
    resource.addVersionHistory(v2);
    VersionHistory v3 = new VersionHistory(LATEST_RESOURCE_VERSION, new Date(), PublicationStatus.PRIVATE);
    resource.addVersionHistory(v3);
    assertEquals(3, resource.getVersionHistory().size());

    TaxonomicCoverage coverage1 = new TaxonomicCoverage();
    coverage1.setDescription("Description1");

    TaxonKeyword keyword1 = new TaxonKeyword();
    keyword1.setRank("Kingdom");
    keyword1.setCommonName("Plants");
    keyword1.setScientificName("Plantae");

    TaxonKeyword keyword2 = new TaxonKeyword();
    keyword2.setRank("Class");
    keyword2.setScientificName("Equisetopsida");

    TaxonKeyword keyword3 = new TaxonKeyword();
    keyword3.setCommonName("Sedges");

    List<TaxonKeyword> keywordList = new ArrayList<TaxonKeyword>();
    keywordList.add(keyword1);
    keywordList.add(keyword2);
    keywordList.add(keyword3);
    coverage1.setTaxonKeywords(keywordList);

    List<TaxonomicCoverage> coverages = new ArrayList<TaxonomicCoverage>();
    coverages.add(coverage1);
    resource.getEml().setTaxonomicCoverages(coverages);

    // mock returning Eml file, with actual resource metadata (Eml)
    File tmpEmlFile = File.createTempFile("eml", ".xml");
    EmlWriter.writeEmlFile(tmpEmlFile, resource.getEml());
    when(mockDataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(tmpEmlFile);

    // mock returning DwC-A file, with file that doesn't exist. This means the resource is metadata-only
    File nonExistingDwca = new File("dwca", ".zip");
    assertFalse(nonExistingDwca.exists());
    when(mockDataDir.resourceDwcaFile(anyString(), any(BigDecimal.class))).thenReturn(nonExistingDwca);

    action = new ResourceAction(textProvider, mockCfg, mockRegistrationManager, mockResourceManager, mockVocabManager,
      mockDataDir);
    action.setResource(resource);
  }

  @Test
  public void testSetOrganizedTaxonomicCoverages() {
    List<OrganizedTaxonomicCoverage> coverages =
      action.constructOrganizedTaxonomicCoverages(resource.getEml().getTaxonomicCoverages());
    assertEquals(1, coverages.size());
    assertEquals(3, coverages.get(0).getKeywords().size());
    assertEquals("Plantae (Plants)", coverages.get(0).getKeywords().get(0).getDisplayNames().get(0));
    assertEquals("Equisetopsida", coverages.get(0).getKeywords().get(1).getDisplayNames().get(0));
    assertEquals("Sedges", coverages.get(0).getKeywords().get(2).getDisplayNames().get(0));
  }

  @Test
  public void testFindDoiAssignedToPublishedVersion() {
    action.setVersion(new BigDecimal("1.34"));

    // DOI must be PUBLIC to be assigned
    VersionHistory history = new VersionHistory(new BigDecimal("1.34"), new Date(), PublicationStatus.PUBLIC);
    history.setModifiedBy(MANAGER);
    history.setStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    history.setDoi(new DOI("10.1126", "IO65467"));
    resource.addVersionHistory(history);
    action.setResource(resource);

    assertNull(action.findDoiAssignedToPublishedVersion());

    // Change DOI to PUBLIC now, and ensure it is returned
    resource.removeVersionHistory(new BigDecimal("1.34"));
    history.setStatus(IdentifierStatus.PUBLIC);
    resource.addVersionHistory(history);

    assertEquals("10.1126/io65467", action.findDoiAssignedToPublishedVersion().getDoiName());
  }

  /**
   * When manager is not logged in, ensure published REGISTERED versions shown publicly.
   */
  @Test
  public void testDetailForPublishedRegisteredVersion() {
    // simulate pre v2.2 resource that is registered and has no VersionHistory
    action.getResource().setKey(UUID.randomUUID());
    action.getResource().setStatus(PublicationStatus.REGISTERED);
    action.getResource().getVersionHistory().clear();
    assertTrue(action.getResource().isRegistered());
    assertTrue(action.getResource().getVersionHistory().isEmpty());
    assertEquals(LATEST_RESOURCE_VERSION, action.getResource().getEmlVersion());

    // the last published version was registered and therefore can be shown publicly
    assertEquals(BaseAction.SUCCESS, action.detail());
  }

  /**
   * When manager is not logged in, ensure published PRIVATE versions not shown publicly.
   */
  @Test
  public void testDetailForPublishedPrivateVersion() {
    // the last published version (3.0) was private at the time of publication
    assertEquals(BaseAction.NOT_ALLOWED, action.detail());
  }

  /**
   * When manager is not logged in, ensure specific published PUBLIC versions are shown publicly.
   */
  @Test
  public void testDetailForSpecificPublishedPublicVersion() {
    // the published version 2.0 was public at time of publication
    action.setVersion(RESOURCE_VERSION_TWO);
    assertEquals(BaseAction.SUCCESS, action.detail());
  }

  /**
   * When manager is not logged in, ensure specific published PRIVATE versions are shown publicly.
   */
  @Test
  public void testDetailForSpecificPrivatePublicVersion() {
    // the published version 1.0 was private at time of publication
    action.setVersion(RESOURCE_VERSION_ONE);
    assertEquals(BaseAction.NOT_ALLOWED, action.detail());
  }

  /**
   * When manager IS not logged in, ensure ALL published versions are shown.
   */
  @Test
  public void testDetailForLoggedInManager() {
    // simulate manager being logged in
    Map<String, Object> session = new HashMap<String, Object>();
    session.put(Constants.SESSION_USER, MANAGER);
    action.setSession(session);

    assertNotNull(action.getCurrentUser());

    // ensure all versions available to manager
    action.setVersion(RESOURCE_VERSION_ONE);
    assertEquals(BaseAction.SUCCESS, action.detail());
    // ensure warning was generated (about this resource being private and not available to everyone)
    assertEquals(1, action.getActionWarnings().size());

    action.setVersion(RESOURCE_VERSION_TWO);
    assertEquals(BaseAction.SUCCESS, action.detail());

    action.setVersion(LATEST_RESOURCE_VERSION);
    assertEquals(BaseAction.SUCCESS, action.detail());
  }
}
