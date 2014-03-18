package org.gbif.ipt.action.admin;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.service.manage.impl.ResourceManagerImplTest;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.ReportHandler;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PublishAllResourcesActionTest {

  private PublishAllResourcesAction action;

  @Before
  public void setup()
    throws IOException, ParserConfigurationException, SAXException, AlreadyExistingException, ImportException,
    InvalidFilenameException {

    ResourceManagerImplTest test = new ResourceManagerImplTest();
    ResourceManagerImpl mockResourceManager = test.getResourceManagerImpl();
    // prepare and add resource
    Resource resource = test.getNonRegisteredMetadataOnlyResource();
    mockResourceManager.save(resource);
    // mock generateDwca() throwing PublicationException, not actually possible, but used to test failed publications
    GenerateDwcaFactory mockDwcaFactory = mockResourceManager.getDwcaFactory();
    when(mockDwcaFactory.create(any(Resource.class), any(ReportHandler.class)))
      .thenThrow(new PublicationException(PublicationException.TYPE.DWCA, "Mock exception"));

    // mock action
    action = new PublishAllResourcesAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
      mock(RegistrationManager.class), mockResourceManager, mock(RegistryManager.class));

  }

  @Test
  public void testExecuteFailsOnPublish() throws Exception {
    Resource resource = action.resourceManager.get("res2");

    // make a few pre-publication assertions
    assertEquals(3, resource.getEml().getEmlVersion());

    // populate a source mapping, and assign it to resource
    ExtensionMapping em = new ExtensionMapping();
    PropertyMapping pm = new PropertyMapping();
    pm.setTerm(DwcTerm.occurrenceID);
    pm.setIndex(1);
    em.setFields(Sets.newHashSet(pm));
    Extension extension = new Extension();
    extension.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);
    em.setExtension(extension);
    resource.addMapping(em);

    // trigger publish all
    String result = action.execute();

    // make some post-failed-publication assertions
    assertEquals("success", result);

    // PublicationException logged in ActionError
    assertEquals(1, action.getActionErrors().size());
    // # of publish event failures for resource captured
    assertEquals(1, action.resourceManager.getProcessFailures().size());
    assertFalse(action.resourceManager.hasMaxProcessFailures(resource));
    assertEquals(3, resource.getEml().getEmlVersion());
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
