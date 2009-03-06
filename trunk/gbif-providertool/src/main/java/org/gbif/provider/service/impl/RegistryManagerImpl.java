package org.gbif.provider.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
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

	public boolean registerOrg() {
		// need to register a new organisation?
		if (StringUtils.trimToNull(cfg.getOrg().getUddiID())!=null){
			log.warn("The organisation is already registered");
			return false;
		}
    	PostMethod method = new PostMethod();
		method.releaseConnection();
		return true;
	}

	public boolean registerIPT() {
		// need to register a new organisation?
		if (StringUtils.trimToNull(cfg.getIpt().getUddiID())==null){
			log.warn("This IPT is already registered");
			return false;
		}
		log.warn("IPT service registration not implemented");
    	PostMethod method = new PostMethod();
		method.releaseConnection();
		return false;
	}

	public boolean registerResource(Long resourceId) {
    	PostMethod method = new PostMethod();
		method.releaseConnection();
		return false;
	}
}
