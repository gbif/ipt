package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.webapp.action.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;


public class SourceFileUploadAction extends BaseAction{
	private static final long serialVersionUID = -3698917712584074200L;
	@Autowired
	private DatasourceInspectionManager datasourceInspectionManager;
    private GenericManager<ViewMappingBase> viewMappingManager;
    private File file;
    private String fileContentType;
    private String fileFileName;
	private Long mapping_id;
	/**
     * Upload the file
     * @return String with result (cancel, input or sucess)
     * @throws Exception if something goes wrong
	 */
    public String upload() throws Exception {
        if (this.cancel != null) {
            return "cancel";
        }
        if (mapping_id == null){
        	log.error("mapping_id required for file upload");
        	return ERROR;
        }
        // check where it who wants this file and where to save
		ViewMappingBase mapping = viewMappingManager.get(mapping_id);
		DatasourceBasedResource resource = mapping.getResource();
        // the directory to upload to
		File targetFile = cfg.getSourceFile(resource.getId(), mapping.getExtension());
		log.debug(String.format("Uploading source file for resource %s to file %s",resource.getId(), targetFile.getAbsolutePath()));
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

        // place the data into the request for retrieval on next page
        getRequest().setAttribute("location", targetFile.getAbsolutePath());
        
        // process file
		mapping.setSourceFile(targetFile);
		List<String> headers = datasourceInspectionManager.getHeader(mapping);
		log.info(String.format("Tab file %s uploaded with %s columns", targetFile.getAbsolutePath(), headers .size()));
		if (headers.size() > 1){
			// save file in view mapping
	        viewMappingManager.save(mapping);
	        saveMessage(getText("mapping.sourceFileUploaded", String.valueOf(headers.size())));
		}else{
			mapping.setSourceFile(null);
	        viewMappingManager.save(mapping);
	        saveMessage(getText("mapping.sourceFileBroken", String.valueOf(headers.size())));
		}
		return SUCCESS;
    }

    /**
     * Default method - returns "input"
     * @return "input"
     */
    public String execute() {
        return INPUT;
    }

    @Override
    public void validate() {
        if (getRequest().getMethod().equalsIgnoreCase("post")) {
            getFieldErrors().clear();
            if ("".equals(fileFileName) || file == null) {
                super.addFieldError("file", getText("errors.requiredField", new String[] {getText("uploadForm.file")}));
            } else if (file.length() > 2097152) {
                addActionError(getText("maxLengthExceeded"));
            }
        }
    }
    
    
    
    

	
	
	

	public Long getMapping_id() {
		return mapping_id;
	}


	public void setMapping_id(Long mapping_id) {
		this.mapping_id = mapping_id;
	}

	public void setViewMappingManager(
			GenericManager<ViewMappingBase> viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}

}
