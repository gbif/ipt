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
import org.gbif.file.CSVReader;
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
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Resource;
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
import org.gbif.ipt.service.InvalidFilenameException;
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
import javax.validation.constraints.NotNull;
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
   */
  @Test(expected = GeneratorException.class)
  public void testResourceWithNoCore() throws Exception {
    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mock(SourceManager.class), mockAppConfig);
    generateDwca.call();
  }

  @Test
  public void testGenerateCoreFromSingleSourceFile() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // create resource from single source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence.txt");
    Resource resource = getResource(resourceXML, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig);
    int recordCount = generateDwca.call();

    // 2 rows in core file
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
    assertEquals(4, archive.getCore().getFieldsSorted().size());

    // confirm order of fields appear honors order of Occurrence Core Extension
    assertEquals("basisOfRecord", archive.getCore().getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("occurrenceID", archive.getCore().getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("scientificName", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());
    assertEquals("kingdom", archive.getCore().getFieldsSorted().get(3).getTerm().simpleName());

    // confirm data written to file
    CSVReader reader = archive.getCore().getCSVReader();
    // 1st record
    String[] row = reader.next();
    assertEquals("1", row[0]);
    assertEquals("Animalia", row[1]);
    assertEquals("1", row[2]);
    assertEquals("puma concolor", row[3]);
    assertEquals("Animalia", row[4]);
    // 2nd record
    row = reader.next();
    assertEquals("2", row[0]);
    assertEquals("Animalia", row[1]);
    assertEquals("2", row[2]);
    assertEquals("pumm:concolor", row[3]);
    assertEquals("Animalia", row[4]);
    reader.close();
  }

  @Test
  public void testGenerateCoreFromSingleSourceFileNoIdMapped() throws Exception {
    // retrieve sample zipped resource XML configuration file, with no id mapped
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_no_id_mapped.xml");
    // create resource from single source file with an id column with non unique values (mapped to individual ID).
    // since the non unique ids aren't mapped to the core record identifier (occurrenceID) validation isn't triggered.
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_non_unique_ids.txt");
    Resource resource = getResource(resourceXML, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig);
    int recordCount = generateDwca.call();

    // 4 rows in core file
    assertEquals(4, recordCount);

    // confirm existence of DwC-A
    File dwca = new File(resourceDir, DataDir.DWCA_FILENAME);
    assertTrue(dwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, dwca, true);

    Archive archive = ArchiveFactory.openArchive(dir);
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(4, archive.getCore().getFieldsSorted().size());

    // confirm order of fields appear honors order of Occurrence Core Extension
    assertEquals("basisOfRecord", archive.getCore().getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("individualID", archive.getCore().getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("scientificName", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());
    assertEquals("kingdom", archive.getCore().getFieldsSorted().get(3).getTerm().simpleName());

    // confirm data written to file
    CSVReader reader = archive.getCore().getCSVReader();
    // 1st record
    String[] row = reader.next();
    // no id was mapped, so the first column (ID column, index 0) is empty
    assertEquals("", row[0]);
    assertEquals("Observation", row[1]);
    assertEquals("1", row[2]);
    assertEquals("puma concolor", row[3]);
    assertEquals("Animalia", row[4]);
    // 2nd record
    row = reader.next();
    assertEquals("", row[0]);
    assertEquals("Observation", row[1]);
    assertEquals("2", row[2]);
    assertEquals("Panthera onca", row[3]);
    assertEquals("Animalia", row[4]);
    reader.close();
  }

  /**
   * A generated DwC-a with occurrenceID mapped, but missing one or more occurrenceID values, is expected to
   * throw a GeneratorException.
   */
  @Test(expected = GeneratorException.class)
  public void testValidateCoreFromSingleSourceFileMissingIds() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // create resource, with single source file that is missing occurrenceIDs
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_missing_ids.txt");
    Resource resource = getResource(resourceXML, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig);
    generateDwca.call();
  }

  /**
   * A generated DwC-a with occurrenceID mapped, but having non unique occurrenceID values, is expected to
   * throw a GeneratorException.
   */
  @Test(expected = GeneratorException.class)
  public void testValidateCoreFromSingleSourceFileNonUniqueIds() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // create resource, with single source file that has non-unique occurrenceIDs, regardless of case
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_non_unique_ids.txt");
    Resource resource = getResource(resourceXML, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig);
    generateDwca.call();
  }

  /**
   * A generated DwC-a with occurrenceID mapped, but with occurrenceID values that are non unique when compared with
   * case insensitivity, is expected to throw a GeneratorException. E.g. FISHES:1 and fishes:1 are considered equal.
   */
  @Test(expected = GeneratorException.class)
  public void testValidateCoreFromSingleSourceFileNonUniqueIdsCase() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // create resource, with single source file that has unique occurrenceIDs due when compared with case sensitivity
    // and non-unique occurrenceIDs when compared with case sensitivity
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_non_unique_ids_case.txt");
    Resource resource = getResource(resourceXML, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig);
    generateDwca.call();
  }

  /**
   * Generates a test Resource.
   * </br>
   * The test resource is built from the test occurrence resource /res1 (res1/resource.xml, res1/eml.xml) mocking all
   * necessary methods executed by GenerateDwca.call().
   * </br>
   * For flexibility, the source file used to generate the core data file can be changed. The columns of this
   * source file must match the resourceXML configuration file passed in. By changing the source file and resource
   * configuration file, multiple scenarios can be created for testing.
   *
   * @param resourceXML resource (XML) configuration file defining column mapping of sourceFile
   * @param sourceFile source file
   *
   * @return test Resource
   */
  private Resource getResource(@NotNull File resourceXML, @NotNull File sourceFile)
    throws IOException, SAXException, ParserConfigurationException, AlreadyExistingException, ImportException,
    InvalidFilenameException {
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

    // mock finding resource.xml file
    when(mockDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);

    // retrieve sample zipped resource folder
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");

    // retrieve sample eml.xml file
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

    // copy source file to tmp folder
    File copied = new File(resourceDir, "source.txt");

    // mock file to which source file gets copied to
    when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(copied);

    // mock log file
    when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(new File(resourceDir, "log.txt"));

    // mock creation of zipped dwca in temp directory - this later becomes the actual archive generated
    when(mockDataDir.tmpFile(anyString(), anyString())).thenReturn(new File(tmpDataDir, DataDir.DWCA_FILENAME));

    // mock creation of zipped dwca in resource directory
    when(mockDataDir.resourceDwcaFile(anyString())).thenReturn(new File(resourceDir, DataDir.DWCA_FILENAME));

    // mock creation of versioned zipped dwca in resource directory
    when(mockDataDir.resourceDwcaFile(anyString(), anyInt()))
      .thenReturn(new File(resourceDir, VERSIONED_ARCHIVE_FILENAME));

    // add SourceBase.TextFileSource fileSource to test Resource
    FileSource fileSource = mockSourceManager.add(resource, sourceFile, "occurrence.txt");
    resource.getMappings().get(0).setSource(fileSource);
    return resource;
  }
}
