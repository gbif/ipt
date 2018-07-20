package org.gbif.ipt.validation;

import org.gbif.doi.service.ServiceConfig;
import org.gbif.ipt.action.admin.OrganisationsAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class OrganisationSupportIT {

  private static final Logger LOG = Logger.getLogger(OrganisationSupportIT.class);

  private static final DefaultHttpClient CLIENT = HttpUtil.newMultithreadedClient(10000, 3, 2);
  private static final String ORGANISATION_KEY = UUID.fromString("dce7a3c9-ea78-4be7-9abc-e3838de70dc5").toString();
  private static final String VALID_ORGANISATION_PASSWORD = "password";
  private static final OrganisationsAction action =
    new OrganisationsAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      mock(OrganisationSupport.class), mock(OrganisationsAction.RegisteredOrganisations.class),
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

    mockRegistryManager = mock(RegistryManager.class);
    when(mockRegistryManager.validateOrganisation(ORGANISATION_KEY, VALID_ORGANISATION_PASSWORD)).thenReturn(true);
  }

  @Parameterized.Parameters
  public static Iterable data() throws IOException {

    // read DataCite config from YAML file
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    InputStream dc = FileUtils.classpathStream("datacite.yaml");
    ServiceConfig dcCfg = mapper.readValue(dc, ServiceConfig.class);
    //LOG.info("DataCite password (read from Maven property datacite.password)= " + dcCfg.getPassword());

    // organisation with valid DataCite account
    Organisation o1 = new Organisation();
    o1.setName("NHM");
    o1.setPassword(VALID_ORGANISATION_PASSWORD);
    o1.setKey(ORGANISATION_KEY);
    o1.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    o1.setAgencyAccountUsername(dcCfg.getUsername());
    o1.setAgencyAccountPassword(dcCfg.getPassword());
    o1.setDoiPrefix(Constants.TEST_DOI_PREFIX);

    // organisation with valid EZID account
    Organisation o2 = new Organisation();
    o2.setName("NHM");
    o2.setPassword(VALID_ORGANISATION_PASSWORD);
    o2.setKey(ORGANISATION_KEY);
    o2.setDoiRegistrationAgency(DOIRegistrationAgency.EZID);
    o2.setAgencyAccountUsername("apitest");
    o2.setAgencyAccountPassword("apitest");
    o2.setDoiPrefix(Constants.EZID_TEST_DOI_SHOULDER);

    // organisation with DataCite account that does not authenticate (wrong password)
    Organisation o3 = new Organisation();
    o3.setName("NHM");
    o3.setPassword(VALID_ORGANISATION_PASSWORD);
    o3.setKey(ORGANISATION_KEY);
    o3.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    o3.setAgencyAccountUsername(dcCfg.getUsername());
    o3.setAgencyAccountPassword("wrongPassword");
    o3.setDoiPrefix(Constants.TEST_DOI_PREFIX);

    // organisation with EZID account that does not authenticate (wrong password)
    Organisation o4 = new Organisation();
    o4.setName("NHM");
    o4.setPassword(VALID_ORGANISATION_PASSWORD);
    o4.setKey(ORGANISATION_KEY);
    o4.setDoiRegistrationAgency(DOIRegistrationAgency.EZID);
    o4.setAgencyAccountUsername("apitest");
    o4.setAgencyAccountPassword("wrongPassword");
    o4.setDoiPrefix(Constants.EZID_TEST_DOI_SHOULDER);

    // organisation with DataCite account that does not authenticate (wrong prefix)
    Organisation o5 = new Organisation();
    o5.setName("NHM");
    o5.setPassword(VALID_ORGANISATION_PASSWORD);
    o5.setKey(ORGANISATION_KEY);
    o5.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    o5.setAgencyAccountUsername(dcCfg.getUsername());
    o5.setAgencyAccountPassword(dcCfg.getPassword());
    o5.setDoiPrefix("10.9999"); // wrong

    // organisation with EZID account that does not authenticate (wrong prefix)
    Organisation o6 = new Organisation();
    o6.setName("NHM");
    o6.setPassword(VALID_ORGANISATION_PASSWORD);
    o6.setKey(ORGANISATION_KEY);
    o6.setDoiRegistrationAgency(DOIRegistrationAgency.EZID);
    o6.setAgencyAccountUsername("apitest");
    o6.setAgencyAccountPassword("apitest");
    o6.setDoiPrefix("10.9999/FK2"); // wrong

    return Arrays.asList(new Object[][] {{o1, true}, {o2, true}, {o3, false}, {o4, false}, {o5, false}, {o6, false}});
  }

  @Test
  @Ignore("Ignoring during DataCite API regression: https://github.com/datacite/poodle/issues/7")
  public void testValidate() {
    LOG.info("Testing " + organisation.getDoiRegistrationAgency() + "...");
    // EZID only: one-time login over SSL stores a session cookie, clear otherwise test reuses existing valid connection
    if (DOIRegistrationAgency.EZID.equals(organisation.getDoiRegistrationAgency())) {
      CLIENT.getCookieStore().clear();
    }

    OrganisationSupport organisationSupport = new OrganisationSupport(mockRegistryManager, mockCfg, CLIENT);
    assertEquals(isValid, organisationSupport.validate(action, organisation));
  }
}
