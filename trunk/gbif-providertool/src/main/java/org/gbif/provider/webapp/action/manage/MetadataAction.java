package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResizeImage;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class MetadataAction extends BaseMetadataResourceAction implements Preparable{
	@Autowired
	@Qualifier("checklistResourceManager")
	private GenericResourceManager<ChecklistResource> checklistResourceManager;
	@Autowired
	private OccResourceManager occResourceManager;
	@Autowired
	protected ResourceFactory resourceFactory;
	@Autowired
	private CacheManager cacheManager;
	protected List<?> resources;
	protected Map<String, String> occurrenceResourceTypes = new HashMap<String, String>();
	protected Map<String, String> checklistResourceTypes = new HashMap<String, String>();
	protected Map<String, String> otherResourceTypes = new HashMap<String, String>();
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
		if (resource == null && resourceType!=null) {
			// create new empty resource
			if (resourceType.equalsIgnoreCase(OCCURRENCE)){
				resource = resourceFactory.newOccurrenceResourceInstance();				
			}else if (resourceType.equalsIgnoreCase(CHECKLIST)){
				resource = resourceFactory.newChecklistResourceInstance();				
			}else{
				resource = resourceFactory.newMetadataResourceInstance();				
			}
		}
	}
		
	public String execute(){
		return SUCCESS;
	}

	public String list(){
		if (resourceType!=null && resourceType.equalsIgnoreCase(ExtensionType.Occurrence.alias)){
			resources = occResourceManager.getResourcesByUser(getCurrentUser().getId());
		}else if (resourceType!=null && resourceType.equalsIgnoreCase(ExtensionType.Checklist.alias)){
			resources = checklistResourceManager.getResourcesByUser(getCurrentUser().getId());
		}else{
			resources = resourceManager.getResourcesByUser(getCurrentUser().getId());
		}
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
		return resourceType;
	}

	public String publish() {
		resourceManager.publish(resource_id);
		return SUCCESS;
	}
	
	public String delete() {
		if (resource != null && resourceType!=null) {
			// remove resource with appropiate manager
			if (resourceType.equalsIgnoreCase(OCCURRENCE)){
				occResourceManager.remove((OccurrenceResource)resource);
			}else if (resourceType.equalsIgnoreCase(CHECKLIST)){
				checklistResourceManager.remove((ChecklistResource)resource);
			}else{
				resourceManager.remove(resource);
			}
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
		}else{
			saveMessage("Can't identify resource to be deleted");			
		}

		return "delete";
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
	
	
	public List<?> getResources() {
		return resources;
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
		if (resource != null){
			if (resource instanceof OccurrenceResource){
				return occurrenceResourceTypes;
			}else if (resource instanceof ChecklistResource){
				return checklistResourceTypes;
			}
		}
		// if not those 2, return any resource type
		Map<String, String> allTypes = new HashMap<String, String>();
		allTypes.putAll(checklistResourceTypes);
		allTypes.putAll(occurrenceResourceTypes);
		allTypes.putAll(otherResourceTypes);
		return allTypes;
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
