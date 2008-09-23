package org.gbif.provider.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.util.ServletContextAware;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ProviderCfg;
import org.gbif.provider.service.ProviderCfgManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResourceLoader;

public class AppConfig implements ServletContextAware, org.springframework.web.context.ServletContextAware{
	protected final Log log = LogFactory.getLog(AppConfig.class);
	private ProviderCfgManager providerCfgManager;
	private ProviderCfg cfg;
	// need some static fields to create static methods that can be used outside of spring managed contexts, e.g. for hibernate objects
	// fields are managed by regular instance setters thanks to singleton
	private static String baseURL;	
	private static String dataDIR;	
	private static ServletContext context;	

	@Autowired
	public void setServletContext(ServletContext ctx) {
		context=ctx;
		if (cfg!=null){
			dataDIR=getDataDir();
			log.info("Set new application baseDIR: "+dataDIR);
		}
		log.info("Configured new CONTEXT: "+context.toString());
	}


	private AppConfig(ProviderCfgManager providerCfgManager) {
		super();
		this.providerCfgManager = providerCfgManager;
		cfg=providerCfgManager.load();
		setBaseUrl(cfg.getBaseUrl());
		setDataDir(cfg.getDataDir());
	}

	
	
	// OTHER UTILITY METHODS, MOSTLY DEFINING PATHS & URLs
	
