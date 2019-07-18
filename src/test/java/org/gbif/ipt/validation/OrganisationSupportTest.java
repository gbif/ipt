package org.gbif.ipt.validation;

import org.gbif.ipt.action.admin.OrganisationsAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class OrganisationSupportTest {

  private static final String ORGANISATION_KEY = UUID.fromString("dce7a3c9-ea78-4be7-9abc-e3838de70dc5").toString();
  private static final String VALID_ORGANISATION_PASSWORD = "password";
  private static final OrganisationsAction action =
    new OrganisationsAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      mock(OrganisationSupport.class), mock(OrganisationsAction.RegisteredOrganisations.class),
      mock(ResourceManager.class));

  private AppConfig mockCfg;
  private Organisation organisation;
  private boolean isValid;

  public OrganisationSupportTest(Organisation organisation, boolean isValid, AppConfig cfg) {
    this.organisation = organisation;
    this.isValid = isValid;
    this.mockCfg = cfg;
  }

  @Parameterized.Parameters
  public static Object[][] data() {
    // config in production mode
    AppConfig mockCfgProduction = mock(AppConfig.class);
    when(mockCfgProduction.getRegistryType()).thenReturn(AppConfig.REGISTRY_TYPE.PRODUCTION);

    // config in test mode
    AppConfig mockCfgTest = mock(AppConfig.class);
    when(mockCfgTest.getRegistryType()).thenReturn(AppConfig.REGISTRY_TYPE.DEVELOPMENT);

    // valid organisation to begin with
    Organisation o1 = new Organisation();
    o1.setName("NHM");
    o1.setPassword(VALID_ORGANISATION_PASSWORD);
    o1.setKey(ORGANISATION_KEY);

    // organisation with no key
    Organisation o2 = new Organisation();
    o2.setName("NHM");
    o2.setPassword(VALID_ORGANISATION_PASSWORD);

    // organisation with no password
    Organisation o3 = new Organisation();
    o3.setName("NHM");
    o3.setPassword("");
    o3.setKey(ORGANISATION_KEY);

    // organisation with invalid password
    Organisation o4 = new Organisation();
    o4.setName("NHM");
    o4.setPassword("BAD_PASSWORD");
    o4.setKey(ORGANISATION_KEY);

    // organisation DOI registration agency account, except agency
    Organisation o5 = new Organisation();
    o5.setName("NHM");
    o5.setPassword(VALID_ORGANISATION_PASSWORD);
    o5.setKey(ORGANISATION_KEY);
    o5.setDoiRegistrationAgency(null);
    o5.setAgencyAccountUsername("DK.TEST");
    o5.setAgencyAccountPassword("GOOD_PASSWORD");
    o5.setDoiPrefix("10.1234");

    // organisation DOI registration agency account, except account username
    Organisation o6 = new Organisation();
    o6.setName("NHM");
    o6.setPassword(VALID_ORGANISATION_PASSWORD);
    o6.setKey(ORGANISATION_KEY);
    o6.setDoiRegistrationAgency(null);
    o6.setAgencyAccountUsername("");
    o6.setAgencyAccountPassword("GOOD_PASSWORD");
    o6.setDoiPrefix("10.1234");

    // organisation DOI registration agency account, except account password
    Organisation o7 = new Organisation();
    o7.setName("NHM");
    o7.setPassword(VALID_ORGANISATION_PASSWORD);
    o7.setKey(ORGANISATION_KEY);
    o7.setDoiRegistrationAgency(null);
    o7.setAgencyAccountUsername("DK.TEST");
    o7.setAgencyAccountPassword("");
    o7.setDoiPrefix("10.1234");

    // organisation DOI registration agency account, except account prefix
    Organisation o8 = new Organisation();
    o8.setName("NHM");
    o8.setPassword(VALID_ORGANISATION_PASSWORD);
    o8.setKey(ORGANISATION_KEY);
    o8.setDoiRegistrationAgency(null);
    o8.setAgencyAccountUsername("DK.TEST");
    o8.setAgencyAccountPassword("GOOD_PASSWORD");
    o8.setDoiPrefix("");

    // organisation DOI registration agency account, except prefix is invalid for the IPT production mode
    Organisation o9 = new Organisation();
    o9.setName("NHM");
    o9.setPassword(VALID_ORGANISATION_PASSWORD);
    o9.setKey(ORGANISATION_KEY);
    o9.setDoiRegistrationAgency(null);
    o9.setAgencyAccountUsername("DK.TEST");
    o9.setAgencyAccountPassword("GOOD_PASSWORD");
    o9.setDoiPrefix(Constants.TEST_DOI_PREFIX);

    // organisation DOI registration agency account, except prefix is invalid for the IPT test mode
    Organisation o10 = new Organisation();
    o10.setName("NHM");
    o10.setPassword(VALID_ORGANISATION_PASSWORD);
    o10.setKey(ORGANISATION_KEY);
    o10.setDoiRegistrationAgency(null);
    o10.setAgencyAccountUsername("DK.TEST");
    o10.setAgencyAccountPassword("GOOD_PASSWORD");
    o10.setDoiPrefix("10.1234");

    // organisation DOI registration agency account, except prefix is invalid because it doesn't start with 10.
    Organisation o11 = new Organisation();
    o11.setName("NHM");
    o11.setPassword(VALID_ORGANISATION_PASSWORD);
    o11.setKey(ORGANISATION_KEY);
    o11.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    o11.setAgencyAccountUsername("DK.TEST");
    o11.setAgencyAccountPassword("GOOD_PASSWORD");
    o11.setDoiPrefix("prefix");

    return new Object[][] {
        {o1, true, mockCfgProduction},
        {o2, false, mockCfgProduction},
        {o3, false, mockCfgProduction},
        {o4, false, mockCfgProduction},
        {o5, false, mockCfgProduction},
        {o6, false, mockCfgProduction},
        {o7, false, mockCfgProduction},
        {o8, false, mockCfgProduction},
        {o9, false, mockCfgProduction},
        {o10, false, mockCfgTest},
        {o11, false, mockCfgProduction}
    };
  }

  @Test
  public void testValidateInProductionMode() {
    RegistryManager mockRegistryManager = mock(RegistryManager.class);
    when(mockRegistryManager.validateOrganisation(ORGANISATION_KEY, VALID_ORGANISATION_PASSWORD)).thenReturn(true);
    OrganisationSupport organisationSupport = new OrganisationSupport(mockRegistryManager, mockCfg);
    assertEquals(isValid, organisationSupport.validate(action, organisation));
  }
}
