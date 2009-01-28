package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.SourceSql;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;


public class SourceAction extends BaseDataResourceAction implements Preparable{
	private static final long serialVersionUID = -3698917712584074200L;
	@Autowired
	private SourceInspectionManager sourceInspectionManager;
	@Autowired
    private SourceManager sourceManager;
    private List<SourceFile> fileSources = new ArrayList<SourceFile>();
    private List<SourceSql> sqlSources = new ArrayList<SourceSql>();
    private SourceBase source;
    // file upload
    private File file;
    private String fileContentType;
    private String fileFileName;
	private Long sid;
	// preview only
    private List<String> previewHeader;
    private List<List<? extends Object>> preview;

    

    @Override
	public void prepare() {
		super.prepare();
		if (sid != null) {
			source = sourceManager.get(sid);
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
    	if (resource_id==null){
    		return ERROR;
    	}
    	List<SourceBase> sources = sourceManager.getAll(resource_id);
    	for (SourceBase s : sources){
    		if (s instanceof SourceFile){
    			// try to determine filesize
    			SourceFile sf = (SourceFile) s;
    			File f = cfg.getSourceFile(resource_id, sf.getFilename());
    			if (!f.exists() || !f.isFile()) {
    			    log.warn(String.format("SourceFile %s for resource %s doesn't exist.", sf.getFilename(), resource_id));
    	    		sf.setFileSize(-1l);
    			}else{    			    
    				sf.setFileSize(f.length());
    				if (sf.getDateUploaded()==null){
        				sf.setDateUploaded(new Date(f.lastModified()));
    				}
    			}
    			fileSources.add(sf);
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
		source.setResource(resource);
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
		File targetFile = cfg.getSourceFile(resource.getId(), fileFileName);
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

        // process file. Check if file was uploaded before
		SourceFile fsource = sourceManager.getSourceByFilename(resource_id, fileFileName);
		if (fsource==null){
			// new source
			fsource = new SourceFile();
			fsource.setResource(resource);
			fsource.setFilename(fileFileName);
		}
		// set new upload timestamp
		fsource.setDateUploaded(new Date());

		List<String> headers = sourceInspectionManager.getHeader(fsource);
		log.info(String.format("Tab file %s uploaded with %s columns", targetFile.getAbsolutePath(), headers .size()));
		if (headers.size() > 1){
			// save file in view mapping
			sourceManager.save(fsource);
	        saveMessage(getText("sources.sourceFileUploaded", String.valueOf(headers.size())));
		}else{
			fsource.setResource(null);
	        saveMessage(getText("sources.sourceFileBroken", String.valueOf(headers.size())));
		}
		
		// get sources data
		return list();
    }

    
    
    
	public String sourcePreview(){
		if (source == null || source.getId()==null){
			if (sid==null){
				throw new NullPointerException("SourceID sid required");				
			}else{
				throw new IllegalArgumentException("SourceID sid doesnt exist");				
			}
		}
		log.debug("prepareSourceDataPreview");
        // get resultset preview
		try {
            // get first 5 rows into list of list for previewing data
            preview = sourceInspectionManager.getPreview(source);
            previewHeader = (List<String>) preview.remove(0);
        } catch (Exception e) {
            String msg = getText("view.sqlError");
            saveMessage(msg);
            log.warn(msg, e);
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
            } else if (file.length() > 104857600) {
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

	public SourceBase getSource() {
		return source;
	}

	public void setSource(SourceSql source) {
		this.source = source;
	}

	public List<List<? extends Object>> getPreview() {
		return preview;
	}

	public List<String> getPreviewHeader() {
		return previewHeader;
	}


}
