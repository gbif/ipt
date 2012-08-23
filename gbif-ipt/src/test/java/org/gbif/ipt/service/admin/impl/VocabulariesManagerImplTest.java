package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import javax.xml.parsers.SAXParserFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VocabulariesManagerImplTest {

  VocabulariesManager manager;

  @Before
  public void setup() {

    Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());

    // construct VocabularyFactory
    DefaultHttpClient httpClient = injector.getInstance(DefaultHttpClient.class);
    SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
    VocabularyFactory vocabularyFactory = new VocabularyFactory(httpClient, saxf);

    AppConfig cfg = mock(AppConfig.class);
    DataDir dataDir = mock(DataDir.class);

    DefaultHttpClient client = mock(DefaultHttpClient.class);
    RegistryManager registryManager = mock(RegistryManager.class);
    ExtensionManager extensionManager = mock(ExtensionManager.class);
    ConfigWarnings warnings = new ConfigWarnings();
    SimpleTextProvider textProvider = mock(SimpleTextProvider.class);
    RegistrationManager registrationManager = mock(RegistrationManager.class);

    // mock data directory's .vocabularies folder
    File myTmpVocabDir = FileUtils.getClasspathFile("vocabularies");
    assertTrue(myTmpVocabDir.isDirectory());
    when(dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER)).thenReturn(myTmpVocabDir);

    // mock vocabularies.xml from actual test resources file
    File vocabulariesXml = org.gbif.utils.file.FileUtils.getClasspathFile("vocabularies/vocabularies.xml");
    when(dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER + "/" + VocabulariesManagerImpl.PERSISTENCE_FILE)).thenReturn(vocabulariesXml);

    // mock VocabularyFactory.build - that returns a Vocabulary object from input stream on the supplied .vocab file

    manager = new VocabulariesManagerImpl(cfg, dataDir, vocabularyFactory,
      client, registryManager, extensionManager, warnings, textProvider, registrationManager);
  }

  @Test
  public void testLoad() {
    assertEquals(1, manager.load());
  }

}
