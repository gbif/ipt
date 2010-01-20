package org.gbif.provider.webapp.action;


import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.security.ui.rememberme.TokenBasedRememberMeServices;

/**
 * Action to allow new users to sign up.
 */
public class LogoutAction extends BaseAction implements SessionAware, ServletRequestAware, ServletResponseAware{
    private static final long serialVersionUID = 6558311334878272308L;
    protected Map session;
	private HttpServletResponse response;
	private HttpServletRequest request;

    public String execute() {
        session.clear();
    	Cookie terminate = new Cookie(TokenBasedRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY, null);
    	String contextPath = request.getContextPath();
    	terminate.setPath(contextPath != null && contextPath.length() > 0 ? contextPath : "/");
    	terminate.setMaxAge(0);
    	response.addCookie(terminate);
        return SUCCESS;
    }

	public void setSession(Map session) {
		this.session=session;		
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request=request;
	}

	public void setServletResponse(HttpServletResponse response) {
		this.response=response;
	}

}
