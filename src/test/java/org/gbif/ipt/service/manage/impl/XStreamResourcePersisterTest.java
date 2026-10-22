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

import org.gbif.ipt.config.TestBeanProvider;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredEmlTemporalCoverage;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.DataPackageFieldConverter;
import org.gbif.ipt.model.converter.DataPackageIdentifierConverter;
import org.gbif.ipt.model.converter.ExtensionMappingConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.TableSchemaNameConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * resource.xml and inferredMetadata.xml written and read by {@link XStreamResourcePersister}.
 */
public class XStreamResourcePersisterTest {

  private static final String CREATOR_EMAIL = "creator@example.org";
  private static final String ORGANISATION_KEY = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";
  private static final String SQL_PASSWORD = "s3cr3t-Pa55word";

  private XStreamResourcePersister persister;
  private User creator;
  private Organisation organisation;

  @BeforeEach
  public void setUp() {
    creator = new User();
    creator.setEmail(CREATOR_EMAIL);
    creator.setRole(User.Role.Manager);

    organisation = new Organisation();
    organisation.setKey(ORGANISATION_KEY);
    organisation.setName("Academy of Natural Sciences");

    UserAccountManager userAccountManager = mock(UserAccountManager.class);
    when(userAccountManager.get(CREATOR_EMAIL)).thenReturn(creator);
    RegistrationManager registrationManager = mock(RegistrationManager.class);
    when(registrationManager.get(ORGANISATION_KEY)).thenReturn(organisation);

    DataPackageIdentifierConverter schemaConverter = mock(DataPackageIdentifierConverter.class);
    TableSchemaNameConverter tableSchemaNameConverter = new TableSchemaNameConverter();
    ResourceConvertersManager converters = new ResourceConvertersManager(
        new UserEmailConverter(userAccountManager),
        new OrganisationKeyConverter(registrationManager),
        mock(ExtensionMappingConverter.class),
        mock(ExtensionRowTypeConverter.class),
        mock(ConceptTermConverter.class),
        schemaConverter,
        tableSchemaNameConverter,
        new DataPackageFieldConverter(schemaConverter, tableSchemaNameConverter),
        new JdbcInfoConverter(TestBeanProvider.provideJdbcSupport()));
    persister = new XStreamResourcePersister(converters, new PasswordEncrypter(TestBeanProvider.providePasswordEncryption()));
  }

