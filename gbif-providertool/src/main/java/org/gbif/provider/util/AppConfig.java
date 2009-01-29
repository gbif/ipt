package org.gbif.provider.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.Transient;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.util.ServletContextAware;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.ProviderCfg;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.service.ProviderCfgManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResourceLoader;

public class AppConfig{
	protected final Log log = LogFactory.getLog(AppConfig.class);
	@Autowired
	private IptNamingStrategy namingStrategy;
	private ProviderCfgManager providerCfgManager;
	private ProviderCfg cfg;
	// need some static fields to create static methods that can be used outside of spring managed contexts, e.g. for hibernate objects
	// fields are managed by regular instance setters thanks to singleton
	private static String baseURL;	
	private static String dataDIR;	
	private static File webappDIR;	

	private AppConfig(ProviderCfgManager providerCfgManager, String webappDir, String dataDir) {
		super();
		AppConfig.dataDIR = dataDir; // new File(dataDir).getAbsolutePath();
		log.info("IPT_DATA_DIR: "+dataDIR);
		
		AppConfig.webappDIR = new File(webappDir);
		log.info("IPT_WEBAPP_DIR: "+webappDIR.getAbsolutePath());

		this.providerCfgManager = providerCfgManager;
		cfg=providerCfgManager.load();
		setBaseUrl(cfg.getBaseUrl());
		log.info("IPT_BASE_URL: "+baseURL);
		log.info("GEOSERVER_URL: "+cfg.getGeoserverUrl());
		log.info("GEOSERVER_DATA_DIR: "+cfg.getGeoserverDataDir());
	}

	
	
	// OTHER UTILITY METHODS, MOSTLY DEFINING PATHS & URLs
	
	// ALL ESSENTIAL DATA DIR
	public String getDataDir() {
		return dataDIR;
	}

