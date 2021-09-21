package org.gbif.ipt.task;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.gbif.dwc.ArchiveFile;
import org.gbif.dwc.DwcFiles;
import org.gbif.dwc.record.Record;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.Archive;
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
import org.gbif.ipt.model.SqlSource;
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
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.ClosableIterator;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.properties.PropertiesUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateDwcaIT {

  private static final String RESOURCE_SHORTNAME = "res1";
  private static final String VERSIONED_ARCHIVE_FILENAME = "dwca-3.0.zip";

  private GenerateDwca generateDwca;
  private ReportHandler mockHandler = mock(ResourceManagerImpl.class);
  private DataDir mockDataDir = MockDataDir.buildMock();
  private AppConfig mockAppConfig = MockAppConfig.buildMock();
  private SourceManager mockSourceManager;
  private static VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);
  private File tmpDataDir;
  private File resourceDir;
  private static DbConfiguration dbCfg;

  public static class DbConfiguration {
    private String username;

    private String password;

    public DbConfiguration() {}

    public void setUsername(String username) {
      this.username = username;
    }

    public String getUsername() {
      return username;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getPassword() {
      return password;
    }
  }

  @BeforeClass
  public static void init() throws IOException {
    // load database account username and password from the properties file
    Properties p = PropertiesUtil.loadProperties("testdb.properties");
    dbCfg = new DbConfiguration();
    dbCfg.setUsername(p.getProperty("username"));
    dbCfg.setPassword(p.getProperty("password"));

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

  @Before
  public void setup() throws IOException {
    resourceDir = FileUtils.createTempDir();

    // tmp directory
    tmpDataDir = FileUtils.createTempDir();
    when(mockDataDir.tmpDir()).thenReturn(tmpDataDir);
  }

  /**
   * Test connects to postgres database (same db that Jenkins/Dev CLB ITs use). The SQL query retrieves a result set
   * that contains a null column value, and is designed to verify the following issue is fixed:
   * https://github.com/gbif/ipt/issues/1205
   * WARNING: Machine needs to be on GBIF VPN to connect to db, otherwise this test fails with connection error
   */
  @Test
  public void testHandlingNullTableColumnValue() throws Exception {
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence.txt");
    Resource resource = getResource(resourceXML, occurrence);

    // replace FileSource for SqlSource
    SqlSource sqlSource = new SqlSource();
    Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());
    JdbcSupport support = injector.getInstance(JdbcSupport.class);
    JdbcSupport.JdbcInfo psql = support.get("pgsql");
    sqlSource.setRdbms(psql);
    sqlSource.setDatabase("clb");
    sqlSource.setHost("builds.gbif.org");
    sqlSource.setUsername(dbCfg.getUsername());
    // column order matches occurrence.txt file, and corresponds to resource.xml mapping
    sqlSource.setSql(
      "SELECT n.id, scientificName, basisOfRecord, kingdom FROM name n, (VALUES(1, null, 'occurrence', 'Animalia')) AS t (occurrenceID, scientificName, basisOfRecord, kingdom) where n.id = t.occurrenceID;");
    sqlSource.setPassword(dbCfg.getPassword());
    resource.getMappings().get(0).setSource(sqlSource);

    generateDwca =
      new GenerateDwca(resource, mockHandler, mockDataDir, mockSourceManager, mockAppConfig, mockVocabulariesManager);
    Map<String, Integer> recordsByExtension = generateDwca.call();
    // count for occurrence core only
    assertEquals(1, recordsByExtension.size());

    // 1 row in result set
    String coreRowType = resource.getCoreRowType();
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, coreRowType);
    int recordCount = recordsByExtension.get(resource.getCoreRowType());
    assertEquals(1, recordCount);

    // confirm existence of versioned (archived) DwC-A "dwca-3.0.zip"
    File versionedDwca = new File(resourceDir, VERSIONED_ARCHIVE_FILENAME);
    assertTrue(versionedDwca.exists());

    // investigate the DwC-A
    File dir = FileUtils.createTempDir();
    CompressionUtil.decompressFile(dir, versionedDwca, true);

    Archive archive = DwcFiles.fromLocation(dir.toPath());
    assertEquals(DwcTerm.Occurrence, archive.getCore().getRowType());
    assertEquals(0, archive.getCore().getId().getIndex().intValue());
    assertEquals(4, archive.getCore().getFieldsSorted().size());

    // confirm data written to file
    ArchiveFile core = archive.getCore();
    ClosableIterator<Record> iterator = core.iterator();

    // 1st record
    Record record = iterator.next();
    assertEquals("1", record.column(0));
    assertEquals("occurrence", record.column(1));
    assertEquals("1", record.column(2));
    assertNull(record.column(3));
    assertEquals("occurrence", record.column(4));

    iterator.close();
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
   * @param sourceFile  source file
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
    HttpClient httpClient = injector.getInstance(HttpClient.class);
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
    ExtensionFactory extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);
    JdbcSupport support = injector.getInstance(JdbcSupport.class);
    PasswordConverter passwordConverter = injector.getInstance(PasswordConverter.class);
    JdbcInfoConverter jdbcConverter = new JdbcInfoConverter(support);

    // construct occurrence core Extension
    InputStream occurrenceCoreIs =
      GenerateDwcaTest.class.getResourceAsStream("/extensions/dwc_occurrence_2015-04-24.xml");
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
    when(mockDataDir.resourceEmlFile(anyString())).thenReturn(emlXML);

    // mock finding dwca.zip file that does not exist
    when(mockDataDir.resourceDwcaFile(anyString())).thenReturn(new File("dwca.zip"));

    // create SourceManagerImpl
    mockSourceManager = new SourceManagerImpl(mock(AppConfig.class), mockDataDir);

    // archival mode on
    when(mockAppConfig.isArchivalMode()).thenReturn(true);

    // create ResourceManagerImpl
    ResourceManagerImpl resourceManager =
      new ResourceManagerImpl(mockAppConfig, mockDataDir, mockEmailConverter, mockOrganisationKeyConverter,
        extensionRowTypeConverter, jdbcConverter, mockSourceManager, extensionManager, mockRegistryManager,
        conceptTermConverter, mockDwcaFactory, passwordConverter, mockEml2Rtf, mockVocabulariesManager,
        mockSimpleTextProvider, mockRegistrationManager);

    // create a new resource.
    // create user
    User creator = new User();
    creator.setFirstname("Leonardo");
    creator.setLastname("Pisano");
    creator.setEmail("fi@liberabaci.com");
    creator.setLastLoginToNow();
    creator.setRole(User.Role.Manager);
    creator.setPassword("011235813");
    Resource resource = resourceManager.create(RESOURCE_SHORTNAME, null, zippedResourceFolder, creator, baseAction);

    // copy source file to tmp folder
    File copied = new File(resourceDir, "occurrence.txt");

    // mock file to which source file gets copied to
    when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(copied);
    // publication log file
    File publicationLogFile = new File(resourceDir, DataDir.PUBLICATION_LOG_FILENAME);
    when(mockDataDir.resourcePublicationLogFile(RESOURCE_SHORTNAME)).thenReturn(publicationLogFile);
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
}
