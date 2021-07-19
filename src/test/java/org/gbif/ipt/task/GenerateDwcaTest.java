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

import org.gbif.api.model.common.DOI;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.gbif.utils.file.csv.CSVReader;
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
import org.gbif.ipt.model.voc.IdentifierStatus;
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
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateDwcaTest {
  private static final Logger LOG = LogManager.getLogger(GenerateDwcaTest.class);
  private static final String RESOURCE_SHORTNAME = "res1";
  private static final String VERSIONED_ARCHIVE_FILENAME = "dwca-3.0.zip";

  private GenerateDwca generateDwca;
  private Resource resource;
  private User creator;
  private ReportHandler mockHandler;
  private DataDir mockDataDir = MockDataDir.buildMock();
  private AppConfig mockAppConfig = MockAppConfig.buildMock();
  private SourceManager mockSourceManager;
  private static VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);
  private File tmpDataDir;
  private File resourceDir;

  @BeforeClass
  public static void init() {
    // populate HashMap from basisOfRecord vocabulary, with lowercase keys (used in basisOfRecord validation)
    Map<String, String> basisOfRecords = Maps.newHashMap();
    basisOfRecords.put("preservedspecimen", "Preserved Specimen");
    basisOfRecords.put("fossilspecimen", "Fossil Specimen");
    basisOfRecords.put("livingspecimen", "Living Specimen");
    basisOfRecords.put("humanobservation", "Human Observation");
    basisOfRecords.put("machineobservation", "Machine Observation");
    basisOfRecords.put("materialsample", "Material Sample");
    basisOfRecords.put("occurrence", "Occurrence");

    when(
      mockVocabulariesManager.getI18nVocab(Constants.VOCAB_URI_BASIS_OF_RECORDS, Locale.ENGLISH.getLanguage(), false))
      .thenReturn(basisOfRecords);
  }

  @Before
  public void setup() throws IOException {
    // create resource, version 3.0
    resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);
    resource.setEmlVersion(new BigDecimal("3.0"));

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
    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mock(SourceManager.class), mockAppConfig,
      mock(VocabulariesManager.class));
    generateDwca.call();
  }

  @Test
  public void testGenerateCoreFromSingleSourceFile() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // create resource from single source file (with empty line as last line)
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence.txt");
    Resource resource = getResource(resourceXML, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    Map<String, Integer> recordsByExtension = generateDwca.call();
    // count for occurrence core only
    assertEquals(1, recordsByExtension.size());

    // 2 rows in core file
    String coreRowType = resource.getCoreRowType();
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, coreRowType);
    int recordCount = recordsByExtension.get(resource.getCoreRowType());
    assertEquals(2, recordCount);

    // confirm existence of versioned (archived) DwC-A "dwca-3.0.zip"
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, versionedDwca, true);

    Archive archive = ArchiveFactory.openArchive(dir);
    assertEquals(DwcTerm.Occurrence, archive.getCore().getRowType());
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
    assertEquals("occurrence", row[1]);
    assertEquals("1", row[2]);
    assertEquals("puma concolor", row[3]);
    assertEquals("occurrence", row[4]);
    // 2nd record
    row = reader.next();
    assertEquals("2", row[0]);
    assertEquals("occurrence", row[1]);
    assertEquals("2", row[2]);
    assertEquals("pumm:concolor", row[3]);
    assertEquals("occurrence", row[4]);
    reader.close();

    // since basisOfRecord was occurrence, and this is ambiguous, there should be a warning message!
    boolean foundWarningAboutAmbiguousBOR = false;
    // since there was an empty line at bottom of file, there should be a warning message!
    boolean foundWarningAboutEmptyLine = false;
    for (Iterator<TaskMessage> iter = generateDwca.report().getMessages().iterator(); iter.hasNext();) {
      TaskMessage msg = iter.next();
      if (msg.getMessage().startsWith("2 line(s) use ambiguous basisOfRecord")) {
         foundWarningAboutAmbiguousBOR = true;
      } else if (msg.getMessage().startsWith("1 empty line(s) skipped")) {
        foundWarningAboutEmptyLine = true;
      }
    }
    assertTrue(foundWarningAboutAmbiguousBOR);
    assertTrue(foundWarningAboutEmptyLine);
  }

  /**
   * Confirm resource DOI used for datasetID, when setting "doi used for DatasetID" has been turned on in the extension
   * mapping.
   */
  @Test
  public void testGenerateCoreFromSingleSourceFileDOIForDatasetID() throws Exception {
    // retrieve sample zipped resource XML configuration file, where setting "doi used for datasetID" has been turned on
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_doi_dataset_id.xml");
    // create resource from single source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_doi_dataset_id.txt");
    // set DOI
    Resource resource = getResource(resourceXML, occurrence);
    resource.setDoi(new DOI("10.5072/gc8gqc"));
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    // assert DOI set properly
    assertNotNull(resource.getDoi());
    assertEquals("10.5072/gc8gqc", resource.getDoi().getDoiName());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, resource.getIdentifierStatus());

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    Map<String, Integer> recordsByExtension = generateDwca.call();
    // count for occurrence core only
    assertEquals(1, recordsByExtension.size());

    // 2 rows in core file
    String coreRowType = resource.getCoreRowType();
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, coreRowType);
    int recordCount = recordsByExtension.get(resource.getCoreRowType());
    assertEquals(2, recordCount);

    // confirm existence of versioned (archived) DwC-A "dwca-3.0.zip"
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, versionedDwca, true);

    Archive archive = ArchiveFactory.openArchive(dir);
    assertEquals(DwcTerm.Occurrence, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(5, archive.getCore().getFieldsSorted().size());

    // confirm order of fields appear honors order of Occurrence Core Extension
    assertEquals("datasetID", archive.getCore().getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("basisOfRecord", archive.getCore().getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("occurrenceID", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());
    assertEquals("scientificName", archive.getCore().getFieldsSorted().get(3).getTerm().simpleName());
    assertEquals("kingdom", archive.getCore().getFieldsSorted().get(4).getTerm().simpleName());

    // confirm data written to file
    CSVReader reader = archive.getCore().getCSVReader();
    // 1st record
    String[] row = reader.next();
    assertEquals("1", row[0]);
    assertEquals("doi:10.5072/gc8gqc", row[1]); // confirm resource DOI used for datasetID
    assertEquals("occurrence", row[2]);
    assertEquals("1", row[3]);
    assertEquals("puma concolor", row[4]);
    assertEquals("occurrence", row[5]);

    // 2nd record
    row = reader.next();
    assertEquals("2", row[0]);
    assertEquals("doi:10.5072/gc8gqc", row[1]); // confirm resource DOI used for datasetID
    assertEquals("occurrence", row[2]);
    assertEquals("2", row[3]);
    assertEquals("pumm:concolor", row[4]);
    assertEquals("occurrence", row[5]);
    reader.close();
  }

  /**
   * Use generated JSON for DynamicProperties
   */
  @Test
  public void testGenerateCoreFromSingleSourceFileJsonDynamicProperties() throws Exception {
    // retrieve sample zipped resource XML configuration file, where setting "generated dynamic properties" has been turned on
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_generate_dynamic_properties.xml");
    // create resource from single source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_dynamic_properties.txt");
    Resource resource = getResource(resourceXML, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    Map<String, Integer> recordsByExtension = generateDwca.call();
    // count for occurrence core only
    assertEquals(1, recordsByExtension.size());

    // 2 rows in core file
    String coreRowType = resource.getCoreRowType();
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, coreRowType);
    int recordCount = recordsByExtension.get(resource.getCoreRowType());
    assertEquals(2, recordCount);

    // confirm existence of versioned (archived) DwC-A "dwca-3.0.zip"
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, versionedDwca, true);

    Archive archive = ArchiveFactory.openArchive(dir);
    assertEquals(DwcTerm.Occurrence, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(4, archive.getCore().getFieldsSorted().size());

    // confirm order of fields appear honors order of Occurrence Core Extension
    assertEquals("basisOfRecord", archive.getCore().getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("dynamicProperties", archive.getCore().getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("occurrenceID", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());
    assertEquals("scientificName", archive.getCore().getFieldsSorted().get(3).getTerm().simpleName());

    // confirm data written to file
    CSVReader reader = archive.getCore().getCSVReader();
    // 1st record
    String[] row = reader.next();
    assertEquals("1", row[0]);
    assertEquals("occurrence", row[1]);
    assertEquals("{\"customValue\":\"val1\",\"datasetID\":\"ds_77\",\"kingdom\":\"animalia\"}", row[2]);
    assertEquals("1", row[3]);
    assertEquals("puma concolor", row[4]);

    // 2nd record
    row = reader.next();
    assertEquals("2", row[0]);
    assertEquals("occurrence", row[1]);
    assertEquals("{\"customValue\":\"val2\",\"datasetID\":\"ds_77\",\"kingdom\":\"animalia\"}", row[2]);
    assertEquals("2", row[3]);
    assertEquals("pumm:concolor", row[4]);
    reader.close();
  }

  /**
   * Use standard mapping for DynamicProperties
   */
  @Test
  public void testGenerateCoreFromSingleSourceFileStandardDynamicProperties() throws Exception {
    // retrieve sample zipped resource XML configuration file, where setting "generated dynamic properties" has been turned off
    // and standard mapping is used
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_standard_dynamic_properties.xml");
    // create resource from single source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_dynamic_properties.txt");
    Resource resource = getResource(resourceXML, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    Map<String, Integer> recordsByExtension = generateDwca.call();
    // count for occurrence core only
    assertEquals(1, recordsByExtension.size());

    // 2 rows in core file
    String coreRowType = resource.getCoreRowType();
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, coreRowType);
    int recordCount = recordsByExtension.get(resource.getCoreRowType());
    assertEquals(2, recordCount);

    // confirm existence of versioned (archived) DwC-A "dwca-3.0.zip"
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, versionedDwca, true);

    Archive archive = ArchiveFactory.openArchive(dir);
    assertEquals(DwcTerm.Occurrence, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(4, archive.getCore().getFieldsSorted().size());

    // confirm order of fields appear honors order of Occurrence Core Extension
    assertEquals("basisOfRecord", archive.getCore().getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("dynamicProperties", archive.getCore().getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("occurrenceID", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());
    assertEquals("scientificName", archive.getCore().getFieldsSorted().get(3).getTerm().simpleName());

    // confirm data written to file
    CSVReader reader = archive.getCore().getCSVReader();
    // 1st record
    String[] row = reader.next();
    assertEquals("1", row[0]);
    assertEquals("occurrence", row[1]);
    assertEquals("val1", row[2]);
    assertEquals("1", row[3]);
    assertEquals("puma concolor", row[4]);

    // 2nd record
    row = reader.next();
    assertEquals("2", row[0]);
    assertEquals("occurrence", row[1]);
    assertEquals("val2", row[2]);
    assertEquals("2", row[3]);
    assertEquals("pumm:concolor", row[4]);
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

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    Map<String, Integer> recordsByExtension = generateDwca.call();
    // count for occurrence core only
    assertEquals(1, recordsByExtension.size());

    // 4 rows in core file
    String coreRowType = resource.getCoreRowType();
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, coreRowType);
    int recordCount = recordsByExtension.get(resource.getCoreRowType());
    assertEquals(4, recordCount);

    // confirm existence of versioned DwC-A "dwca-3.0.zip"
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, versionedDwca, true);

    Archive archive = ArchiveFactory.openArchive(dir);
    assertEquals(DwcTerm.Occurrence, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(4, archive.getCore().getFieldsSorted().size());

    // confirm order of fields appear honors order of Occurrence Core Extension
    assertEquals("basisOfRecord", archive.getCore().getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("organismID", archive.getCore().getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("scientificName", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());
    assertEquals("kingdom", archive.getCore().getFieldsSorted().get(3).getTerm().simpleName());

    // confirm data written to file
    CSVReader reader = archive.getCore().getCSVReader();
    // 1st record
    String[] row = reader.next();
    // no id was mapped, so the first column (ID column, index 0) is empty
    assertEquals("", row[0]);
    assertEquals("HumanObservation", row[1]);
    assertEquals("1", row[2]);
    assertEquals("puma concolor", row[3]);
    assertEquals("Animalia", row[4]);
    // 2nd record
    row = reader.next();
    assertEquals("", row[0]);
    assertEquals("HumanObservation", row[1]);
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

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mock(VocabulariesManager.class));
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

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mock(VocabulariesManager.class));
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

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mock(VocabulariesManager.class));
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
    InputStream occurrenceCoreIs = GenerateDwcaTest.class.getResourceAsStream(
      "/extensions/dwc_occurrence_2015-04-24.xml");
    Extension occurrenceCore = extensionFactory.build(occurrenceCoreIs);
    ExtensionManager extensionManager = mock(ExtensionManager.class);

    // construct event core Extension
    InputStream eventCoreIs = GenerateDwcaTest.class.getResourceAsStream("/extensions/dwc_event_2015-04-24.xml");
    Extension eventCore = extensionFactory.build(eventCoreIs);

    // mock ExtensionManager returning occurrence core Extension
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence")).thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Event")).thenReturn(eventCore);
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
    when(mockDataDir.resourceEmlFile(anyString())).thenReturn(emlXML);

    // mock finding dwca.zip file that does not exist
    when(mockDataDir.resourceDwcaFile(anyString())).thenReturn(new File("dwca.zip"));

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
    File copied = new File(resourceDir, "occurrence.txt");

    // mock file to which source file gets copied to
    when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(copied);
    // mock log file
    when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(new File(resourceDir, "log.txt"));
    // add SourceBase.TextFileSource fileSource to test Resource
    FileSource fileSource = mockSourceManager.add(resource, sourceFile, "occurrence.txt");
    resource.getMappings().get(0).setSource(fileSource);

    // mock creation of zipped dwca in temp directory - this later becomes the actual archive generated
    when(mockDataDir.tmpFile(anyString(), anyString())).thenReturn(new File(tmpDataDir, "dwca.zip"));

    // mock creation of versioned zipped dwca in resource directory
    when(mockDataDir.resourceDwcaFile(anyString(), any(BigDecimal.class)))
      .thenReturn(new File(resourceDir, VERSIONED_ARCHIVE_FILENAME));

    return resource;
  }

  @Test
  public void testCreateFileName() throws Exception {
    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mock(VocabulariesManager.class));

    // DwC-A directory
    File dir = FileUtils.createTempDir();

    // first file
    File materialsample = new File(dir, "materialsample.txt");
    materialsample.createNewFile();

    String fileName = generateDwca.createFileName(dir, "materialsample");
    assertEquals("materialsample2.txt", fileName);

    // second file
    File materialsample2 = new File(dir, "materialsample2.txt");
    materialsample2.createNewFile();

    fileName = generateDwca.createFileName(dir, "materialsample");
    assertEquals("materialsample3.txt", fileName);

    // third file
    File materialsample3 = new File(dir, "materialsample3.txt");
    materialsample3.createNewFile();

    fileName = generateDwca.createFileName(dir, "materialsample");
    assertEquals("materialsample4.txt", fileName);
  }

  /**
   * Confirm occurrence core with rows missing basisOfRecord throws GeneratorException.
   */
  @Test(expected = GeneratorException.class)
  public void testGenerateCoreFromSingleSourceFileMissingBasisOfRecord() throws Exception {
    // retrieve sample zipped resource XML configuration file corresponding to occurrence_missing_bor.txt
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_doi_dataset_id.xml");
    // create resource from single source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_missing_bor.txt");
    Resource resource = getResource(resourceXML, occurrence);
    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    generateDwca.call();
  }

  /**
   * Confirm occurrence core with rows with basisOfRecord not matching Darwin Core Type Vocabulary throws
   * GeneratorException.
   */
  @Test(expected = GeneratorException.class)
  public void testGenerateCoreFromSingleSourceFileNonMatchingBasisOfRecord() throws Exception {
    // retrieve sample zipped resource XML configuration file corresponding to occurrence_missing_bor.txt
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_doi_dataset_id.xml");
    // create resource from single source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_non_matching_bor.txt");
    Resource resource = getResource(resourceXML, occurrence);
    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    generateDwca.call();
  }

  /**
   * Confirm occurrence core missing required basisOfRecord mapping throws GeneratorException.
   */
  @Test(expected = GeneratorException.class)
  public void testGenerateCoreFromSingleSourceFileMissingBasisOfRecordMapping() throws Exception {
    // retrieve sample zipped resource XML configuration file corresponding to occurrence_missing_bor.txt
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_no_bor_mapped.xml");
    // create resource from single source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_no_bor_mapped.txt");
    Resource resource = getResource(resourceXML, occurrence);
    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    generateDwca.call();
  }

  /**
   * Test makes sure the multi-value field delimiter gets set on the appropriate term mappings in the meta.xml.
   */
  @Test
  public void testMultiValueFieldDelimiterSet() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_multivalue.xml");
    // create resource from single source file
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence_multivalue.txt");
    Resource resource = getResource(resourceXML, occurrence);
    resource.getMappings().get(0).getSource().setMultiValueFieldsDelimitedBy("|");

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    Map<String, Integer> recordsByExtension = generateDwca.call();
    // count for occurrence core only
    assertEquals(1, recordsByExtension.size());

    // 2 rows in core file
    String coreRowType = resource.getCoreRowType();
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, coreRowType);
    int recordCount = recordsByExtension.get(resource.getCoreRowType());
    assertEquals(2, recordCount);

    // confirm existence of versioned (archived) DwC-A "dwca-3.0.zip"
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, versionedDwca, true);

    Archive archive = ArchiveFactory.openArchive(dir);
    assertEquals(DwcTerm.Occurrence, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(4, archive.getCore().getFieldsSorted().size());
    assertEquals("|", archive.getCore().getField(DwcTerm.associatedMedia).getDelimitedBy());
    assertNull(archive.getCore().getField(DwcTerm.occurrenceID).getDelimitedBy());

    // confirm order of fields appear honors order of Occurrence Core Extension
    assertEquals("associatedMedia", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());

    // confirm data written to file
    CSVReader reader = archive.getCore().getCSVReader();
    // 1st record
    String[] row = reader.next();
    assertEquals("http://dummyimage.com/1|http://dummyimage.com/2", row[3]);
    // 2nd record
    row = reader.next();
    assertEquals("http://dummyimage.com/3|http://dummyimage.com/4", row[3]);
    reader.close();
  }

  /**
   * A generated DwC-a with event core, but not having associated occurrences, is expected to show a warning message
   */
  @Test
  public void testValidateEventCoreFromSingleSourceFileMissingOccurrenceExtension() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_event_1.xml");
    // create sampling event resource, with single source file
    File event = FileUtils.getClasspathFile("resources/res1/event.txt");
    Resource resource = getResource(resourceXML, event);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mock(VocabulariesManager.class));
    generateDwca.call();

    // check for warning message
    boolean foundWarning = false;
    for (Iterator<TaskMessage> iter = generateDwca.report().getMessages().iterator(); iter.hasNext();) {
      TaskMessage msg = iter.next();
      if (msg.getMessage().equals("The sampling event resource has no associated occurrences.")) {
        foundWarning = true;
      }
    }
    assertTrue(foundWarning);
  }

  @Test
  public void testTabRow() throws IOException {
    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);

    String[] elements = new String[] {"1", "humanObservation", "Panthera tigris"};
    String tabRow = generateDwca.tabRow(elements);
    assertEquals("1\thumanObservation\tPanthera tigris\n", tabRow);

    // with line breaking characters replaced with empty space
    elements = new String[] {"OBS\t1", "human\rObservation", "Panthera ti\ngris"};
    tabRow = generateDwca.tabRow(elements);
    assertEquals("OBS 1\thuman Observation\tPanthera ti gris\n", tabRow);

    // check column with null value is still represented
    elements = new String[] {"1", null, "humanObservation"};
    tabRow = generateDwca.tabRow(elements);
    assertEquals("1\t\thumanObservation\n", tabRow);

    // with null values
    elements = new String[] {null, null, null};
    tabRow = generateDwca.tabRow(elements);
    assertNull(tabRow);
  }
}
