/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.task;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.mock.*;
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
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for the DCAT generation
 */
public class GenerateDCATTest {

    private static final String RESOURCE_SHORTNAME = "res1";
    private static final String VERSIONED_ARCHIVE_FILENAME = "dwca-3.0.zip";

    private GenerateDwca generateDwca;
    private Resource resource;
    private User creator;
    private ReportHandler mockHandler;
    private DataDir mockDataDir = MockDataDir.buildMock();
    private AppConfig mockAppConfig = MockAppConfig.buildMock();
    private SourceManager mockSourceManager;
    private GenerateDCAT mockGenerateDCAT;
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

        mockGenerateDCAT = new GenerateDCAT(mockAppConfig, MockRegistrationManager.buildMock(), MockResourceManager.buildMock());
    }

    @Test
    public void testCreatePrefixes() {
        System.out.println(mockGenerateDCAT.createPrefixesInformation());
    }

    @Test
    public void testCreateDCATDataset() {
        Resource res = null;
        // retrieve sample zipped resource XML configuration file
        File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
        // create resource from single source file
        File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence.txt");
        try {
            res = getResource(resourceXML, occurrence);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(mockGenerateDCAT.createDCATDatasetInformation(res));
    }

    @Test
    public void testCreateDCATDistribution() {
        Resource res = null;
        // retrieve sample zipped resource XML configuration file
        File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
        // create resource from single source file
        File occurrence = FileUtils.getClasspathFile("resources/res1/occurrence.txt");
        try {
            res = getResource(resourceXML, occurrence);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(mockGenerateDCAT.createDCATDistributionInformation(res));
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
        GenerateDCAT mockGenerateDCAT = mock(GenerateDCAT.class);
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
        File copied = new File(resourceDir, "source.txt");

        // mock file to which source file gets copied to
        when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(copied);

        // mock log file
        when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(new File(resourceDir, "log.txt"));

        // mock creation of zipped dwca in temp directory - this later becomes the actual archive generated
        when(mockDataDir.tmpFile(anyString(), anyString())).thenReturn(new File(tmpDataDir, "dwca.zip"));

        // mock creation of versioned zipped dwca in resource directory
        when(mockDataDir.resourceDwcaFile(anyString(), any(BigDecimal.class)))
                .thenReturn(new File(resourceDir, VERSIONED_ARCHIVE_FILENAME));

        // add SourceBase.TextFileSource fileSource to test Resource
        FileSource fileSource = mockSourceManager.add(resource, sourceFile, "occurrence.txt");
        resource.getMappings().get(0).setSource(fileSource);
        return resource;
    }
}
