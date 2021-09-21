package org.gbif.ipt.service.admin.impl;

import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtensionManagerImplTest {

  private static final TermFactory TERM_FACTORY = TermFactory.instance();
  private ExtensionManager extensionManager;
  private ExtensionFactory extensionFactory;
  private ResourceManager resourceManager;
  private AppConfig appConfig;

  @Before
  public void setup() throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    resourceManager = mock(ResourceManager.class);
    DataDir mockDataDir = mock(DataDir.class);
    appConfig = new AppConfig(mockDataDir);

    ConfigWarnings warnings = new ConfigWarnings();
    SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);

    Injector injector = Guice.createInjector(new ServletModule(), new Struts2GuicePluginModule(), new IPTModule());

    // construct ExtensionFactory using injected parameters
    HttpClient httpClient = injector.getInstance(HttpClient.class);
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = injector.getInstance(SAXParserFactory.class);
    extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);

    // construct mock RegistryManager:
    // mock getExtensions() response from Registry with local test resource (list of extensions from extensions.json)
    HttpClient mockHttpUtil = mock(HttpClient.class);
    ExtendedResponse mockResponse = mock(ExtendedResponse.class);
    mockResponse.setContent(IOUtils
      .toString(
          ExtensionManagerImplTest.class.getResourceAsStream("/responses/extensions_sandbox.json"),
          StandardCharsets.UTF_8));
    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager mockRegistryManager =
      new RegistryManagerImpl(appConfig, mockDataDir, mockHttpUtil, saxf, warnings, mockSimpleTextProvider,
        mockRegistrationManager, resourceManager);

    File myTmpDir = org.gbif.ipt.utils.FileUtils.createTempDir();
    assertTrue(myTmpDir.isDirectory());

    // copy occurrence core extension file to temporary directory
    File occCore = FileUtils.getClasspathFile("extensions/dwc_occurrence.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(occCore, myTmpDir);
    File tmpOccCore = new File(myTmpDir, "dwc_occurrence.xml");
    assertTrue(tmpOccCore.exists());

    // copy newer version of occurrence core extension to temporary directory
    File newerOccCore = FileUtils.getClasspathFile("extensions/dwc_occurrence_2015-04-24.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(newerOccCore, myTmpDir);
    File tmpNewerOccCore = new File(myTmpDir, "dwc_occurrence_2015-04-24.xml");
    assertTrue(tmpNewerOccCore.exists());

    // copy latest version of taxon core extension to temporary directory
    File taxonCore = FileUtils.getClasspathFile("extensions/dwc_taxon_2015-04-24.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(taxonCore, myTmpDir);
    File tmpTaxonCore = new File(myTmpDir, "dwc_taxon_2015-04-24.xml");
    assertTrue(tmpTaxonCore.exists());

    // copy latest version of event core extension to temporary directory
    File eventCore = FileUtils.getClasspathFile("extensions/dwc_event_2015-04-24.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(eventCore, myTmpDir);
    File tmpEventCore = new File(myTmpDir, "dwc_event_2015-04-24.xml");
    assertTrue(tmpEventCore.exists());

    // mock returning temporary files when looked up by their 'safe' filenames
    when(mockDataDir.tmpFile("http_rs_gbif_org_core_dwc_occurrence_xml.xml")).thenReturn(tmpOccCore);
    when(mockDataDir.tmpFile("http_rs_gbif_org_sandbox_core_dwc_occurrence_2015-04-24_xml.xml"))
      .thenReturn(tmpNewerOccCore);
    when(mockDataDir.tmpFile("http_rs_gbif_org_sandbox_core_dwc_taxon_2015-04-24_xml.xml"))
      .thenReturn(tmpTaxonCore);
    when(mockDataDir.tmpFile("http_rs_gbif_org_sandbox_core_dwc_event_2015-04-24_xml.xml"))
      .thenReturn(tmpEventCore);

    // Mock downloading extension into tmpFile - we're cheating by handling the actual file already as if it
    // were downloaded already. Furthermore, mock download() response with StatusLine with 200 OK response code
    StatusLine sl = mock(StatusLine.class);
    when(sl.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(mockHttpUtil.download(any(URL.class), any(File.class))).thenReturn(sl);

    // mock returning newly created occurrence core extension file
    File occCoreExtension = new File(myTmpDir, "http_rs_tdwg_org_dwc_terms_Occurrence.xml");
    when(mockDataDir.configFile(ExtensionManagerImpl.CONFIG_FOLDER + "/http_rs_tdwg_org_dwc_terms_Occurrence.xml"))
      .thenReturn(occCoreExtension);

    // mock returning newly created taxon core extension file
    File taxonCoreExtension = new File(myTmpDir, "http_rs_tdwg_org_dwc_terms_Taxon.xml");
    when(mockDataDir.configFile(ExtensionManagerImpl.CONFIG_FOLDER + "/http_rs_tdwg_org_dwc_terms_Taxon.xml"))
      .thenReturn(taxonCoreExtension);

    // mock returning newly created event core extension file
    File eventCoreExtension = new File(myTmpDir, "http_rs_tdwg_org_dwc_terms_Event.xml");
    when(mockDataDir.configFile(ExtensionManagerImpl.CONFIG_FOLDER + "/http_rs_tdwg_org_dwc_terms_Event.xml"))
      .thenReturn(eventCoreExtension);

    // create instance
    extensionManager =
      new ExtensionManagerImpl(appConfig, mockDataDir, extensionFactory, resourceManager, mockHttpUtil, warnings,
        mockSimpleTextProvider, mockRegistrationManager, mockRegistryManager);
  }

  @Test
  public void testInstallCoreTypes() {
    extensionManager.installCoreTypes();
    assertEquals(3, extensionManager.list().size());
    // get ext. and assert a couple properties
    Extension ext = extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence");
    assertEquals(169, ext.getProperties().size());
    // confirm the extension attributes are read correctly from the XML (not the JSON)
    assertEquals("Darwin Core Occurrence", ext.getTitle());
    assertEquals("Occurrence", ext.getName());
    assertTrue(ext.getDescription().startsWith("The category"));
    assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#Occurrence", ext.getLink().toString());
    assertNotNull(ext.getIssued());
    assertEquals("dwc:Taxon dwc:Event", ext.getSubject());
    assertNull(ext.getUrl());
    assertFalse(ext.isLatest()); // this isn't persisted, only populated when deserialising JSON list from registry
  }

  @Test
  public void testListCore() {
    extensionManager.installCoreTypes();
    assertEquals(3, extensionManager.list().size());

    // of the three cores, only the occurrence core is suitable for use on the taxon core
    List<Extension> results = extensionManager.listCore(Constants.DWC_ROWTYPE_TAXON);
    assertEquals(1, results.size());

    // of the three cores, only the occurrence core is suitable for use on the event core
    results = extensionManager.listCore(Constants.DWC_ROWTYPE_EVENT);
    assertEquals(1, results.size());
  }

  @Test
  public void testList() {
    extensionManager.installCoreTypes();
    assertEquals(3, extensionManager.list().size());

    // search excludes core types, otherwise it would return the occurrence core which is suitable for use on taxon core
    List<Extension> results = extensionManager.list(Constants.DWC_ROWTYPE_TAXON);
    assertEquals(0, results.size());
  }

  /**
   * Test when IPT is configured with extra core type not matching a registered extension.
   */
  @Test(expected = InvalidConfigException.class)
  public void testInstallCoreTypesBadCoreConfiguration() throws Exception {
    File tmpDir = org.gbif.ipt.utils.FileUtils.createTempDir();;
    File dataDirLocation = new File(tmpDir, "datadir.location");
    File testDataDir = FileUtils.getClasspathFile("dataDir");
    org.apache.commons.io.FileUtils.copyDirectoryToDirectory(testDataDir, tmpDir); // copy testDataDir to tmp location
    File dataDir = new File(tmpDir, "dataDir");
    assertTrue(dataDir.isDirectory() && dataDir.exists());
    // Configure IPT, with extra core type
    DataDir builtDataDir = DataDir.buildFromLocationFile(dataDirLocation);
    builtDataDir.setDataDir(dataDir);
    appConfig = new AppConfig(builtDataDir);
    // trigger exception
    extensionManager.installCoreTypes();
  }

  /**
   * Update an extension that has no associated mappings to it.
   */
  @Test
  public void testUpdate() throws IOException {
    // first install old version of occurrence extension
    extensionManager.install(new URL("http://rs.gbif.org/core/dwc_occurrence.xml"));
    Extension ext = extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence");
    assertEquals(161, ext.getProperties().size());
    assertNull(ext.getIssued());

    // now update to latest version of occurrence extension issued 2015-04-24
    extensionManager.update(Constants.DWC_ROWTYPE_OCCURRENCE);
    ext = extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence");
    assertEquals(169, ext.getProperties().size());
    assertNotNull(ext.getIssued());
  }

  /**
   * Update an extension that has an associated mappings to it.
   */
  @Test
  public void testUpdateWithAssociatedMappings() throws IOException {
    // first install old version of occurrence extension
    extensionManager.install(new URL("http://rs.gbif.org/core/dwc_occurrence.xml"));
    Extension ext = extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence");
    assertEquals(161, ext.getProperties().size());
    assertNull(ext.getIssued());

    Resource r = getTestResource(ext);

    // populate list of resources, and mock resourceManager.list()
    List<Resource> resources = new ArrayList<>();
    resources.add(r);
    when(resourceManager.list()).thenReturn(resources);

    // now update to latest version of occurrence extension issued 2015-04-24
    extensionManager.update(Constants.DWC_ROWTYPE_OCCURRENCE);
    ext = extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence");
    assertEquals(169, ext.getProperties().size());
    assertNotNull(ext.getIssued());

    // verify migration was successful
    ExtensionMapping migrated = r.getMapping(Constants.DWC_ROWTYPE_OCCURRENCE, 0);
    assertNotNull(migrated.getExtension().getIssued());
    // test for example index 3 (rights term should have been replaced by dc:license)
    PropertyMapping licenseMapping = migrated.getField(DcTerm.license.qualifiedName());
    assertEquals(0, licenseMapping.getIndex().compareTo(3));
  }

  @Test
  public void testMigrateResourceToNewExtensionVersion() throws IOException {
    ExtensionManagerImpl manager =
      new ExtensionManagerImpl(mock(AppConfig.class), mock(DataDir.class), extensionFactory,
        mock(ResourceManager.class), mock(HttpClient.class), mock(ConfigWarnings.class), mock(SimpleTextProvider.class),
        mock(RegistrationManager.class), mock(RegistryManager.class));
    File myTmpDir = org.gbif.ipt.utils.FileUtils.createTempDir();

    // load current (installed) version of Occurrence extension
    File occCore = FileUtils.getClasspathFile("extensions/dwc_occurrence.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(occCore, myTmpDir);
    File tmpOccCore = new File(myTmpDir, "dwc_occurrence.xml");
    Extension current = manager.loadFromFile(tmpOccCore);
    assertNotNull(current);

    // load newer (latest) version of Occurrence extension
    File newerOccCore = FileUtils.getClasspathFile("extensions/dwc_occurrence_2015-04-24.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(newerOccCore, myTmpDir);
    File tmpNewerOccCore = new File(myTmpDir, "dwc_occurrence_2015-04-24.xml");
    Extension newer = manager.loadFromFile(tmpNewerOccCore);
    assertNotNull(newer);

    Resource r = getTestResource(current);

    // perform migration
    manager.migrateResourceToNewExtensionVersion(r, current, newer);

    // verify migration was successful
    ExtensionMapping migrated = r.getMapping(Constants.DWC_ROWTYPE_OCCURRENCE, 0);

    // index 0 (id term should have stayed the same)
    PropertyMapping verifiedIdMapping = migrated.getField(DwcTerm.occurrenceID.qualifiedName());
    assertEquals(0, verifiedIdMapping.getIndex().compareTo(0));

    // index 1 (individualID term should have been replaced by dwc:organismID)
    PropertyMapping organismIdMapping = migrated.getField(DwcTerm.organismID.qualifiedName());
    assertEquals(0, organismIdMapping.getIndex().compareTo(1));

    // index 3 (rights term should have been replaced by dc:license)
    PropertyMapping licenseMapping = migrated.getField(DcTerm.license.qualifiedName());
    assertEquals(0, licenseMapping.getIndex().compareTo(3));

    // index 2 (formerly dc:source) and index 4 (formerly dwc:occurrenceDetails) could both be migrated to dc:references
    // only one property mapping to dc:references can exist though
    PropertyMapping referencesMapping = migrated.getField(DcTerm.references.qualifiedName());
    assertTrue(referencesMapping.getIndex().compareTo(2) == 0 || referencesMapping.getIndex().compareTo(4) == 0);
  }

  /**
   * Create and return a Resource having an ExtensionMapping to Occurrence extension with some property mappings.
   */
  private Resource getTestResource(Extension extension) {
    Resource r = new Resource();
    r.setShortname("ants");
    ExtensionMapping em = new ExtensionMapping();
    em.setExtension(extension);
    Set<PropertyMapping> propertyMappings = new HashSet<>();

    // index 0 (id term)
    PropertyMapping idMapping = new PropertyMapping();
    Term occurrenceIdTerm = TERM_FACTORY.findTerm("http://rs.tdwg.org/dwc/terms/occurrenceID");
    idMapping.setTerm(occurrenceIdTerm);
    idMapping.setIndex(0);
    em.setIdColumn(0);
    propertyMappings.add(idMapping);

    // index 1 (deprecated term that will get replaced by dwc:organismID)
    PropertyMapping individualIdMapping = new PropertyMapping();
    Term individualIdTerm = TERM_FACTORY.findTerm("http://rs.tdwg.org/dwc/terms/individualID");
    individualIdMapping.setTerm(individualIdTerm);
    individualIdMapping.setIndex(1);
    propertyMappings.add(individualIdMapping);

    // index 2 (deprecated term that will get replaced by dc:references
    PropertyMapping sourceMapping = new PropertyMapping();
    Term sourceTerm = TERM_FACTORY.findTerm("http://purl.org/dc/terms/source");
    sourceMapping.setTerm(sourceTerm);
    sourceMapping.setIndex(2);
    propertyMappings.add(sourceMapping);

    // index 3 (deprecated term that will get replaced by dc:references)
    PropertyMapping rightsMapping = new PropertyMapping();
    Term rightsTerm = TERM_FACTORY.findTerm("http://purl.org/dc/terms/rights");
    rightsMapping.setTerm(rightsTerm);
    rightsMapping.setIndex(3);
    propertyMappings.add(rightsMapping);

    // index 4 (deprecated term that will get replaced by dc:references)
    PropertyMapping occDetailsMapping = new PropertyMapping();
    Term occDetailsTerm = TERM_FACTORY.findTerm("http://rs.tdwg.org/dwc/terms/occurrenceDetails");
    occDetailsMapping.setTerm(occDetailsTerm);
    occDetailsMapping.setIndex(4);
    propertyMappings.add(occDetailsMapping);

    // confirm we have the right number property mappings in the extension mapping
    assertEquals(5, propertyMappings.size());
    em.setFields(propertyMappings);
    r.addMapping(em);
    // confirm resource occurrence extension mapping can be retrieved
    assertEquals(1, r.getMappings(Constants.DWC_ROWTYPE_OCCURRENCE).size());
    assertNull(em.getExtension().getIssued());
    return r;
  }

  @Test
  public void testGetRedundantGroups() throws IOException {
    ExtensionManagerImpl manager =
      new ExtensionManagerImpl(mock(AppConfig.class), mock(DataDir.class), extensionFactory,
        mock(ResourceManager.class), mock(HttpClient.class), mock(ConfigWarnings.class), mock(SimpleTextProvider.class),
        mock(RegistrationManager.class), mock(RegistryManager.class));
    File myTmpDir = org.gbif.ipt.utils.FileUtils.createTempDir();

    // load Occurrence extension
    File occ = FileUtils.getClasspathFile("extensions/dwc_occurrence.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(occ, myTmpDir);
    File tmpOccFile = new File(myTmpDir, "dwc_occurrence.xml");
    Extension occExt = manager.loadFromFile(tmpOccFile);

    // load Event (core) extension
    File evt = FileUtils.getClasspathFile("extensions/dwc_event_2015-04-24.xml");
    org.apache.commons.io.FileUtils.copyFileToDirectory(evt, myTmpDir);
    File tmpEvtFile = new File(myTmpDir, "dwc_event_2015-04-24.xml");
    Extension evtExt = manager.loadFromFile(tmpEvtFile);

    List<String> redundant = manager.getRedundantGroups(occExt, evtExt);

    // confirm Occurrence extension has 4 redundant groups that already appear in the core Event extension
    assertEquals(4, redundant.size());
    assertTrue(redundant.contains("Event"));
    assertTrue(redundant.contains("Record Level"));
    assertTrue(redundant.contains("Location"));
    assertTrue(redundant.contains("GeologicalContext"));
  }
}
