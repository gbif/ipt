package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResizeImage;
import org.gbif.provider.webapp.action.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public abstract class BaseResourceManagerAction<T extends Resource> extends BaseAction implements Preparable, SessionAware{
	protected Map session;
	@Autowired
	protected ResourceFactory resourceFactory;
    protected GenericResourceManager<T> resourceManager;
	@Autowired
	protected EmlManager emlManager;
	protected Long resource_id;
	protected T resource;
	protected List<T> resources;
	protected Eml eml;
	// file upload
	protected File file;
	protected String fileContentType;
	protected String fileFileName;

    protected abstract T newResource();
    
	public void prepare() {
		if (resource_id == null) {
			resource = newResource();
		} else {
			resource = resourceManager.get(resource_id);
			// update recently viewed resources in session
			updateRecentResouces();
		}
		eml = emlManager.load(resource);
	}
		
	public String execute(){
		assert(resource!=null);
		return SUCCESS;
	}

	public String list(){
		resources = resourceManager.getTop(10);
		return SUCCESS;
	}
	
	public String save(){
		assert(resource!=null);
		if (cancel != null) {
			return "cancel";
		}
		if (delete != null) {
			return delete();
		}

		boolean isNew = (resource.getId() == null);
		resource = resourceManager.save(resource);
		String key = (isNew) ? "resource.added" : "resource.updated";
		saveMessage(getText(key));
		if (isNew){
			updateRecentResouces();
		}
		// logo
		if (uploadLogo()){
			saveMessage(getText("resource.logoUploaded"));
		}
		return SUCCESS;
	}

	public String delete() {
		assert(resource!=null);
		resourceManager.remove(resource);
		saveMessage(getText("resource.deleted"));

		// update recently viewed resources in session
		Object previousQueue = session.get(Constants.RECENT_RESOURCES);
		if (previousQueue != null && previousQueue instanceof Queue){
			Queue<LabelValue> queue = (Queue) previousQueue;
			LabelValue res = new LabelValue(resource.getTitle(), resource_id.toString());
			// remove entry from queue if it existed before
			queue.remove(res);
			// save back to session
			session.put(Constants.RECENT_RESOURCES, queue);
		}		
		return "delete";
	}

	public String importEml() {
		assert(resource!=null);
        if ("".equals(fileFileName) || file == null) {
        	return ERROR;
        }
        // final eml destination
		saveMessage(getText("Sorry, reading EML is not yet implemented properly"));
    	return ERROR;

//    	File emlFile = cfg.getEmlFile(resource.getId());
//        try {
//        	uploadData(emlFile);
//    		// try to parse the EML
//		} catch (IOException e) {
//			log.error("Couldnt upload EML file",e);
//			saveMessage(getText("Error uploading file"));
//        	return ERROR;
//		}
//		
//		log.info(String.format("EML %s uploaded and parsed for resource %s", emlFile.getAbsolutePath(), resource_id));
//		return SUCCESS;
	}
	
	private boolean uploadLogo() {
        if ("".equals(fileFileName) || file == null) {
        	return false;
        }
        // final logo destination
		File logoFile = cfg.getResourceLogoFile(resource_id);
        try {
        	uploadData(logoFile);
    		// do sth with the file
			ResizeImage.resizeImage(file, logoFile, Constants.LOGO_SIZE, Constants.LOGO_SIZE);
		} catch (IOException e) {
			log.error("Couldnt upload or resize logo",e);
			saveMessage(getText("Couldnt upload or resize logo"));
        	return false;
		}

		log.info(String.format("Logo %s uploaded and resized for resource %s", logoFile.getAbsolutePath(), resource_id));
		return true;
	}

	private void uploadData(File targetFile) throws IOException{
        //retrieve the file data
        InputStream stream = new FileInputStream(file);

        //write the file to the file specified
        OutputStream bos = new FileOutputStream(targetFile);
        int bytesRead;
        byte[] buffer = new byte[8192];

        while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }

        bos.close();
        stream.close();
	}
	
	private void updateRecentResouces(){
		LabelValue res = new LabelValue(resource.getTitle(), resource.getId().toString());
		Queue<LabelValue> queue; 
		Object rr = session.get(Constants.RECENT_RESOURCES);
		if (rr != null && rr instanceof Queue){
			queue = (Queue) rr;
		}else{
			queue = new ConcurrentLinkedQueue<LabelValue>(); 
		}
		// remove old entry from queue if it existed before and insert at tail again
		queue.remove(res);
		queue.add(res);
		if (queue.size()>10){
			// only remember last 10 resources
			queue.remove();
		}
		// save back to session
		log.debug("Recently viewed resources: "+queue.toString());
		session.put(Constants.RECENT_RESOURCES, queue);
	}

	
	public List<T> getResources() {
		return resources;
	}

	public Eml getEml() {
		return eml;
	}

	public void setSession(Map session) {
		this.session = session;
	}


    public void setFile(File file) {
        this.file = file;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

    public File getFile() {
        return file;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public String getFileFileName() {
        return fileFileName;
    }
	public void setResource_id(Long resource_id) {
		this.resource_id = resource_id;
	}

	public Long getResource_id() {
		return resource_id;
	}
	
	public T getResource() {
		return resource;
	}

	public void setResource(T resource) {
		this.resource = resource;
	}
    
}
