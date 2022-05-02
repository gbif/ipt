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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.DataSchemaIdentifierConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.metadata.eml.MaintenanceUpdateFrequency;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class AutoPublishedTest {

  private AppConfig mockAppConfig = MockAppConfig.buildMock();

  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
  private Calendar calendar = Calendar.getInstance();

  private ResourceManagerImpl resourceManager;

  @BeforeEach
  public void setup() {
    resourceManager = getResourceManagerImpl();
  }

  public ResourceManagerImpl getResourceManagerImpl() {
    return new ResourceManagerImpl(
      mockAppConfig,
      mock(DataDir.class),
      mock(UserEmailConverter.class),
      mock(OrganisationKeyConverter.class),
      mock(ExtensionRowTypeConverter.class),
      mock(DataSchemaIdentifierConverter.class),
      mock(JdbcInfoConverter.class),
      mock(SourceManager.class),
      mock(ExtensionManager.class),
      mock(DataSchemaManager.class),
      mock(RegistryManager.class),
      mock(ConceptTermConverter.class),
      mock(GenerateDwcaFactory.class),
      mock(GenerateDataPackageFactory.class),
      mock(PasswordEncrypter.class),
      mock(Eml2Rtf.class),
      mock(VocabulariesManager.class),
      mock(SimpleTextProvider.class),
      mock(RegistrationManager.class));
  }

  public Resource getResource() {
    Resource r = new Resource();
    r.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);
    return r;
  }

  @Test
  public void testAnnuallyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setAutoPublishingFrequency(MaintenanceUpdateFrequency.ANNUALLY.getIdentifier(),
      "july",
      "",
      14, // 14th
      "",
      15,
      8);

    // Current date is before July ==> July, same year
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-07-14T15:08");

    // Current date is after July ==> July, mext year
    resourceManager.updateNextPublishedDate(formatter.parse("2001-11-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2002-07-14T15:08");
  }

  @Test
  public void testFebruaryAnnuallyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setAutoPublishingFrequency(MaintenanceUpdateFrequency.ANNUALLY.getIdentifier(),
      "february",
      "",
      31, // 31st
      "",
      15,
      8);

    // "31st of February" = 3rd of March
    resourceManager.updateNextPublishedDate(formatter.parse("2001-01-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-03-03T15:08");
  }

  @Test
  public void testBiAnnuallyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setAutoPublishingFrequency(MaintenanceUpdateFrequency.BIANNUALLY.getIdentifier(),
      "",
      "march_september",
      14, // 14th
      "",
      15,
      8);

    // Current date is before the first month (March) ==> March
    resourceManager.updateNextPublishedDate(formatter.parse("2001-01-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-03-14T15:08");

    // Current date is between the 2 months (March and September) ==> September
    resourceManager.updateNextPublishedDate(formatter.parse("2001-05-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-09-14T15:08");

    // Current date is after the second month (September) ==> March, next year
    resourceManager.updateNextPublishedDate(formatter.parse("2001-11-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2002-03-14T15:08");
  }

  @Test
  public void testMonthlyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setAutoPublishingFrequency(MaintenanceUpdateFrequency.MONTHLY.getIdentifier(),
      "",
      "",
      14, // 14th
      "",
      15,
      8);

    // Current date is before 14th ==> same month
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-05T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-14T15:08");

    // Current date is after 14th ==> next month
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-05-14T15:08");

    // Current date is equal 14th, just after ==> next month
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-14T16:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-05-14T15:08");

    // Current date is equal 14th, just before ==> same day
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-14T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-14T15:08");
  }

  @Test
  public void testFebruaryMonthlyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setAutoPublishingFrequency(MaintenanceUpdateFrequency.MONTHLY.getIdentifier(),
      "",
      "",
      31, // 31st
      "",
      15,
      8);

    // Current date is February ==> March ("31st of February")
    resourceManager.updateNextPublishedDate(formatter.parse("2001-02-05T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-03-03T15:08");
  }

  @Test
  public void testWeeklyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setAutoPublishingFrequency(MaintenanceUpdateFrequency.WEEKLY.getIdentifier(),
      "",
      "",
      0,
      "wednesday",
      15,
      8);

    // 2001-04-01: Sunday
    // 2001-04-04: Wednesday
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-01T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-04T15:08");

    // 2001-04-04: Wednesday
    // 2001-04-04: Wednesday
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-04T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-04T15:08");

    // 2001-04-06: Friday
    // 2001-04-06: Wednesday
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-06T16:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-11T15:08");

    // 2001-04-30: Monday
    // 2001-05-02: Wednesday
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-30T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-05-02T15:08");
  }

  @Test
  public void testDailyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setAutoPublishingFrequency(MaintenanceUpdateFrequency.DAILY.getIdentifier(),
      "",
      "",
      0,
      "",
      15,
      8);

    // Current date is before ==> same day
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-21T15:08");

    // Current date is after ==> next day
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-21T16:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-22T15:08");

    // Current date is last day of month ==> next month
    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-30T16:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-05-01T15:08");
  }

  @Test
  public void testBackwardCompatibilityAnnuallyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.ANNUALLY.getIdentifier());

    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2002-04-21T14:08");
  }

  @Test
  public void testBackwardCompatibilityBiAnnuallyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.BIANNUALLY.getIdentifier());

    resourceManager.updateNextPublishedDate(formatter.parse("2001-01-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-07-22T14:08");

    resourceManager.updateNextPublishedDate(formatter.parse("2001-11-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2002-05-22T14:08");
  }

  @Test
  public void testBackwardCompatibilityMonthlyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.MONTHLY.getIdentifier());

    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-05T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-05-05T14:08");
  }

  @Test
  public void testBackwardCompatibilityWeeklyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.WEEKLY.getIdentifier());

    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-01T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-08T14:08");
  }

  @Test
  public void testBackwardCompatibilityDailyFrequency()
    throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.DAILY.getIdentifier());

    resourceManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals(formatter.format(resource.getNextPublished()), "2001-04-22T14:08");
  }
}
