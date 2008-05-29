package org.gbif.provider.model.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import org.appfuse.model.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResolvableBase;
import org.appfuse.model.User;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.security.Authentication;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;


public class AuditInterceptor extends EmptyInterceptor {
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	public boolean onSave(Object obj, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if (obj instanceof Timestampable) {
			setLastModified(state, propertyNames);
        }
        return false;
	}


	@Override
	public boolean onFlushDirty(Object obj, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		if (obj instanceof Timestampable) {            
			setLastModified(currentState, propertyNames);
        }
        return false;
	}


    private void setLastModified(Object[] state, String[] propertyNames) {
		int i = 0;
		for (String prop : propertyNames){
			if (prop.equals("modified")){
				state[i]=new Date();
				break;
			}
			i+=1;
		}		
	}
    
    /**
     * Gets the current user's id from the Acegi/Spring SecurityContext
     * 
     * @return current user's userId
     */
    private String getUserName() {
    	SecurityContext secureContext = (SecurityContext) SecurityContextHolder.getContext();

        // secure context will be null when running unit tests so leave userId
        // as null
        if (secureContext != null) {
            Authentication auth = (Authentication) ((SecurityContext) SecurityContextHolder.getContext()).getAuthentication();

            String userName = null;
            if (auth.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                userName = userDetails.getUsername();
            } else {
                userName = auth.getPrincipal().toString();
            }
            
            if(userName == null || userName.equals("")) {
                return "anonymousUser";
            } else {
                return userName;
            }
            
        } else {
            return "anonymousUser";
        }
    }	
}
