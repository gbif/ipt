/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.GBIFRegistryManager;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.Gbrds.OrganisationApi;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

import java.util.List;
import java.util.UUID;

/**
 * @author tim
 * @author josecuadra
 */
@Singleton
public class GBIFRegistryManagerImpl extends BaseManager implements GBIFRegistryManager {
  private final XStream xstream = new XStream();
  private final Gbrds client;
  private final OrganisationApi orgApi;

  /**
	 * 
	 */
  @Inject
  public GBIFRegistryManagerImpl(Gbrds client) {
    super();
    this.client = client;
    orgApi = client.getOrganisationApi();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#deregister(org.gbif.ipt.model.Resource)
   */
  public void deregister(Resource resource) throws RegistryException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#listAllOrganisations()
   */
  public List<Organisation> listAllOrganisations() {
    List<GbrdsOrganisation> list = orgApi.list().execute().getResult();
    return Lists.transform(list, new Function<GbrdsOrganisation, Organisation>() {
      public Organisation apply(GbrdsOrganisation go) {
        Organisation o = new Organisation();
        o.setKey(go.getKey().toString());
        o.setName(go.getName());
        return o;
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation)
   */
  public UUID register(Resource resource, Organisation organisation) throws RegistryException {
    return UUID.randomUUID();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#validateOrganisation(java.lang.String, java.lang.String)
   */
  public boolean validateOrganisation(String key, String password) {
    return orgApi.validateCredentials(OrgCredentials.with(key, password)).execute().getResult();
  }

  /*
   * public static void main(String args[]) { GBIFRegistryManagerImpl rg = new GBIFRegistryManagerImpl(); try {
   * rg.save(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
   */

}
