package org.gbif.ipt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.ipt.utils.InputStreamUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppConfig {
	protected static final String DATADIR_PROPFILE = "ipt.properties";
	private static final String CLASSPATH_PROPFILE = "/application.properties";
	public static final String BASEURL = "ipt.baseURL";
	public static final String DEBUG = "debug";
	public static final String GMAPS_KEY = "google.maps.key";
	public static final String ANALYTICS_GBIF = "analytics.gbif";
	public static final String ANALYTICS_KEY = "analytics.key";
	private static final String PRODUCTION_TYPE_LOCKFILE = ".gbifreg";
	private Properties properties = new Properties();
	private Log log = LogFactory.getLog(this.getClass());
	private DataDir dataDir;
	private boolean testInstallation=true;


	@Inject
	public AppConfig(DataDir dataDir) {
		super();
		this.dataDir=dataDir;
		// also loaded via ConfigManager constructor if datadir was linked at startup already
		// If it wasnt, this is the only place to load at least the default classpath config settings
		loadConfig();
	}


	protected void loadConfig() {
		InputStreamUtils streamUtils = new InputStreamUtils();
		InputStream configStream = streamUtils.classpathStream(CLASSPATH_PROPFILE);
		try {
			Properties props=new Properties();
			props.load(configStream);
			log.debug("Loaded default configuration from application.properties");
			if (dataDir.dataDir!=null && dataDir.dataDir.exists()){
				// read user configuration from data dir if it exists
				File userCfgFile = new File(dataDir.dataDir, "config/"+DATADIR_PROPFILE);
				if (userCfgFile.exists()){
					try {
						props.load(new FileInputStream(userCfgFile));
						log.debug("Loaded user configuration from "+userCfgFile.getAbsolutePath());
					} catch (IOException e) {
						log.warn("DataDir configured, but failed to load user configuration from "+userCfgFile.getAbsolutePath(), e);
					}
				}else{
					log.warn("DataDir configured, but user configuration doesnt exist: "+userCfgFile.getAbsolutePath());
				}
				// check if this datadir is a production or test installation
				// we use a hidden file to indicate the production type
				File productionLockFile = getProductionLockFile();
				if (productionLockFile.exists()){
					setTestInstallation(false);
				}
			}
			// without error replace existing config with new one
			this.properties=props;
		} catch (IOException e) {
			log.error("Failed to load the default application configuration from application.properties", e);
		}
	}
	
	private File getProductionLockFile(){
		return dataDir.configFile(PRODUCTION_TYPE_LOCKFILE);		
	}
	
	protected void setTestInstallation(boolean b) {
		this.testInstallation=b;		
	}
	public boolean isTestInstallation() {
		return testInstallation;
	}


	/**
	 * @throws IOException 
	 * 
	 */
	protected void saveConfig() throws IOException{
		// make sure production lock exists
		if (!testInstallation){
			File productionLockFile = getProductionLockFile();
			if (!productionLockFile.exists()){
				FileUtils.touch(productionLockFile);
			}
		}
		// save property config file
		File userCfgFile = new File(dataDir.dataDir, "config/"+DATADIR_PROPFILE);
		if (userCfgFile.exists()){
		}
		OutputStream out = new FileOutputStream(userCfgFile);
		
		Properties props = (Properties) properties.clone();
		Enumeration<?> e = props.propertyNames();
	    while (e.hasMoreElements()) {
	      String key = (String) e.nextElement();
	      if (key.startsWith("dev.")){
	    	  props.remove(key);
	      }
	    }
	    props.store(out, "IPT configuration, last saved "+new Date().toString());
	    out.close();
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	public void setProperty(String key, String value) {
		properties.setProperty(key, StringUtils.trimToEmpty(value));
	}

	public String getBaseURL() {
		String base = properties.getProperty(BASEURL);
		while (base!=null && base.endsWith("/")){
			base=base.substring(0, base.length()-1);
		}
		return base;
	}

	public String getGoogleMapsKey() {
		return properties.getProperty(GMAPS_KEY);
	}

	public String getVersion(){
		return properties.getProperty("dev.version");
	}
	
	public boolean debug() {
		return "true".equalsIgnoreCase(properties.getProperty(DEBUG));
	}

	public boolean isGbifAnalytics() {
		return "true".equalsIgnoreCase(properties.getProperty(ANALYTICS_GBIF));
	}
	
	public String getAnalyticsKey() {
		return properties.getProperty(ANALYTICS_KEY);
	}

}