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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.DataPackageFieldConverter;
import org.gbif.ipt.model.converter.DataPackageIdentifierConverter;
import org.gbif.ipt.model.converter.TableSchemaNameConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.manage.impl.ResourceConvertersManager;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.service.manage.impl.SourceManagerImpl;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.ipt.model.Agent;
import org.gbif.metadata.eml.ipt.model.KeywordSet;
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for the DCAT generation.
 */
public class GenerateDCATTest {
  private static final String RESOURCE_SHORTNAME = "res1";
  private static AppConfig mockAppConfig = MockAppConfig.buildMock();
  private static GenerateDCAT mockGenerateDCAT;
  private DataDir mockDataDir = MockDataDir.buildMock();
  private static RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);

  @BeforeAll
  public static void init() {
    when(mockAppConfig.getResourceArchiveUrl(RESOURCE_SHORTNAME)).thenReturn("distributionURL");
    when(mockAppConfig.getResourceUrl(RESOURCE_SHORTNAME)).thenReturn("resourceURL");
    when(mockAppConfig.getBaseUrl()).thenReturn("baseURL");

    //create IPT
    Ipt ipt = new Ipt();
    ipt.setDescription("Test IPT for testing");
    ipt.setName("Test IPT");
    ipt.setKey(UUID.randomUUID().toString());
    ipt.setOrganisationKey(UUID.randomUUID().toString());
    when(mockRegistrationManager.getIpt()).thenReturn(ipt);

    mockGenerateDCAT = new GenerateDCAT(mockAppConfig, mockRegistrationManager, mock(ResourceManager.class));
  }

  @Test
  public void testFeed() {
    Organisation orgStub = new Organisation();
    orgStub.setKey("d7dddbf4-2cf0-4f39-9b2a-bb099caae36c");
    orgStub.setName("test organisation");
    orgStub.setHomepageURL("[www.gbif.org]");
    when(mockRegistrationManager.getHostingOrganisation()).thenReturn(orgStub);
    String expectedFeed = "<https://www.gbif.org/publisher/d7dddbf4-2cf0-4f39-9b2a-bb099caae36c#Organization> a foaf:Agent ; foaf:name \"test organisation\" ; foaf:homepage <www.gbif.org> .";

    String actualFeed = mockGenerateDCAT.getFeed();

    assertTrue(actualFeed.contains(expectedFeed));
    verify(mockRegistrationManager, atLeastOnce()).getHostingOrganisation();
  }

  @Test
  public void testCreateCatalog() {
    String dcat = mockGenerateDCAT.createDCATCatalogInformation();
    assertTrue(dcat.contains("a dcat:Catalog"));
    assertTrue(dcat.contains("dct:title \"Test IPT\""));
    assertTrue(dcat.contains("dct:description \"Test IPT for testing\""));
    assertTrue(dcat.contains("dcat:themeTaxonomy <http://eurovoc.europa.eu/218403>"));
    assertTrue(dcat.contains("dct:license <https://creativecommons.org/publicdomain/zero/1.0/>"));
    assertTrue(dcat.contains(
      "dct:spatial [ a dct:Location ; locn:geometry \"{ \\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": [ 0.0,0.0 ] }\" ]"));
    assertTrue(dcat.contains("a skos:ConceptScheme"));
  }

  @Test
  public void testCreatePrefixes() {
    String prefixes = mockGenerateDCAT.createPrefixesInformation();
    assertTrue(prefixes.contains("@prefix schema: <http://schema.org/>"));
    assertTrue(prefixes.contains("@prefix dct: <http://purl.org/dc/terms/>"));
    assertTrue(prefixes.contains("@prefix adms: <http://www.w3.org/ns/adms#>"));
    assertTrue(prefixes.contains("@prefix xsd: <http://www.w3.org/2001/XMLSchema#>"));
    assertTrue(prefixes.contains("@prefix skos: <http://www.w3.org/2004/02/skos/core#>"));
    assertTrue(prefixes.contains("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"));
    assertTrue(prefixes.contains("@prefix vcard: <http://www.w3.org/2006/vcard/ns#>"));
    assertTrue(prefixes.contains("@prefix dcat: <http://www.w3.org/ns/dcat#>"));
    assertTrue(prefixes.contains("@prefix locn: <http://www.w3.org/ns/locn#>"));
    assertTrue(prefixes.contains("@prefix foaf: <http://xmlns.com/foaf/0.1/>"));
  }

  @Test
  public void testCreateDCATDataset() throws Exception {
    // create resource from single source file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    Resource res = getResource(resourceXML);
    assertNotNull(res.getTitle());
    assertEquals("TEST \"RESOURCE\"", res.getTitle());

    String dcat = mockGenerateDCAT.createDCATDatasetInformation(res);
    assertTrue(dcat.contains("a dcat:Dataset"));
    // ensure literals (e.g. title, description) are properly escaped
    assertTrue(dcat.contains("dct:title \"" + "TEST \\\"RESOURCE\\\"" + "\""));
    assertTrue(dcat.contains("dct:description \"" + "Test \\\"description\\\"" + "\""));
    // ensure duplicate keywords have been removed
    assertTrue(dcat.contains("dcat:keyword \"" + "Phytosociology" + "\"" + " , " + "\"" + "Occurrence" + "\"" + " , " + "\"" + "Observation" + "\"" + " ;"));
    assertTrue(dcat.contains("dcat:theme <http://eurovoc.europa.eu/5463>"));
    assertTrue(dcat.contains(
      "dcat:contactPoint [ a vcard:Individual ; vcard:fn \"Eric Stienen\"; vcard:hasEmail <mailto:eric.stienen@inbo.be> ]"));
    assertTrue(dcat.contains("dcat:distribution <distributionURL>"));
  }

  /**
   * Test that turtle format requiring line breaks to be escaped is honored.
   */
  @Test
  public void testCreateDCATDatasetNewline() throws Exception {
    // create resource from single source file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    Resource res = getResource(resourceXML);

    String dcat = mockGenerateDCAT.createDCATDatasetInformation(res);
    assertTrue(dcat.contains("a dcat:Dataset"));
    // ensure line break is properly escaped
    assertTrue(dcat.contains("dct:description \"" + "Test \\\"description\\\""));
    assertTrue(dcat.contains("dcat:distribution <distributionURL>"));
  }

  /**
   * Test that turtle format requiring three double quotes for string literals with CR or LF.
   */
  @Test
  public void testCreateDCATDatasetNewlineInsideParagraph() throws Exception {
    // create resource from single source file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    Resource res = getResource(resourceXML);

    String dcat = mockGenerateDCAT.createDCATDatasetInformation(res);
    assertTrue(dcat.contains("a dcat:Dataset"));
    // ensure line break is properly escaped
    assertTrue(dcat.contains("dct:description \"" + "Test \\\"description\\\""));
    assertTrue(dcat.contains("dcat:distribution <distributionURL>"));
  }

  @Test
  public void testCreateDCATDistribution() throws Exception {
    // create resource from single source file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    Resource res = getResource(resourceXML);
    assertNotNull(res.getEml().parseLicenseUrl());

    String dcat = mockGenerateDCAT.createDCATDistributionInformation(res);
    assertTrue(dcat.contains("a dcat:Distribution"));
    assertTrue(dcat.contains("dct:description \"Darwin Core Archive\""));
    assertTrue(dcat.contains("dct:format \"dwc-a\""));
    assertTrue(dcat.contains("dcat:mediaType \"application/zip\""));
    assertTrue(dcat.contains("dcat:downloadURL <distributionURL>"));
  }

  /**
   * Generates a test Resource from zipped resource folder, and populates resource with license, contacts, etc.
   *
   * @param resourceXML resource (XML) configuration file defining column mapping of sourceFile
   *
   * @return test Resource
   */
  private Resource getResource(@NotNull File resourceXML) throws Exception {
    UserAccountManager mockUserAccountManager = mock(UserAccountManager.class);
    UserEmailConverter mockEmailConverter = new UserEmailConverter(mockUserAccountManager);
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
    PasswordEncrypter passwordEncrypter = injector.getInstance(PasswordEncrypter.class);
    JdbcInfoConverter jdbcConverter = new JdbcInfoConverter(support);

    DataPackageSchemaManager mockSchemaManager = mock(DataPackageSchemaManager.class);

    // construct occurrence core Extension
    InputStream occurrenceCoreIs =
      GenerateDwcaTest.class.getResourceAsStream("/extensions/dwc_occurrence_2015-04-24.xml");
    Extension occurrenceCore = extensionFactory.build(occurrenceCoreIs);
    ExtensionManager extensionManager = mock(ExtensionManager.class);

    // mock ExtensionManager returning occurrence core Extension
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence")).thenReturn(occurrenceCore);

    ExtensionRowTypeConverter extensionRowTypeConverter = new ExtensionRowTypeConverter(extensionManager);
    ConceptTermConverter conceptTermConverter = new ConceptTermConverter(extensionRowTypeConverter);

    // mock finding resource.xml file
    when(mockDataDir.resourceFile(anyString())).thenReturn(resourceXML);

    // mock finding inferredMetadata.xml file
    when(mockDataDir.resourceInferredMetadataFile(anyString())).thenReturn(new File(DataDir.INFERRED_METADATA_FILENAME));

    // retrieve sample zipped resource folder
    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");

    // retrieve sample eml.xml file
    File emlXML = FileUtils.getClasspathFile("resources/res1/eml.xml");

    // mock finding eml.xml file
    when(mockDataDir.resourceEmlFile(anyString())).thenReturn(emlXML);

    // mock finding dwca.zip file that does not exist
    when(mockDataDir.resourceDwcaFile(anyString())).thenReturn(new File("dwca.zip"));

    // create SourceManagerImpl
    SourceManager mockSourceManager = new SourceManagerImpl(mock(AppConfig.class), mockDataDir);

    // create temp directory
    File tmpDataDir = FileUtils.createTempDir();
    when(mockDataDir.tmpDir()).thenReturn(tmpDataDir);

    ResourceConvertersManager mockResourceConvertersManager = new ResourceConvertersManager(
        mockEmailConverter, mockOrganisationKeyConverter, extensionRowTypeConverter,
        new ConceptTermConverter(extensionRowTypeConverter), mock(DataPackageIdentifierConverter.class),
        mock(TableSchemaNameConverter.class), mock(DataPackageFieldConverter.class), jdbcConverter);

    // create ResourceManagerImpl
    ResourceManagerImpl resourceManager =
      new ResourceManagerImpl(
          mockAppConfig,
          mockDataDir,
          mockResourceConvertersManager,
          mockSourceManager,
          extensionManager,
          mockSchemaManager,
          mockRegistryManager,
          mockDwcaFactory,
          mock(GenerateDataPackageFactory.class),
          passwordEncrypter,
          mockEml2Rtf,
          mockVocabulariesManager,
          mockSimpleTextProvider,
          mockRegistrationManager,
          mock(MetadataReader.class),
          mock(ResourceMetadataInferringService.class));

    // creator
    User creator = new User();
    creator.setEmail("jcuadra@gbif.org");
    creator.setLastname("Cuadra");

    // create a new resource.
    Resource resource = resourceManager.create(RESOURCE_SHORTNAME, null, zippedResourceFolder, creator, baseAction);

    // update resource title and description, to have double quotation marks which need to be escaped
    resource.setTitle("TEST \"RESOURCE\"");
    resource.getEml().setDescription("Test \"description\"");

    // update keyword sets: should be three, with "Occurrence" and "Observation" repeating more than once which breaks the feed
    resource.getEml().getKeywords().clear();

    KeywordSet keywordSet1 = new KeywordSet();
    keywordSet1.add("Phytosociology");
    keywordSet1.add("Occurrence");
    keywordSet1.add("Observation");
    keywordSet1.setKeywordThesaurus("n/a");

    KeywordSet keywordSet2 = new KeywordSet();
    keywordSet2.add("Occurrence");
    keywordSet2.setKeywordThesaurus("http://rs.gbif.org/vocabulary/gbif/dataset_type.xml");

    KeywordSet keywordSet3 = new KeywordSet();
    keywordSet3.add("Observation");
    keywordSet3.setKeywordThesaurus("http://rs.gbif.org/vocabulary/gbif/dataset_subtype.xml");

    resource.getEml().addKeywordSet(keywordSet1);
    resource.getEml().addKeywordSet(keywordSet2);
    resource.getEml().addKeywordSet(keywordSet3);

    // CCO
    resource.getEml().setIntellectualRights(
      "This work is licensed under <a href=\"http://creativecommons.org/publicdomain/zero/1.0/legalcode\">Creative Commons CCZero (CC0) 1.0 License</a>.");
    assertEquals("http://creativecommons.org/publicdomain/zero/1.0/legalcode", resource.getEml().parseLicenseUrl());

    // set creator, contact, and metadata provider
    Agent agent = new Agent();
    agent.setFirstName("Eric");
    agent.setLastName("Stienen");
    agent.addEmail("eric.stienen@inbo.be");
    resource.getEml().addCreator(agent);
    resource.getEml().addContact(agent);
    resource.getEml().addMetadataProvider(agent);

    return resource;
  }
}
