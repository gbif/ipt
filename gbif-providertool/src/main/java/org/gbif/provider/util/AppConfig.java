package org.gbif.provider.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ProviderCfg;
import org.gbif.provider.service.ProviderCfgManager;

public class AppConfig {
	protected final Log log = LogFactory.getLog(AppConfig.class);
	private ProviderCfgManager providerCfgManager;
	private static ProviderCfg cfg;
	
	
	private AppConfig(ProviderCfgManager providerCfgManager) {
		super();
		this.providerCfgManager = providerCfgManager;
		cfg=providerCfgManager.load();
		// assure directories exist 
		setDataDir(cfg.getDataDir());
	}

	
	
	// OTHER MOSTLY STATIC UTILITY METHODS

	// RESOURCE BASICS
	public static File getResourceDataDir(Long resourceId) {
		File dir = new File(getDataDir(), resourceId.toString());
	    if (!dir.exists()) {
	        dir.mkdirs();
	    }
		return dir;
	}
	public static String getResourceDataUrl(Long resourceId) {
		return String.format("%s/data/%s", getBaseUrl(), resourceId.toString());
	}	

	public static File getResourceLogoFile(Long resourceId) {
		File file = new File(getResourceDataDir(resourceId), "logo.jpg");
		return file;    	
	}

	public static String getResourceLogoUrl(Long resourceId) {
		return String.format("%s/logo.jpg", getResourceDataUrl(resourceId));
	}

	
	// CORE RECORDS
    public static String getDetailUrl(CoreRecord core){
    	if (core.getResource()==null){
    		throw new IllegalArgumentException("Core records needs a resource");
    	}
    	return String.format("%s/%s/detail.html", getResourceDataUrl(core.getResource().getId()), core.getGuid());
    }

    // SOURCE/UPLOAD FILES
    public static File getSourceFile(Long resourceId, Extension extension) throws IOException{    	
		File file = new File(getResourceDataDir(resourceId), String.format("source-%s.txt", extension.getTablename()));
		return file;
	}    

    // DUMP FILES
    public static File getDumpFile(Long resourceId, Extension extension) throws IOException{    	
		File file = new File(getResourceDataDir(resourceId), String.format("data-%s.txt", extension.getTablename()));
		return file;
	}    

    public static File getDumpArchiveFile(Long resourceId){
		File file = new File(getResourceDataDir(resourceId), "data.zip");
		return file;    	
    }

    public static String getDumpArchiveUrl(Long resourceId){
		return String.format("%s/data.zip", getResourceDataUrl(resourceId));
    }

    // SERVICE ENDPOINTS
	public static String getTapirEndpoint(Long resourceId){
		String base = getBaseUrl();
    	return String.format("%s/tapir/%s/", base, resourceId.toString());
	}
	
	public static String getWfsEndpoint(Long resourceId){
		String base = getBaseUrl();
    	return String.format("%s/wfs/%s", base, resourceId.toString());
	}
	
	
	
	
	// CFG DELEGATE METHODS
	public static String getBaseUrl() {
		return cfg.getBaseUrl();
	}

	public static String getContactEmail() {
		return cfg.getMeta().getContactEmail();
	}

	public static String getContactName() {
		return cfg.getMeta().getContactName();
	}

	public static String getDataDir() {
		return cfg.getDataDir();
	}

	public static String getDescription() {
		return cfg.getMeta().getDescription();
	}

	public static String getDescriptionImage() {
		return cfg.getDescriptionImage();
	}

	public static String getGeoserverUrl() {
		return cfg.getGeoserverUrl();
	}

	public static String getLink() {
		return cfg.getMeta().getLink();
	}

	public static String getTitle() {
		return cfg.getMeta().getTitle();
	}

	public void setBaseUrl(String baseUrl) {
		cfg.setBaseUrl(trimUrl(baseUrl));
	}

	public void setContactEmail(String contactEmail) {
		cfg.getMeta().setContactEmail(contactEmail);
	}

	public void setContactName(String contactName) {
		cfg.getMeta().setContactName(contactName);
	}

	public void setDataDir(String dataDir) {
		if (dataDir == null){
		    throw new NullPointerException();    			
		}else{
			File dataDirFile = new File(dataDir);
			if (!dataDirFile.exists()){
				dataDirFile.mkdirs();
				log.info("Created new main data directory at "+dataDir);
			}
			cfg.setDataDir(dataDir);
		}
	}

	public void setDescription(String description) {
		cfg.getMeta().setDescription(description);
	}

	public void setDescriptionImage(String descriptionImage) {
		cfg.setDescriptionImage(descriptionImage);
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
