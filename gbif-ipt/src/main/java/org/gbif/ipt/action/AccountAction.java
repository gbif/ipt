package org.gbif.ipt.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.UserAccountManager;

import com.google.inject.Inject;


public class AccountAction extends BaseAction implements ServletRequestAware{
	@Inject
	private UserAccountManager userManager;
	
	private String redirectUrl;
	private String email;
	private String password;

	private HttpServletRequest request;

	public String login(){
		if (email!=null){
			User user=userManager.authenticate(email, password);
			if (user!=null){
				log.info("User "+email+" logged in successfully");
				user.setLastLoginToNow();
//				agentService.update(user);
				session.put(Constants.SESSION_USER, user);
				// remember previous URL to redirect back to
				setRedirectUrl();
				return SUCCESS;
			}else{
	            addFieldError("email", "The email - password combination does not exists");
				log.info("User "+email+" failed to log in with password "+password);
			}
		}
		return INPUT;
	}
	
	private void setRedirectUrl(){
		redirectUrl=getBase()+"/";
		// if we have a request refer back to the originally requested page
		if ( request != null ) {
			String referer = request.getHeader( "Referer" );
			if ( referer != null && referer.startsWith(cfg.getBaseURL()) && !(referer.endsWith("login"))) {
				redirectUrl=referer;
			}
		}
		log.info("Redirecting to "+redirectUrl);
	}
	
	public String logout(){
		setRedirectUrl();
		session.clear();
		return SUCCESS;
	}

	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email!=null){
			this.email = email;
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request=request;		
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

}
