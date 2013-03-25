package org.gbif.provider.util;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.web.context.ServletContextAware;

/**
 * Extended PropertyPlaceholderConfigurer that dynamically resolves 2 properties from the current servlet context:
 *   ${dataDir} - the absolute path to a directory called data within the webapp
 *   ${webappDir} - the absolute path to the webapp directory
 * The servlet context is only used in case the properties are not resolvable via the regular PropertyPlaceholderConfigurer.
 * Property files override the servlet context defaults.
 *  
 * @author markus
 *
 */
public class WebContextPropertyResolver extends PropertyPlaceholderConfigurer implements ServletContextAware{
	private final Log log = LogFactory.getLog(WebContextPropertyResolver.class);
	private ServletContext context;
	
	
	@Override
	protected String resolvePlaceholder(String placeholder, Properties props) {
		String result = super.resolvePlaceholder(placeholder, props);
		if (result==null){
			if (context==null){
				throw new NullPointerException("Servlet context is required for WebContextPropertyResolver");
			}
			// only use the webcontext placeholders as defaults that can be overridden by regular property definitions
			if (placeholder.equalsIgnoreCase("datadir")){
				result = context.getRealPath("/data");
				log.debug(String.format("Resolved DATADIR placeholder with servlet context : %s",result));
			}
			else if (placeholder.equalsIgnoreCase("webappdir")){
				result = context.getRealPath("/");
				log.debug(String.format("Resolved WEBAPPDIR placeholder with servlet context : %s",result));
			}
		}
		return result;
	}

	@Autowired(required=true)
	public void setServletContext(ServletContext servletContext) {
		this.context = servletContext;		
	}
	
}
