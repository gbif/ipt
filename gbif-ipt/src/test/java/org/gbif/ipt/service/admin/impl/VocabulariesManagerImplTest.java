package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.config.InjectingTestClassRunner;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockExtensionManager;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;

import com.google.inject.Inject;

import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.parsers.SAXParserFactory;

@RunWith(InjectingTestClassRunner.class)
public class VocabulariesManagerImplTest {

  @Inject
  private DataDir dataDir;

  /* for test only */
  public static void main(String[] args) {
    new VocabulariesManagerImplTest().getVocabManager();
  }

  private VocabulariesManager getVocabManager() {
    // AppConfig
    AppConfig appConfig = MockAppConfig.buildMock();

    // DataDir
    // DataDir dataDir = DataDir.buildMock();

    // Client and VocabularyFactory
    IPTModule mod = new IPTModule();
    SAXParserFactory sax = mod.provideNsAwareSaxParserFactory();
    DefaultHttpClient client = new DefaultHttpClient();
    VocabularyFactory vocabFactory = new VocabularyFactory(client, sax);

    // RegistryManager
    RegistryManager registryManager = MockRegistryManager.buildMock();

    // ExtensionManager
    ExtensionManager extensionManager = MockExtensionManager.buildMock();

    // ConfigWarnings
    ConfigWarnings warnings = new ConfigWarnings();

    // initialise the VocabularyManager instance.
    VocabulariesManager vocabManager = new VocabulariesManagerImpl(appConfig, dataDir, vocabFactory, client,
        registryManager, extensionManager, warnings);

    // All general stubbing functionalities should be in the corresponding mock classes.
    // If an specific stub configuration is needed only for this tests, the methods should be implemented here.

    return vocabManager;
  }

  @Ignore
  @Test
  public void loadVocabularies() {
    VocabulariesManager vocabManager = getVocabManager();
    int num = vocabManager.load();
    System.out.println(num);
  }

}
