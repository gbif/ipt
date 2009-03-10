package org.gbif.provider.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

import freemarker.template.Configuration;

public class HttpBaseManager {
	protected static HttpClient client =  new HttpClient(new MultiThreadedHttpConnectionManager());
	protected final Log log = LogFactory.getLog(HttpBaseManager.class);
	@Autowired
	protected AppConfig cfg;
	@Autowired
	protected Configuration freemarker;

	protected void setCredentials(AuthScope scope, String username, String password){
		client.getState().setCredentials(
	            scope,
	            new UsernamePasswordCredentials(StringUtils.trimToEmpty(username), StringUtils.trimToEmpty(password))
	        );		
	}
	
	protected String executeGet(String uri, boolean authenticate){
		NameValuePair[] params = new NameValuePair[0];
		return executeGet(uri, params, authenticate);
	}
	protected String executeGet(String uri, NameValuePair[] params, boolean authenticate){
		log.debug("Getting "+uri);
		String result = null;
		GetMethod method = newHttpGet(uri, authenticate);
		method.setQueryString(params);
		try {
	        client.executeMethod(method);
	        if(succeeded(method)){
	        	result = method.getResponseBodyAsString();
	        }
		} catch (Exception e) {
			log.warn(uri+": "+ e.toString());
		} finally{
			if (method!=null){
				method.releaseConnection();
			}
		}
		return result;
	}
	protected String executePost(String uri, String content, String contentType, boolean authenticate){
		log.debug("Posting to "+uri);
		String result = null;
		PostMethod method = newHttpPost(uri, authenticate);
        RequestEntity body=null;
		try {
			body = new StringRequestEntity(content, contentType, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		method.setRequestEntity(body);
		try {
	        client.executeMethod(method);
	        if(succeeded(method)){
	        	result = method.getResponseBodyAsString();
	        }
		} catch (Exception e) {
			log.warn(uri+": "+ e.toString());
		} finally{
			if (method!=null){
				method.releaseConnection();
			}
		}
		return result;
	}
	protected String executePost(String uri, NameValuePair[] params, boolean authenticate){
		String result = null;
		log.debug("Posting to "+uri);
		PostMethod method = newHttpPost(uri, authenticate);
		method.setRequestBody(params);
		try {
	        client.executeMethod(method);
	        if(succeeded(method)){	        	
	        	result = method.getResponseBodyAsString();
	        }
		} catch (Exception e) {
			log.warn(uri+": "+ e.toString());
		} finally{
			if (method!=null){
				method.releaseConnection();
			}
		}
		return result;
	}
	private GetMethod newHttpGet(String url, boolean authenticate){
		GetMethod method = new GetMethod(url);
        method.setFollowRedirects(true);
		method.setDoAuthentication(authenticate);
        return method;
	}
	
	private PostMethod newHttpPost(String url, boolean authenticate){
		PostMethod method = new PostMethod(url);
        //method.setFollowRedirects(true);
		method.setDoAuthentication(authenticate);
        return method;
	}

	protected InputStream getStream(String source){
		return new ByteArrayInputStream(source.getBytes());
	}
	
	protected boolean succeeded(HttpMethodBase method) {
		
		if (method.getStatusCode()==200){
			return true;
		}
		try {
			log.warn("Http request to "+ method.getURI() +" failed: "+method.getStatusLine());
		} catch (URIException e) {
			log.warn("Http request to ??? failed: "+method.getStatusLine());
		}
		log.info("REQUEST:\n"+method.getRequestHeaders()+"\n----------\nRESPONSE\n"+method.getResponseHeaders());
		return false;
	}

}
