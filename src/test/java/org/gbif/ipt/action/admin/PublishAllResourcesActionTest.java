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
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.service.manage.impl.ResourceManagerImplTest;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.ReportHandler;
import org.gbif.metadata.eml.ipt.EmlFactory;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PublishAllResourcesActionTest {

  private PublishAllResourcesAction action;

  @BeforeEach
  public void setup() throws Exception {

    ResourceManagerImplTest test = new ResourceManagerImplTest();
    ResourceManagerImpl mockResourceManager = test.getResourceManagerImpl();
    // prepare and add resource
    Resource resource = test.getNonRegisteredMetadataOnlyResource();
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
    when(test.getMockedDataDir().resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(emlXML);

    // mock finding versioned RTF file - not important which one
    File rtf = File.createTempFile("short-1.1", ".rtf");
    when(test.getMockedDataDir().resourceRtfFile(anyString(), any(BigDecimal.class))).thenReturn(rtf);

    // mock action
    action = new PublishAllResourcesAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
      mockRegistrationManager, mockResourceManager, mock(RegistryManager.class));

  }

  @Test
  public void testExecuteFailsOnPublish() throws Exception {
    Resource resource = action.resourceManager.get("res2");

    // make a few pre-publication assertions
    assertEquals(BigDecimal.valueOf(1.0), resource.getReplacedEmlVersion());
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

}
