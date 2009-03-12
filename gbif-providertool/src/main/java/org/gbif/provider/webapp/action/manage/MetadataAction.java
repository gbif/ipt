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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.model.voc.ResourceType;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResizeImage;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class MetadataAction extends BaseMetadataResourceAction implements Preparable, ServletRequestAware{
	@Autowired
	protected ResourceFactory resourceFactory;
	protected List<? extends Resource> resources;
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
            put("sun.jdbc.odbc.JdbcOdbcDriver", "Generic ODBC");
        }  
    };
	private Map<String, String> resourceTypeMap = translateI18nMap(new HashMap<String, String>(ResourceType.htmlSelectMap));
	protected HttpServletRequest request;
    
    
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
		resource=null;
		resources = getResourceTypeMatchingManager().getResourcesByUser(getCurrentUser().getId());
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
		resource.setDirty();
		resource = resourceManager.save(resource);								
//		if (resourceType.equalsIgnoreCase(OCCURRENCE)){
//			resource = occResourceManager.save((OccurrenceResource)resource);				
//		}else if (resourceType.equalsIgnoreCase(CHECKLIST)){
//			resource = checklistResourceManager.save((ChecklistResource)resource);				
//		}else{
//			resource = metaResourceManager.save(resource);								
//		}
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
		// publish only whne POSTed, not with ordinary GET
		if (request.getMethod().equalsIgnoreCase("post")){
			Resource res = getResourceTypeMatchingManager().publish(resource_id);
		}
		return SUCCESS;
	}
	
	public String publishAll() {
		list();
		for (Resource res : resources){
			if (res.isDirty()){
				getResourceTypeMatchingManager().publish(res.getId());
				saveMessage("Published "+res.getTitle());
			}
		}
		return SUCCESS;
	}
	public String republish() {
		list();
		int i=0;
		for (Resource res : resources){
			if (res.getStatus().equals(PublicationStatus.dirty)){
				i++;
				getResourceTypeMatchingManager().publish(res.getId());
			}
		}
		saveMessage("Republished "+i+" modified resources");
		return SUCCESS;
	}
	
	public String delete() {
		if (resource != null && resourceType!=null) {
			// remove resource with appropiate manager
			getResourceTypeMatchingManager().remove(resource.getId());
			log.debug("Resource deleted");
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

	public Map<String, String> getJdbcDriverClasses() {
		return jdbcDriverClasses;
	}

	public Map<String, String> getResourceTypeMap() {
		return resourceTypeMap;
	}
	public void setServletRequest(HttpServletRequest request) {
		this.request=request;
	}
}
