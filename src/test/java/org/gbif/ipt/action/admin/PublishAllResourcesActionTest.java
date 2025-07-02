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
package org.gbif.ipt.action.admin;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.config.TestBeanProvider;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.DataPackageFieldConverter;
import org.gbif.ipt.model.converter.DataPackageIdentifierConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.TableSchemaNameConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.ExtensionsHolder;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.manage.impl.ResourceConvertersManager;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.service.manage.impl.ResourceManagerImplTest;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.ReportHandler;
import org.gbif.ipt.validation.DataPackageMetadataValidator;
import org.gbif.metadata.eml.ipt.EmlFactory;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.parsers.SAXParserFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PublishAllResourcesActionTest extends IptBaseTest {

  private static final String DATASET_SUBTYPE_SPECIMEN_IDENTIFIER = "specimen";
  private static final String RESOURCE_SHORTNAME = "res2";

  private PublishAllResourcesAction action;

  private final AppConfig mockAppConfig = MockAppConfig.buildMock();
  private final VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);
  private final UserAccountManager mockUserAccountManager = mock(UserAccountManager.class);
  private final UserEmailConverter mockEmailConverter = new UserEmailConverter(mockUserAccountManager);
  private final RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
  private final OrganisationKeyConverter mockOrganisationKeyConverter = new OrganisationKeyConverter(mockRegistrationManager);
  private final SourceManager mockSourceManager = mock(SourceManager.class);
  private final RegistryManager mockRegistryManager = MockRegistryManager.buildMock();
  private final GenerateDwcaFactory mockDwcaFactory = mock(GenerateDwcaFactory.class);
  private final Eml2Rtf mockEml2Rtf = mock(Eml2Rtf.class);
  private final SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
  private final DataDir mockedDataDir = MockDataDir.buildMock();
  private final BaseAction baseAction = new BaseAction(mockSimpleTextProvider, mockAppConfig, mockRegistrationManager);

  private User creator;

  @TempDir
  File resourceDir;

  @BeforeEach
  public void setup() throws Exception {
    ResourceManagerImpl mockResourceManager = getResourceManagerImpl();
    // prepare and add resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // ensure resource has mandatory metadata filled in, meaning its EML validates and it has a valid publishing org
    Eml eml = EmlFactory.build(FileUtils.classpathStream("data/eml.xml"));
    eml.setEmlVersion(BigDecimal.valueOf(3.0));
    eml.setPreviousEmlVersion(BigDecimal.valueOf(1.0));
    resource.setEml(eml);
    // assign publishing organisation to resource
    Organisation o = new Organisation();
    o.setName("TestOrg");
    o.setKey(UUID.randomUUID().toString());
    resource.setOrganisation(o);

    // mock successful lookup for organization by key (done by RegistrationManager in EmlValidator)
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    when(mockRegistrationManager.get(any(UUID.class))).thenReturn(o);

    mockResourceManager.save(resource);
    // mock generateDwca() throwing PublicationException, not actually possible, but used to test failed publications
    GenerateDwcaFactory mockDwcaFactory = mockResourceManager.getDwcaFactory();
    when(mockDwcaFactory.create(any(Resource.class), any(ReportHandler.class)))
      .thenThrow(new PublicationException(PublicationException.TYPE.DWCA, "Mock exception"));

    // mock finding versioned EML file - not important which one
    File emlXML = File.createTempFile("eml-1.1", ".xml");
    when(mockedDataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(emlXML);

    // mock finding versioned RTF file - not important which one
    File rtf = File.createTempFile("short-1.1", ".rtf");
    when(mockedDataDir.resourceRtfFile(anyString(), any(BigDecimal.class))).thenReturn(rtf);

    // mock action
    action = new PublishAllResourcesAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
      mockRegistrationManager, mockResourceManager, mock(RegistryManager.class), mock(DataPackageMetadataValidator.class));

  }

  @Test
  public void testExecuteFailsOnPublish() throws Exception {
    Resource resource = action.resourceManager.get("res2");

    // make a few pre-publication assertions
    assertEquals(BigDecimal.valueOf(1.0), resource.getReplacedMetadataVersion());
    assertEquals(BigDecimal.valueOf(3.0), resource.getEmlVersion());
    assertEquals(BigDecimal.valueOf(3.0), resource.getEml().getEmlVersion());

    // populate a source mapping, and assign it to resource
    ExtensionMapping em = new ExtensionMapping();
    PropertyMapping pm = new PropertyMapping();
    pm.setTerm(DwcTerm.occurrenceID);
    pm.setIndex(1);
    Set<PropertyMapping> fields = new HashSet<>();
    fields.add(pm);
    em.setFields(fields);
    Extension extension = new Extension();
    extension.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);
    em.setExtension(extension);
    resource.addMapping(em);

    // trigger publish all
    String result = action.execute();

    // make some post-failed-publication assertions
    assertEquals("success", result);

    // PublicationException logged in ActionError
    assertEquals(2, action.getActionErrors().size());
    // # of publish event failures for resource captured
    assertEquals(1, action.resourceManager.getProcessFailures().size());
    assertFalse(action.resourceManager.hasMaxProcessFailures(resource));
    assertEquals(BigDecimal.valueOf(3.0), resource.getEml().getEmlVersion());
    assertNull(resource.getNextPublished());
    assertNull(resource.getLastPublished());

    // trigger publish all again
    action.execute();
    assertFalse(action.resourceManager.hasMaxProcessFailures(resource));
    // # of publish event failures for resource captured, should have incremented by 1
    assertEquals(2, action.resourceManager.getProcessFailures().size());

    // trigger publish all again
    action.execute();
    assertTrue(action.resourceManager.hasMaxProcessFailures(resource));
    // # of publish event failures for resource captured, should have incremented by 1
    assertEquals(3, action.resourceManager.getProcessFailures().size());

    // trigger publish all again
    action.execute();
    assertTrue(action.resourceManager.hasMaxProcessFailures(resource));
    // since max failures was reached, publication not scheduled, and number of publication failures stays the same
    assertEquals(3, action.resourceManager.getProcessFailures().size());
  }

  public ResourceManagerImpl getResourceManagerImpl() throws Exception {
    // mock creation of datasetSubtypes Map, with 2 occurrence subtypes, and 6 checklist subtypes
    Map<String, String> datasetSubtypes = new LinkedHashMap<>();
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
    HttpClient httpClient = TestBeanProvider.provideHttpClient();
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = TestBeanProvider.provideNsAwareSaxParserFactory();
    ExtensionFactory extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);
    JdbcSupport support = TestBeanProvider.provideJdbcSupport();
    PasswordEncrypter passwordEncrypter = new PasswordEncrypter(TestBeanProvider.providePasswordEncryption());
    JdbcInfoConverter jdbcConverter = new JdbcInfoConverter(support);

    // construct occurrence core Extension
    InputStream occurrenceCoreIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/dwc_occurrence.xml");
    Extension occurrenceCore = extensionFactory.build(occurrenceCoreIs);

    // construct occurrence core Extension
    InputStream eventCoreIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/dwc_event_2015-04-24.xml");
    Extension eventCore = extensionFactory.build(eventCoreIs);

    // construct simple images extension
    InputStream simpleImageIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/simple_image.xml");
    Extension simpleImage = extensionFactory.build(simpleImageIs);

    ExtensionManager extensionManager = mock(ExtensionManager.class);
    ExtensionsHolder extensionsHolder = mock(ExtensionsHolder.class);
    DataPackageSchemaManager mockSchemaManager = mock(DataPackageSchemaManager.class);

    // mock ExtensionManager returning different Extensions
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence"))
        .thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Event"))
        .thenReturn(eventCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord"))
        .thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.gbif.org/terms/1.0/Image"))
        .thenReturn(simpleImage);

    when(extensionsHolder.getExtensionsByRowtype()).thenReturn(
        Map.ofEntries(
            Map.entry("http://rs.tdwg.org/dwc/terms/Occurrence", occurrenceCore),
            Map.entry("http://rs.tdwg.org/dwc/terms/Event", eventCore),
            Map.entry("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord", occurrenceCore),
            Map.entry("http://rs.gbif.org/terms/1.0/Image", simpleImage)));

    ExtensionRowTypeConverter extensionRowTypeConverter = new ExtensionRowTypeConverter(extensionsHolder);
    ConceptTermConverter conceptTermConverter = new ConceptTermConverter(extensionRowTypeConverter);

    ResourceConvertersManager mockResourceConvertersManager = new ResourceConvertersManager(
        mockEmailConverter, mockOrganisationKeyConverter, extensionRowTypeConverter,
        conceptTermConverter, mock(DataPackageIdentifierConverter.class),
        mock(TableSchemaNameConverter.class), mock(DataPackageFieldConverter.class), jdbcConverter);

    // mock finding dwca.zip file that does not exist
    when(mockedDataDir.resourceDwcaFile(anyString())).thenReturn(new File("dwca.zip"));

    return new ResourceManagerImpl(
        mockAppConfig,
        mockedDataDir,
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
  }

  public Resource getNonRegisteredMetadataOnlyResource() throws Exception {
    // retrieve resource configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // copy to resource folder
    File copiedResourceXML = new File(resourceDir, DataDir.PERSISTENCE_FILENAME);
    org.apache.commons.io.FileUtils.copyFile(resourceXML, copiedResourceXML);
    // mock finding resource.xml file from resource directory
    when(mockedDataDir.resourceFile(anyString())).thenReturn(copiedResourceXML);

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
    when(mockedDataDir.resourceEmlFile(anyString())).thenReturn(copiedEmlXML);
    // mock finding versioned dwca file
    when(mockedDataDir.resourceDwcaFile(anyString(), eq(BigDecimal.valueOf(3.1))))
        .thenReturn(File.createTempFile("dwca-4.0", "zip"));
    // mock finding previous versioned dwca file
    when(mockedDataDir.resourceDwcaFile(anyString(), eq(BigDecimal.valueOf(3.0))))
        .thenReturn(File.createTempFile("dwca-3.0", "zip"));

    // retrieve sample rtf.xml
    File rtfXML = FileUtils.getClasspathFile("resources/res1/rtf-res1.rtf");
    // copy to resource folder
    File copiedRtfXML = new File(resourceDir, "rtf-res2.rtf");
    org.apache.commons.io.FileUtils.copyFile(rtfXML, copiedRtfXML);

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
    resource.setMetadataVersion(BigDecimal.valueOf(3.0));
    return resource;
  }
}
