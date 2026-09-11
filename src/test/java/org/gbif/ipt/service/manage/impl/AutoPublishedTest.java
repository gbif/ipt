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

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDarwinCoreDataPackageFactory;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.metadata.eml.ipt.model.MaintenanceUpdateFrequency;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class AutoPublishedTest extends IptBaseTest {

  private final AppConfig mockAppConfig = MockAppConfig.buildMock();

  private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);

  private ResourcePublicationManager resourcePublicationManager;

  @BeforeEach
  public void setup() {
    resourcePublicationManager = getResourcePublicationManager();
  }

  public ResourcePublicationManager getResourcePublicationManager() {
    return new ResourcePublicationManagerImpl(
        mockAppConfig,
        mock(DataDir.class),
        mock(ResourceManager.class),
        mock(RegistryManager.class),
        mock(RegistrationManager.class),
        mock(Eml2Rtf.class),
        mock(GenerateDwcaFactory.class),
        mock(GenerateDataPackageFactory.class),
        mock(GenerateDarwinCoreDataPackageFactory.class),
        mock(SimpleTextProvider.class),
        mock(ResourceMetadataInferringService.class));
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
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals("2001-07-14T15:08", formatter.format(resource.getNextPublished()));

    // Current date is after July ==> July, mext year
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-11-21T14:08"), resource);
    assertEquals("2002-07-14T15:08", formatter.format(resource.getNextPublished()));
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
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-01-21T14:08"), resource);
    assertEquals("2001-03-03T15:08", formatter.format(resource.getNextPublished()));
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
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-01-21T14:08"), resource);
    assertEquals("2001-03-14T15:08", formatter.format(resource.getNextPublished()));

    // Current date is between the 2 months (March and September) ==> September
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-05-21T14:08"), resource);
    assertEquals("2001-09-14T15:08", formatter.format(resource.getNextPublished()));

    // Current date is after the second month (September) ==> March, next year
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-11-21T14:08"), resource);
    assertEquals("2002-03-14T15:08", formatter.format(resource.getNextPublished()));
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
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-05T14:08"), resource);
    assertEquals("2001-04-14T15:08", formatter.format(resource.getNextPublished()));

    // Current date is after 14th ==> next month
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals("2001-05-14T15:08", formatter.format(resource.getNextPublished()));

    // Current date is equal 14th, just after ==> next month
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-14T16:08"), resource);
    assertEquals("2001-05-14T15:08", formatter.format(resource.getNextPublished()));

    // Current date is equal 14th, just before ==> same day
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-14T14:08"), resource);
    assertEquals("2001-04-14T15:08", formatter.format(resource.getNextPublished()));
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
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-02-05T14:08"), resource);
    assertEquals("2001-03-03T15:08", formatter.format(resource.getNextPublished()));
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
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-01T14:08"), resource);
    assertEquals("2001-04-04T15:08", formatter.format(resource.getNextPublished()));

    // 2001-04-04: Wednesday
    // 2001-04-04: Wednesday
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-04T14:08"), resource);
    assertEquals("2001-04-04T15:08", formatter.format(resource.getNextPublished()));

    // 2001-04-06: Friday
    // 2001-04-06: Wednesday
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-06T16:08"), resource);
    assertEquals("2001-04-11T15:08", formatter.format(resource.getNextPublished()));

    // 2001-04-30: Monday
    // 2001-05-02: Wednesday
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-30T14:08"), resource);
    assertEquals("2001-05-02T15:08", formatter.format(resource.getNextPublished()));
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
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals("2001-04-21T15:08", formatter.format(resource.getNextPublished()));

    // Current date is after ==> next day
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-21T16:08"), resource);
    assertEquals("2001-04-22T15:08", formatter.format(resource.getNextPublished()));

    // Current date is last day of month ==> next month
    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-30T16:08"), resource);
    assertEquals("2001-05-01T15:08", formatter.format(resource.getNextPublished()));
  }

  @Test
  public void testBackwardCompatibilityAnnuallyFrequency()
      throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.ANNUALLY.getIdentifier());

    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals("2002-04-21T14:08", formatter.format(resource.getNextPublished()));
  }

  @Test
  public void testBackwardCompatibilityBiAnnuallyFrequency()
      throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.BIANNUALLY.getIdentifier());

    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-01-21T14:08"), resource);
    assertEquals("2001-07-22T14:08", formatter.format(resource.getNextPublished()));

    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-11-21T14:08"), resource);
    assertEquals("2002-05-22T14:08", formatter.format(resource.getNextPublished()));
  }

  @Test
  public void testBackwardCompatibilityMonthlyFrequency()
      throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.MONTHLY.getIdentifier());

    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-05T14:08"), resource);
    assertEquals("2001-05-05T14:08", formatter.format(resource.getNextPublished()));
  }

  @Test
  public void testBackwardCompatibilityWeeklyFrequency()
      throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.WEEKLY.getIdentifier());

    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-01T14:08"), resource);
    assertEquals("2001-04-08T14:08", formatter.format(resource.getNextPublished()));
  }

  @Test
  public void testBackwardCompatibilityDailyFrequency()
      throws ParseException {
    Resource resource = getResource();

    resource.setUpdateFrequency(MaintenanceUpdateFrequency.DAILY.getIdentifier());

    resourcePublicationManager.updateNextPublishedDate(formatter.parse("2001-04-21T14:08"), resource);
    assertEquals("2001-04-22T14:08", formatter.format(resource.getNextPublished()));
  }
}
