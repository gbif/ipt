package org.gbif.provider.geoserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class GeoserverUtils {
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
	
	public void reloadCatalog(String geoserverBaseUrl, String username, String password) throws IOException{
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
	       System.out.println("------------------------------------------------------\nresponseString:"+responseString);

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
	       System.out.println("------------------------------------------------------\nresponseString:"+responseString);

	        //logout
	       URL url3=new URL(String.format("%s/geoserver/admin/logout.do", geoserverBaseUrl));
	       URLConnection conn3 = url3.openConnection();
	       conn3.setRequestProperty("Cookie",cookieString);
	       conn3.connect();
	        inStream = conn3.getInputStream();
	       in = new BufferedReader(new InputStreamReader(inStream));
	       responseString=new String();
	       while(in.ready())
	       {   responseString+= in.readLine();  }
	      System.out.println("------------------------------------------------------\nresponseString:"+responseString);		
	}
}
