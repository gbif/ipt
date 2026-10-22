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

import org.gbif.api.model.common.DOI;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.metadata.datacite.RelatedIdentifierType;
import org.gbif.doi.metadata.datacite.RelationType;
import org.gbif.doi.service.DoiException;
import org.gbif.doi.service.DoiExistsException;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.metadata.eml.ipt.IptEmlWriter;
import org.gbif.metadata.eml.ipt.model.Agent;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link ResourceDoiServiceImpl} with the DOI registration agency mocked.
 */
public class ResourceDoiServiceImplTest {

  private static final String SHORTNAME = "ants";
  private static final URI RESOURCE_URI = URI.create("https://ipt.example.org/resource?r=ants");
  private static final URI VERSION_1_0_URI = URI.create("https://ipt.example.org/resource?r=ants&v=1.0");
  private static final DOI DOI_1 = new DOI("10.21373/abc123");
  private static final DOI DOI_2 = new DOI("10.21373/def456");

  @TempDir
  Path tempDir;

  private DoiService doiService;
  private BaseAction action;
  private ResourceDoiServiceImpl service;
  private Resource resource;

  @BeforeEach
  public void setUp() {
    AppConfig cfg = mock(AppConfig.class);
    when(cfg.getResourceUri(SHORTNAME)).thenReturn(RESOURCE_URI);
    when(cfg.getResourceVersionUri(SHORTNAME, new BigDecimal("1.0"))).thenReturn(VERSION_1_0_URI);
    DataDir dataDir = mock(DataDir.class);
    when(dataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenAnswer(invocation ->
        tempDir.resolve("eml-" + invocation.getArgument(1, BigDecimal.class).toPlainString() + ".xml").toFile());
    doiService = mock(DoiService.class);
    RegistrationManager registrationManager = mock(RegistrationManager.class);
    when(registrationManager.getDoiService()).thenReturn(doiService);
    action = mock(BaseAction.class);
    when(action.getText(anyString(), any(String[].class))).thenAnswer(invocation -> invocation.getArgument(0));

    service = new ResourceDoiServiceImpl(cfg, dataDir, registrationManager);
    resource = resource();
  }

  @Test
  public void testWorkflowWithoutDoi() {
    service.executeDoiWorkflow(resource, new BigDecimal("1.0"), null, action);

    verifyNoInteractions(doiService);
    verify(action, never()).addActionMessage(anyString());
  }

  @Test
  public void testWorkflowForPrivateResource() {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.setStatus(PublicationStatus.PRIVATE);

    service.executeDoiWorkflow(resource, new BigDecimal("1.0"), null, action);

    verifyNoInteractions(doiService);
  }

  @Test
  public void testWorkflowForUnreservedDoi() {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.UNRESERVED);

    service.executeDoiWorkflow(resource, new BigDecimal("1.0"), null, action);

    verifyNoInteractions(doiService);
  }

