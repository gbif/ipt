package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.file.FileUtils;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VocabulariesManagerImplTest {
  private Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());

  // construct VocabularyFactory
  private DefaultHttpClient httpClient = injector.getInstance(DefaultHttpClient.class);
  private SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
  private VocabularyFactory vocabularyFactory = new VocabularyFactory(httpClient, saxf);

  private AppConfig mockAppCfg = mock(AppConfig.class);
  private DataDir mockDataDir = mock(DataDir.class);

  private RegistryManager registryManager = mock(RegistryManager.class);
  private ExtensionManager extensionManager = mock(ExtensionManager.class);
  private ConfigWarnings warnings = new ConfigWarnings();
  private SimpleTextProvider textProvider = mock(SimpleTextProvider.class);
  private RegistrationManager registrationManager = mock(RegistrationManager.class);

  VocabulariesManager manager;

  @Test
  public void testLoad() {
    // mock data directory's .vocabularies folder
    File myTmpVocabDir = FileUtils.getClasspathFile("vocabularies");
    assertTrue(myTmpVocabDir.isDirectory());
    when(mockDataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER)).thenReturn(myTmpVocabDir);

    // mock vocabularies.xml from actual test resources file
    File vocabulariesXml = org.gbif.utils.file.FileUtils.getClasspathFile("vocabularies/vocabularies.xml");
    when(mockDataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/" + VocabulariesManagerImpl.PERSISTENCE_FILE)).thenReturn(vocabulariesXml);

    // mock existence of deprecated vocab file
    when(mockDataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/" +
                                Constants.DEPRECATED_VOCAB_URL_RESOLVABLE_RESOURCE_TYPE)).thenReturn(new File("empty"));

    manager = new VocabulariesManagerImpl(mockAppCfg, mockDataDir, vocabularyFactory,
      httpClient, registryManager, extensionManager, warnings, textProvider, registrationManager);

    assertEquals(1, manager.load());
  }

  @Test
  public void testLoadFromVersion203() {
    // mock data directory's .vocabularies from 2.0.3 version of IPT
    File myTmpVocabDir = FileUtils.getClasspathFile("vocabularies/vocabularies_203");
    assertTrue(myTmpVocabDir.isDirectory());
    when(mockDataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER)).thenReturn(myTmpVocabDir);

    // mock vocabularies.xml from 2.0.3 version of IPT
    File vocabulariesXml = org.gbif.utils.file.FileUtils.getClasspathFile("vocabularies/vocabularies_203/vocabularies.xml");
    when(mockDataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/" + VocabulariesManagerImpl.PERSISTENCE_FILE)).thenReturn(vocabulariesXml);

    // mock deprecated vocabulary file
    File deprecatedVocab = FileUtils.getClasspathFile("vocabularies/vocabularies_203/http_rs_gbif_org_vocabulary_gbif_resource_type_xml.vocab");
    assertTrue(deprecatedVocab.isFile());

    // mock returning deprecated vocab file
    when(mockDataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/" +
                                Constants.DEPRECATED_VOCAB_URL_RESOLVABLE_RESOURCE_TYPE)).thenReturn(deprecatedVocab);

    manager = new VocabulariesManagerImpl(mockAppCfg, mockDataDir, vocabularyFactory,
      httpClient, registryManager, extensionManager, warnings, textProvider, registrationManager);

    // only 1 vocabulary should be loaded - the deprecated one should be removed
    assertEquals(1, manager.load());
  }

}
