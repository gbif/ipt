package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResizeImage;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public abstract class MetadataBaseAction extends BaseMetadataResourceAction implements Preparable{
	@Autowired
	protected ResourceFactory resourceFactory;
	@Autowired
	protected EmlManager emlManager;
	protected Eml eml;
	protected List<Resource> resources;
	protected Map<String, String> occurrenceResourceTypes = new HashMap<String, String>();
	protected Map<String, String> checklistResourceTypes = new HashMap<String, String>();
	protected Map<String, String> otherResourceTypes = new HashMap<String, String>();
	private String resourceClassAlias;
	// file/logo upload
	protected File file;
	protected String fileContentType;
	protected String fileFileName;
	private final Map<String, String> jdbcDriverClasses = new HashMap<String, String>()   
    {  
        {  
            put("com.mysql.jdbc.Driver", "MySQL");
            put("org.postgresql.Driver", "Postgres");
            put("org.h2.Driver", "H2");
            put("net.sourceforge.jtds.jdbc.Driver", "MS SQL Server");  
            put("oracle.jdbc.OracleDriver", "Oracle");  
            put("org.hsqldb.jdbcDriver", "HSQL");  
            put("org.apache.derby.jdbc.ClientDriver", "Derby");  
        }  
    };  	
    
    
	public void prepare() {
		super.prepare();
		if (resource == null) {
			// create new empty resource
			if (resourceClassAlias.equalsIgnoreCase(OccurrenceResource.ALIAS)){
				resource = resourceFactory.newOccurrenceResourceInstance();				
			}else if (resourceClassAlias.equalsIgnoreCase(ChecklistResource.ALIAS)){
				resource = resourceFactory.newChecklistResourceInstance();				
			}else{
				resource = resourceFactory.newMetadataResourceInstance();				
			}
		}
		eml = emlManager.load(resource);
	}
		
	public String execute(){
		return SUCCESS;
	}

	public String list(){
		resources = resourceManager.getResourcesByUser(getCurrentUser().getId());
		return SUCCESS;
	}
	
	public String save(){
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
		File logoFile = cfg.getResourceLogoFile(resource.getId());
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
	
	
	public List<Resource> getResources() {
		return resources;
	}

	public Eml getEml() {
		return eml;
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


	public Map<String, String> getResourceTypes(){
		
	}

	public String getResourceClassAlias() {
		return resourceClassAlias;
	}
	public void setResourceClassAlias(String resourceClassAlias) {
		this.resourceClassAlias = resourceClassAlias;
	}

	
	public void setChecklistResourceTypes(Map<String, String> checklistResourceTypes) {
		this.checklistResourceTypes = translateI18nMap(checklistResourceTypes);
	}
	public void setOccurrenceResourceTypes(Map<String, String> occurrenceResourceTypes) {
		this.occurrenceResourceTypes = translateI18nMap(occurrenceResourceTypes);
	}
	public void setOtherResourceTypes(Map<String, String> otherResourceTypes) {
		this.otherResourceTypes = translateI18nMap(otherResourceTypes);
	}
	
	
	public Map<String, String> getJdbcDriverClasses() {
		return jdbcDriverClasses;
	}
}
