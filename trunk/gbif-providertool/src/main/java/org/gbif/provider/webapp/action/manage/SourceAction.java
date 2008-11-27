package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.SourceSql;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.GenericResourceRelatedManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;


public class SourceAction extends BaseResourceAction implements Preparable{
	private static final long serialVersionUID = -3698917712584074200L;
	@Autowired
	private SourceInspectionManager sourceInspectionManager;
	@Autowired
	@Qualifier("sourceManager")
    private GenericResourceRelatedManager<SourceBase> sourceManager;
    private List<SourceFile> fileSources = new ArrayList<SourceFile>();
    private List<SourceSql> sqlSources = new ArrayList<SourceSql>();
    private SourceSql source;
	private DataResource dataResource;
    // file upload
    private File file;
    private String fileContentType;
    private String fileFileName;
	private Long sid;

    

    @Override
	public void prepare() {
		super.prepare();
		dataResource = (DataResource) resource;
		if (sid != null) {
			SourceBase s = sourceManager.get(sid);
			if (s instanceof SourceSql){
				source = (SourceSql) s;
			}
		}else{
			source = new SourceSql();
		}
	}

	/**
     * Default method - returns "input"
     * @return "input"
     */
    public String execute() {
        return SUCCESS;
    }

    public String list() {
    	List<SourceBase> sources = sourceManager.getAll(resource_id);
    	for (SourceBase s : sources){
    		if (s instanceof SourceFile){
    			fileSources.add((SourceFile) s);
    		}else{
    			sqlSources.add((SourceSql) s);
    		}
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
		source.setResource(dataResource);
    	sourceManager.save(source);
    	return SUCCESS;
    }
    
    
    public String delete() {
    	if (sid != null){
        	sourceManager.remove(sid);
    	}
		return "delete";
	}

	/**
     * Upload a source file
     * @return String with result (cancel, input or sucess)
     * @throws Exception if something goes wrong
	 */
    public String upload() throws Exception {
        // the file to upload to
		File targetFile = cfg.getSourceFile(dataResource.getId(), fileFileName);
		log.debug(String.format("Uploading source file for resource %s to file %s",dataResource.getId(), targetFile.getAbsolutePath()));
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

        // process file
    	// FIXME: select existing source with that filename if already exists.
		SourceFile fsource = new SourceFile();
		fsource.setFilename(fileFileName);
		fsource.setResource(dataResource);

		List<String> headers = sourceInspectionManager.getHeader(fsource);
		log.info(String.format("Tab file %s uploaded with %s columns", targetFile.getAbsolutePath(), headers .size()));
		if (headers.size() > 1){
			// save file in view mapping
			sourceManager.save(fsource);
	        saveMessage(getText("view.sourceFileUploaded", String.valueOf(headers.size())));
		}else{
			fsource.setResource(null);
	        saveMessage(getText("view.sourceFileBroken", String.valueOf(headers.size())));
		}
		return SUCCESS;
    }

    
    /* Validate source file upload is a valid tab file
     * (non-Javadoc)
     * @see com.opensymphony.xwork2.ActionSupport#validate()
     */
    public void validateSave() {
        if (source != null) {        	
            getFieldErrors().clear();            
            try {
				sourceInspectionManager.getHeader(source);
			} catch (Exception e) {
				this.addActionError(getText("sources.invalidSql"));
			}
        }
    }
    
    /* Validate sql source change/insert is a valid SQL statement
     * (non-Javadoc)
     * @see com.opensymphony.xwork2.ActionSupport#validate()
     */
    public void validateUpload() {
        if (getRequest().getMethod().equalsIgnoreCase("post")) {
            getFieldErrors().clear();
            if ("".equals(fileFileName) || file == null) {
                super.addFieldError("file", getText("errors.requiredField", new String[] {getText("uploadForm.file")}));
            } else if (file.length() > 2097152) {
                addActionError(getText("maxLengthExceeded"));
            }
        }
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

	public Long getSid() {
		return sid;
	}

	public void setSid(Long id) {
		this.sid = id;
	}

	public List<SourceBase> getSources() {
		return ListUtils.union(fileSources, fileSources);
	}

	public List<SourceFile> getFileSources() {
		return fileSources;
	}

	public List<SourceSql> getSqlSources() {
		return sqlSources;
	}

	public SourceSql getSource() {
		return source;
	}

	public void setSource(SourceSql source) {
		this.source = source;
	}

}
