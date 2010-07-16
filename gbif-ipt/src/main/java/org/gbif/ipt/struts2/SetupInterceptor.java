package org.gbif.ipt.struts2;

import org.gbif.ipt.action.admin.SetupAction;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.ConfigManager;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
* An Interceptor that makes sure an admin user is currently logged in and returns a notAllowed otherwise
*/
public class SetupInterceptor extends AbstractInterceptor{
	public final String SETUP_RESULTNAME="setupIncomplete";
	@Inject 
	private ConfigManager configManager;
	
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		if (!configManager.setupComplete()){
	        Object action = invocation.getAction();
	        if (action instanceof SetupAction){
	    		return invocation.invoke();
	        }else{
				return SETUP_RESULTNAME;
	        }
		}
		return invocation.invoke();
	}

}