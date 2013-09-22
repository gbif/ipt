package org.gbif.ipt.action.portal;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceActionTest {
  private ResourceAction action;
  private Resource resource;

  @Before
  public void setup() {
    SimpleTextProvider mockTextProvider = mock(SimpleTextProvider.class);
    AppConfig mockCfg = mock(AppConfig.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    VocabulariesManager mockVocabManager = mock(VocabulariesManager.class);
    DataDir dataDir = mock(DataDir.class);

    // mock: vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, Locale.getDefault().getLanguage(), false);
    Map<String, String> ranks = new HashMap<String, String>();
    ranks.put("Class", "http://rs.gbif.org/vocabulary/gbif/rank/class");
    ranks.put("kingdom", "http://rs.gbif.org/vocabulary/gbif/rank/kingdom");
    when(mockVocabManager.getI18nVocab(anyString(), anyString(), anyBoolean())).thenReturn(ranks);

    action = new ResourceAction(mockTextProvider, mockCfg, mockRegistrationManager, mockResourceManager,
      mockVocabManager, dataDir);

    // setup Resource with TaxonomicCoverage with 3 TaxonKeyword
    resource = new Resource();
    resource.setShortname("test_resource");

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
  }

  @Test
  public void testSetOrganizedTaxonomicCoverages() {
    List<OrganizedTaxonomicCoverage> coverages = action.constructOrganizedTaxonomicCoverages(
      resource.getEml().getTaxonomicCoverages());
    assertEquals(1, coverages.size());
    assertEquals(3, coverages.get(0).getKeywords().size());
    assertEquals("Plantae (Plants)", coverages.get(0).getKeywords().get(0).getDisplayNames().get(0));
    assertEquals("Equisetopsida", coverages.get(0).getKeywords().get(1).getDisplayNames().get(0));
    assertEquals("Sedges", coverages.get(0).getKeywords().get(2).getDisplayNames().get(0));
  }
}
