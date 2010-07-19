/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.gbif.ipt.model.registry.Organisation;
import org.gbif.ipt.model.registry.Registry;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.GBIFRegistryManager;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.registry.api.client.Gbrds;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

/**
 * @author tim
 * @author josecuadra
 */
@Singleton
public class GBIFRegistryManagerImpl extends BaseManager implements GBIFRegistryManager {
	public static final String PERSISTENCE_FILE = "registry.xml";
	private Registry registry = new Registry();
	private final XStream xstream = new XStream();
	private Gbrds client;

	/**
	 * 
	 */
	@Inject
	public GBIFRegistryManagerImpl(Gbrds client) {
		super();
		defineXstreamMapping();
	}
	
	private void defineXstreamMapping(){
		xstream.alias("registry", Registry.class);
		xstream.alias("organisation", Organisation.class);
	}	

	public void save() throws IOException {
		log.debug("SAVING REGISTRY INFO ...");
		Writer registryWriter = FileUtils.startNewUtf8File(dataDir.configFile(PERSISTENCE_FILE));
		//Writer registryWriter = FileUtils.startNewUtf8File(new File("/tmp/reg.xml"));
		xstream.toXML(registry,registryWriter);
		registryWriter.close();
	}	
	
	public void load() throws InvalidConfigException {
		Reader registryReader = null;
		try {
			registryReader = FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE));
			registry = (Registry)xstream.fromXML(registryReader);
		} catch (FileNotFoundException e) {
			log.debug(e);
			throw new InvalidConfigException(TYPE.REGISTRY_CONFIG, "Couldnt read registry information: "+e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new InvalidConfigException(TYPE.REGISTRY_CONFIG, "Couldnt read registry information: "+e.getMessage());
		} finally {
			if (registryReader!=null){
				try {
					registryReader.close();
				} catch (IOException e) {
				}			
			}
		}
	}	
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gbif.ipt.service.admin.GBIFRegistryManager#listAllOrganisations()
	 */
	public List<Organisation> listAllOrganisations() {
		return registry.getIptOrganisations();
	}
	
	
	public URL getExtensionListUrl() {
		// until the registry handles JSONP with a callback parameter we need a local json file!
		URL url = null;
		try {
			url = new URL(cfg.getBaseURL()+"/extensions.json");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}
	
	
	/*public static void main(String args[]) {
		GBIFRegistryManagerImpl rg = new GBIFRegistryManagerImpl();
		try {
			rg.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/

	
}
