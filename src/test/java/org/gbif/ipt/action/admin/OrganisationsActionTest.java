package org.gbif.ipt.action.admin;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.ipt.validation.OrganisationSupport;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganisationsActionTest {

  private static final UUID ORGANISATION_KEY = UUID.fromString("dce7a3c9-ea78-4be7-9abc-e3838de70dc5");

  // TODO: 2019-06-20 analyze
  /**
   * Mock EZID account being used to assign a DOI to a resource, then try to change the primary DOI agency account to
   * a DataCite account. This needs to be prevented, because it will render the existing DOI impossible to update.
   */
  @Test
  public void testIsAnotherAccountInUseAlready() throws Exception {
    // RegistrationManager .getFromDisk returning Organisation configured with EZID account
    RegistrationManager mockRegistrationManagerEZID = mock(RegistrationManager.class);
    Organisation organisationWithEZID = new Organisation();
    organisationWithEZID.setKey(ORGANISATION_KEY.toString());
    organisationWithEZID.setDoiRegistrationAgency(DOIRegistrationAgency.EZID);
    organisationWithEZID.setAgencyAccountPrimary(true);
    when(mockRegistrationManagerEZID.getFromDisk(ORGANISATION_KEY.toString())).thenReturn(organisationWithEZID);

    // ResourceManager returning 1 resource that has been assigned a DOI from above Organisation with EZID account
    ResourceManager mockResourceManager2 = mock(ResourceManager.class);
    List<Resource> resources2 = Lists.newArrayList();
    Resource r2 = new Resource();
    r2.setShortname("ants");
    r2.setStatus(PublicationStatus.PUBLIC);
    r2.setDoi(DOIUtils.mintDOI(DOIRegistrationAgency.EZID, Constants.EZID_TEST_DOI_SHOULDER));
    r2.setDoiOrganisationKey(ORGANISATION_KEY);
    resources2.add(r2);
    when(mockResourceManager2.list()).thenReturn(resources2);

    // configure action
    OrganisationsAction action =
      new OrganisationsAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mockRegistrationManagerEZID,
        mock(OrganisationSupport.class), mock(OrganisationsAction.RegisteredOrganisations.class),
        mockResourceManager2);

    // Mock Organisation being saved in form
    Organisation savedOrganisation = new Organisation();
    savedOrganisation.setKey(ORGANISATION_KEY.toString());
    savedOrganisation.setDoiRegistrationAgency(DOIRegistrationAgency.EZID);
    assertFalse(action.isAnotherAccountInUseAlready(savedOrganisation)); // success

    // Mock Organisation being saved in form, but this time using a DataCite account
    savedOrganisation.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    assertTrue(action.isAnotherAccountInUseAlready(savedOrganisation)); // failure
  }
}
