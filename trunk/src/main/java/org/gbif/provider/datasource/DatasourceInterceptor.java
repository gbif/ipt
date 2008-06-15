package org.gbif.provider.datasource;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class DatasourceInterceptor extends AbstractInterceptor{
	protected static final Log log = LogFactory.getLog(DatasourceInterceptor.class);

	public static final String SESSION_ATTRIBUTE = "CURRENT_RESOURCE_ID";
	public static final String PARAMETER_NAME = "resource_id";
	
	public String intercept(ActionInvocation invocation) throws Exception {
		//get requested resource
		Object requested_id = invocation.getInvocationContext().getParameters().get(PARAMETER_NAME);
		if (requested_id != null && requested_id.getClass().isArray() && ((Object[]) requested_id).length == 1) {
			requested_id = ((Object[]) requested_id)[0];
		}
		//save it in session
		Long resourceId = null;
		if (requested_id != null) {
			// cast to integer
			try {
				resourceId = Long.valueOf(requested_id.toString());
				if (resourceId != null){
					invocation.getInvocationContext().getSession().put(SESSION_ATTRIBUTE, resourceId);
					log.info("Changed resourceId in session to " + resourceId);
				}
			} catch (NumberFormatException e) {
				log.warn("Requested resource_id is no integer: "+requested_id);				
			}
		}
		//set id in datasource context
		resourceId = (Long) invocation.getInvocationContext().getSession().get(SESSION_ATTRIBUTE);
		if (resourceId != null){
			DatasourceContextHolder.setResourceId(resourceId);
			log.debug("Set datasource context to resourceId=" + resourceId);
		}else{
			log.debug("No datasource context. resourceId=null");
		}

		// continue with the rest of the interceptor stack not setting the result name here
        return invocation.invoke();
	}

}