	// WEBAPP BASICS
	public static File getWebappDir() {
		return webappDIR;
	}
	public static File getWebappFile(String relPath){
		return new File(webappDIR,relPath);
	}
	public URL getWebappURL(String relPath) {
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
	public String getAtomFeedURL() {
		return getAtomFeedURL(1);
	}
	public String getAtomFeedURL(Integer page) {
		return String.format("%s/data/atom.xml?page=%s", baseURL, page);
	}

	
	// RESOURCE BASICS
	public String getResourceUrl(String guid) {
		return String.format("%s/resource/%s/", baseURL, guid);
	}

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
	public URL getResourceCacheUrl(Long resourceId, String relPath) {
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
	public static File getResourceSourceFile(Long resourceId, String filename) {
		return getResourceDataFile(resourceId, String.format("sources/%s",filename));
	}
	public String getResourceDataUrl(Long resourceId) {
		return String.format("%s/data/%s", baseURL, resourceId);
	}	

	public static File getResourceLogoFile(Long resourceId) {
		File file = new File(getResourceDataDir(resourceId), "logo.jpg");
		return file;    	
	}

	public String getResourceLogoUrl(Long resourceId) {
		return String.format("%s/logo.jpg", getResourceDataUrl(resourceId));
	}

	public static File getMetadataFile(Long resourceId) {
		return getResourceDataFile(resourceId, "metadata/metadata.xml");
	}

	public static File getEmlFile(Long resourceId, int version) {
		String ver = "";
		if (version>0){
			ver = "-"+version;
		}
		File eml = new File(getResourceDataDir(resourceId), String.format("metadata/eml%s.xml",ver));
		return eml;    	
	}
	public static File getEmlFile(Long resourceId) {
		return getEmlFile(resourceId, 0);    	
	}

	public String getEmlUrl(String guid) {
		return String.format("%s/data/%s/eml.xml", baseURL, guid);
	}

	// CORE RECORDS
    public String getDetailUrl(CoreRecord core){
    	return getDetailUrl(core, "html");
    }
    public String getDetailUrl(CoreRecord core, String format){
    	if (core.getResource()==null){
    		throw new IllegalArgumentException("Core records needs a resource");
    	}
    	return String.format("%s/%s/detail.%s", getResourceDataUrl(core.getResource().getId()), core.getGuid(), format);
    }

    // SOURCE/UPLOAD FILES
    public File getSourceFile(Long resourceId, String fileName){
    	File f = new File(getResourceDataDir(resourceId), String.format("sources/%s", fileName));
	    if (!f.getParentFile().exists()) {
	        f.getParentFile().mkdirs();
	    }
		return f;
	}    

    // DUMP FILES
    public File getDumpFile(Long resourceId, Extension extension) throws IOException{    	
		File file = new File(getResourceDataDir(resourceId), String.format("data-%s.txt", namingStrategy.extensionTableName(extension)));
		return file;
	}    

    public File getDumpArchiveFile(Long resourceId){
		File file = new File(getResourceDataDir(resourceId), "data.zip");
		return file;    	
    }

    public String getDumpArchiveUrl(Long resourceId){
		return String.format("%s/data.zip", getResourceDataUrl(resourceId));
    }

    // TCS ARCHIVE
    public File getTcsArchiveFile(Long resourceId){
		File file = new File(getResourceDataDir(resourceId), "tcsArchive.zip");
		return file;    	
    }
	public String getTcsArchiveUrl(Long resourceId){
		return String.format("%s/tcsArchive.zip", getResourceDataUrl(resourceId));
	}
    
    // SERVICE ENDPOINTS
	public String getTapirEndpoint(Long resourceId){
		String base = getBaseUrl();
    	return String.format("%s/tapir/%s/", base, resourceId.toString());
	}
	public String getWfsEndpoint(Long resourceId){
    	return String.format("%s/wfs?request=DescribeFeatureType&typeName=gbif:resource%s", getGeoserverUrl(), resourceId);
	}
	public String getWmsEndpoint(Long resourceId){
    	return String.format("%s/wms?request=GetMap&bbox=-180,-90,180,90&layers=gbif:countries,gbif:resource%s&width=320&height=160&bgcolor=0x7391AD&Format=image/jpeg", getGeoserverUrl(), resourceId);
	}
		
	
	
	// CFG DELEGATE METHODS
	public Long getId() {
		return cfg.getId();
	}
	public String getUddiID() {
		return cfg.getUddiID();
	}
	public String getUddiSharedKey() {
		return cfg.getUddiSharedKey();
	}
	public String getBaseUrl() {
		return cfg.getBaseUrl();
	}
	public String getContactEmail() {
		return cfg.getMeta().getContactEmail();
	}
	public String getContactName() {
		return cfg.getMeta().getContactName();
	}
	public String getDescription() {
		return cfg.getMeta().getDescription();
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
	public Point getLocation() {
		return cfg.getMeta().getLocation();
	}
	public String getGeoserverDataDir() {
		return cfg.getGeoserverDataDir();
	}
	public String getGoogleMapsApiKey() {
		return cfg.getGoogleMapsApiKey();
	}
	public File getGeoserverDataDirFile() {
		if (StringUtils.trimToNull(cfg.getGeoserverDataDir())!=null){
			return new File(cfg.getGeoserverDataDir());
		}
		return null;
	}
	public String getGeoserverPass() {
		return cfg.getGeoserverPass();
	}
	public String getGeoserverUser() {
		return cfg.getGeoserverUser();
	}

	
	

	public void setBaseUrl(String baseUrl) {
		cfg.setBaseUrl(trimUrl(baseUrl));
		baseURL=getBaseUrl();
	}
	public void setUddiID(String uddiID) {
		cfg.setUddiID(uddiID);
	}
	public void setUddiSharedKey(String uddiSharedKey) {
		cfg.setUddiSharedKey(uddiSharedKey);
	}
	public void setContactEmail(String contactEmail) {
		cfg.getMeta().setContactEmail(contactEmail);
	}
	public void setContactName(String contactName) {
		cfg.getMeta().setContactName(contactName);
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
	public void setLocation(Point location) {
		cfg.getMeta().setLocation(location);
	}
	public void setGoogleMapsApiKey(String googleMapsApiKey) {
		cfg.setGoogleMapsApiKey(googleMapsApiKey);
	}
	public void setGeoserverDataDir(String geoserverDataDir) {
		cfg.setGeoserverDataDir(geoserverDataDir);
	}
	public void setGeoserverPass(String geoserverPass) {
		cfg.setGeoserverPass(geoserverPass);
	}
	public void setGeoserverUser(String geoserverUser) {
		cfg.setGeoserverUser(geoserverUser);
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
