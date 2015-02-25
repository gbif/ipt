/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.service.manage.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.UnsupportedArchiveException;
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
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.ipt.utils.ResourceUtils;
import org.gbif.metadata.eml.Eml;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceManagerImplTest {

  // Mock classes
  private AppConfig mockAppConfig = MockAppConfig.buildMock();
  private UserAccountManager mockUserAccountManager = mock(UserAccountManager.class);
  private UserEmailConverter mockEmailConverter = new UserEmailConverter(mockUserAccountManager);
  private RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
  private OrganisationKeyConverter mockOrganisationKeyConverter = new OrganisationKeyConverter(mockRegistrationManager);
  private JdbcInfoConverter mockJdbcConverter = mock(JdbcInfoConverter.class);
  private SourceManager mockSourceManager = mock(SourceManager.class);
  private RegistryManager mockRegistryManager = MockRegistryManager.buildMock();
  private GenerateDwcaFactory mockDwcaFactory = mock(GenerateDwcaFactory.class);
  private PasswordConverter mockPasswordConverter = mock(PasswordConverter.class);
  private Eml2Rtf mockEml2Rtf = mock(Eml2Rtf.class);
  private VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);
  private SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);

  private DataDir mockedDataDir = MockDataDir.buildMock();
  private BaseAction baseAction = new BaseAction(mockSimpleTextProvider, mockAppConfig, mockRegistrationManager);

  private User creator;
  private Resource resource;
  private Ipt ipt;
  private Organisation organisation;
  private JdbcSupport support;

  private File resourceDir;

  private static final String DATASET_TYPE_OCCURRENCE_IDENTIFIER = "occurrence";
  private static final String DATASET_SUBTYPE_SPECIMEN_IDENTIFIER = "specimen";
  private static final String RESOURCE_SHORTNAME = "res2";

  @Before
  public void setup() throws IOException {
    // create user.
    creator = new User();
    creator.setFirstname("Leonardo");
    creator.setLastname("Pisano");
    creator.setEmail("fi@liberabaci.com");
    creator.setLastLoginToNow();
    creator.setRole(Role.Manager);
    creator.setPassword("011235813");

    resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);

    // resource directory
    resourceDir = FileUtils.createTempDir();

    // tmp directory
    File tmpDataDir = FileUtils.createTempDir();
    when(mockedDataDir.tmpDir()).thenReturn(tmpDataDir);

    organisation = new Organisation();
    organisation.setKey("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");
    organisation.setName("Academy of Natural Sciences");

    ipt = new Ipt();
    ipt.setKey("27c24cba-13c5-47d1-96a1-16abd8f11437");
    ipt.setName("Test IPT");
  }

  public ResourceManagerImpl getResourceManagerImpl() throws IOException, SAXException, ParserConfigurationException {

    // mock creation of datasetSubtypes Map, with 2 occurrence subtypes, and 6 checklist subtypes
    Map<String, String> datasetSubtypes = new LinkedHashMap<String, String>();
    datasetSubtypes.put("", "Select a subtype");
    datasetSubtypes.put("taxonomicAuthority", "Taxonomic Authority");
    datasetSubtypes.put("nomenclatorAuthority", "Nomenclator Authority");
    datasetSubtypes.put("inventoryThematic", "Inventory Thematic");
    datasetSubtypes.put("inventoryRegional", "Inventory Regional");
    datasetSubtypes.put("globalSpeciesDataset", "Global Species Dataset");
    datasetSubtypes.put("derivedFromOccurrence", "Derived from Occurrence");
    datasetSubtypes.put(DATASET_SUBTYPE_SPECIMEN_IDENTIFIER, "Specimen");
    datasetSubtypes.put("observation", "Observation");
    // mock getting the vocabulary
    when(mockVocabulariesManager.getI18nVocab(anyString(), anyString(), anyBoolean())).thenReturn(datasetSubtypes);

    // mock the cfg
    when(mockAppConfig.getBaseUrl()).thenReturn("http://localhost:7001/ipt");
    // mock resource link used as EML GUID
    when(mockAppConfig.getResourceGuid("bees")).thenReturn("http://localhost:7001/ipt/resource?id=bees");
    when(mockAppConfig.getResourceGuid("res2")).thenReturn("http://localhost:7001/ipt/resource?id=res2");

    // construct ExtensionFactory using injected parameters
    Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());
    DefaultHttpClient httpClient = injector.getInstance(DefaultHttpClient.class);
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
    ExtensionFactory extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);
    support = injector.getInstance(JdbcSupport.class);
    PasswordConverter passwordConverter = injector.getInstance(PasswordConverter.class);
    JdbcInfoConverter jdbcConverter = new JdbcInfoConverter(support);

    // construct occurrence core Extension
    InputStream occurrenceCoreIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/dwc_occurrence.xml");
    Extension occurrenceCore = extensionFactory.build(occurrenceCoreIs);
    ExtensionManager extensionManager = mock(ExtensionManager.class);

    // mock ExtensionManager returning occurrence core Extension
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence")).thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord"))
      .thenReturn(occurrenceCore);

    ExtensionRowTypeConverter extensionRowTypeConverter = new ExtensionRowTypeConverter(extensionManager);
    ConceptTermConverter conceptTermConverter = new ConceptTermConverter(extensionRowTypeConverter);

    return new ResourceManagerImpl(mockAppConfig, mockedDataDir, mockEmailConverter, mockOrganisationKeyConverter,
      extensionRowTypeConverter, jdbcConverter, mockSourceManager, extensionManager, mockRegistryManager,
      conceptTermConverter, mockDwcaFactory, passwordConverter, mockEml2Rtf, mockVocabulariesManager,
      mockSimpleTextProvider, mockRegistrationManager);
  }

  /**
   * test resource creation from zipped resource folder.
   */
  @Test
  public void testCreateFromZippedFile()
    throws AlreadyExistingException, ImportException, SAXException, ParserConfigurationException, IOException,
    InvalidFilenameException {
    // retrieve sample zipped resource folder
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // mock finding resource.xml file
    when(mockedDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);

    // retrieve sample zipped resource folder
    File emlXML = FileUtils.getClasspathFile("resources/res1/eml.xml");
    // mock finding eml.xml file
    when(mockedDataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(emlXML);

    // create instance of manager
    ResourceManager resourceManager = getResourceManagerImpl();

    // retrieve sample zipped resource folder
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");

    // create a new resource.
    resourceManager.create("res1", null, zippedResourceFolder, creator, baseAction);

    // test if new resource was added to the resources list.
    assertEquals(1, resourceManager.list().size());

    // get added resource.
    Resource res = resourceManager.get("res1");

    // test if resource was added correctly.
    assertEquals("res1", res.getShortname());
    assertEquals(creator, res.getCreator());
    assertEquals(creator, res.getModifier());

    // test if resource.xml was created.
    assertTrue(mockedDataDir.resourceFile("res1", ResourceManagerImpl.PERSISTENCE_FILE).exists());

    // properties that get preserved
    // there is 1 source file
    assertEquals(1, res.getSources().size());
    assertEquals("occurrence", res.getSources().get(0).getName());
    assertEquals(18, res.getSource("occurrence").getColumns());
    assertEquals(1, ((TextFileSource) res.getSource("occurrence")).getIgnoreHeaderLines());
    assertEquals(15, ((TextFileSource) res.getSource("occurrence")).getRows());

    // there is 1 mapping
    assertEquals(1, res.getMappings().size());
    assertEquals("occurrence", res.getMappings().get(0).getSource().getName());
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, res.getMappings().get(0).getExtension().getRowType());
    assertEquals(4, res.getMappings().get(0).getFields().size());
    assertEquals(0, res.getMappings().get(0).getIdColumn().intValue());

    // properties that get reset
    assertEquals(Constants.INITIAL_RESOURCE_VERSION, res.getEmlVersion());
    // the resource shouldn't be registered
    assertFalse(res.isRegistered());
    // the resource shouldn't have any managers
    assertEquals(0, res.getManagers().size());
    // the resource shouldn't have a last published date
    assertNull(res.getLastPublished());
    // the resource shouldn't be registered (no org, no key)
    assertNull(res.getKey());
    assertNull(res.getOrganisation());
    // the status should be private
    assertEquals(PublicationStatus.PRIVATE, res.getStatus());
    // the resource should have a created date
    assertNotNull(res.getCreated());
    // the record count is 0
    assertEquals(0, res.getRecordsPublished());
    // the DOI was reset
    assertNull(res.getDoi());
    assertEquals(IdentifierStatus.UNRESERVED, res.getIdentifierStatus());
    assertNull(res.getDoiOrganisationKey());
    // the change summary was reset
    assertNull(res.getChangeSummary());
    // the VersionHistory was cleared
    assertEquals(0, res.getVersionHistory().size());
    // the auto-publication was reset
    assertEquals(PublicationMode.AUTO_PUBLISH_OFF, res.getPublicationMode());
    assertNull(res.getUpdateFrequency());
    assertNull(res.getNextPublished());
    // the other last modified dates were also reset
    assertNull(res.getMetadataModified());
    assertNull(res.getMappingsModified());
    assertNull(res.getSourcesModified());

    // eml properties loaded from eml.xml
    assertEquals("TEST RESOURCE", res.getEml().getTitle());
    assertEquals("Test description", res.getEml().getDescription());
    assertEquals(Constants.INITIAL_RESOURCE_VERSION, res.getEml().getEmlVersion());
  }

  /**
   * test resource creation from single DwC-A zipped file.
   */
  @Test
  public void testCreateFromSingleZippedFile()
    throws AlreadyExistingException, ImportException, SAXException, ParserConfigurationException, IOException,
    InvalidFilenameException {

    // create instance of manager
    ResourceManager resourceManager = getResourceManagerImpl();

    // retrieve sample DwC-A file
    File dwca = FileUtils.getClasspathFile("resources/occurrence.txt.zip");

    // create copy of DwC-A file in tmp dir, used to mock saving source resource filesource
    File tmpDir = FileUtils.createTempDir();
    List<File> files = CompressionUtil.decompressFile(tmpDir, dwca);
    File uncompressed = files.get(0);
    TextFileSource fileSource = new TextFileSource();
    fileSource.setFile(uncompressed);
    // it has 16 rows, plus 1 header line
    fileSource.setRows(16);
    fileSource.setIgnoreHeaderLines(1);
    fileSource.setEncoding("UTF-8");
    fileSource.setFieldsTerminatedByEscaped("/t");
    fileSource.setName("singleTxt");

    when(mockSourceManager.add(any(Resource.class), any(File.class), anyString())).thenReturn(fileSource);

    // create a new resource.
    resourceManager.create(RESOURCE_SHORTNAME, null, dwca, creator, baseAction);

    // test if new resource was added to the resources list.
    assertEquals(1, resourceManager.list().size());

    // get added resource.
    Resource res = resourceManager.get(RESOURCE_SHORTNAME);

    // test if resource was added correctly.
    assertEquals(RESOURCE_SHORTNAME, res.getShortname());
    assertEquals(creator, res.getCreator());
    assertEquals(creator, res.getModifier());

    // test if resource.xml was created.
    assertTrue(mockedDataDir.resourceFile(RESOURCE_SHORTNAME, ResourceManagerImpl.PERSISTENCE_FILE).exists());

    assertEquals(BigDecimal.valueOf(1.0), res.getEml().getEmlVersion());
    assertEquals(BigDecimal.valueOf(1.0), res.getEmlVersion());

    // note: source gets added to resource in sourceManager.add, and since we're mocking this call we can't set source

    // there is 1 mapping
    assertEquals(1, res.getMappings().size());
    assertEquals("singletxt", res.getMappings().get(0).getSource().getName());
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, res.getMappings().get(0).getExtension().getRowType());
    assertEquals(22, res.getMappings().get(0).getFields().size());
    assertEquals(0, res.getMappings().get(0).getIdColumn().intValue());

    // there are no eml properties except default shortname as title since there was no eml.xml file included
    assertEquals(RESOURCE_SHORTNAME, res.getEml().getTitle());
    assertEquals(null, res.getEml().getDescription());

    // properties that never get set on new resource creation

    // the resource shouldn't be registered
    assertFalse(res.isRegistered());
    // the resource shouldn't have any managers
    assertEquals(0, res.getManagers().size());
    // the resource shouldn't have a last published date
    assertNull(res.getLastPublished());
    // the resource shouldn't be registered (no org, no key)
    assertNull(res.getKey());
    assertNull(res.getOrganisation());
    // the status should be private
    assertEquals(PublicationStatus.PRIVATE, res.getStatus());
    // the resource should have a created date
    assertNotNull(res.getCreated());
    // the num rowIterator published is 0
    assertEquals(0, res.getRecordsPublished());
  }

  /**
   * test resource creation from single DwC-A gzipped file.
   */
  @Test
  public void testCreateFromSingleGzipFile()
    throws AlreadyExistingException, ImportException, SAXException, ParserConfigurationException, IOException,
    InvalidFilenameException {

    // create instance of manager
    ResourceManager resourceManager = getResourceManagerImpl();

    // retrieve sample gzip DwC-A file
    File dwca = FileUtils.getClasspathFile("resources/occurrence.txt.gz");

    // create copy of DwC-A file in tmp dir, used to mock saving source resource filesource
    File tmpDir = FileUtils.createTempDir();
    List<File> files = CompressionUtil.ungzipFile(tmpDir, dwca, false);
    File uncompressed = files.get(0);
    TextFileSource fileSource = new TextFileSource();
    fileSource.setFile(uncompressed);
    // it has 16 rows, plus 1 header line
    fileSource.setRows(16);
    fileSource.setIgnoreHeaderLines(1);
    fileSource.setEncoding("UTF-8");
    fileSource.setFieldsTerminatedByEscaped("/t");
    fileSource.setName("singleTxt");

    when(mockSourceManager.add(any(Resource.class), any(File.class), anyString())).thenReturn(fileSource);

    // create a new resource.
    resourceManager.create("res-single-gz", null, dwca, creator, baseAction);

    // test if new resource was added to the resources list.
    assertEquals(1, resourceManager.list().size());

    // get added resource.
    Resource res = resourceManager.get("res-single-gz");

    // test if resource was added correctly.
    assertEquals("res-single-gz", res.getShortname());
    assertEquals(creator, res.getCreator());
    assertEquals(creator, res.getModifier());

    // test if resource.xml was created.
    assertTrue(mockedDataDir.resourceFile("res-single-gz", ResourceManagerImpl.PERSISTENCE_FILE).exists());

    // note: source gets added to resource in sourceManager.add, and since we're mocking this call we can't set source

    // there is 1 mapping
    assertEquals(1, res.getMappings().size());
    assertEquals("singletxt", res.getMappings().get(0).getSource().getName());
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, res.getMappings().get(0).getExtension().getRowType());
    assertEquals(22, res.getMappings().get(0).getFields().size());
    assertEquals(0, res.getMappings().get(0).getIdColumn().intValue());

    // there are no eml properties except default shortname as title since there was no eml.xml file included
    assertEquals("res-single-gz", res.getEml().getTitle());
    assertEquals(null, res.getEml().getDescription());
  }

  /**
   * test resource creation from zipped file, but resource.xml references non-existent extension.
   */
  @Test(expected = ImportException.class)
  public void testCreateFromZippedFileNonexistentExtension()
    throws AlreadyExistingException, ImportException, SAXException, ParserConfigurationException, IOException,
    InvalidFilenameException {
    // retrieve sample zipped resource folder
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_nonexistent_ext.xml");
    // mock finding resource.xml file
    when(mockedDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);

    // create instance of manager
    ResourceManager resourceManager = getResourceManagerImpl();

    // retrieve sample zipped resource folder
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");

    // create a new resource.
    resourceManager.create("res1", null, zippedResourceFolder, creator, baseAction);
  }

  /**
   * test resource creation from file, but filename presumed to contain an illegal non-alphanumeric character
   */
  @Test(expected = InvalidFilenameException.class)
  public void testCreateFromZippedFileWithInvalidFilename()
    throws AlreadyExistingException, ImportException, SAXException, ParserConfigurationException, IOException,
    InvalidFilenameException {
    // create instance of manager
    ResourceManager resourceManager = getResourceManagerImpl();
    // mock SourceManager trying to add file with illegal character in filename
    when(mockSourceManager.add(any(Resource.class), any(File.class), anyString()))
      .thenThrow(new InvalidFilenameException("Bad filename!"));
    // retrieve sample gzip DwC-A file
    File dwca = FileUtils.getClasspathFile("resources/occurrence.txt.gz");
    // create a new resource, triggering exception
    resourceManager.create("res-single-gz", null, dwca, creator, baseAction);
  }

  /**
   * Create a resource from zipped file, but using a resource.xml whose occurrence core coreIdColumn mapping uses
   * auto-generated IDs. Since the auto-generating IDs feature is only available for taxon core extension since IPT 2.1
   * test that the occurrence core coreIdColumn mapping is reset to NO ID instead.
   */
  @Test
  public void testLoadFromDirResetAutoGeneratedIds()
    throws ParserConfigurationException, SAXException, IOException, AlreadyExistingException, ImportException,
    InvalidFilenameException {
    // retrieve resource.xml configuration file with occurrence core coreIdColumn mapping using auto-generated IDs
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_auto_ids.xml");
    // mock finding resource.xml file
    when(mockedDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);

    // create a new resource from zipped resource folder, but using the mocked resource.xml above
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");
    resourceManager.create("res1", null, zippedResourceFolder, creator, baseAction);

    // assert occurrence core ExtensionMapping coreID has been reset to NO ID inside loadFromDir()
    Resource created = resourceManager.get("res1");
    assertEquals(ExtensionMapping.NO_ID, created.getMappings().get(0).getIdColumn());
  }

  /**
   * Test simple resource creation.
   */
  @Test
  public void testSimpleCreate()
    throws AlreadyExistingException, SAXException, ParserConfigurationException, IOException {
    ResourceManager resourceManager = getResourceManagerImpl();

    // create a new resource.
    resourceManager.create("math", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);

    // test if new resource was added to the resources list.
    assertEquals(1, resourceManager.list().size());

    // get added resource.
    Resource addedResource = resourceManager.get("math");

    // test if resource was added correctly.
    assertEquals("math", addedResource.getShortname());
    assertEquals(creator, addedResource.getCreator());
    assertEquals(Constants.DATASET_TYPE_METADATA_IDENTIFIER, addedResource.getCoreType());

    // test if resource.xml was created.
    assertTrue(mockedDataDir.resourceFile("math", ResourceManagerImpl.PERSISTENCE_FILE).exists());
  }

  /**
   * Test resource retrieval from resource.xml file. The loadFromDir method is responsible for this retrieval.
   */
  @Test
  public void testLoadFromDir()
    throws IOException, SAXException, ParserConfigurationException, AlreadyExistingException {
    ResourceManagerImpl resourceManager = getResourceManagerImpl();

    String shortName = "ants";

    // create a new resource.
    resourceManager.create(shortName, DATASET_TYPE_OCCURRENCE_IDENTIFIER, creator);
    // get added resource.
    Resource addedResource = resourceManager.get(shortName);
    addedResource.setEmlVersion(Constants.INITIAL_RESOURCE_VERSION);
    // indicate it is a dataset subtype Specimen
    addedResource.setSubtype(DATASET_SUBTYPE_SPECIMEN_IDENTIFIER);

    // add SQL source, and save resource
    SqlSource source = new SqlSource();
    // connection/db params
    source.setName("danbif_db_source");
    source.setDatabase("DanBIF");
    source.setHost("50.19.64.6");
    source.setPassword("Dan=bif=17=5321");
    source.setUsername("DanBIFUser");
    source.setColumns(44);

    // query
    source.setSql("SELECT * FROM occurrence_record where datasetID=1");

    // other params
    source.setEncoding("UTF-8");
    source.setDateFormat("YYYY-MM-DD");
    source.setReadable(true);

    // rdbms param
    JdbcSupport.JdbcInfo info = support.get("mysql");
    source.setRdbms(info);

    // set resource on source
    source.setResource(addedResource);

    // add source to resource
    addedResource.addSource(source, true);

    // save
    resourceManager.save(addedResource);

    // retrieve resource file
    File resourceFile = mockedDataDir.resourceFile(shortName, "resource.xml");
    assertTrue(resourceFile.exists());

    // retrieve resource directory
    File resourceDir = resourceFile.getParentFile();
    assertTrue(resourceDir.exists());

    // load resource
    Resource persistedResource = resourceManager.loadFromDir(resourceDir);

    // make some assertions about resource
    assertEquals(shortName, persistedResource.getShortname());
    assertEquals(DATASET_TYPE_OCCURRENCE_IDENTIFIER, persistedResource.getCoreType());
    assertEquals(PublicationStatus.PRIVATE, persistedResource.getStatus());
    assertEquals(1, persistedResource.getSources().size());
    assertEquals(BigDecimal.valueOf(1.0), persistedResource.getEmlVersion());
    assertEquals(BigDecimal.valueOf(1.0), persistedResource.getEml().getEmlVersion());
    assertEquals(0, persistedResource.getRecordsPublished());
    // should be 1 KeywordSet corresponding to Dataset Type vocabulary
    assertEquals(2, persistedResource.getEml().getKeywords().size());
    assertEquals(StringUtils.capitalize(DATASET_TYPE_OCCURRENCE_IDENTIFIER),
      persistedResource.getEml().getKeywords().get(0).getKeywordsString());
    assertEquals(StringUtils.capitalize(DATASET_SUBTYPE_SPECIMEN_IDENTIFIER),
      persistedResource.getEml().getKeywords().get(1).getKeywordsString());

    // make some assertions about SQL source
    SqlSource persistedSource = (SqlSource) persistedResource.getSources().get(0);
    assertEquals("Dan=bif=17=5321", persistedSource.getPassword());
    assertEquals("danbif_db_source", persistedSource.getName());
    assertEquals("DanBIF", persistedSource.getDatabase());
    assertEquals("50.19.64.6", persistedSource.getHost());
    assertEquals("DanBIFUser", persistedSource.getUsername());
    assertEquals(44, persistedSource.getColumns());
    assertEquals("SELECT * FROM occurrence_record where datasetID=1", persistedSource.getSql());
    assertEquals("com.mysql.jdbc.Driver", persistedSource.getJdbcDriver());
    assertEquals("UTF-8", persistedSource.getEncoding());
    assertEquals("YYYY-MM-DD", persistedSource.getDateFormat());
    assertTrue(persistedSource.isReadable());

  }

  @Test
  public void testInferCoreType() throws IOException, SAXException, ParserConfigurationException {
    ResourceManagerImpl manager = getResourceManagerImpl();
    // create test resource
    Resource resource = new Resource();
    // add mapping to taxon core
    ExtensionMapping mapping = new ExtensionMapping();
    Extension ext = new Extension();
    ext.setRowType(Constants.DWC_ROWTYPE_TAXON);
    mapping.setExtension(ext);
    resource.addMapping(mapping);

    resource = manager.inferCoreType(resource);
    // assert the coreType has now been correctly inferred
    assertEquals(Resource.CoreRowType.CHECKLIST.toString().toLowerCase(), resource.getCoreType().toLowerCase());
  }

  @Test
  public void testInferSubtype() throws IOException, SAXException, ParserConfigurationException {
    ResourceManagerImpl manager = getResourceManagerImpl();
    // create test resource
    Resource resource = new Resource();
    resource.setSubtype("unknown");
    resource = manager.standardizeSubtype(resource);
    // assert the subtype has been set to null, since it doesn't correspond to a known vocab term
    assertEquals(null, resource.getSubtype());

    resource.setSubtype(DATASET_SUBTYPE_SPECIMEN_IDENTIFIER);
    resource = manager.standardizeSubtype(resource);
    // assert the subtype has been set to "specimen", since it does correspond to the known vocab term "specimen"
    assertEquals(DATASET_SUBTYPE_SPECIMEN_IDENTIFIER, resource.getSubtype());
  }

  @Test
  public void testUpdateAlternateIdentifierForIPTURLToResource()
    throws IOException, SAXException, ParserConfigurationException {
    ResourceManagerImpl manager = getResourceManagerImpl();

    // mock finding eml.xml file
    when(mockedDataDir.resourceEmlFile(anyString(), any(BigDecimal.class)))
      .thenReturn(File.createTempFile("eml", "xml"));

    // create PRIVATE test resource
    Resource resource = new Resource();
    resource.setShortname("bees");
    Eml eml = new Eml();
    eml.setTitle("Bees of Kansas");
    eml.setAlternateIdentifiers(new LinkedList<String>());
    resource.setEml(eml);
    resource.setStatus(PublicationStatus.PRIVATE);

    // update alt. id
    manager.updateAlternateIdentifierForIPTURLToResource(resource);

    // update the alt. id - it should not have been set, since the resource is Private
    assertTrue(resource.getEml().getAlternateIdentifiers().size() == 0);

    // change resource to PUBLIC
    resource.setStatus(PublicationStatus.PUBLIC);

    // mock returning the public resource URL
    when(mockAppConfig.getResourceUrl("bees")).thenReturn("http://localhost:7001/ipt/resource?r=bees");

    // update alt. id
    manager.updateAlternateIdentifierForIPTURLToResource(resource);

    // assert it has been set
    assertEquals("http://localhost:7001/ipt/resource?r=bees", resource.getEml().getAlternateIdentifiers().get(0));

    // mock changing the the baseURL now (returning a different public resource URL)
    when(mockAppConfig.getResourceUrl("bees")).thenReturn("http://192.38.28.24:7001/ipt/resource?r=bees");

    manager = new ResourceManagerImpl(mockAppConfig, mockedDataDir, mockEmailConverter, mockOrganisationKeyConverter,
      mock(ExtensionRowTypeConverter.class), mockJdbcConverter, mockSourceManager, mock(ExtensionManager.class),
      mockRegistryManager, mock(ConceptTermConverter.class), mockDwcaFactory, mockPasswordConverter, mockEml2Rtf,
      mockVocabulariesManager, mockSimpleTextProvider, mockRegistrationManager);

    // update alt. id
    manager.updateAlternateIdentifierForIPTURLToResource(resource);
    // assert it has been set
    assertEquals("http://192.38.28.24:7001/ipt/resource?r=bees", resource.getEml().getAlternateIdentifiers().get(0));

    // create PRIVATE test resource, with existing alt id
    resource.setStatus(PublicationStatus.PRIVATE);

    // update alt. id
    manager.updateAlternateIdentifierForIPTURLToResource(resource);

    // update the alt. id - it should disapear since the resource is Private now
    assertTrue(resource.getEml().getAlternateIdentifiers().size() == 0);
  }

  @Test
  public void testUpdateAlternateIdentifierForRegistry()
    throws IOException, SAXException, ParserConfigurationException {
    ResourceManagerImpl manager = getResourceManagerImpl();

    // mock finding eml.xml file
    when(mockedDataDir.resourceEmlFile(anyString(), any(BigDecimal.class)))
      .thenReturn(File.createTempFile("eml", "xml"));

    // create PRIVATE test resource
    Resource resource = new Resource();
    resource.setShortname("bees");
    Eml eml = new Eml();
    eml.setTitle("Bees of Kansas");
    eml.setAlternateIdentifiers(new LinkedList<String>());
    resource.setEml(eml);
    resource.setStatus(PublicationStatus.PRIVATE);

    // update alt. id
    manager.updateAlternateIdentifierForRegistry(resource);
    // update the alt. id - it should not have been set, since the resource isn't registered yet
    assertTrue(resource.getEml().getAlternateIdentifiers().size() == 0);

    // change resource to PUBLIC
    resource.setStatus(PublicationStatus.PUBLIC);
    // update alt. id
    manager.updateAlternateIdentifierForRegistry(resource);
    // update the alt. id - it should not have been set, since the resource isn't registered yet
    assertTrue(resource.getEml().getAlternateIdentifiers().size() == 0);

    // change resource to Registered and give it a Registry UUID
    UUID key = UUID.randomUUID();
    resource.setKey(key);
    resource.setStatus(PublicationStatus.REGISTERED);

    // update alt. id
    manager.updateAlternateIdentifierForRegistry(resource);
    // assert it has been set
    assertEquals(key.toString(), resource.getEml().getAlternateIdentifiers().get(0));

    // try to update alt. id again
    manager.updateAlternateIdentifierForRegistry(resource);
    // there should still only be 1
    assertTrue(resource.getEml().getAlternateIdentifiers().size() == 1);
  }

  @Test
  public void testRegisterMigratedResource() throws IOException, SAXException, ParserConfigurationException {
    ResourceManager manager = getResourceManagerImpl();

    String registeredDigirResourceUUID = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

    // indicate resource is migrated from DiGIR, by supplying the Registry UUID for the existing resource in the
    // resource's eml.alternateIdentifiers
    resource.getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    // mock returning list of resources that are associated to the Academy of Natural Sciences organization
    List<Resource> organisationsResources = new ArrayList<Resource>();
    Resource r1 = new Resource();
    r1.setKey(UUID.fromString(registeredDigirResourceUUID));
    r1.setTitle("Herpetology");
    organisationsResources.add(r1);

    when(mockRegistryManager.getOrganisationsResources(anyString())).thenReturn(organisationsResources);

    manager.register(resource, organisation, ipt, baseAction);

    // get registered resource.
    Resource registered = manager.get(resource.getShortname());

    assertEquals(PublicationStatus.REGISTERED, registered.getStatus());
    assertEquals(registeredDigirResourceUUID, registered.getKey().toString());
    assertEquals(organisation, registered.getOrganisation());
  }

  @Test(expected = InvalidConfigException.class)
  public void testRegisterMigratedResourceTooManyUUID() throws IOException, SAXException, ParserConfigurationException {
    ResourceManager manager = getResourceManagerImpl();

    String registeredDigirResourceUUID = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";
    String extraUUID = "7615e6d1-9ebd-4302-9a7e-4913ca8b2bb4";

    resource.getEml().getAlternateIdentifiers().clear();
    // indicate resource is migrated from DiGIR, by supplying the Registry UUID for the existing resource in the
    // resource's eml.alternateIdentifiers
    resource.getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    // add the extra (unwanted) UUID to list of alternate identifiers - at most there should be 1 only before reg.
    resource.getEml().getAlternateIdentifiers().add(extraUUID);

    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    manager.register(resource, organisation, ipt, baseAction);
  }

  @Test(expected = InvalidConfigException.class)
  public void testRegisterMigratedResourceWithBadUUID() throws IOException, SAXException, ParserConfigurationException {
    ResourceManager manager = getResourceManagerImpl();

    // supply random UUID in the resource's eml.alternateIdentifiers that won't match one of organisation's resources
    resource.getEml().getAlternateIdentifiers().clear();
    resource.getEml().getAlternateIdentifiers().add(UUID.randomUUID().toString());
    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    // mock returning list of resources that are associated to the Academy of Natural Sciences organization
    List<Resource> organisationsResources = new ArrayList<Resource>();
    Resource r1 = new Resource();
    // resource has different UUID than the one in the alternate identifiers list - interpreted as failed migration
    r1.setKey(UUID.fromString(UUID.randomUUID().toString()));
    r1.setTitle("Herpetology");
    organisationsResources.add(r1);

    when(mockRegistryManager.getOrganisationsResources(anyString())).thenReturn(organisationsResources);

    manager.register(resource, organisation, ipt, baseAction);
  }

  @Test(expected = InvalidConfigException.class)
  public void testRegisterMigratedResourceWithDuplicateUUIDCase1()
    throws IOException, SAXException, ParserConfigurationException, AlreadyExistingException {
    ResourceManagerImpl manager = getResourceManagerImpl();

    String registeredDigirResourceUUID = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

    // indicate resource is migrated from DiGIR, by supplying the Registry UUID for the existing resource in the
    // resource's eml.alternateIdentifiers
    resource.getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    // ensure there is at least one public resource already having an alternate identifier with this UUID
    manager.create("res1", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    manager.get("res1").getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    manager.get("res1").setStatus(PublicationStatus.PUBLIC);

    // should throw InvalidConfigException
    manager.register(resource, organisation, ipt, baseAction);
  }

  @Test(expected = InvalidConfigException.class)
  public void testRegisterMigratedResourceWithDuplicateUUIDCase2()
    throws IOException, SAXException, ParserConfigurationException, AlreadyExistingException {
    ResourceManagerImpl manager = getResourceManagerImpl();

    String registeredDigirResourceUUID = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

    // indicate resource is migrated from DiGIR, by supplying the Registry UUID for the existing resource in the
    // resource's eml.alternateIdentifiers
    resource.getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    // ensure there is at least one registered resource already having this UUID
    manager.create("res1", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    manager.get("res1").setKey(UUID.fromString(registeredDigirResourceUUID));
    manager.get("res1").setStatus(PublicationStatus.REGISTERED);

    // should throw InvalidConfigException
    manager.register(resource, organisation, ipt, baseAction);
  }

  @Test
  public void testDetectDuplicateUsesOfUUID()
    throws AlreadyExistingException, ParserConfigurationException, SAXException, IOException {
    ResourceManagerImpl manager = getResourceManagerImpl();

    UUID candidate = UUID.fromString("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");

    // ensure there is at least one public resource already having an alternate identifier with this UUID
    manager.create("res1", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    manager.get("res1").getEml().getAlternateIdentifiers().add(candidate.toString());
    manager.get("res1").setStatus(PublicationStatus.PUBLIC);

    // ensure there is at least one registered resource already having this UUID
    manager.create("res2", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    manager.get("res2").setKey(UUID.fromString(candidate.toString()));
    manager.get("res2").setStatus(PublicationStatus.REGISTERED);

    // create the resource that is to be registered
    manager.create("res3", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    manager.get("res3").setKey(UUID.fromString(candidate.toString()));
    manager.get("res3").setStatus(PublicationStatus.PUBLIC);

    // detect the number of duplicate usages of the UUID assigned to the resource about to get registered
    List<String> names = manager.detectDuplicateUsesOfUUID(candidate, "res3");

    assertEquals(2, names.size());
  }

  /**
   * test open archive of zipped file, with DwC-A located inside parent folder.
   */
  @Test
  public void testOpenArchiveInsideParentFolder() throws ParserConfigurationException, SAXException, IOException {
    // create instance of manager
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    // decompress archive
    File dwcaDir = FileUtils.createTempDir();
    // DwC-A located inside parent folder
    File dwca = FileUtils.getClasspathFile("resources/dwca-inside-parent.zip");
    // decompress the incoming file
    CompressionUtil.decompressFile(dwcaDir, dwca, true);
    // open DwC-A located inside parent folder
    Archive archive = resourceManager.openArchiveInsideParentFolder(dwcaDir);
    assertNotNull(archive);
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, archive.getCore().getRowType());
  }

  /**
   * test failure, opening archive of zipped file, with invalid DwC-A located inside parent folder.
   */
  @Test(expected = UnsupportedArchiveException.class)
  public void testOpenArchiveInsideParentFolderFails() throws ParserConfigurationException, SAXException, IOException {
    // create instance of manager
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    // decompress archive
    File dwcaDir = FileUtils.createTempDir();
    // DwC-A located inside parent folder, with invalid meta.xml
    File dwca = FileUtils.getClasspathFile("resources/dwca-inside-parent-invalid.zip");
    // decompress the incoming file
    CompressionUtil.decompressFile(dwcaDir, dwca, true);
    // open DwC-A located inside parent folder, which throws UnsupportedArchiveException wrapping SaxParseException
    resourceManager.openArchiveInsideParentFolder(dwcaDir);
  }

  /**
   * test null result, opening archive of zipped file not located inside parent folder.
   */
  @Test
  public void testOpenArchiveInsideParentFolderNull() throws ParserConfigurationException, SAXException, IOException {
    // create instance of manager
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    // decompress archive
    File dwcaDir = FileUtils.createTempDir();
    // DwC-A located inside parent folder, with invalid meta.xml
    File dwca = FileUtils.getClasspathFile("resources/occurrence.txt.zip");
    // decompress the incoming file
    CompressionUtil.decompressFile(dwcaDir, dwca, true);
    // open DwC-A, not located inside parent folder, which throws UnsupportedArchiveException wrapping SaxParseException
    Archive archive = resourceManager.openArchiveInsideParentFolder(dwcaDir);
    assertNull(archive);
  }

  @Test
  public void testPublishNonRegisteredMetadataOnlyResource()
    throws ParserConfigurationException, SAXException, IOException, AlreadyExistingException, ImportException,
    InvalidFilenameException {
    // create instance of manager
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // configure turning auto-publishing daily
    resource.setUpdateFrequency("daily");
    resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);

    // make a few pre-publication assertions
    assertEquals(BigDecimal.valueOf(3.0), resource.getEml().getEmlVersion());
    Date created = resource.getCreated();
    assertNotNull(created);
    Date pubDate = resource.getEml().getPubDate();
    assertNotNull(pubDate);
    Date lastPublished = resource.getLastPublished();
    assertNull(lastPublished);
    assertNull(resource.getNextPublished());
    assertEquals(Constants.DATASET_TYPE_METADATA_IDENTIFIER, resource.getCoreType());

    // publish
    resourceManager.publish(resource, BigDecimal.valueOf(3.1), baseAction);

    // make some post-publication assertions
    assertEquals(BigDecimal.valueOf(3.1), resource.getEml().getEmlVersion());
    assertNotNull(resource.getNextPublished());
    assertEquals(created.toString(), resource.getCreated().toString());
    assertNotEquals(pubDate.toString(), resource.getEml().getPubDate());
    assertNotNull(resource.getLastPublished().toString());
    assertTrue(new File(resourceDir, DataDir.EML_XML_FILENAME).exists());
    assertTrue(new File(resourceDir, "eml-3.1.xml").exists());
    assertTrue(new File(resourceDir, "rtf-res2.rtf").exists());
    assertTrue(new File(resourceDir, "rtf-res2-3.1.rtf").exists());
  }

  /**
   * Do publish, test trying to update DOI assigned to resource.
   * </br>
   * Publishes non-registered metadata-only resource that has been assigned a DOI. When trying to publish a new
   * minor version an exception is thrown because the DataCite metadata is invalid (missing publisher).
   */
  @Test(expected = PublicationException.class)
  public void testPublishResourceWithDOIAssignedButInvalidDOIMetadata()
    throws ParserConfigurationException, SAXException, IOException, AlreadyExistingException, ImportException,
    InvalidFilenameException {
    // create instance of manager
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // configure reserved DOI
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
    resource.setStatus(PublicationStatus.PUBLIC);
    Date released = new Date();
    resource.setLastPublished(released);
    // versionHistory
    VersionHistory history =
      new VersionHistory(new BigDecimal("3.0"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history.setModifiedBy(resource.getModifier());
    history.setDoi(doi);
    history.setStatus(IdentifierStatus.PUBLIC);
    history.setReleased(released);
    resource.addVersionHistory(history);

    // make a few pre-publication assertions
    assertEquals(new BigDecimal("3.0"), resource.getEml().getEmlVersion());
    Date created = resource.getCreated();
    assertNotNull(created);
    Date pubDate = resource.getEml().getPubDate();
    assertNotNull(pubDate);
    assertEquals(Constants.DATASET_TYPE_METADATA_IDENTIFIER, resource.getCoreType());
    assertTrue(resource.isAlreadyAssignedDoi());
    assertNotNull(resource.getAssignedDoi());
    assertEquals(new BigDecimal("3.0"), resource.getLastPublishedVersionsVersion());
    assertEquals(PublicationStatus.PUBLIC, resource.getLastPublishedVersionsPublicationStatus());
    assertEquals(new BigDecimal("3.1"), resource.getNextVersion());

    // publish, will try to update DOI, triggering exception
    resourceManager.publish(resource, resource.getNextVersion(), baseAction);
  }

  /**
   * Do publish, test trying to register DOI (first DOI assigned to resource).
   * </br>
   * Publishes non-registered metadata-only resource that has a DOI reserved, but no DOI assigned.
   * When trying to publish a new major version an exception is thrown because the DataCite metadata is invalid
   * (missing publisher).
   */
  @Test(expected = PublicationException.class)
  public void testPublishPublicResourceWithDOIReservedButInvalidDOIMetadata()
    throws ParserConfigurationException, SAXException, IOException, AlreadyExistingException, ImportException,
    InvalidFilenameException {
    // create instance of manager
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // configure reserved DOI
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.setStatus(PublicationStatus.PUBLIC);
    Date released = new Date();
    resource.setLastPublished(released);
    // versionHistory - no DOI
    VersionHistory history =
      new VersionHistory(new BigDecimal("3.0"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history.setModifiedBy(resource.getModifier());
    history.setReleased(released);
    resource.addVersionHistory(history);

    // make a few pre-publication assertions
    assertFalse(resource.isAlreadyAssignedDoi());
    assertNull(resource.getAssignedDoi());
    assertEquals(new BigDecimal("3.0"), resource.getLastPublishedVersionsVersion());
    assertEquals(PublicationStatus.PUBLIC, resource.getLastPublishedVersionsPublicationStatus());
    // next published version is a major version change
    assertEquals(new BigDecimal("4.0"), resource.getNextVersion());

    // publish, will try to register DOI, triggering exception
    resourceManager.publish(resource, resource.getNextVersion(), baseAction);
  }

  /**
   * Do publish, test trying to replace current assigned DOI with new DOI that has been reserved.
   * </br>
   * Publishes non-registered metadata-only resource that has a new DOI reserved, and existing DOI assigned.
   * When trying to publish a new major version an exception is thrown because the DataCite metadata is invalid
   * (missing publisher).
   */
  @Test(expected = PublicationException.class)
  public void testPublishPublicResourceWithDOIAssignedAndReservedButInvalidDOIMetadata()
    throws ParserConfigurationException, SAXException, IOException, AlreadyExistingException, ImportException,
    InvalidFilenameException {
    // create instance of manager
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // configure reserved DOI
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.setStatus(PublicationStatus.PUBLIC);
    Date released = new Date();
    resource.setLastPublished(released);
    // versionHistory - no DOI
    VersionHistory history =
      new VersionHistory(new BigDecimal("3.0"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history.setModifiedBy(resource.getModifier());
    history.setDoi(DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX));
    history.setStatus(IdentifierStatus.PUBLIC);
    history.setReleased(released);
    resource.addVersionHistory(history);

    // make a few pre-publication assertions
    assertTrue(resource.isAlreadyAssignedDoi());
    assertNotNull(resource.getAssignedDoi());
    assertEquals(new BigDecimal("3.0"), resource.getLastPublishedVersionsVersion());
    assertEquals(PublicationStatus.PUBLIC, resource.getLastPublishedVersionsPublicationStatus());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, resource.getIdentifierStatus());
    // next published version is a major version change
    assertEquals(new BigDecimal("4.0"), resource.getNextVersion());

    // publish, will try to replace DOI with new reserved DOI, triggering exception
    resourceManager.publish(resource, resource.getNextVersion(), baseAction);
  }

  @Test
  public void testHasMaxProcessFailures() throws ParserConfigurationException, SAXException, IOException {
    ResourceManagerImpl resourceManager = getResourceManagerImpl();

    ListMultimap<String, Date> processFailures = ArrayListMultimap.create();
    processFailures.put("res1", new Date());
    processFailures.put("res1", new Date());
    processFailures.put("res2", new Date());
    processFailures.put("res2", new Date());
    processFailures.put("res2", new Date());
    resourceManager.getProcessFailures().putAll(processFailures);

    Resource resource = new Resource();
    resource.setShortname("res1");
    resource.setTitle("Mammals");
    assertFalse(resourceManager.hasMaxProcessFailures(resource));
    resource.setShortname("res2");
    assertTrue(resourceManager.hasMaxProcessFailures(resource));
  }

  @Test(expected = PublicationException.class)
  public void testPublishNonRegisteredMetadataOnlyResourceFailure()
    throws ParserConfigurationException, SAXException, IOException, AlreadyExistingException, ImportException,
    InvalidFilenameException {
    // create instance of manager
    ResourceManagerImpl resourceManager = getResourceManagerImpl();
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // save resource
    resourceManager.save(resource);

    // make pre-publication assertions
    assertEquals(BigDecimal.valueOf(3.0), resource.getEml().getEmlVersion());

    //to trigger PublicationException, indicate publication already in progress (add Future to processFutures)
    resourceManager.getProcessFutures().put(resource.getShortname(), mock(Future.class));

    // publish, catching expected Exception
    resourceManager.publish(resource, BigDecimal.valueOf(4.0), baseAction);
  }

  /**
   * Ensure resource whose last published version is public gets returned in list of published public versions.
   */
  @Test
  public void testListPublishedPublicVersions()
    throws ParserConfigurationException, SAXException, IOException, InvalidFilenameException, ImportException,
    AlreadyExistingException {
    // create a new resource using configuration file (resource.xml) that has version history
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_version_history.xml");
    when(mockedDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);
    File emlXML = FileUtils.getClasspathFile("resources/res1/eml.xml");
    when(mockedDataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(emlXML);
    ResourceManager resourceManager = getResourceManagerImpl();
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");
    resourceManager.create("res1", null, zippedResourceFolder, creator, baseAction);

    assertEquals(1, resourceManager.list().size());
    Resource r = resourceManager.list().get(0);
    assertEquals(PublicationStatus.PRIVATE, r.getStatus());
    assertTrue(resourceManager.listPublishedPublicVersions().isEmpty());

    // mock last published version being public
    VersionHistory history = new VersionHistory(Constants.INITIAL_RESOURCE_VERSION, new Date(), PublicationStatus.PUBLIC);
    r.addVersionHistory(history);

    // test if last published version of resource was public (shown in list of public resources)
    assertEquals(1, resourceManager.listPublishedPublicVersions().size());
  }

  /**
   * Ensure resource whose last published version is registered gets returned in list of published public versions
   * despite not having a VersionHistory. Simulates pre IPT v2.2 resource, since VersionHistory was added from v2.2 on.
   */
  @Test
  public void testListPublishedRegisteredVersions()
    throws ParserConfigurationException, SAXException, IOException, InvalidFilenameException, ImportException,
    AlreadyExistingException {
    // create new resource from configuration file (resource.xml) that does not have version history
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    when(mockedDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);
    File emlXML = FileUtils.getClasspathFile("resources/res1/eml.xml");
    when(mockedDataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(emlXML);
    ResourceManager resourceManager = getResourceManagerImpl();
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");
    resourceManager.create("res1", null, zippedResourceFolder, creator, baseAction);

    // ensure resource is registered and it has no VersionHistory - simulating pre IPT v2.2 resource
    assertEquals(1, resourceManager.list().size());
    Resource created = resourceManager.list().get(0);
    created.setKey(UUID.randomUUID());
    created.setStatus(PublicationStatus.REGISTERED);
    assertTrue(created.isRegistered());
    assertTrue(created.getVersionHistory().isEmpty());

    // test if last published version of resource was public (shown in list of public resources)
    assertEquals(1, resourceManager.listPublishedPublicVersions().size());
  }

  /**
   * Return a Non Registered Metadata Only Resource used for testing.
   *
   * @return a Non Registered Metadata Only Resource used for testing
   */
  public Resource getNonRegisteredMetadataOnlyResource()
    throws IOException, SAXException, ParserConfigurationException, AlreadyExistingException, ImportException,
    InvalidFilenameException {
    // retrieve resource configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // copy to resource folder
    File copiedResourceXML = new File(resourceDir, ResourceManagerImpl.PERSISTENCE_FILE);
    org.apache.commons.io.FileUtils.copyFile(resourceXML, copiedResourceXML);
    // mock finding resource.xml file from resource directory
    when(mockedDataDir.resourceFile(anyString(), anyString())).thenReturn(copiedResourceXML);

    // retrieve sample eml.xml
    File emlXML = FileUtils.getClasspathFile("resources/res1/eml.xml");
    // copy to resource folder
    File copiedEmlXML = new File(resourceDir, DataDir.EML_XML_FILENAME);
    org.apache.commons.io.FileUtils.copyFile(emlXML, copiedEmlXML);

    // mock new saved eml-3.1.xml file being versioned (represents new minor version)
    File versionThreeEmlXML = new File(resourceDir, "eml-3.1.xml");
    org.apache.commons.io.FileUtils.copyFile(emlXML, versionThreeEmlXML);
    // mock finding versioned eml-3.1.xml file
    when(mockedDataDir.resourceEmlFile(anyString(), eq(BigDecimal.valueOf(3.1)))).thenReturn(versionThreeEmlXML);

    // mock new saved eml-4.0.xml file being versioned (represents new major version)
    File versionFourEmlXML = new File(resourceDir, "eml-4.0.xml");
    org.apache.commons.io.FileUtils.copyFile(emlXML, versionFourEmlXML);
    // mock finding versioned eml-4.0.xml file
    when(mockedDataDir.resourceEmlFile(anyString(), eq(BigDecimal.valueOf(4.0)))).thenReturn(versionFourEmlXML);

    // mock finding eml.xml file
    when(mockedDataDir.resourceEmlFile(anyString(), Matchers.<BigDecimal>eq(null))).thenReturn(copiedEmlXML);
    // mock finding versioned dwca file
    when(mockedDataDir.resourceDwcaFile(anyString(), eq(BigDecimal.valueOf(3.1))))
      .thenReturn(File.createTempFile("dwca-4.0", "zip"));
    // mock finding previous versioned dwca file
    when(mockedDataDir.resourceDwcaFile(anyString(), eq(BigDecimal.valueOf(3.0))))
      .thenReturn(File.createTempFile("dwca-3.0", "zip"));
    // mock finding dwca file
    when(mockedDataDir.resourceDwcaFile(anyString(), Matchers.<BigDecimal>eq(null)))
      .thenReturn(File.createTempFile("dwca", "zip"));

    // retrieve sample rtf.xml
    File rtfXML = FileUtils.getClasspathFile("resources/res1/rtf-res1.rtf");
    // copy to resource folder
    File copiedRtfXML = new File(resourceDir, "rtf-res2.rtf");
    org.apache.commons.io.FileUtils.copyFile(rtfXML, copiedRtfXML);
    // mock finding rtf-res2.xml file
    when(mockedDataDir.resourceRtfFile(anyString())).thenReturn(copiedRtfXML);

    // mock new saved rtf-res2-3.1.xml file being versioned (new minor version)
    File versionThreeRtfXML = new File(resourceDir, "rtf-res2-3.1.rtf");
    org.apache.commons.io.FileUtils.copyFile(rtfXML, versionThreeRtfXML);
    // mock finding versioned rtf-res2-3.1.xml file
    when(mockedDataDir.resourceRtfFile(anyString(), eq(BigDecimal.valueOf(3.1)))).thenReturn(versionThreeRtfXML);

    // mock new saved rtf-res2-4.0.xml file being versioned (new major version)
    File versionFourRtfXML = new File(resourceDir, "rtf-res2-4.0.rtf");
    org.apache.commons.io.FileUtils.copyFile(rtfXML, versionFourRtfXML);
    // mock finding versioned rtf-res2-4.0.xml file
    when(mockedDataDir.resourceRtfFile(anyString(), eq(BigDecimal.valueOf(4.0)))).thenReturn(versionFourRtfXML);

    // create ResourceManagerImpl
    ResourceManagerImpl resourceManager = getResourceManagerImpl();

    // create a new resource.
    Resource resource = resourceManager.create(RESOURCE_SHORTNAME, null, copiedEmlXML, creator, baseAction);
    resource.setEmlVersion(BigDecimal.valueOf(3.0));
    return resource;
  }

  /**
   * Needed by PublishAllResourcesActionTest.
   */
  public DataDir getMockedDataDir() {
    return mockedDataDir;
  }


  /**
   * Ensure previous published version can be reconstructed properly.
   */
  @Test
  public void testReconstructVersion() throws Exception {
    // create a new resource using configuration file (resource.xml) that has version history
    // and manually set organisation and a few Eml properties to mock new metadata entered for pending version
    File cfgFile = org.gbif.utils.file.FileUtils.getClasspathFile("resources/res1/resource_version_history.xml");
    when(mockedDataDir.resourceFile(anyString(), anyString())).thenReturn(cfgFile);
    File resourceDirectory = cfgFile.getParentFile();
    assertTrue(resourceDirectory.isDirectory());
    Resource resource = getResourceManagerImpl().loadFromDir(resourceDirectory);
    String shortname = "res1";
    assertEquals(shortname, resource.getShortname());
    BigDecimal version = new BigDecimal("1.1");
    assertEquals(version.toPlainString(), resource.getEmlVersion().toPlainString());
    DOI doi = new DOI("doi:10.5072/gc8abc");
    assertNotNull(resource.getDoi());
    assertEquals(doi.toString(), resource.getDoi().toString());
    assertNotNull(resource.getVersionHistory());
    assertEquals(2, resource.getVersionHistory().size());
    VersionHistory historyForVersionOnePointOne = resource.findVersionHistory(version);
    assertNotNull(historyForVersionOnePointOne.getDoi());
    assertEquals(doi.toString(), historyForVersionOnePointOne.getDoi().toString());
    assertEquals(IdentifierStatus.PUBLIC, historyForVersionOnePointOne.getStatus());
    assertEquals(PublicationStatus.PUBLIC, historyForVersionOnePointOne.getPublicationStatus());
    assertEquals(2, resource.getRecordsPublished());
    Organisation organisation = new Organisation();
    organisation.setKey("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");
    assertNull(resource.getOrganisation());
    resource.setOrganisation(organisation);
    assertEquals(organisation.getKey(), resource.getOrganisation().getKey());
    resource.getEml().setTitle("Title for pending version 1.2");
    resource.getEml().setDescription("Title for pending version 1.2");

    // retrieve previous persisted Eml file for version 1.1
    File emlXMLVersionOnePointOne = org.gbif.utils.file.FileUtils.getClasspathFile("resources/res1/eml-1.1.xml");
    // reconstruct resource version 1.1
    Resource reconstructed = ResourceUtils
      .reconstructVersion(version, shortname, doi, organisation, historyForVersionOnePointOne,
        emlXMLVersionOnePointOne);

    assertEquals(shortname, reconstructed.getShortname());
    assertEquals(version, reconstructed.getEmlVersion());
    assertEquals(doi, reconstructed.getDoi());
    assertEquals(IdentifierStatus.PUBLIC, reconstructed.getIdentifierStatus());
    assertEquals(PublicationStatus.PUBLIC, reconstructed.getStatus());
    assertEquals(historyForVersionOnePointOne.getReleased(), reconstructed.getLastPublished());
    assertEquals(organisation, reconstructed.getOrganisation());
    assertEquals(1, reconstructed.getRecordsPublished()); // changed
    // ensure reconstructed resource uses eml-1.1.xml
    assertEquals("Title for version 1.1", reconstructed.getEml().getTitle()); // changed
    assertEquals("Test description for version 1.1", reconstructed.getEml().getDescription()); // changed
  }

  @Test
  public void testConvertVersion() throws ParserConfigurationException, SAXException, IOException {
    Resource r = new Resource();
    r.setEmlVersion(BigDecimal.valueOf(4));
    assertEquals(0, r.getEmlVersion().scale());
    assertEquals(4, r.getEmlVersion().intValueExact());
    // do conversion 4 -> 4.0
    BigDecimal converted = getResourceManagerImpl().convertVersion(r);
    assertEquals(new BigDecimal("4.0"), converted);
    // ensure conversions aren't repeated
    r.setEmlVersion(converted);
    assertNull(getResourceManagerImpl().convertVersion(r));
  }

  @Test
  public void testConstructVersionHistoryForLastPublishedVersion() throws ParserConfigurationException, SAXException, IOException {
    Resource r = new Resource();
    r.setEmlVersion(new BigDecimal("4.0"));
    r.setStatus(PublicationStatus.PUBLIC);
    r.setRecordsPublished(100);
    Date lastPublished = new Date();
    r.setLastPublished(lastPublished);

    VersionHistory history = getResourceManagerImpl().constructVersionHistoryForLastPublishedVersion(r);
    assertNotNull(history);
    assertEquals("4.0", history.getVersion());
    assertEquals(lastPublished, history.getReleased());
    assertEquals(PublicationStatus.PUBLIC, history.getPublicationStatus());
    assertEquals(100, history.getRecordsPublished());

    // properties not set
    assertNull(history.getDoi());
    assertNull(history.getStatus());
    assertNull(history.getChangeSummary());
    assertNull(history.getModifiedBy());

    // next version?
    assertEquals("4.1",r.getNextVersion().toPlainString());
  }

  /**
   * Simulates what happens to a resource when upgrading an IPT to IPT v2.2:
   * Ensure resource created using IPT v2.1 loads successfully.
   * Specifically, it's important the version number gets converted from integer to major_version.minor_version format,
   * the eml, rtf, and dwca versioned files get renamed using the major_version.minor_version format, and that the
   * VersionHistory gets populated with the last published version.
   */
  @Test
  public void testLoadPre2Point2Resource()
    throws ParserConfigurationException, SAXException, IOException, InvalidFilenameException, ImportException,
    AlreadyExistingException {
    // create new resource from configuration file (resource.xml) that does not have version history
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource_v1_1.xml");
    when(mockedDataDir.resourceFile(anyString(), anyString())).thenReturn(resourceXML);

    // mock returning eml-19.xml in temp directory
    File eml = File.createTempFile("eml-19", ".xml", mockedDataDir.tmpDir());
    when(mockedDataDir.resourceEmlFile(resourceDir.getName(), new BigDecimal("19"))).thenReturn(eml);

    // mock returning eml-19.0.xml in temp directory, that doesn't exist!
    File emlNew = new File(eml.getParentFile(), "eml-19.0.xml");
    assertFalse(emlNew.exists());
    when(mockedDataDir.resourceEmlFile(resourceDir.getName(), new BigDecimal("19.0"))).thenReturn(emlNew);

    // mock returning res1-19.rtf in temp directory
    File rtf = File.createTempFile("res1-19", ".rtf", mockedDataDir.tmpDir());
    when(mockedDataDir.resourceRtfFile(resourceDir.getName(), new BigDecimal("19"))).thenReturn(rtf);

    // mock returning res1-19.0.rtf in temp directory, that doesn't exist!
    File rtfNew = new File(eml.getParentFile(), "res1-19.0.rtf");
    assertFalse(rtfNew.exists());
    when(mockedDataDir.resourceRtfFile(resourceDir.getName(), new BigDecimal("19.0"))).thenReturn(rtfNew);

    // mock returning dwca-19.zip in temp directory
    File dwca = File.createTempFile("dwca-19", ".zip", mockedDataDir.tmpDir());
    when(mockedDataDir.resourceDwcaFile(resourceDir.getName(), new BigDecimal("19"))).thenReturn(dwca);

    // mock returning dwca-19.0.zip in temp directory
    File dwcaNew = new File(dwca.getParentFile(), "dwca-19.0.zip");
    assertFalse(dwcaNew.exists());
    when(mockedDataDir.resourceDwcaFile(resourceDir.getName(), new BigDecimal("19.0"))).thenReturn(dwcaNew);

    ResourceManagerImpl resourceManager = getResourceManagerImpl();

    Resource loaded = resourceManager.loadFromDir(resourceDir);
    assertEquals("19.0", loaded.getEmlVersion().toPlainString());
    assertEquals(1, loaded.getVersionHistory().size());
    assertEquals(IdentifierStatus.UNRESERVED, loaded.getIdentifierStatus());

    // assert files exist now
    assertTrue(emlNew.exists());
    assertTrue(rtfNew.exists());
    assertTrue(dwcaNew.exists());

    // next version?
    assertEquals("19.1",loaded.getNextVersion().toPlainString());
  }
}
