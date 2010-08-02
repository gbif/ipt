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
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.Gbrds.OrganisationApi;
import org.gbif.registry.api.client.GbrdsOrganisation;

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
  private static final String PERSISTENCE_FILE = "registry.xml";
  // private Registry registry = new Registry();
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
    // defineXstreamMapping();
  }

  /*
   * private void defineXstreamMapping(){ //xstream.alias("registry", Registry.class); //xstream.alias("organisation",
   * Organisation.class); }
   * public void save() throws IOException { log.debug("SAVING REGISTRY INFO ..."); Writer registryWriter =
   * FileUtils.startNewUtf8File(dataDir.configFile(PERSISTENCE_FILE)); //Writer registryWriter =
   * FileUtils.startNewUtf8File(new File("/tmp/reg.xml")); xstream.toXML(registry,registryWriter);
   * registryWriter.close(); }
   * public void load() throws InvalidConfigException { Reader registryReader = null; try { registryReader =
   * FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE)); registry =
   * (Registry)xstream.fromXML(registryReader); } catch (FileNotFoundException e) { log.debug(e); throw new
   * InvalidConfigException(TYPE.REGISTRY_CONFIG, "Couldnt read registry information: "+e.getMessage()); } catch
   * (IOException e) { log.error(e.getMessage(), e); throw new InvalidConfigException(TYPE.REGISTRY_CONFIG,
   * "Couldnt read registry information: "+e.getMessage()); } finally { if (registryReader!=null){ try {
   * registryReader.close(); } catch (IOException e) { } } } }
   */

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#deregister(org.gbif.ipt.model.Resource)
   */
  public void deregister(Resource resource) throws RegistryException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#validateOrganisation(java.lang.String, java.lang.String)
   */
  /*
   * (non-Javadoc)
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
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation)
   */
  public UUID register(Resource resource, Organisation organisation) throws RegistryException {
    return UUID.randomUUID();
  }

  public boolean validateOrganisation(String key, String password) {
    return orgApi.validateCredentials(OrgCredentials.with(key, password)).execute().getResult();
  }

  /*
   * public static void main(String args[]) { GBIFRegistryManagerImpl rg = new GBIFRegistryManagerImpl(); try {
   * rg.save(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
   */

}
