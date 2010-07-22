/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.model.registry.BriefOrganisation;
import org.gbif.ipt.service.BaseManager;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author tim
 * @author josecuadra
 */
@Singleton
public class GBIFRegistryManagerImpl extends BaseManager implements GBIFRegistryManager {
  public static final String PERSISTENCE_FILE = "registry.xml";
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
   * 
   * public void save() throws IOException { log.debug("SAVING REGISTRY INFO ..."); Writer registryWriter =
   * FileUtils.startNewUtf8File(dataDir.configFile(PERSISTENCE_FILE)); //Writer registryWriter =
   * FileUtils.startNewUtf8File(new File("/tmp/reg.xml")); xstream.toXML(registry,registryWriter);
   * registryWriter.close(); }
   * 
   * public void load() throws InvalidConfigException { Reader registryReader = null; try { registryReader =
   * FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE)); registry =
   * (Registry)xstream.fromXML(registryReader); } catch (FileNotFoundException e) { log.debug(e); throw new
   * InvalidConfigException(TYPE.REGISTRY_CONFIG, "Couldnt read registry information: "+e.getMessage()); } catch
   * (IOException e) { log.error(e.getMessage(), e); throw new InvalidConfigException(TYPE.REGISTRY_CONFIG,
   * "Couldnt read registry information: "+e.getMessage()); } finally { if (registryReader!=null){ try {
   * registryReader.close(); } catch (IOException e) { } } } }
   */

  public URL getExtensionListUrl() {
    // until the registry handles JSONP with a callback parameter we need a local json file!
    URL url = null;
    try {
      url = new URL(cfg.getBaseURL() + "/extensions.json");
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return url;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#validateOrganisation(java.lang.String, java.lang.String)
   */
  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.admin.GBIFRegistryManager#listAllOrganisations()
   */
  public List<BriefOrganisation> listAllOrganisations() {
    List<GbrdsOrganisation> list = orgApi.list().execute().getResult();
    return Lists.transform(list, new Function<GbrdsOrganisation, BriefOrganisation>() {
      public BriefOrganisation apply(GbrdsOrganisation go) {
        BriefOrganisation o = new BriefOrganisation();
        o.setKey(go.getKey());
        o.setName(go.getName());
        return o;
      }
    });
  }

  public boolean validateOrganisation(String key, String password) {
    return orgApi.validateCredentials(OrgCredentials.with(key, password)).execute().getResult();
  }

  /*
   * public static void main(String args[]) { GBIFRegistryManagerImpl rg = new GBIFRegistryManagerImpl(); try {
   * rg.save(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
   */

}
