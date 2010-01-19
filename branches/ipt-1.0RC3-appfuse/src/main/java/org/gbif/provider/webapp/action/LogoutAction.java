package org.gbif.provider.webapp.action;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.RequestAware;
import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.Constants;
import org.appfuse.service.UserExistsException;
import org.appfuse.webapp.util.RequestUtil;
import org.gbif.provider.model.User;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.rememberme.TokenBasedRememberMeServices;

import sun.org.mozilla.javascript.internal.ContextAction;

/**
 * Action to allow new users to sign up.
 */
public class LogoutAction extends BaseAction implements SessionAware, RequestAware{
    private static final long serialVersionUID = 6558311334878272308L;
    protected Map session;

    public String execute() {
        if (ServletActionContext.getRequest().getMethod().equals("GET")) {
            return INPUT;
        }
     	if (request.getSession(false) != null) {
    	    session.invalidate();
    	}
    	Cookie terminate = new Cookie(TokenBasedRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY, null);
    	String contextPath = ServletActionContext.getRequest().getContextPath();
    	terminate.setPath(contextPath != null && contextPath.length() > 0 ? contextPath : "/");
    	terminate.setMaxAge(0);
    	response.addCookie(terminate);
        return SUCCESS;
    }

	public void setSession(Map session) {
		this.session=session;		
	}

	public void setRequest(Map request) {
		// TODO Auto-generated method stub
		
	}


}
