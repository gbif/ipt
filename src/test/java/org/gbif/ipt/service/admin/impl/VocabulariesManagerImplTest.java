package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VocabulariesManagerImplTest {

  private static File TMP_DIR;
  private VocabulariesManager manager;
  private DataDir dataDir;
  private AppConfig appConfig;

  @BeforeClass
  public static void beforeClassSetup() {
    TMP_DIR = org.gbif.ipt.utils.FileUtils.createTempDir();
  }

  @Before
  public void setup() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
    dataDir = mock(DataDir.class);
    appConfig = new AppConfig(dataDir);
    ConfigWarnings warnings = new ConfigWarnings();

    Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());
    SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
    VocabularyFactory vocabularyFactory = new VocabularyFactory(saxf);

    // construct mock RegistryManager:
    // mock getVocabularies() response from Registry with local test resource (list of vocabularies from thesauri_sandbox.json)
    HttpClient mockHttpClient = mock(HttpClient.class);
    ExtendedResponse mockResponse = mock(ExtendedResponse.class);
    mockResponse.setContent(
      IOUtils.toString(
          ExtensionManagerImplTest.class.getResourceAsStream("/responses/thesauri_sandbox.json"),
          StandardCharsets.UTF_8));
    when(mockHttpClient.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager mockRegistryManager =
      new RegistryManagerImpl(appConfig, dataDir, mockHttpClient, saxf, warnings, mock(SimpleTextProvider.class),
        mock(RegistrationManager.class), mock(ResourceManager.class));

    assertTrue(TMP_DIR.isDirectory());

    // copy vocabulary file to temporary directory
    File newRanksVoc = FileUtils.getClasspathFile("thesauri/rank_2015-04-24.xml");
    File datasetTypeVoc = FileUtils.getClasspathFile("thesauri/dataset_type.xml");
    File languageVoc = FileUtils.getClasspathFile("thesauri/639-2.xml");
    File countryVoc = FileUtils.getClasspathFile("thesauri/3166-1.xml");
    File roleVoc = FileUtils.getClasspathFile("thesauri/agent_role.xml");
    File frequencyVoc = FileUtils.getClasspathFile("thesauri/update_frequency.xml");
    File methodsVoc = FileUtils.getClasspathFile("thesauri/preservation_method.xml");
    File subtypesVoc = FileUtils.getClasspathFile("thesauri/dataset_subtype.xml");

    org.apache.commons.io.FileUtils.copyFileToDirectory(newRanksVoc, TMP_DIR);
    org.apache.commons.io.FileUtils.copyFileToDirectory(datasetTypeVoc, TMP_DIR);
    org.apache.commons.io.FileUtils.copyFileToDirectory(languageVoc, TMP_DIR);
    org.apache.commons.io.FileUtils.copyFileToDirectory(countryVoc, TMP_DIR);
    org.apache.commons.io.FileUtils.copyFileToDirectory(roleVoc, TMP_DIR);
    org.apache.commons.io.FileUtils.copyFileToDirectory(frequencyVoc, TMP_DIR);
    org.apache.commons.io.FileUtils.copyFileToDirectory(methodsVoc, TMP_DIR);
    org.apache.commons.io.FileUtils.copyFileToDirectory(subtypesVoc, TMP_DIR);

    File tmpNewRankVoc = new File(TMP_DIR, "rank_2015-04-24.xml");
    assertTrue(tmpNewRankVoc.exists());
    File tmpDatasetTypeVoc = new File(TMP_DIR, "dataset_type.xml");
    File tmpLanguageVoc = new File(TMP_DIR, "639-2.xml");
    File tmpCountryVoc = new File(TMP_DIR, "3166-1.xml");
    File tmpRoleVoc = new File(TMP_DIR, "agent_role.xml");
    File tmpFrequencyVoc = new File(TMP_DIR, "update_frequency.xml");
    File tmpMethodVoc = new File(TMP_DIR, "preservation_method.xml");
    File tmpSubtypeVoc = new File(TMP_DIR, "dataset_subtype.xml");

    // mock returning temporary files when looked up by their 'safe' filenames
    when(dataDir.tmpFile("http_rs_gbif_org_sandbox_vocabulary_gbif_rank_2015-04-24_xml.xml")).thenReturn(tmpNewRankVoc);
    when(dataDir.tmpFile("http_rs_gbif_org_vocabulary_gbif_dataset_type_xml.xml")).thenReturn(tmpDatasetTypeVoc);
    when(dataDir.tmpFile("http_rs_gbif_org_vocabulary_iso_639-2_xml.xml")).thenReturn(tmpLanguageVoc);
    when(dataDir.tmpFile("http_rs_gbif_org_vocabulary_iso_3166-1_alpha2_xml.xml")).thenReturn(tmpCountryVoc);
    when(dataDir.tmpFile("http_rs_gbif_org_vocabulary_gbif_agent_role_xml.xml")).thenReturn(tmpRoleVoc);
    when(dataDir.tmpFile("http_rs_gbif_org_vocabulary_eml_update_frequency_xml.xml")).thenReturn(tmpFrequencyVoc);
    when(dataDir.tmpFile("http_rs_gbif_org_vocabulary_gbif_preservation_method_xml.xml")).thenReturn(tmpMethodVoc);
    when(dataDir.tmpFile("http_rs_gbif_org_vocabulary_gbif_dataset_subtype_xml.xml")).thenReturn(tmpSubtypeVoc);

    // mock returning newly created and installed vocabulary files
    File rankInstalled = new File(TMP_DIR, "http_rs_gbif_org_vocabulary_gbif_rank.vocab");
    File datasetTypeInstalled = new File(TMP_DIR, "http_rs_gbif_org_vocabulary_gbif_datasetType.vocab");
    File languageInstalled = new File(TMP_DIR, "http_iso_org_639-2.vocab");
    File countryInstalled = new File(TMP_DIR, "http_iso_org_iso3166-1_alpha2.vocab");
    File roleInstalled = new File(TMP_DIR, "http_rs_gbif_org_vocabulary_gbif_agentRole.vocab");
    File frequencyInstalled = new File(TMP_DIR, "http_rs_gbif_org_vocabulary_eml_updateFrequency.vocab");
    File methodInstalled = new File(TMP_DIR, "http_rs_gbif_org_vocabulary_gbif_preservation_method.vocab");
    File subtypeInstalled = new File(TMP_DIR, "http_rs_gbif_org_vocabulary_gbif_datasetSubtype.vocab");

    when(dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/http_rs_gbif_org_sandbox_vocabulary_gbif_rank_2015-04-24_xml.vocab"))
      .thenReturn(rankInstalled);
    when(
      dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/http_rs_gbif_org_vocabulary_gbif_dataset_type_xml.vocab"))
      .thenReturn(datasetTypeInstalled);
    when(dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/http_rs_gbif_org_vocabulary_iso_639-2_xml.vocab"))
      .thenReturn(languageInstalled);
    when(dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/http_rs_gbif_org_vocabulary_iso_3166-1_alpha2_xml.vocab"))
      .thenReturn(countryInstalled);
    when(
      dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/http_rs_gbif_org_vocabulary_gbif_agent_role_xml.vocab"))
      .thenReturn(roleInstalled);
    when(dataDir
      .configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/http_rs_gbif_org_vocabulary_eml_update_frequency_xml.vocab"))
      .thenReturn(frequencyInstalled);
    when(dataDir.configFile(
      VocabulariesManagerImpl.CONFIG_FOLDER + "/http_rs_gbif_org_vocabulary_gbif_preservation_method_xml.vocab"))
      .thenReturn(methodInstalled);
    when(dataDir
      .configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/http_rs_gbif_org_vocabulary_gbif_dataset_subtype_xml.vocab"))
      .thenReturn(subtypeInstalled);

    // Mock downloading vocabulary into tmpFile - we're cheating by handling the actual file already as if it
    // were downloaded already. Furthermore, mock download() response with StatusLine with 200 OK response code
    StatusLine sl = mock(StatusLine.class);
    when(sl.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(mockHttpClient.download(any(URL.class), any(File.class))).thenReturn(sl);

    manager =
      new VocabulariesManagerImpl(appConfig, dataDir, vocabularyFactory, mockHttpClient, mockRegistryManager, warnings,
        mock(SimpleTextProvider.class), mock(RegistrationManager.class));
  }

  /**
   * Test installing default vocabularies, and ensuring they are the latest versions. After, mock having installed
   * an out-of-date vocabulary, and test updating default vocabularies to the latest versions.
   */
  @Test
  public void testInstallDefaults() throws IOException {
    assertTrue(manager.list().isEmpty());
    manager.installOrUpdateDefaults();
    assertFalse(manager.list().isEmpty());

    // verify all installed vocabularies use latest version
    for (Vocabulary v : manager.list()) {
      assertTrue(v.isLatest());
    }

    // mock installing out-of-date rank vocabulary
    for (Vocabulary v : manager.list()) {
      if (v.getUriString().equalsIgnoreCase(Constants.VOCAB_URI_RANKS)) {
        v.setIssued(null);
        v.setLatest(false);
      }
    }

    // once again, prepare mock downloaded file since the earlier version was moved above
    File newRanksVoc = FileUtils.getClasspathFile("thesauri/rank_2015-04-24.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(newRanksVoc, TMP_DIR);
    File tmpNewRankVoc = new File(TMP_DIR, "rank_2015-04-24.xml");
    when(dataDir.tmpFile("http_rs_gbif_org_sandbox_vocabulary_gbif_rank_2015-04-24_xml.xml")).thenReturn(tmpNewRankVoc);

    // try updating all default installed vocabularies to use latest version
    manager.installOrUpdateDefaults();

    // verify all installed vocabularies use latest version
    for (Vocabulary v : manager.list()) {
      assertTrue(v.isLatest());
    }
  }

  /**
   * Test loading vocabularies into memory.
   */
  @Test
  public void testLoad() throws IOException {
    File vocabDir = new File(TMP_DIR, VocabulariesManagerImpl.CONFIG_FOLDER);
    assertTrue(vocabDir.mkdir());
    assertTrue(vocabDir.isDirectory());

    // add vocabulary to directory
    File ranksVoc = FileUtils.getClasspathFile("thesauri/rank.xml");
    File renamed = new File(vocabDir, "http_rs_gbif_org_vocabulary_gbif_rank_xml.vocab");
    org.apache.commons.io.FileUtils.copyFile(ranksVoc, renamed);
    assertTrue(renamed.exists());
    assertEquals(1, vocabDir.listFiles().length);
    when(dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER)).thenReturn(vocabDir);

    assertTrue(manager.list().isEmpty());
    assertEquals(1, manager.load());
    assertFalse(manager.list().isEmpty());

    Vocabulary v = manager.get("http://rs.gbif.org/vocabulary/gbif/rank");
    assertEquals("Taxonomic Rank GBIF Vocabulary", v.getTitle());
    assertEquals("http://rs.gbif.org/vocabulary/gbif/rank.xml", v.getUriResolvable().toString());
  }

  // TODO: 20/09/2021 empty test
  @Test
  public void testUpdateIfChanged() {

  }
}
