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
package org.gbif.ipt.validation;

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
import org.gbif.utils.file.properties.PropertiesUtil;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganisationSupportIT {

  private static final Logger LOG = LogManager.getLogger(OrganisationSupportIT.class);

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

  @BeforeAll
  public static void init() {
    // config in production mode
    mockCfg = mock(AppConfig.class);
    when(mockCfg.getRegistryType()).thenReturn(AppConfig.REGISTRY_TYPE.DEVELOPMENT);
    // TODO: 2019-06-17 consider using the property instead
    when(mockCfg.getDataCiteUrl()).thenReturn("https://api.test.datacite.org");

    mockRegistryManager = mock(RegistryManager.class);
    when(mockRegistryManager.validateOrganisation(ORGANISATION_KEY, VALID_ORGANISATION_PASSWORD)).thenReturn(true);
  }

  public static Stream<Arguments> data() throws IOException {
    // read DataCite config from YAML file
    Properties p = PropertiesUtil.loadProperties("datacite.properties");
    ClientConfiguration cfg = ClientConfiguration.builder()
      .withBaseApiUrl(p.getProperty("baseApiUrl"))
      .withTimeOut(Long.valueOf(p.getProperty("timeOut")))
      .withFileCacheMaxSizeMb(Long.valueOf(p.getProperty("fileCacheMaxSizeMb")))
      .withUser(p.getProperty("user"))
      .withPassword(p.getProperty("password"))
      .build();
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

    return Stream.of(
        Arguments.of(o1, true),
        Arguments.of(o3, false),
        Arguments.of(o5, false)
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testValidate(Organisation organisation, boolean isValid) {
    LOG.info("Testing " + organisation.getDoiRegistrationAgency() + "...");
    OrganisationSupport organisationSupport = new OrganisationSupport(mockRegistryManager, mockCfg);
    assertEquals(isValid, organisationSupport.validate(action, organisation));
  }
}
