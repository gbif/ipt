/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.task;

import org.gbif.dwc.Archive;
import org.gbif.dwc.ArchiveFile;
import org.gbif.dwc.DwcFiles;
import org.gbif.dwc.record.Record;
import org.gbif.dwc.terms.DwcTerm;
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
import org.gbif.ipt.model.converter.PasswordEncrypter;
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
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.ClosableIterator;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests generating sampling event DwC-A: having an event core with occurrence extension.
 */
public class GenerateDwcaEventTest {
  private static final String RESOURCE_SHORTNAME = "event";
  private static final String VERSIONED_ARCHIVE_FILENAME = "dwca-2.0.zip";

  private GenerateDwca generateDwca;
  private AppConfig mockAppConfig = MockAppConfig.buildMock();
  private DataDir mockDataDir = MockDataDir.buildMock();
  private SourceManager mockSourceManager;
  private Resource resource;
  private File resourceDir;
  private ReportHandler mockHandler;
  private static VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);

  @BeforeAll
  public static void init() {
    // populate HashMap from basisOfRecord vocabulary, with lowercase keys (used in basisOfRecord validation)
    Map<String, String> basisOfRecords = new HashMap<>();
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

  @Test
  public void testGenerateEventArchive() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/event/resource.xml");
    File occurrence = FileUtils.getClasspathFile("resources/event/occurrence.txt");
    File event = FileUtils.getClasspathFile("resources/event/event.txt");
    resource = getResource(resourceXML, event, occurrence);

    generateDwca = new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig,
      mockVocabulariesManager);
    Map<String, Integer> recordsByExtension = generateDwca.call();
    // record count for event core and occurrence extension
    assertEquals(2, recordsByExtension.size());

    // 2 rows in core file
    String coreRowType = resource.getCoreRowType();
    assertEquals(Constants.DWC_ROWTYPE_EVENT, coreRowType);
    int recordCount = recordsByExtension.get(resource.getCoreRowType());
    assertEquals(2, recordCount);

    // 2 rows in extension file
    int extRecordCount = recordsByExtension.get(Constants.DWC_ROWTYPE_OCCURRENCE);
    assertEquals(2, extRecordCount);

    // confirm existence of versioned (archived) DwC-A "dwca-2.0.zip"
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, versionedDwca, true);

    Archive archive = DwcFiles.fromLocation(dir.toPath());

    // investigate event core data file
    assertEquals(DwcTerm.Event, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(4, archive.getCore().getFieldsSorted().size());

    // confirm order of fields in core data file honors order of Event Core Extension
    assertEquals("eventID", archive.getCore().getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("samplingProtocol", archive.getCore().getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("sampleSizeValue", archive.getCore().getFieldsSorted().get(2).getTerm().simpleName());
    assertEquals("sampleSizeUnit", archive.getCore().getFieldsSorted().get(3).getTerm().simpleName());

    // confirm data written to event core data file
    ArchiveFile coreFile = archive.getCore();
    ClosableIterator<Record> iterator = coreFile.iterator();
    // 1st core record
    Record record = iterator.next();
    assertEquals("1", record.column(0));
    assertEquals("1", record.column(1));
    assertEquals("mist net", record.column(2));
    assertEquals("5", record.column(3));
    assertEquals("metre", record.column(4));
    // 2nd core record
    record = iterator.next();
    assertEquals("2", record.column(0));
    assertEquals("2", record.column(1));
    assertEquals("mist net", record.column(2));
    assertEquals("5", record.column(3));
    assertEquals("metre", record.column(4));
    iterator.close();

    // investigate extension file
    ArchiveFile extFile = archive.getExtensions().iterator().next();
    assertEquals(DwcTerm.Occurrence, extFile.getRowType());
    assertEquals(0, extFile.getId().getIndex().intValue());
    assertEquals(3, extFile.getFieldsSorted().size());

    // confirm order of fields appear honors order of Occurrence Core Extension
    assertEquals("basisOfRecord", extFile.getFieldsSorted().get(0).getTerm().simpleName());
    assertEquals("scientificName", extFile.getFieldsSorted().get(1).getTerm().simpleName());
    assertEquals("kingdom", extFile.getFieldsSorted().get(2).getTerm().simpleName());

    // confirm data written to file
    iterator = extFile.iterator();
    // 1st record
    record = iterator.next();
    assertEquals("1", record.column(0));
    assertEquals("occurrence", record.column(1));
    assertEquals("puma concolor", record.column(2));
    assertEquals("animalia", record.column(3));
    // 2nd record
    record = iterator.next();
    assertEquals("2", record.column(0));
    assertEquals("occurrence", record.column(1));
    assertEquals("pumm:concolor", record.column(2));
    assertEquals("animalia", record.column(3));
    iterator.close();

    // since basisOfRecord was occurrence, and this is ambiguous, there should be a warning message!
    boolean foundWarningAboutAmbiguousBOR = false;
    // since there was an empty line at bottom of file, there should be a warning message!
    boolean foundWarningAboutEmptyLine = false;
    // since occurrenceId isn't mapped in occurrence extension, there should be a warning message!
    boolean foundWarningAboutUnmappedOccurrenceId = false;
    for (Iterator<TaskMessage> iter = generateDwca.report().getMessages().iterator(); iter.hasNext();) {
      TaskMessage msg = iter.next();
      if (msg.getMessage().startsWith("2 line(s) use ambiguous basisOfRecord")) {
        foundWarningAboutAmbiguousBOR = true;
      } else if (msg.getMessage().startsWith("1 empty line(s) skipped")) {
        foundWarningAboutEmptyLine = true;
      } else if (msg.getMessage().startsWith("No occurrenceId found in occurrence extension.")) {
        foundWarningAboutUnmappedOccurrenceId = true;
      }
    }
    assertTrue(foundWarningAboutAmbiguousBOR);
    assertTrue(foundWarningAboutEmptyLine);
    assertTrue(foundWarningAboutUnmappedOccurrenceId);
  }

  /**
   * A generated DwC-a with event core and occurrence extension missing a core recordID value is expected to
   * throw a GeneratorException.
   */
  @Test
  public void testGenerateCoreMissingID() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/event/resource.xml");
    File occurrence = FileUtils.getClasspathFile("resources/event/occurrence.txt");
    File event = FileUtils.getClasspathFile("resources/event/event_missing_id.txt");
    resource = getResource(resourceXML, event, occurrence);

    generateDwca =
      new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig, mockVocabulariesManager);
    assertThrows(GeneratorException.class, () -> generateDwca.call());
  }

  /**
   * A generated DwC-a with event core and occurrence extension with recordID unmapped is expected to
   * throw a GeneratorException.
   */
  @Test
  public void testGenerateCoreIDUnmapped() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/event/resource.xml");
    File occurrence = FileUtils.getClasspathFile("resources/event/occurrence.txt");
    File event = FileUtils.getClasspathFile("resources/event/event_id_unmapped.txt");
    resource = getResource(resourceXML, event, occurrence);

    generateDwca =
      new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig, mockVocabulariesManager);
    assertThrows(GeneratorException.class, () -> generateDwca.call());
  }

  /**
   * A generated DwC-a with event core and occurrence extension having a duplicate core recordID value is expected to
   * throw a GeneratorException.
   */
  @Test
  public void testGenerateCoreDuplicateID() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/event/resource.xml");
    File occurrence = FileUtils.getClasspathFile("resources/event/occurrence.txt");
    File event = FileUtils.getClasspathFile("resources/event/event_duplicate_id.txt");
    resource = getResource(resourceXML, event, occurrence);

    generateDwca =
      new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig, mockVocabulariesManager);
    assertThrows(GeneratorException.class, () -> generateDwca.call());
  }

  /**
   * A generated DwC-a with event core and occurrence extension missing a extension recordID value is expected to
   * throw a GeneratorException.
   */
  @Test
  public void testGenerateExtensionMissingID() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/event/resource.xml");
    File occurrence = FileUtils.getClasspathFile("resources/event/occurrence_missing_id.txt");
    File event = FileUtils.getClasspathFile("resources/event/event.txt");
    resource = getResource(resourceXML, event, occurrence);

    generateDwca =
      new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig, mockVocabulariesManager);
    assertThrows(GeneratorException.class, () -> generateDwca.call());
  }

  /**
   * A generated DwC-a with event core and occurrence extension missing a basisOfRecord value is expected to
   * throw a GeneratorException.
   */
  @Test
  public void testGenerateExtensionMissingBOR() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/event/resource.xml");
    File occurrence = FileUtils.getClasspathFile("resources/event/occurrence_missing_bor.txt");
    File event = FileUtils.getClasspathFile("resources/event/event.txt");
    resource = getResource(resourceXML, event, occurrence);

    generateDwca =
      new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig, mockVocabulariesManager);
    assertThrows(GeneratorException.class, () -> generateDwca.call());
  }

  /**
   * A generated DwC-a with event core and occurrence extension with basisOfRecord unmapped is expected to
   * throw a GeneratorException.
   */
  @Test
  public void testGenerateExtensionBORUnmapped() throws Exception {
    // retrieve sample zipped resource XML configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/event/resource.xml");
    File occurrence = FileUtils.getClasspathFile("resources/event/occurrence_bor_unmapped.txt");
    File event = FileUtils.getClasspathFile("resources/event/event.txt");
    resource = getResource(resourceXML, event, occurrence);

    generateDwca =
      new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig, mockVocabulariesManager);
    assertThrows(GeneratorException.class, () -> generateDwca.call());
  }

  /**
   * Generates a test sampling event resource having an event core with occurrence extension.
   */
  private Resource getResource(@NotNull File resourceXML, @NotNull File eventSourceFile,
    @NotNull File occurrenceSourceFile)
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
    mockHandler = mock(ResourceManagerImpl.class);
    resourceDir = FileUtils.createTempDir();
    BaseAction baseAction = new BaseAction(mockSimpleTextProvider, mockAppConfig, mockRegistrationManager);

    // construct ExtensionFactory using injected parameters
    Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());
    HttpClient httpClient = injector.getInstance(HttpClient.class);
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
    ExtensionFactory extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);
    JdbcSupport support = injector.getInstance(JdbcSupport.class);
    PasswordEncrypter passwordEncrypter = injector.getInstance(PasswordEncrypter.class);
    JdbcInfoConverter jdbcConverter = new JdbcInfoConverter(support);

    ExtensionManager extensionManager = mock(ExtensionManager.class);

    // construct event core Extension
    InputStream eventCoreIs = GenerateDwcaTest.class.getResourceAsStream("/extensions/dwc_event_2015-04-24.xml");
    Extension eventCore = extensionFactory.build(eventCoreIs);

    // construct occurrence core Extension
    InputStream occurrenceCoreIs = GenerateDwcaTest.class.getResourceAsStream(
      "/extensions/dwc_occurrence_2015-04-24.xml");
    Extension occurrenceCore = extensionFactory.build(occurrenceCoreIs);

    // mock ExtensionManager returning occurrence core Extension
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence")).thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Event")).thenReturn(eventCore);

    ExtensionRowTypeConverter extensionRowTypeConverter = new ExtensionRowTypeConverter(extensionManager);
    ConceptTermConverter conceptTermConverter = new ConceptTermConverter(extensionRowTypeConverter);

    // mock finding resource.xml file
    when(mockDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);

    // retrieve sample zipped resource folder
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/event/event.zip");

    // retrieve sample eml.xml file
    File emlXML = FileUtils.getClasspathFile("resources/event/eml.xml");
    // mock finding eml.xml file
    when(mockDataDir.resourceEmlFile(anyString())).thenReturn(emlXML);

    // mock finding dwca.zip file that does not exist
    when(mockDataDir.resourceDwcaFile(anyString())).thenReturn(new File("dwca.zip"));

    // create SourceManagerImpl
    mockSourceManager = new SourceManagerImpl(mock(AppConfig.class), mockDataDir);

    // create temp directory
    File tmpDataDir = FileUtils.createTempDir();
    when(mockDataDir.tmpDir()).thenReturn(tmpDataDir);

    // publication log file
    File publicationLogFile = new File(resourceDir, DataDir.PUBLICATION_LOG_FILENAME);
    when(mockDataDir.resourcePublicationLogFile(RESOURCE_SHORTNAME)).thenReturn(publicationLogFile);

    // create ResourceManagerImpl
    ResourceManagerImpl resourceManager =
      new ResourceManagerImpl(
          mockAppConfig,
          mockDataDir,
          mockEmailConverter,
          mockOrganisationKeyConverter,
          extensionRowTypeConverter,
          jdbcConverter,
          mockSourceManager,
          extensionManager,
          mockRegistryManager,
          conceptTermConverter,
          mockDwcaFactory,
          passwordEncrypter,
          mockEml2Rtf,
          mockVocabulariesManager,
          mockSimpleTextProvider,
          mockRegistrationManager);

    // create user
    User creator = new User();
    creator.setFirstname("Leonardo");
    creator.setLastname("Pisano");
    creator.setEmail("fi@liberabaci.com");
    creator.setLastLoginToNow();
    creator.setRole(User.Role.Manager);
    creator.setPassword("011235813");

    // create a new resource.
    resource = resourceManager.create(RESOURCE_SHORTNAME, Resource.CoreRowType.SAMPLINGEVENT.toString(),
      zippedResourceFolder, creator, baseAction);

    // copy event source file to tmp folder
    File copiedEvent = new File(resourceDir, "event.txt");
    // mock file to which event source file gets copied to
    when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(copiedEvent);
    // mock log file
    when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(new File(resourceDir, "log.txt"));
    // add event fileSource to test Resource
    FileSource eventFileSource = mockSourceManager.add(resource, eventSourceFile, "event.txt");
    resource.getMappings().get(0).setSource(eventFileSource);

    // copy occurrence source file to tmp folder
    File copiedOccurrence = new File(resourceDir, "occurrence.txt");
    // mock file to which occurrence source file gets copied to
    when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(copiedOccurrence);
    // add occurrence fileSource to test Resource
    FileSource occurrenceFileSource = mockSourceManager.add(resource, occurrenceSourceFile, "occurrence.txt");
    resource.getMappings().get(1).setSource(occurrenceFileSource);

    // mock creation of zipped dwca in temp directory - this later becomes the actual archive generated
    when(mockDataDir.tmpFile(anyString(), anyString())).thenReturn(new File(tmpDataDir, "dwca.zip"));

    // mock creation of versioned zipped dwca in resource directory
    when(mockDataDir.resourceDwcaFile(anyString(), any(BigDecimal.class)))
      .thenReturn(new File(resourceDir, VERSIONED_ARCHIVE_FILENAME));

    return resource;
  }
}
