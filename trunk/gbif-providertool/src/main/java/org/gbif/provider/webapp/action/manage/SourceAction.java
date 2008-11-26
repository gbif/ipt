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

import org.apache.commons.collections.ListUtils;
import org.gbif.provider.model.DataResource;
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


public class SourceAction extends BaseResourceAction{
	private static final long serialVersionUID = -3698917712584074200L;
	@Autowired
	private SourceInspectionManager sourceInspectionManager;
	@Autowired
	@Qualifier("sourceManager")
    private GenericResourceRelatedManager<SourceBase> sourceManager;
	@Autowired
	@Qualifier("fileSourceManager")
    private GenericResourceRelatedManager<SourceFile> fileSourceManager;
	@Autowired
	@Qualifier("sqlSourceManager")
    private GenericResourceRelatedManager<SourceSql> sqlSourceManager;
    private List<SourceFile> fileSources;
    private List<SourceSql> sqlSources;
    // file upload
    private File file;
    private String fileContentType;
    private String fileFileName;
	private Long sid;

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
	

    /**
     * Default method - returns "input"
     * @return "input"
     */
    public String execute() {
    	fileSources = fileSourceManager.getAll(resource_id);
    	sqlSources  = sqlSourceManager.getAll(resource_id);
        return SUCCESS;
    }
    
    /**
     * Upload a source file
     * @return String with result (cancel, input or sucess)
     * @throws Exception if something goes wrong
	 */
    public String upload() throws Exception {
        if (this.cancel != null) {
            return "cancel";
        }
        if (sid == null){
        	log.error("source id required for file upload");
        	return ERROR;
        }
        // check where it who wants this file and where to save
		DataResource resource = (DataResource) this.getResource();
		
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

        // place the data into the request for retrieval on next page
        getRequest().setAttribute("location", targetFile.getAbsolutePath());
        
        // process file
		SourceFile source = new SourceFile();
		source.setFilename(fileFileName);
		source.setResource(resource);

		List<String> headers = sourceInspectionManager.getHeader(source);
		log.info(String.format("Tab file %s uploaded with %s columns", targetFile.getAbsolutePath(), headers .size()));
		if (headers.size() > 1){
			// save file in view mapping
			sourceManager.save(source);
	        saveMessage(getText("view.sourceFileUploaded", String.valueOf(headers.size())));
		}else{
			source.setResource(null);
	        saveMessage(getText("view.sourceFileBroken", String.valueOf(headers.size())));
		}
		return SUCCESS;
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
	public Map<String, String> getJdbcDriverClasses() {
		return jdbcDriverClasses;
	}
    
}
