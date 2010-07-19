/**
 * 
 */
package org.gbif.ipt.action.admin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.gbif.ipt.action.FormAction;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.admin.DwCExtensionManager;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.GbrdsExtension;
import org.gbif.registry.api.client.Gbrds.IptApi;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

/**
 * The Action responsible for all user input relating to the DarwinCore extension management.
 * 
 * @author tim
 */
public class DwCExtensionsAction extends FormAction {
	@Inject
	private DwCExtensionManager extensionManager;
	@Inject
	private RegisteredExtensions registered;	
	private List<Extension> extensions;
	private Extension extension;
	
	/**
	 * A session scoped bean to keep a list of all extensions with basic metadata.
	 * There wont be any properties listed at all.
	 * The extensions listed here will be created based on the json the registry emits in the extension list:
	 * @See extensions.json
	 * 
	 * The reason for keeping this in the session is to load the extension list only once - but not to store it continuesly in memory.
	 * Once the admin has logged out all this info will be removed again and only the installed extensions remain in memory.
	 * 
	 * @author markus
	 *
	 */
	@SessionScoped
	public static class RegisteredExtensions{
		public List<GbrdsExtension> extensions=new ArrayList<GbrdsExtension>();
		private Gbrds client;
		
		@Inject
		public RegisteredExtensions(Gbrds client) {
			super();
			this.client = client;
		}
		
		public void load(){
			IptApi api = client.getIptApi();
		    extensions = api.listExtensions().execute().getResult();
		}
		
	}
	
	public String list(){
		extensions=extensionManager.list();
		return SUCCESS;
	}

	@Override
	public void prepare() throws Exception{
		super.prepare();
		// in case
		if (registered.extensions.isEmpty()){
			registered.load();
		}
		if (id!=null){
			// modify existing user
			extension=extensionManager.get(id);
			if (extension==null){
				// set notFound flag to true so FormAction will return a NOT_FOUND 404 result name
				notFound=true;
			}
		}
	}

	@Override
	public String delete() {
		extensionManager.delete(id);
		return SUCCESS;
	}

	@Override
	public String save(){
		try {
			install();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return SUCCESS;
	}
	
	public void install() throws Exception{
		URL extensionURL;
		try {
			extensionURL = new URL(id);
			extensionManager.install(extensionURL);
			addActionMessage(getText("admin.config.extension.success", id));
		} catch (MalformedURLException e) {
			log.debug(e);
			addActionError(getText("admin.config.extension.error", id));
		}
	}

	public List<Extension> getExtensions() {
		return extensions;
	}
}
