package org.gbif.ipt.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.log4j.Logger;
import org.gbif.datacite.rest.client.configuration.ClientConfiguration;
import org.gbif.ipt.action.admin.OrganisationsAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.file.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class OrganisationSupportIT {

  private static final Logger LOG = Logger.getLogger(OrganisationSupportIT.class);

  private static final String ORGANISATION_KEY = UUID.fromString("dce7a3c9-ea78-4be7-9abc-e3838de70dc5").toString();
  private static final String VALID_ORGANISATION_PASSWORD = "password";
  private static final OrganisationsAction action = new OrganisationsAction(
      mock(SimpleTextProvider.class),
      mock(AppConfig.class),
      mock(RegistrationManager.class),
      mock(OrganisationSupport.class),
      mock(OrganisationsAction.RegisteredOrganisations.class),
      mock(ResourceManager.class));

  private static AppConfig mockCfg;
  private static RegistryManager mockRegistryManager;

  private Organisation organisation;
  private boolean isValid;

  public OrganisationSupportIT(Organisation organisation, boolean isValid) {
    this.organisation = organisation;
    this.isValid = isValid;
  }

  @BeforeClass
  public static void init() {
    // config in production mode
    mockCfg = mock(AppConfig.class);
    when(mockCfg.getRegistryType()).thenReturn(AppConfig.REGISTRY_TYPE.DEVELOPMENT);
    // TODO: 2019-06-17 consider using the property instead
    when(mockCfg.getDataCiteUrl()).thenReturn("https://api.test.datacite.org");

    mockRegistryManager = mock(RegistryManager.class);
    when(mockRegistryManager.validateOrganisation(ORGANISATION_KEY, VALID_ORGANISATION_PASSWORD)).thenReturn(true);
  }

  @Parameterized.Parameters
  public static Object[][] data() throws IOException {

    // read DataCite config from YAML file
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    InputStream dc = FileUtils.classpathStream("datacite.yaml");
    ClientConfiguration cfg = mapper.readValue(dc, ClientConfiguration.class);
    //LOG.info("DataCite password (read from Maven property datacite.password)= " + dcCfg.getPassword());

    // organisation with valid DataCite account
    Organisation o1 = new Organisation();
    o1.setName("NHM");
    o1.setPassword(VALID_ORGANISATION_PASSWORD);
    o1.setKey(ORGANISATION_KEY);
    o1.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    o1.setAgencyAccountUsername(cfg.getUser());
    o1.setAgencyAccountPassword(cfg.getPassword());
    o1.setDoiPrefix(Constants.TEST_DOI_PREFIX);

    // organisation with DataCite account that does not authenticate (wrong password)
    Organisation o3 = new Organisation();
    o3.setName("NHM");
    o3.setPassword(VALID_ORGANISATION_PASSWORD);
    o3.setKey(ORGANISATION_KEY);
    o3.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    o3.setAgencyAccountUsername(cfg.getUser());
    o3.setAgencyAccountPassword("wrongPassword");
    o3.setDoiPrefix(Constants.TEST_DOI_PREFIX);

    // organisation with DataCite account that does not authenticate (wrong prefix)
    Organisation o5 = new Organisation();
    o5.setName("NHM");
    o5.setPassword(VALID_ORGANISATION_PASSWORD);
    o5.setKey(ORGANISATION_KEY);
    o5.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    o5.setAgencyAccountUsername(cfg.getUser());
    o5.setAgencyAccountPassword(cfg.getPassword());
    o5.setDoiPrefix("10.9999"); // wrong

    return new Object[][]{
        {o1, true},
        {o3, false},
        {o5, false},
    };
  }

  @Test
  public void testValidate() {
    LOG.info("Testing " + organisation.getDoiRegistrationAgency() + "...");
    OrganisationSupport organisationSupport = new OrganisationSupport(mockRegistryManager, mockCfg);
    assertEquals(isValid, organisationSupport.validate(action, organisation));
  }
}
