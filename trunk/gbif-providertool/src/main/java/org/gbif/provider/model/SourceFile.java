package org.gbif.provider.model;

import java.io.File;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.util.AppConfig;

@Entity
public class SourceFile extends SourceBase {
	private static Log log = LogFactory.getLog(SourceFile.class);
	private String filename;

	public SourceFile() {
		super();
	}
	public SourceFile(File targetFile) {
		super();
		setFile(targetFile);
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setFile(File file) {
		if (file != null){
			setFilename(file.getName());
		}else{
			setFilename(null);
		}
	}
	
	@Override
	@Transient
	public boolean isValid() {
		if (resource != null){
			if (AppConfig.getResourceSourceFile(resource.getId(), filename).exists()){
				return true;
			}
		}
		return false;
	}	
}
