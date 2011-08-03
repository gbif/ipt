package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.commons.lang.xwork.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * An Interceptor that makes sure a requested resource is either public or the current user has rights to manage the private resource.
 */
public class PrivateResourceInterceptor extends AbstractInterceptor {
	private static Logger log = Logger.getLogger(RequireAdminInterceptor.class);
	@Inject
	private ResourceManager resourceManager;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		String requestedResource = RequireManagerInterceptor.getResourceParam(invocation);		
		if (requestedResource != null) {
			// does resource exist at all?
			Resource resource = resourceManager.get(requestedResource);
			if (resource == null) {
				return BaseAction.NOT_FOUND;
			}
			// private? 
			if (PublicationStatus.PRIVATE == resource.getStatus()){
				Map<String, Object> session = invocation.getInvocationContext().getSession();
				User user = (User) session.get(Constants.SESSION_USER);
				// user authorised?
				if (user == null || !isAuthorized(user, resource, invocation)){
					return BaseAction.NOT_ALLOWED;
				}
			}
		}
		return invocation.invoke();
	}

	private boolean isAuthorized(User user, Resource resource, ActionInvocation invocation) {
		if (user.hasAdminRights()) {
			return true;
		}
		if (resource != null && resource.getCreator() != null && resource.getCreator().equals(user)) {
			return true;
		}
		if (user.hasManagerRights()) {
			for (User m : resource.getManagers()) {
				if (user.equals(m)) {
					return true;
				}
			}
		}
		return false;
	}
}