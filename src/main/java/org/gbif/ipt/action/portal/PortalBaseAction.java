/**
 * 
 */
package org.gbif.ipt.action.portal;

import org.apache.commons.lang.StringUtils;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

/**
 * The base of all portal actions.
 * 
 * @author
 */
public class PortalBaseAction extends BaseAction {
	@Inject
	protected ResourceManager resourceManager;
	protected Resource resource;

	public PortalBaseAction() {

	}
	public Resource getResource() {
		return resource;
	}

	@Override
	public void prepare() throws Exception {
		super.prepare();
		// look for resource parameter
		String res = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
		if (res==null){
			// try session instead
			try {
				res = (String) session.get(Constants.SESSION_RESOURCE);
			} catch (Exception e) {
				// swallow. if session is not yet opened we get an exception here...
			}
		}
		if (res!=null){
			resource = resourceManager.get(res);		
		}
	}
}
