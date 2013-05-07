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

package org.gbif.ipt.task;

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.service.manage.impl.SourceManagerImpl;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateDwcaTest {

  private static final String RESOURCE_SHORTNAME = "res1";
  private static final String VERSIONED_ARCHIVE_FILENAME = "dwca-3.zip";

  private GenerateDwca generateDwca;
  private Resource resource;
  private User creator;
  private ReportHandler mockHandler;
  private DataDir mockDataDir = MockDataDir.buildMock();
  private AppConfig mockAppConfig = MockAppConfig.buildMock();
  private SourceManager mockSourceManager;
  private File tmpDataDir;
  private File resourceDir;

  @Before
  public void setup() throws IOException {
    // create resource
    resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);

    // create user
    creator = new User();
    creator.setFirstname("Leonardo");
    creator.setLastname("Pisano");
    creator.setEmail("fi@liberabaci.com");
    creator.setLastLoginToNow();
    creator.setRole(User.Role.Manager);
    creator.setPassword("011235813");

    mockHandler = mock(ResourceManagerImpl.class);

    resourceDir = FileUtils.createTempDir();
    File publicationLogFile = new File(resourceDir, DataDir.PUBLICATION_LOG_FILENAME);

    // publication log file
    when(mockDataDir.resourcePublicationLogFile(RESOURCE_SHORTNAME)).thenReturn(publicationLogFile);

    // tmp directory
    tmpDataDir = FileUtils.createTempDir();
    when(mockDataDir.tmpDir()).thenReturn(tmpDataDir);

    // archival mode on
    when(mockAppConfig.isArchivalMode()).thenReturn(true);
  }

  /**
   * A resource with no core is expected to throw a GeneratorException.
   *
   * @throws Exception
   */
  @Test(expected = GeneratorException.class)
  public void testResourceWithNoCore() throws Exception {
    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mock(SourceManager.class), mockAppConfig);
    generateDwca.call();
  }

  @Test
  public void testGenerateCoreFromSingleSourceFile() throws Exception {
    // create resource
    Resource resource = getResource();

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig);
    int recordCount = generateDwca.call();

    // 2 records in core file
    assertEquals(2, recordCount);

    // confirm existence of DwC-A
    File dwca = new File(resourceDir, DataDir.DWCA_FILENAME);
    assertTrue(dwca.exists());

    // confirm existence of versioned (archived) DwC-A
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, dwca, true);

    Archive archive = ArchiveFactory.openArchive(dir);
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(3, archive.getCore().getFieldsSorted().size());

    // confirm order of fields appear in order of Occurrence Core Extension
    assertEquals("basisOfRecord", archive.getCore().getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("scientificName", archive.getCore().getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("kingdom", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());
  }

  @Test
  public void testGenerateCoreFromTwoMappings() {
    // TODO: write test
  }

  @Test
  public void testGenerateExtensionsFromMultipleMappings() {
    // TODO: write test
  }

  private Resource getResource()
    throws IOException, SAXException, ParserConfigurationException, AlreadyExistingException, ImportException {
    UserAccountManager mockUserAccountManager = mock(UserAccountManager.class);
    UserEmailConverter mockEmailConverter = new UserEmailConverter(mockUserAccountManager);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    OrganisationKeyConverter mockOrganisationKeyConverter = new OrganisationKeyConverter(mockRegistrationManager);
    RegistryManager mockRegistryManager = MockRegistryManager.buildMock();
    GenerateDwcaFactory mockDwcaFactory = mock(GenerateDwcaFactory.class);
    Eml2Rtf mockEml2Rtf = mock(Eml2Rtf.class);
    VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);
    SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
    BaseAction baseAction = new BaseAction(mockSimpleTextProvider, mockAppConfig, mockRegistrationManager);


    // construct ExtensionFactory using injected parameters
    Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());
    DefaultHttpClient httpClient = injector.getInstance(DefaultHttpClient.class);
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
    ExtensionFactory extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);
    JdbcSupport support = injector.getInstance(JdbcSupport.class);
    PasswordConverter passwordConverter = injector.getInstance(PasswordConverter.class);
    JdbcInfoConverter jdbcConverter = new JdbcInfoConverter(support);

    // construct occurrence core Extension
    InputStream occurrenceCoreIs = GenerateDwcaTest.class.getResourceAsStream("/extensions/dwc_occurrence.xml");
    Extension occurrenceCore = extensionFactory.build(occurrenceCoreIs);
    ExtensionManager extensionManager = mock(ExtensionManager.class);

    // mock ExtensionManager returning occurrence core Extension
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence")).thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord"))
      .thenReturn(occurrenceCore);

    ExtensionRowTypeConverter extensionRowTypeConverter = new ExtensionRowTypeConverter(extensionManager);
    ConceptTermConverter conceptTermConverter = new ConceptTermConverter(extensionRowTypeConverter);

    // retrieve sample zipped resource folder
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // mock finding resource.xml file
    when(mockDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);

    // retrieve sample zipped resource folder
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");

    // retrieve sample zipped resource folder
    File emlXML = FileUtils.getClasspathFile("resources/res1/eml.xml");
    // mock finding eml.xml file
    when(mockDataDir.resourceEmlFile(anyString(), anyInt())).thenReturn(emlXML);

    // create SourceManagerImpl
    mockSourceManager = new SourceManagerImpl(mock(AppConfig.class), mockDataDir);

    // create ResourceManagerImpl
    ResourceManagerImpl resourceManager =
      new ResourceManagerImpl(mockAppConfig, mockDataDir, mockEmailConverter, mockOrganisationKeyConverter,
        extensionRowTypeConverter, jdbcConverter, mockSourceManager, extensionManager, mockRegistryManager,
        conceptTermConverter, mockDwcaFactory, passwordConverter, mockEml2Rtf, mockVocabulariesManager,
        mockSimpleTextProvider, mockRegistrationManager);

    // create a new resource.
    resource = resourceManager.create(RESOURCE_SHORTNAME, null, zippedResourceFolder, creator, baseAction);

    // retrieve source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence.txt");

    // copy source file to tmp folder
    File copied = new File(resourceDir, "source.txt");

    // mock file to which source file gets copied to
    when(mockDataDir.sourceFile(any(Resource.class), any(Source.class))).thenReturn(copied);

    // mock log file
    when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(new File(resourceDir, "log.txt"));

    // mock creation of zipped dwca in temp directory - this later becomes the actual archive generated
    when(mockDataDir.tmpFile(anyString(), anyString())).thenReturn(new File(tmpDataDir, DataDir.DWCA_FILENAME));

    // mock creation of zipped dwca in resource directory
    when(mockDataDir.resourceDwcaFile(anyString())).thenReturn(new File(resourceDir, DataDir.DWCA_FILENAME));

    // mock creation of versioned zipped dwca in resource directory
    when(mockDataDir.resourceDwcaFile(anyString(), anyInt()))
      .thenReturn(new File(resourceDir, VERSIONED_ARCHIVE_FILENAME));

    // add Source.FileSource fileSource to test Resource
    Source.FileSource fileSource = mockSourceManager.add(resource, occurrence, "occurrence.txt");
    resource.getMappings().get(0).setSource(fileSource);
    return resource;
  }

}