  @Test
  public void testRoundTrip() {
    Resource resource = new Resource();
    resource.setShortname("bees");
    resource.setTitle("Bees of the world");
    resource.setCoreType("occurrence");
    resource.setSubtype("specimen");
    resource.setCreator(creator);
    resource.addManager(creator);
    resource.setOrganisation(organisation);
    resource.setStatus(PublicationStatus.PUBLIC);
    resource.setMetadataVersion(new BigDecimal("2.0"));
    resource.setRecordsPublished(42);
    Date released = new Date(1_700_000_000_000L);
    resource.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), released, PublicationStatus.PUBLIC));

    Resource loaded = load(save(resource));

    // the title is part of the EML, stored separately
    assertNull(loaded.getTitle());
    assertEquals("occurrence", loaded.getCoreType());
    assertEquals("specimen", loaded.getSubtype());
    assertSame(creator, loaded.getCreator());
    assertTrue(loaded.getManagers().contains(creator));
    assertSame(organisation, loaded.getOrganisation());
    assertEquals(PublicationStatus.PUBLIC, loaded.getStatus());
    assertEquals(new BigDecimal("2.0"), loaded.getMetadataVersion());
    assertEquals(42, loaded.getRecordsPublished());
    assertEquals(1, loaded.getVersionHistory().size());
    VersionHistory history = loaded.getVersionHistory().get(0);
    assertEquals("1.0", history.getVersion());
    assertEquals(released, history.getReleased());
    assertEquals(PublicationStatus.PUBLIC, history.getPublicationStatus());
  }

  @Test
  public void testAliases() throws Exception {
    Resource resource = new Resource();
    resource.setShortname("bees");
    resource.setCreator(creator);
    resource.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PUBLIC));
    TextFileSource source = new TextFileSource();
    source.setName("occurrences");
    source.setResource(resource);
    resource.addSource(source, false);

    String xml = save(resource);

    assertTrue(xml.startsWith("<resource>"), xml);
    assertTrue(xml.contains("<filesource>"), xml);
    assertTrue(xml.contains("<versionhistory>"), xml);
    // only the user's email is stored
    assertTrue(xml.contains("<creator>" + CREATOR_EMAIL + "</creator>"), xml);
  }

  @Test
  public void testTransientFieldsAreNotStored() throws Exception {
    Resource resource = new Resource();
    resource.setShortname("bees");
    resource.setTitle("Bees");
    resource.setInferredMetadata(new InferredEmlMetadata());
    TextFileSource source = new TextFileSource();
    source.setName("occurrences");
    source.setFile(new File("/some/absolute/path/occurrences.txt"));
    source.setResource(resource);
    resource.addSource(source, false);

    String xml = save(resource);

    assertFalse(xml.contains("<shortname>"), xml);
    assertFalse(xml.contains("<eml>"), xml);
    assertFalse(xml.contains("<dataPackageMetadata>"), xml);
    assertFalse(xml.contains("<inferredMetadata>"), xml);
    assertFalse(xml.contains("/some/absolute/path"), xml);

    Resource loaded = load(xml);
    // shortname is set from the folder name by the loader
    assertNull(loaded.getShortname());
    assertNull(((TextFileSource) loaded.getSources().get(0)).getFile());
  }

  @Test
  public void testSqlSourcePasswordIsEncrypted() throws Exception {
    Resource resource = new Resource();
    resource.setShortname("bees");
    SqlSource source = new SqlSource();
    source.setName("db");
    source.setPassword(SQL_PASSWORD);
    source.setResource(resource);
    resource.addSource(source, false);

    String xml = save(resource);

    assertFalse(xml.contains(SQL_PASSWORD), xml);
    Resource loaded = load(xml);
    SqlSource loadedSource = assertInstanceOf(SqlSource.class, loaded.getSources().get(0));
    assertEquals(SQL_PASSWORD, loadedSource.getPassword());
  }

  @Test
  public void testUnknownElementsAreIgnored() {
    Resource resource = new Resource();
    resource.setShortname("bees");
    resource.setCoreType("occurrence");
    String xml = save(resource).replace("</resource>", "  <removedInLaterVersion>x</removedInLaterVersion>\n</resource>");

    Resource loaded = load(xml);

    assertEquals("occurrence", loaded.getCoreType());
  }

  @Test
  public void testInferredEmlMetadataRoundTrip() {
    InferredEmlMetadata metadata = new InferredEmlMetadata();
    Date lastModified = new Date(1_700_000_000_000L);
    metadata.setLastModified(lastModified);
    InferredEmlTemporalCoverage temporalCoverage = new InferredEmlTemporalCoverage();
    temporalCoverage.setInferred(true);
    temporalCoverage.getErrors().add("some error");
    metadata.setInferredTemporalCoverage(temporalCoverage);

    StringWriter writer = new StringWriter();
    persister.saveInferredMetadata(metadata, writer);
    String xml = writer.toString();

    assertTrue(xml.startsWith("<inferredMetadata>"), xml);
    assertTrue(xml.contains("<inferredTemporalCoverage>"), xml);
    InferredEmlMetadata loaded = persister.loadInferredEmlMetadata(stream(xml));
    assertEquals(lastModified, loaded.getLastModified());
    assertNotNull(loaded.getInferredTemporalCoverage());
    assertTrue(loaded.getInferredTemporalCoverage().isInferred());
    assertTrue(loaded.getInferredTemporalCoverage().getErrors().contains("some error"));
  }

  @Test
  public void testInferredCamtrapMetadataRoundTrip() {
    InferredCamtrapMetadata metadata = new InferredCamtrapMetadata();
    Date lastModified = new Date(1_700_000_000_000L);
    metadata.setLastModified(lastModified);
    InferredCamtrapTemporalScope temporalScope = new InferredCamtrapTemporalScope();
    temporalScope.setStartDate(new Date(1_600_000_000_000L));
    temporalScope.setEndDate(new Date(1_650_000_000_000L));
    temporalScope.setInferred(true);
    metadata.setInferredTemporalScope(temporalScope);

    StringWriter writer = new StringWriter();
    persister.saveInferredMetadata(metadata, writer);
    String xml = writer.toString();

    assertTrue(xml.startsWith("<inferredMetadataCamtrap>"), xml);
    assertTrue(xml.contains("<inferredTemporalScope>"), xml);
    InferredCamtrapMetadata loaded = persister.loadInferredCamtrapMetadata(stream(xml));
    assertEquals(lastModified, loaded.getLastModified());
    assertEquals(new Date(1_600_000_000_000L), loaded.getInferredTemporalScope().getStartDate());
    assertEquals(new Date(1_650_000_000_000L), loaded.getInferredTemporalScope().getEndDate());
    assertTrue(loaded.getInferredTemporalScope().isInferred());
  }

  private String save(Resource resource) {
    StringWriter writer = new StringWriter();
    persister.save(resource, writer);
    return writer.toString();
  }

  private Resource load(String xml) {
    return persister.load(stream(xml));
  }

  private static ByteArrayInputStream stream(String xml) {
    return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
  }
}
