package org.gbif.provider.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class RegistryManagerImpl extends HttpBaseManager implements RegistryManager{
	public static final String REGISTRY_ORG_URL = "http://gbrds.gbif.org/registry/organization";
	public static final String REGISTRY_SERVICE_URL = "http://gbrds.gbif.org/registry/service";

	public List<URI> listExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean registerIPT() {
		log.warn("IPT service registration not implemented");
		return false;
	}

	public boolean registerResource(Long resourceId) {
		// TODO Auto-generated method stub
		return false;
	}

	public String findOrganisationsAsJSON(String q) {
        HttpGet httpget = new HttpGet(REGISTRY_ORG_URL+".json?q="+q);
        String json = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
			json = httpclient.execute(httpget, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}

	public Map<String, ResourceMetadata> findOrganisations(String q) {
		// TODO Auto-generated method stub
		return null;
	}

}
