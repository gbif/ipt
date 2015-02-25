package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtensionManagerImplTest {

  private ExtensionManager extensionManager;

  @Before
  public void setup() throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    AppConfig mockAppConfig = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ConfigWarnings warnings = new ConfigWarnings();
    SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);

    Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());

    // construct ExtensionFactory using injected parameters
    DefaultHttpClient httpClient = injector.getInstance(DefaultHttpClient.class);
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
    ExtensionFactory extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);

    // construct mock RegistryManager:
    // mock getExtensions() response from Registry with local test resource (list of extensions from extensions.json)
    HttpUtil mockHttpUtil = mock(HttpUtil.class);
    HttpUtil.Response mockResponse = mock(HttpUtil.Response.class);
    String response =
      IOUtils.toString(ExtensionManagerImplTest.class.getResourceAsStream("/responses/extensions.json"), "UTF-8");
    mockResponse.content = response;
    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager mockRegistryManager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, saxf, warnings, mockSimpleTextProvider,
        mockRegistrationManager);

    File myTmpDir = Files.createTempDir();
    assertTrue(myTmpDir.isDirectory());

    // for simplicity only do one extension: always use occ core
    File occCore = FileUtils.getClasspathFile("extensions/dwc_occurrence.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(occCore, myTmpDir);
    File tmpOccCore = new File(myTmpDir, "dwc_occurrence.xml");
    assertTrue(tmpOccCore.exists());

    when(mockDataDir.configFile(ExtensionManagerImpl.CONFIG_FOLDER + "/tmp-extension.xml")).thenReturn(tmpOccCore);
    // Mock downloading extension into tmpFile - we're cheating by handling the actual file already as if it
    // were downloaded already. Furthermore, mock download() response with StatusLine with 200 OK response code
    StatusLine sl = mock(StatusLine.class);
    when(sl.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(mockHttpUtil.download(any(URL.class), any(File.class))).thenReturn(sl);
    // mock returning newly created extension file
    File newOccCoreExtension = new File(myTmpDir, "http_rs_tdwg_org_dwc_terms_Occurrence.xml");
    when(mockDataDir.configFile(ExtensionManagerImpl.CONFIG_FOLDER + "/http_rs_tdwg_org_dwc_terms_Occurrence.xml"))
      .thenReturn(newOccCoreExtension);

    // create instance
    extensionManager =
      new ExtensionManagerImpl(mockAppConfig, mockDataDir, extensionFactory, mockResourceManager, mockHttpUtil,
        warnings, mockSimpleTextProvider, mockRegistrationManager, mockRegistryManager);
  }

  @Test
  public void testInstallCoreTypes() {
    extensionManager.installCoreTypes();
    assertEquals(1, extensionManager.list().size());
    // get ext. and assert a couple properties
    Extension ext = extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence");
    assertEquals(161, ext.getProperties().size());
    assertEquals("Darwin Core Occurrence", ext.getTitle());
  }
}
