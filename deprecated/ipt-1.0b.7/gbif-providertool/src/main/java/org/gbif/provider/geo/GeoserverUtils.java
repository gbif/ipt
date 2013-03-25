package org.gbif.provider.geo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.XmlFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class GeoserverUtils {
	private static final String FEATURE_TYPE_TEMPLATE = "/WEB-INF/geoserver/featureTypeInfo.ftl";
	private static final String SEED_TEMPLATE = "/WEB-INF/geoserver/seed.ftl";
	private static final String CATALOG_TEMPLATE = "/WEB-INF/geoserver/catalog.ftl";
	protected final Log log = LogFactory.getLog(GeoserverUtils.class);
	
	@Autowired
	private AppConfig cfg;
	@Autowired
	private Configuration freemarker;
	private DefaultHttpClient httpclient = new DefaultHttpClient();

	
	public String buildFeatureTypeDescriptor(OccurrenceResource resource){
		try {
			return FreeMarkerTemplateUtils.processTemplateIntoString(freemarker.getTemplate(FEATURE_TYPE_TEMPLATE), resource);
		} catch (IOException e) {
			log.error("Freemarker IO template error", e);
		} catch (TemplateException e) {
			log.error("Freemarker template exception", e);
		}
		return null;
	}
	
	public void removeFeatureType(OccurrenceResource resource) throws IOException{
		if (cfg.getGeoserverDataDirFile() == null || !cfg.getGeoserverDataDirFile().exists()){
			log.error("Cannot update geoserver configuration. Geoserver datadir not set correctly!");
			throw new IOException("Geoserver datadir configured wrongly");
		}
		File ftDir = new File(cfg.getGeoserverDataDirFile(), String.format("featureTypes/ipt_resource%s", resource.getId()));
		if (ftDir.exists()){
			FileUtils.deleteDirectory(ftDir);
			log.info("Removed geoserver feature type for resource "+resource.getId());
		}
		try {
			this.reloadCatalog();
		} catch (IOException e) {
			log.error("Cannot reload geoserver catalog. Geoserver not running or URL & login credentials not set correctly?");
			throw new IOException("Cannot reload geoserver catalog");
		}
		// remember new feature hashcode
		resource.setFeatureHash(0);			
	}
	
	public void updateFeatureType(OccurrenceResource resource) throws IOException{
		// create new featuretype description
		String featureTypeInfo = this.buildFeatureTypeDescriptor(resource);
		// commpare hashcode with previous one
		if (featureTypeInfo!=null && (resource.getFeatureHash() == null || !resource.getFeatureHash().equals(featureTypeInfo.hashCode()))){
			log.info("Updating geoserver feature type");
			if (cfg.getGeoserverDataDirFile() == null || !cfg.getGeoserverDataDirFile().exists()){
				log.error("Cannot update geoserver configuration. Geoserver datadir not set correctly!");
				throw new IOException("Geoserver datadir configured wrongly");
			}
			File fti = new File(cfg.getGeoserverDataDirFile(), String.format("featureTypes/ipt_resource%s/info.xml", resource.getId()));
			if (fti.exists()){
				fti.delete();				
			}else{
				FileUtils.forceMkdir(fti.getParentFile());
			}
			fti.createNewFile();
			Writer out = XmlFileUtils.startNewUtf8XmlFile(fti);
	        out.write(featureTypeInfo);
	        out.close();
			try {
				this.reloadCatalog();
			} catch (IOException e) {
				log.error("Cannot reload geoserver catalog. Geoserver not running or URL & login credentials not set correctly?");
				throw new IOException("Cannot reload geoserver catalog");
			}
			// remember new feature hashcode
			resource.setFeatureHash(featureTypeInfo.hashCode());				
		}
	}
	
	public void updateCatalog() throws IOException{
		if (cfg.getGeoserverDataDirFile() == null || !cfg.getGeoserverDataDirFile().exists()){
			log.error("Cannot update geoserver configuration. Geoserver datadir not set correctly!");
			throw new IOException("Geoserver datadir configured wrongly");
		}
		// create new catalog file
		try {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("cfg", cfg);
			String catalog = FreeMarkerTemplateUtils.processTemplateIntoString(freemarker.getTemplate(CATALOG_TEMPLATE), data);
			log.info("Updating geoserver catalog");
			File fti = new File(cfg.getGeoserverDataDirFile(), "catalog.xml");
			if (fti.exists()){
				fti.delete();				
			}
			fti.createNewFile();
			Writer out = XmlFileUtils.startNewUtf8XmlFile(fti);
	        out.write(catalog);
	        out.close();
			try {
				this.reloadCatalog();
			} catch (IOException e) {
				log.error("Cannot reload geoserver catalog. Geoserver not running or URL & login credentials not set correctly?");
				throw new IOException("Cannot reload geoserver catalog");
			}
		} catch (IOException e) {
			log.error("Freemarker IO template error", e);
		} catch (TemplateException e) {
			log.error("Freemarker template exception", e);
		}
	}
	
	public void updateGeowebcache(OccurrenceResource resource){
		// http://localhost:8081/geoserver/gwc/rest/seed/gbif:resource9
		// http://geoserver.org/display/GEOSDOC/5.+GWC+-+GeoWebCache
		try {
			String seedrequest = FreeMarkerTemplateUtils.processTemplateIntoString(freemarker.getTemplate(FEATURE_TYPE_TEMPLATE), resource);
	        httpclient.getCredentialsProvider().setCredentials(
	                new AuthScope("localhost", -1), 
	                new UsernamePasswordCredentials(cfg.getGeoserverUser(), cfg.getGeoserverPass()));
	        log.debug("reload geowebcache");
	        HttpPost httpost = new HttpPost(String.format("%s/gwc/rest/seed/%s.xml", cfg.getGeoserverUrl(),resource.getLayerName()));
	        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	        nvps.add(new BasicNameValuePair("reload_configuration", "1"));
	        HttpEntity body = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
	        httpost.setEntity(body);        
	        HttpResponse response = httpclient.execute(httpost);
	        if (failed(response)){
	        	log.warn("Failed to seed geowebcache for resource "+resource.getId());
	        }
	        consume(response);
		} catch (IOException e) {
			log.error("Freemarker IO template error", e);
		} catch (TemplateException e) {
			log.error("Freemarker template exception", e);
		}
	}
	
	public void reloadCatalog() throws IOException{
		String username = cfg.getGeoserverUser();
		String password = cfg.getGeoserverPass();
		String geoserverURL = cfg.getGeoserverUrl();
		
        httpclient.getCredentialsProvider().setCredentials(
                new AuthScope("localhost", -1), 
                new UsernamePasswordCredentials(username, password));

        // LOGIN
        HttpPost httpost = new HttpPost(geoserverURL+"/admin/loginSubmit.do");

        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("username", username));
        nvps.add(new BasicNameValuePair("password", password));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.ISO_8859_1));

        HttpResponse response = httpclient.execute(httpost);
        if (failed(response)){
        	log.warn("Failed to log into geoserver");
        }else{
            log.debug("login to geoserver succeeded");
        }
        consume(response);

        // RELOAD
        HttpGet httpget = new HttpGet(geoserverURL+"/admin/loadFromXML.do");
        response = httpclient.execute(httpget);
        if (failed(response)){
        	log.warn("Failed to reload catalog");
        }else{
            log.info("Reloaded geoserver catalog");
        }
        consume(response);

        // RELOAD GeoWebCache
        httpost = new HttpPost(geoserverURL+"/gwc/rest/reload");
        nvps.clear();
        nvps.add(new BasicNameValuePair("reload_configuration", "1"));
        HttpEntity body = new UrlEncodedFormEntity(nvps, HTTP.ISO_8859_1);
        httpost.setEntity(body);        
        response = httpclient.execute(httpost);
        consume(response);
        // do same call again. Sme weird bug in geowebcache requires to try this 2 times - also in the web forms!
        response = httpclient.execute(httpost);
        if (failed(response)){
        	log.warn("Failed to reload geowebcache");
        }else{
        	log.info("Reloaded geowebcache");
        }
        consume(response);

        // LOGOUT
        httpget = new HttpGet(geoserverURL+"/admin/logout.do");
        response = httpclient.execute(httpget);
        if (failed(response)){
        	log.warn("Failed to logout of geoserver");
        }else{
            log.debug("logged out");
        }
        consume(response);
    }
	
	
	private void consume(HttpResponse response){
		HttpEntity entity = response.getEntity();
        if (entity != null) {
        	try {
				entity.consumeContent();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	private boolean failed(HttpResponse response){
		if (response.getStatusLine().getStatusCode()==200){
			return false;
		}
		log.warn("Geoserver request failed: "+response.getStatusLine());
		return true;
	}
	
}
