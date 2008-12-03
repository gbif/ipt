package org.gbif.provider.geo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class GeoserverUtils {
	protected final Log log = LogFactory.getLog(GeoserverUtils.class);
	@Autowired
	private AppConfig cfg;
	@Autowired
	private Configuration freemarker;
	
	public String buildFeatureTypeDescriptor(OccurrenceResource resource){
		try {
			System.out.println(freemarker.getSettings());
			return FreeMarkerTemplateUtils.processTemplateIntoString(freemarker.getTemplate("featureTypeInfo.ftl"), resource);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateFeatureType(OccurrenceResource resource) throws IOException{
		// create new featuretype description
		String featureTypeInfo = this.buildFeatureTypeDescriptor(resource);
		// commpare hashcode with previous one
		if (featureTypeInfo!=null && (resource.getFeatureHash() == null || !resource.getFeatureHash().equals(featureTypeInfo.hashCode()))){
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
			FileWriter fstream = new FileWriter(fti);
	        BufferedWriter out = new BufferedWriter(fstream);
	        out.write(featureTypeInfo);
	        out.close();
	        fstream.close();
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
	
	public void reloadCatalog() throws IOException{
		String geoserverBaseUrl = cfg.getGeoserverUrl();
		String username = cfg.getGeoserverUser();
		String password = cfg.getGeoserverPass();
		// do login
			if (geoserverBaseUrl==null){
				geoserverBaseUrl="http://localhost:8080/geoserver";
			}
			if (username==null){
				username="sa";
			}
			if (password==null){
				password="";
			}
	       URL url=new URL(String.format("%s/admin/loginSubmit.do?username=%s&password=%s&submit=Submit", geoserverBaseUrl, username, password));
	       URLConnection conn = url.openConnection();
	       InputStream inStream = conn.getInputStream();
	       String responseString=new String();
	       BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
	       while(in.ready())
	       {   responseString+= in.readLine();  }
	       //System.out.println("------------------------------------------------------\nresponseString:"+responseString);
	       log.debug("Logged into Geoserver as admin");
	       
	        // reload
	       String cookie=conn.getHeaderField("Set-Cookie");
	       System.out.println("cookie-text:"+cookie);
	       cookie = cookie.substring(0, cookie.indexOf(";"));
	       String cookieName = cookie.substring(0, cookie.indexOf("="));
	       String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
	       String cookieString=cookieName+"="+cookieValue;
	       URL url2=new URL(String.format("%s/admin/loadFromXML.do",geoserverBaseUrl));
	       URLConnection conn2 = url2.openConnection();
	       conn2.setRequestProperty("Cookie",cookieString);                 // set the Cookie for request
	       conn2.connect();
	       inStream = conn2.getInputStream();
	       in = new BufferedReader(new InputStreamReader(inStream));
	       responseString=new String();
	       while(in.ready())
	       {   responseString+= in.readLine();  }
	       //System.out.println("------------------------------------------------------\nresponseString:"+responseString);
	       log.info("Reloaded Geoserver config");

	        //logout
	       URL url3=new URL(String.format("%s/admin/logout.do", geoserverBaseUrl));
	       URLConnection conn3 = url3.openConnection();
	       conn3.setRequestProperty("Cookie",cookieString);
	       conn3.connect();
	       inStream = conn3.getInputStream();
	       in = new BufferedReader(new InputStreamReader(inStream));
	       responseString=new String();
	       while(in.ready())
	       {   responseString+= in.readLine();  }
	       //System.out.println("------------------------------------------------------\nresponseString:"+responseString);		
	       log.debug("Logged out of Geoserver");
	}
}