	// WEBAPP BASICS
	public static File getWebappDir() {
		File dir = new File(context.getRealPath("/"));
		return dir;
	}
	public static File getWebappFile(String relPath){
		File f = new File(context.getRealPath(relPath));
		return f;
	}
	public static URL getWebappURL(String relPath) {
		if (relPath.startsWith("/")) {
			relPath = relPath.substring(1);
		}
		String url = String.format("%s/%s", baseURL, relPath);
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	// RESOURCE BASICS
	public static File getResourceCacheDir(Long resourceId) {
		File dir = new File(getWebappDir(), "cache/"+resourceId.toString());
	    if (!dir.exists()) {
	        dir.mkdirs();
	    }
		return dir;
	}
	public static File getResourceCacheFile(Long resourceId, String relPath) {
		if (relPath.startsWith("/")) {
			relPath = relPath.substring(1);
		}
		File f = new File(getResourceCacheDir(resourceId), relPath);
		return f;
	}
	public static URL getResourceCacheUrl(Long resourceId, String relPath) {
		if (relPath.startsWith("/")) {
			relPath = relPath.substring(1);
		}
		URL url = getWebappURL(String.format("cache/%s/%s",resourceId, relPath));
		return url;
	}
	
	public static File getResourceDataDir(Long resourceId) {
		if (resourceId==null){
			throw new NullPointerException("Requires resourceId to find resource data dir");
		}
		File dir = new File(dataDIR, resourceId.toString());
	    if (!dir.exists()) {
	        dir.mkdirs();
	    }
		return dir;
	}
	public static File getResourceDataFile(Long resourceId, String relPath) {
		if (relPath.startsWith("/")) {
			relPath = relPath.substring(1);
		}
		File f = new File(getResourceDataDir(resourceId), relPath);
		return f;
	}
	public static String getResourceDataUrl(Long resourceId) {
		return String.format("%s/data/%s", baseURL, resourceId.toString());
	}	

	public static File getResourceLogoFile(Long resourceId) {
		File file = new File(getResourceDataDir(resourceId), "logo.jpg");
		return file;    	
	}

	public static String getResourceLogoUrl(Long resourceId) {
		return String.format("%s/logo.jpg", getResourceDataUrl(resourceId));
	}

	
	// CORE RECORDS
    public String getDetailUrl(CoreRecord core){
    	if (core.getResource()==null){
    		throw new IllegalArgumentException("Core records needs a resource");
    	}
    	return String.format("%s/%s/detail.html", getResourceDataUrl(core.getResource().getId()), core.getGuid());
    }

    // SOURCE/UPLOAD FILES
    public File getSourceFile(Long resourceId, Extension extension) throws IOException{    	
		File file = new File(getResourceDataDir(resourceId), String.format("source-%s.txt", extension.getTablename()));
		return file;
	}    

    // DUMP FILES
    public File getDumpFile(Long resourceId, Extension extension) throws IOException{    	
		File file = new File(getResourceDataDir(resourceId), String.format("data-%s.txt", extension.getTablename()));
		return file;
	}    

    public File getDumpArchiveFile(Long resourceId){
		File file = new File(getResourceDataDir(resourceId), "data.zip");
		return file;    	
    }

    public String getDumpArchiveUrl(Long resourceId){
		return String.format("%s/data.zip", getResourceDataUrl(resourceId));
    }

    // SERVICE ENDPOINTS
	public String getTapirEndpoint(Long resourceId){
		String base = getBaseUrl();
    	return String.format("%s/tapir/%s/", base, resourceId.toString());
	}
	
	public String getWfsEndpoint(Long resourceId){
		String base = getBaseUrl();
    	return String.format("%s/wfs/%s", base, resourceId.toString());
	}
	
	
	
	
	// CFG DELEGATE METHODS
	public String getBaseUrl() {
		return cfg.getBaseUrl();
	}

	public String getContactEmail() {
		return cfg.getMeta().getContactEmail();
	}

	public String getContactName() {
		return cfg.getMeta().getContactName();
	}

	public String getDataDir() {
		String dir = cfg.getDataDir();
		if (dir==null){
			// use sensible default that tests can access
			File dataDir = new File(getWebappDir(), "WEB-INF/data");
			dir = dataDir.getAbsolutePath();
		}
		return dir;
	}

	public String getDescription() {
		return cfg.getMeta().getDescription();
	}

	public String getEmlUrl() {
		return cfg.getMeta().getEmlUrl();
	}

	public String getDescriptionImage() {
		return cfg.getDescriptionImage();
	}

	public String getGeoserverUrl() {
		return cfg.getGeoserverUrl();
	}

	public String getLink() {
		return cfg.getMeta().getLink();
	}

	public String getTitle() {
		return cfg.getMeta().getTitle();
	}

	public void setBaseUrl(String baseUrl) {
		cfg.setBaseUrl(trimUrl(baseUrl));
		baseURL=getBaseUrl();
	}

	public void setContactEmail(String contactEmail) {
		cfg.getMeta().setContactEmail(contactEmail);
	}

	public void setContactName(String contactName) {
		cfg.getMeta().setContactName(contactName);
	}

	public void setDataDir(String dataDir) {
		if (dataDir != null){
			File dataDirFile = new File(dataDir);
			if (!dataDirFile.exists()){
				dataDirFile.mkdirs();
				log.info("Created new main data directory at "+dataDir);
			}
		}
		cfg.setDataDir(dataDir);
		if (context!=null){
			// can only get dir if context is set. During bean initialisation this might not be the case
			// therefore also set the static dataDIR in setContext method!
			dataDIR=getDataDir();
			log.info("Set new application baseDIR: "+dataDIR);
		}
	}

	public void setDescription(String description) {
		cfg.getMeta().setDescription(description);
	}

	public void setDescriptionImage(String descriptionImage) {
		cfg.setDescriptionImage(descriptionImage);
	}

	public void setEmlUrl(String emlUrl) {
		cfg.getMeta().setEmlUrl(emlUrl);
	}

	public void setGeoserverUrl(String geoserverUrl) {
		cfg.setGeoserverUrl(trimUrl(geoserverUrl));
	}

	public void setLink(String link) {
		cfg.getMeta().setLink(link);
	}

	public void setTitle(String title) {
		cfg.getMeta().setTitle(title);
	}

	
	
	public String getGoogleMapsApiKey() {
		return cfg.getGoogleMapsApiKey();
	}


	public void setGoogleMapsApiKey(String googleMapsApiKey) {
		cfg.setGoogleMapsApiKey(googleMapsApiKey);
	}


	// MANAGER "DELEGATE" METHODS
	public void load() {
		cfg = providerCfgManager.load();
	}

	public void save() {
		providerCfgManager.save(cfg);
	}

	
	
	// UTILITY METHODS
	private static String trimUrl(String url){
		url=url.trim();
		while(url.endsWith("/")){
			url = (String) url.subSequence(0, url.length()-1);
		}
		return url;
	}




}
