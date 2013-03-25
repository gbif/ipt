package org.gbif.provider.webapp;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.appfuse.model.User;
import org.appfuse.webapp.interceptor.UserRoleAuthorizationInterceptor;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;


public class ManagerInterceptor implements Interceptor{
    @Autowired
    @Qualifier("resourceManager")
    protected GenericResourceManager<Resource> resourceManager;

	public String intercept(ActionInvocation invocation) throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();

		// admin users are allowed anything
        if (request.isUserInRole(org.appfuse.Constants.ADMIN_ROLE)) {
            return invocation.invoke();
        }
        
        // managers can only manage resources they created
        if (request.isUserInRole(Constants.MANAGER_ROLE) && isResourceOwner(request)) {
            return invocation.invoke();
        }

        // not authorised
        HttpServletResponse response = ServletActionContext.getResponse();
        handleNotAuthorized(request, response);
        return null;

	}
	
	private boolean isResourceOwner(HttpServletRequest request){		
		//get requested resource
		Object requested_id = request.getParameter(ResourceInterceptor.PARAMETER_NAME);
		if (requested_id != null && requested_id.getClass().isArray() && ((Object[]) requested_id).length == 1) {
			requested_id = ((Object[]) requested_id)[0];
		}
		// cast to integer
		Long resourceId = null;
		if (requested_id != null) {
			try {
				resourceId = Long.valueOf(requested_id.toString());
				// load resource and compare creator to user
				Resource res = resourceManager.get(resourceId);
				Principal user = request.getUserPrincipal();				
				if (res.getCreator()!=null && res.getCreator().getUsername() != user.getName()){
					return false;
				}
			} catch (NumberFormatException e) {
				// do nothing, aint no proper resource anyway
			}
		}
		return true;
	}

	protected void handleNotAuthorized(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	public void destroy() {
	}

	public void init() {
	}
	
}
