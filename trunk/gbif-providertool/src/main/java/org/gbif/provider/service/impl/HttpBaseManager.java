package org.gbif.provider.service.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

import freemarker.template.Configuration;

public class HttpBaseManager {

	protected final Log log = LogFactory.getLog(HttpBaseManager.class);
	@Autowired
	protected AppConfig cfg;
	@Autowired
	protected Configuration freemarker;
	protected DefaultHttpClient httpclient = new DefaultHttpClient();

	protected void consume(HttpResponse response) {
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

	protected boolean failed(HttpResponse response) {
		if (response.getStatusLine().getStatusCode()==200){
			return false;
		}
		log.warn("Http request failed: "+response.getStatusLine());
		log.debug(response.getAllHeaders());
		return true;
	}

}
