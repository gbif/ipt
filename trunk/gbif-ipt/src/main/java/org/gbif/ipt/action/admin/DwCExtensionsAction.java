/**
 * 
 */
package org.gbif.ipt.action.admin;

import java.util.List;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.action.FormAction;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.DwCExtensionManager;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the DarwinCore extension management
 * @author tim
 */
public class DwCExtensionsAction extends FormAction {
	@Inject
	private DwCExtensionManager extensionManager;

	private List<Extension> extensions;
	private Extension extension;

	
	public String list(){
		extensions=extensionManager.list();
		return SUCCESS;
	}

	@Override
	public void prepare() throws Exception{
		super.prepare();
		if (id==null){
			// create new user
			extension=new Extension();
		}else{
			// modify existing user
			extension=extensionManager.get(id);
			if (extension==null){
				// set notFound flag to true so FormAction will return a NOT_FOUND 404 result name
				notFound=true;
			}
		}
	}
}
