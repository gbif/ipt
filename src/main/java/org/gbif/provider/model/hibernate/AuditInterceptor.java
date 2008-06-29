package org.gbif.provider.model.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import org.appfuse.model.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.Resource;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.security.Authentication;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;


public class AuditInterceptor extends EmptyInterceptor {
	protected final Log log = LogFactory.getLog(getClass());
	
	/* 	/* Setting last creation date+user for all Timestampable objects
	 * (non-Javadoc)
	 * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onSave(Object obj, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		boolean isModified = false;
		if (obj instanceof Timestampable) {            
			int i = 0;
			User user = getUser();
			Date now = new Date();
			for (String prop : propertyNames){
				if (prop.equals("created")){
					state[i]=now;
					isModified=true;
				}
				else if (prop.equals("creator")){
					state[i]=user;
					isModified=true;
				}
				else if (prop.equals("modified")){
					state[i]=now;
					isModified=true;
				}
				else if (prop.equals("modifier")){
					state[i]=user;
					isModified=true;
				}
				i+=1;
			}	
		}
        return isModified;

	}


	/* Setting last modification date+user for all Timestampable objects
	 * (non-Javadoc)
	 * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onFlushDirty(Object obj, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		boolean isModified = false;
		if (obj instanceof Timestampable) {            
			int i = 0;
			for (String prop : propertyNames){
				if (prop.equals("modified")){
					currentState[i]=new Date();
					isModified=true;
				}
				else if (prop.equals("modifier")){
					currentState[i]=getUser();
					isModified=true;
				}
				i+=1;
			}	
		}
        return isModified;
	}

    
    /**
     * Gets the current user's name from the Acegi/Spring SecurityContext
     * 
     * @return current user's name
     */
    private String getUserName() {
    	User user = getUser();
    	String userName = "anonymousUser";
        if (user != null) {
            userName = user.getUsername();            
        }            
        return userName;
    }
    
    /**
     * Gets the current appfuse user from the Acegi/Spring SecurityContext
     * 
     * @return current appfuse user
     */
    private User getUser() {
    	SecurityContext secureContext = (SecurityContext) SecurityContextHolder.getContext();
        // secure context will be null when running unit tests so leave userId
        // as null
        if (secureContext != null) {
            Authentication auth = (Authentication) ((SecurityContext) SecurityContextHolder.getContext()).getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                User user = (User) auth.getPrincipal();
        		return user;
            }
        }
		return null;
    }
}
