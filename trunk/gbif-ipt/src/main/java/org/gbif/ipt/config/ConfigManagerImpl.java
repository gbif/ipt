/**
 * 
 */
package org.gbif.ipt.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.struts2.config.ServletContextSingleton;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.ipt.utils.LogFileAppender;
import org.gbif.ipt.utils.XmlFileUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

/**
 * A skeleton implementation for the time being....
 * @author tim
 */
@Singleton
public class ConfigManagerImpl extends BaseManager implements ConfigManager {
	private DataDir dataDir;
	private AppConfig cfg;
	private InputStreamUtils streamUtils;
	private UserAccountManager userManager;
	private ResourceManager resourceManager;


	@Inject	
	public ConfigManagerImpl(DataDir dataDir, AppConfig cfg, InputStreamUtils streamUtils, UserAccountManager userManager, ResourceManager resourceManager) {
		super();
		this.dataDir = dataDir;
		this.cfg = cfg;
		this.streamUtils = streamUtils;
		this.userManager = userManager;
		this.resourceManager = resourceManager;
		if (dataDir.isConfigured()){
			log.info("IPT DataDir configured - loading its configuration");
			try {
				loadDataDirConfig();
			} catch (InvalidConfigException e) {
				throw new IllegalStateException(e);
			}
		}else{
			log.debug("IPT DataDir not configured - no configuration loaded");
		}
	}


	private void reloadLogger() {
		LogFileAppender.LOGDIR = dataDir.loggingFile("").getAbsolutePath();
		log.info("Setting logging dir to "+LogFileAppender.LOGDIR);

		InputStream log4j;
		// use different log4j settings files for production or debug mode
		if (cfg.debug()){
			log4j = streamUtils.classpathStream("/log4j.xml");
		}else{
			log4j = streamUtils.classpathStream("/log4j-production.xml");
		}
		LogManager.resetConfiguration();
		DOMConfigurator domConfig = new DOMConfigurator();
		domConfig.doConfigure(log4j, LogManager.getLoggerRepository());
		log.info("Reloaded log4j for " + (cfg.debug()? "debugging":"production"));
	}

	public boolean setDataDir(File dataDir) throws InvalidConfigException {
		boolean created = this.dataDir.setDataDir(dataDir);
		loadDataDirConfig();
		return created;
	}

	/** Update configuration singleton from config file in data dir
	 * @return true if successful
	 * @throws InvalidConfigException 
	 */
	public void loadDataDirConfig() throws InvalidConfigException {
		log.info("Reading DATA DIRECTORY: " + dataDir.loggingFile("").getAbsolutePath());

		log.debug("Loading IPT config ...");
		cfg.loadConfig();

		log.debug("Reloading log4j settings ...");
		reloadLogger();

		log.debug("Loading user accounts ...");
		userManager.load();
		
		log.debug("Loading resource configurations ...");
		resourceManager.load();
	}

	public void saveConfig() throws InvalidConfigException {
		try {
			cfg.saveConfig();
		} catch (IOException e) {
			log.debug("Cant save IPT configuration: "+e.getMessage(), e);
			throw new InvalidConfigException(TYPE.IPT_CONFIG_WRITE, "Cant save IPT configuration: "+e.getMessage());
		}
	}

	public void setConfigProperty(String key, String value) {
		cfg.setProperty(key, value);		
	}

	public boolean setupComplete() {
		if (dataDir.isConfigured()){
			if (!userManager.list(Role.Admin).isEmpty()){
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.gbif.ipt.service.admin.ConfigManager#setBaseURL(java.net.URL)
	 */
	public void setBaseURL(URL baseURL) throws InvalidConfigException {
		log.info("Updating the baseURL to: " + baseURL);

		// TODO insert some tests to check the base url is not a localhost

		// store in properties file
		cfg.setProperty(AppConfig.BASEURL, baseURL.toString());
	}

	public void setAnalyticsKey(String key) throws InvalidConfigException {
		cfg.setProperty(AppConfig.ANALYTICS_KEY, StringUtils.trimToEmpty(key));
	}

	public void setDebugMode(boolean debug) throws InvalidConfigException {
		cfg.setProperty(AppConfig.DEBUG, Boolean.toString(debug));
		reloadLogger();
	}

	public void setGbifAnalytics(boolean useGbifAnalytics) throws InvalidConfigException {
		cfg.setProperty(AppConfig.ANALYTICS_GBIF, Boolean.toString(useGbifAnalytics));
	}

	public void setGoogleMapsKey(String key) throws InvalidConfigException {
		cfg.setProperty(AppConfig.GMAPS_KEY, StringUtils.trimToEmpty(key));
	}
}
