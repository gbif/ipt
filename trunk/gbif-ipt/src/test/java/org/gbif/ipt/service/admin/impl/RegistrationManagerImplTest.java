package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistrationManagerImplTest {

  // Key of AAA1Organisation - Organisation IPT is associated to
  private static final String HOSTING_ORGANISATION_KEY = "f3e8e9a9-df60-40ca-bb71-7f49313b3150";
  // Key of Academy of Natural Sciences - Organisation Resource is associated to
  private static final String RESOURCE_ORGANISATION_KEY = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

  private RegistrationManager registrationManager;

  @Before
  public void setup() {
    // setup mock instances
    AppConfig mockAppConfig = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);
    ResourceManager mockResourceManager = mock(ResourceManager.class);

    // mock returning registration.xml file
    File registrationXML = FileUtils.getClasspathFile("config/registration.xml");
    when(mockDataDir.configFile(RegistrationManagerImpl.PERSISTENCE_FILE)).thenReturn(registrationXML);

    // create instance of manager
    registrationManager = new RegistrationManagerImpl(mockAppConfig, mockDataDir, mockResourceManager);

    // load associatedOrganisations, hostingOrganisation, etc
    registrationManager.load();

    // mock returning list of resources, including one whose owning organization is a the Academy of Natural Sciences
    Organisation orgResIsAssociatedTo = registrationManager.get(RESOURCE_ORGANISATION_KEY);
    List<Resource> ls = new ArrayList<Resource>();
    Resource res1 = new Resource();
    res1.setOrganisation(orgResIsAssociatedTo);
    ls.add(res1);
    when(mockResourceManager.list()).thenReturn(ls);
  }

  @Test
  public void testDeleteHostingOrganization() {
    // try deleting the organisation AAA1Organisation - it will throw a DeletionNotAllowedException since it is the
    // hosting organisation
    try {
      registrationManager.delete(HOSTING_ORGANISATION_KEY);
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.IPT_REGISTERED_WITH_ORGANISATION, e.getReason());
    }
  }

  @Test
  public void testDeleteOrganizationAssociatedToResource() {
    // try deleting the Academy of Natural Sciences - it will throw a DeletionNotAllowedException since there is a
    // resource associated to it
    try {
      registrationManager.delete(RESOURCE_ORGANISATION_KEY);
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.RESOURCE_REGISTERED_WITH_ORGANISATION, e.getReason());
    }
  }
}