  @Test
  public void testWorkflowRegistersInitialDoi() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);

    service.executeDoiWorkflow(resource, new BigDecimal("1.0"), null, action);

    verify(doiService).register(eq(DOI_1), eq(RESOURCE_URI), any(DataCiteMetadata.class));
    verify(doiService, never()).update(any(), any(DataCiteMetadata.class));
    assertEquals(IdentifierStatus.PUBLIC, resource.getIdentifierStatus());
    verify(action).addActionMessage("manage.overview.publishing.doi.publish.newMajorVersion");
  }

  @Test
  public void testWorkflowUpdatesDoiOfMinorVersion() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);

    service.executeDoiWorkflow(resource, new BigDecimal("1.1"), new BigDecimal("1.0"), action);

    verify(doiService).update(eq(DOI_1), any(DataCiteMetadata.class));
    verify(doiService, never()).register(any(), any(), any(DataCiteMetadata.class));
    verify(doiService, never()).update(any(), any(URI.class));
    verify(action).addActionMessage("manage.overview.publishing.doi.publish.newMinorVersion");
  }

  @Test
  public void testWorkflowReplacesDoiOfPreviousMajorVersion() throws Exception {
    publishVersion1WithDoi1();
    resource.setMetadataVersion(new BigDecimal("2.0"));
    resource.setDoi(DOI_2);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);

    service.executeDoiWorkflow(resource, new BigDecimal("2.0"), new BigDecimal("1.0"), action);

    verify(doiService).register(eq(DOI_2), eq(RESOURCE_URI), any(DataCiteMetadata.class));
    verify(doiService).update(DOI_1, VERSION_1_0_URI);
    verify(doiService).update(eq(DOI_1), any(DataCiteMetadata.class));
    verify(action).addActionMessage("manage.overview.publishing.doi.publish.newMajorVersion.replaces");
  }

  @Test
  public void testRegisterDoi() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);

    service.registerDoi(resource, null);

    ArgumentCaptor<DataCiteMetadata> metadata = ArgumentCaptor.forClass(DataCiteMetadata.class);
    verify(doiService).register(eq(DOI_1), eq(RESOURCE_URI), metadata.capture());
    assertEquals(DOI_1.getDoiName(), metadata.getValue().getIdentifier().getValue().toLowerCase());
    assertEquals(IdentifierStatus.PUBLIC, resource.getIdentifierStatus());
    // the DOI is the first alternate identifier and the citation identifier
    assertEquals(DOI_1.toString(), resource.getEml().getAlternateIdentifiers().get(0));
    assertEquals(DOI_1.getUrl().toString(), resource.getEml().getCitation().getIdentifier());
  }

  @Test
  public void testRegisterDoiReplacingPreviousDoi() throws Exception {
    resource.setDoi(DOI_2);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);

    service.registerDoi(resource, DOI_1);

    ArgumentCaptor<DataCiteMetadata> metadata = ArgumentCaptor.forClass(DataCiteMetadata.class);
    verify(doiService).register(eq(DOI_2), eq(RESOURCE_URI), metadata.capture());
    assertTrue(hasRelatedIdentifier(metadata.getValue(), RelationType.IS_NEW_VERSION_OF, DOI_1));
  }

  @Test
  public void testRegisterExistingDoiUpdatesIt() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    doThrow(new DoiExistsException("exists", DOI_1))
        .when(doiService).register(eq(DOI_1), any(URI.class), any(DataCiteMetadata.class));

    service.registerDoi(resource, null);

    verify(doiService).update(eq(DOI_1), any(DataCiteMetadata.class));
    assertEquals(IdentifierStatus.PUBLIC, resource.getIdentifierStatus());
    assertEquals(DOI_1.toString(), resource.getEml().getAlternateIdentifiers().get(0));
  }

  @Test
  public void testRegisterExistingDoiFailsWhenUpdateFails() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    doThrow(new DoiExistsException("exists", DOI_1))
        .when(doiService).register(eq(DOI_1), any(URI.class), any(DataCiteMetadata.class));
    doThrow(new DoiException("unavailable")).when(doiService).update(eq(DOI_1), any(DataCiteMetadata.class));

    PublicationException e = assertThrows(PublicationException.class, () -> service.registerDoi(resource, null));

    assertEquals(PublicationException.TYPE.DOI, e.getType());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, resource.getIdentifierStatus());
  }

  @Test
  public void testRegisterDoiFailure() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    doThrow(new DoiException("unavailable"))
        .when(doiService).register(eq(DOI_1), any(URI.class), any(DataCiteMetadata.class));

    PublicationException e = assertThrows(PublicationException.class, () -> service.registerDoi(resource, null));

    assertEquals(PublicationException.TYPE.DOI, e.getType());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, resource.getIdentifierStatus());
  }

  @Test
  public void testRegisterDoiWithInvalidMetadata() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    doThrow(new InvalidMetadataException("no title"))
        .when(doiService).register(eq(DOI_1), any(URI.class), any(DataCiteMetadata.class));

    PublicationException e = assertThrows(PublicationException.class, () -> service.registerDoi(resource, null));

    assertTrue(e.getMessage().contains("invalid"), e.getMessage());
  }

  @Test
  public void testRegisterDoiOfResourceNotInRequiredState() {
    // no DOI
    assertThrows(InvalidConfigException.class, () -> service.registerDoi(resource, null));

    // private
    resource.setDoi(DOI_1);
    resource.setStatus(PublicationStatus.PRIVATE);
    assertThrows(InvalidConfigException.class, () -> service.registerDoi(resource, null));

    verifyNoInteractions(doiService);
  }

  @Test
  public void testUpdateDoi() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);

    service.updateDoi(resource);

    verify(doiService).update(eq(DOI_1), any(DataCiteMetadata.class));
    verify(doiService, never()).update(any(), any(URI.class));
  }

  @Test
  public void testUpdateDoiFailure() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
    doThrow(new DoiException("unavailable")).when(doiService).update(eq(DOI_1), any(DataCiteMetadata.class));

    PublicationException e = assertThrows(PublicationException.class, () -> service.updateDoi(resource));

    assertEquals(PublicationException.TYPE.DOI, e.getType());
  }

  @Test
  public void testUpdateDoiOfResourceNotInRequiredState() {
    assertThrows(InvalidConfigException.class, () -> service.updateDoi(resource));

    resource.setDoi(DOI_1);
    resource.setStatus(PublicationStatus.PRIVATE);
    assertThrows(InvalidConfigException.class, () -> service.updateDoi(resource));

    verifyNoInteractions(doiService);
  }

  @Test
  public void testReplaceDoi() throws Exception {
    publishVersion1WithDoi1();
    resource.setMetadataVersion(new BigDecimal("2.0"));
    resource.setDoi(DOI_2);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);

    service.replaceDoi(resource, new BigDecimal("2.0"), new BigDecimal("1.0"));

    // the new DOI is registered first, then the replaced DOI points to its version
    InOrder order = inOrder(doiService);
    ArgumentCaptor<DataCiteMetadata> newMetadata = ArgumentCaptor.forClass(DataCiteMetadata.class);
    order.verify(doiService).register(eq(DOI_2), eq(RESOURCE_URI), newMetadata.capture());
    order.verify(doiService).update(DOI_1, VERSION_1_0_URI);
    ArgumentCaptor<DataCiteMetadata> replacedMetadata = ArgumentCaptor.forClass(DataCiteMetadata.class);
    order.verify(doiService).update(eq(DOI_1), replacedMetadata.capture());

    assertTrue(hasRelatedIdentifier(newMetadata.getValue(), RelationType.IS_NEW_VERSION_OF, DOI_1));
    assertTrue(hasRelatedIdentifier(replacedMetadata.getValue(), RelationType.IS_PREVIOUS_VERSION_OF, DOI_2));
    assertEquals(IdentifierStatus.PUBLIC, resource.getIdentifierStatus());
  }

  @Test
  public void testReplaceDoiWithoutReplacedVersionEml() throws Exception {
    publishVersion1WithDoi1();
    new File(tempDir.toFile(), "eml-1.0.xml").delete();
    resource.setMetadataVersion(new BigDecimal("2.0"));
    resource.setDoi(DOI_2);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);

    PublicationException e = assertThrows(PublicationException.class,
        () -> service.replaceDoi(resource, new BigDecimal("2.0"), new BigDecimal("1.0")));

    assertEquals(PublicationException.TYPE.DOI, e.getType());
    // the new DOI was registered already
    verify(doiService).register(eq(DOI_2), eq(RESOURCE_URI), any(DataCiteMetadata.class));
    verify(doiService, never()).update(any(), any(URI.class));
  }

  @Test
  public void testReplaceDoiFailsWhenReplacedDoiCannotBeUpdated() throws Exception {
    publishVersion1WithDoi1();
    resource.setMetadataVersion(new BigDecimal("2.0"));
    resource.setDoi(DOI_2);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    doThrow(new DoiException("unavailable")).when(doiService).update(DOI_1, VERSION_1_0_URI);

    assertThrows(PublicationException.class,
        () -> service.replaceDoi(resource, new BigDecimal("2.0"), new BigDecimal("1.0")));
  }

  @Test
  public void testReplaceDoiOfResourceNotInRequiredState() throws Exception {
    // no DOI assigned to a published version
    resource.setDoi(DOI_2);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.setMetadataVersion(new BigDecimal("2.0"));
    assertThrows(InvalidConfigException.class,
        () -> service.replaceDoi(resource, new BigDecimal("2.0"), new BigDecimal("1.0")));

    publishVersion1WithDoi1();
    resource.setMetadataVersion(new BigDecimal("2.0"));
    resource.setDoi(DOI_2);
    // another version than the resource's
    assertThrows(InvalidConfigException.class,
        () -> service.replaceDoi(resource, new BigDecimal("3.0"), new BigDecimal("1.0")));
    // unknown replaced version
    assertThrows(InvalidConfigException.class,
        () -> service.replaceDoi(resource, new BigDecimal("2.0"), new BigDecimal("1.5")));
    assertThrows(InvalidConfigException.class,
        () -> service.replaceDoi(resource, new BigDecimal("2.0"), null));

    verifyNoInteractions(doiService);
  }

  /**
   * Minimal resource with valid DataCite metadata.
   */
  private static Resource resource() {
    Resource resource = new Resource();
    Eml eml = new Eml();
    resource.setEml(eml);
    resource.setCoreType("Occurrence");
    resource.setShortname(SHORTNAME);
    resource.setTitle("Ants");
    Calendar cal = Calendar.getInstance();
    cal.set(2013, Calendar.JANUARY, 9);
    eml.setDateStamp(cal.getTime());
    Agent creator = new Agent();
    creator.setFirstName("John");
    creator.setLastName("Smith");
    eml.addCreator(creator);
    Organisation organisation = new Organisation();
    organisation.setName("GBIF");
    organisation.setKey(UUID.randomUUID().toString());
    resource.setOrganisation(organisation);
    resource.setMetadataVersion(Constants.INITIAL_RESOURCE_VERSION);
    resource.setStatus(PublicationStatus.PUBLIC);
    return resource;
  }

  /**
   * Version 1.0 published with DOI_1 registered, and its EML stored.
   */
  private void publishVersion1WithDoi1() throws Exception {
    resource.setDoi(DOI_1);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
    resource.setLastPublished(new Date());
    VersionHistory history = new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PUBLIC);
    history.setDoi(DOI_1);
    history.setStatus(IdentifierStatus.PUBLIC);
    resource.addVersionHistory(history);
    IptEmlWriter.writeEmlFile(new File(tempDir.toFile(), "eml-1.0.xml"), resource.getEml());
  }

  private static boolean hasRelatedIdentifier(DataCiteMetadata metadata, RelationType relationType, DOI doi) {
    return metadata.getRelatedIdentifiers() != null && metadata.getRelatedIdentifiers().getRelatedIdentifier().stream()
        .anyMatch(r -> r.getRelationType() == relationType
            && r.getRelatedIdentifierType() == RelatedIdentifierType.DOI
            && r.getValue().equalsIgnoreCase(doi.getDoiName()));
  }
}
