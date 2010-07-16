/**
 * 
 */
package org.gbif.ipt.action.admin;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.util.ServletContextAware;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.action.FormAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.UserAccountManager;

import com.google.inject.Inject;
import com.opensymphony.xwork2.validator.validators.EmailValidator;



/**
 * The Action responsible for all user input relating to the IPT configuration
 * @author tim
 */
public class SetupAction extends BaseAction implements ServletRequestAware, ServletContextAware{
	private static final long serialVersionUID = 4726973323043063968L;
	private static Pattern emailPattern = Pattern.compile(EmailValidator.emailAddressPattern);
	@Inject
	protected ConfigManager configManager;
	@Inject
	protected UserAccountManager userManager;

	private HttpServletRequest req;

	// action attributes to be set
	protected String dataDirPath;
	protected User user=new User();
	protected Boolean production;
	protected String baseURL;
	private ServletContext ctx;
	
	
	/**
	 * Method called when setting up the IPT for the very first time.
	 * There might not even be a logged in user, be careful to not require an admin!
	 * @return
	 * @throws InvalidConfigException 
	 */
	public String setup() {
		if (dataDirPath!=null){
			File dd = new File (dataDirPath);
			try {
				boolean created = configManager.setDataDir(dd);
				if (created){
					addActionMessage(getText("admin.config.setup.datadir.created"));
				}else{
					addActionMessage(getText("admin.config.setup.datadir.reused"));
				}
			} catch (InvalidConfigException e) {
				log.debug("Failed to setup datadir: "+e.getMessage(), e);
				addActionError(getText("admin.config.setup.datadir.error"));
				addActionError(e.getMessage());
			}
		}
		if (dataDir.isConfigured()){
			// the data dir is already/now configured, skip the first setup step
			return SUCCESS;
		}
		return INPUT;
	}
	

	public String setup2(){
		if (production!=null && StringUtils.trimToNull(user.getEmail())!=null){
			try {
				user.setRole(Role.Admin);
				user.setLastLoginToNow();
				userManager.add(user);
				userManager.save();
				// set IPT type: registry URL
				if (production){
					//TODO: use registry manager
				}else{
					//TODO: use registry manager
				}
				// set baseURL
				try {
					URL burl = new URL(baseURL);
					configManager.setBaseURL(burl);
				} catch (MalformedURLException e) {
				}
				// save config
				configManager.saveConfig();
				addActionMessage(getText("admin.config.setup2.success"));
				return SUCCESS;
			} catch (IOException e) {
				log.error("Failed to setup admin account. Can't write user file: "+e.getMessage(), e);
				addActionError("Failed to setup admin account. Can't write user file: "+e.getMessage());
			} catch (AlreadyExistingException e) {
				log.error("Failed to setup admin account: "+e.getMessage(), e);
				addActionError(e.getMessage());
			} catch (InvalidConfigException e) {
				log.error("Failed to configure IPT: "+e.getMessage(), e);
				addActionError(e.getMessage());
			}
		}
		return INPUT;
	}
	
	@Override
	public void validate() {
		if (production!=null){
			// we are in step2
			if (user.getEmail().length() < 3) {
				addFieldError("user.email", getText("validation.email.required"));
			}else{
				if (! emailPattern.matcher(user.getEmail()).matches()){
					addFieldError("user.email", getText("validation.email.invalid"));
				}
			}
			if (user.getFirstname().length() < 2) {
				addFieldError("user.firstname", getText("validation.firstname.required"));
			}
			if (user.getLastname().length() < 2) {
				addFieldError("user.lastname", getText("validation.lastname.required"));
			}
			if (user.getPassword().length() < 4) {
				addFieldError("user.password", getText("validation.password.required"));
			}			
			if (StringUtils.trimToNull(baseURL)==null) {
				addFieldError("baseURL", getText("validation.baseURL.required"));
			}else{
				try {
					URL burl = new URL(baseURL);
				} catch (MalformedURLException e) {
					addFieldError("baseURL", getText("validation.baseURL.invalid"));
				}
			}		
		}
	}
	/** Tries to guess the current baseURL on the running server from the context
	 * @return baseURL as string
	 */
	public String findBaseURL() {
		// try to detect the baseURL if not configured yet!
	    String appBase = req.getScheme() + "://" + getHostname() + getPort() + req.getContextPath();
		log.info("Auto-Detected IPT BaseURL="+appBase);
		return appBase;
	}

	private String getHostname() {
		String host=req.getServerName();
		try {
		    InetAddress addr = InetAddress.getLocalHost();
		    // Get hostname
		    host = addr.getHostName();
		} catch (UnknownHostException e) {
			// stick with localhost
		}
		return host;
	}
	private String getPort() {
	    if ("http".equalsIgnoreCase(req.getScheme()) && req.getServerPort() != 80 ||
	            "https".equalsIgnoreCase(req.getScheme()) && req.getServerPort() != 443 ) {
	        return (":" + req.getServerPort());
	    } else {
	        return "";
	    }
	}
	
	
	public String getDataDirPath() {
		return dataDirPath;
	}
	public void setDataDirPath(String dataDirPath) {
		this.dataDirPath = dataDirPath;
	}


	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}


	public Boolean isProduction() {
		return production;
	}
	public void setProduction(Boolean production) {
		this.production = production;
	}


	@Override
	public String getBaseURL() {
		// try to detect default values if not yet configured
		if (StringUtils.trimToNull(baseURL)==null){
			baseURL=findBaseURL();
		}
		return  baseURL;
	}
	public void setBaseURL(String baseUrlVerbatim) {
		this.baseURL=baseUrlVerbatim;
	}


	public void setServletRequest(HttpServletRequest request) {
		this.req=request;
	}


	public void setServletContext(ServletContext context) {
		this.ctx=context;
	}
	
}
