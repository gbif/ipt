/**
 * 
 */
package org.gbif.ipt.action.portal;

import org.apache.commons.lang.StringUtils;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Action responsible for serving datadir resource files
 * 
 */
public class ResourceFileAction extends BaseAction {
	@Inject
	private DataDir dataDir;
	@Inject
	protected ResourceManager resourceManager;
	protected String r;
	protected Integer version;
	protected Resource resource;
	private InputStream inputStream;
	protected File data;
	protected String mimeType = "text/plain";
	protected String filename;

	public String eml(){
		// if no specific version is requested use the latest published
		if (version==null){
			version = resource.getEmlVersion();
		}
		data = dataDir.resourceEmlFile(resource.getShortname(), version);
		mimeType="text/xml";
		filename="eml-"+resource.getShortname()+"-v"+version+".xml";
		return execute();
	}

	public Resource getResource() {
		return resource;
	}

	public String getFilename() {
		return filename;
	}

	public String dwca(){
		if (resource==null){
			return NOT_FOUND;
		}
		// server file as set in prepare method
		data = dataDir.resourceDwcaFile(resource.getShortname());
		filename="dwca-"+resource.getShortname()+".zip";
		mimeType="application/zip";
		return execute();
	}

	@Override
	public void prepare() {
	    r = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
		if (r==null){
			// try session instead
			try {
				r = (String) session.get(Constants.SESSION_RESOURCE);
			} catch (Exception e) {
				// swallow. if session is not yet opened we get an exception here...
			}
		}
		resource = resourceManager.get(r);		
	}
	
	@Override
	public String execute() {
		// make sure we have a downlaod filename
		if (filename==null){
			filename=data.getName();
		}
		try {
			inputStream = new FileInputStream(data);
		} catch (FileNotFoundException e) {
			log.warn("Data dir file not found",e);
			return NOT_FOUND;
		}
		return SUCCESS;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public File getData() {
		return data;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getR() {
		return r;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}
