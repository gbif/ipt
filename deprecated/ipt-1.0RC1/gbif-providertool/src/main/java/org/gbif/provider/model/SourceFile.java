package org.gbif.provider.model;

import java.io.File;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.util.AppConfig;

@Entity
public class SourceFile extends SourceBase {
	private static Log log = LogFactory.getLog(SourceFile.class);
	private Date dateUploaded;
	private long fileSize;
	private boolean headers=false;

	public SourceFile() {
		super();
	}
	public SourceFile(File targetFile) {
		super();
		setFile(targetFile);
	}
	
	@Transient
	public String getFilename() {
		return name;
	}
	public void setFilename(String filename) {
		this.name = filename;
	}
	public void setFile(File file) {
		if (file != null){
			setFilename(file.getName());
		}else{
			setFilename(null);
		}
	}
	
	@Transient
	public Date getDateUploaded() {
		return dateUploaded;
	}
	public void setDateUploaded(Date dateUploaded) {
		this.dateUploaded = dateUploaded;
	}
	
	@Override
	@Transient
	public boolean isValid() {
		if (resource != null){
			if (AppConfig.getResourceSourceFile(resource.getId(), name).exists()){
				return true;
			}
		}
		return false;
	}
	
	@Transient
	public long getFileSize() {
		return fileSize;
	}
	@Transient
	public long getFileSizeInKB() {
		return fileSize/1024;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public boolean isHeaders() {
		return headers;
	}
	@Transient
	public boolean hasHeaders() {
		return headers;
	}
	public void setHeaders(boolean headers) {
		this.headers = headers;
	}	
	
	
}
