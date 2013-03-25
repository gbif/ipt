package org.gbif.provider.webapp.action.portal;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ImageCacheManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import static org.gbif.provider.util.Constants.DEFAULT_LOGO;


public class ImageCacheAction extends BaseOccurrenceResourceAction implements ServletContextAware{
	private InputStream inputStream;
	@Autowired
	private AppConfig cfg;
	@Autowired
	private ImageCacheManager imageCacheManager;
	private ServletContext context;
	
    public String execute() throws FileNotFoundException{
//    	imageCacheManager
    	if (resource_id != null){
    		OccurrenceResource res = occResourceManager.get(resource_id);
    		File data = cfg.getArchiveFile(res.getId());
    		inputStream = new FileInputStream(data);
    		return SUCCESS;
    	}
    	return ERROR;
    }
	
   
	public InputStream getInputStream(){
		return inputStream;
	}

	public void setServletContext(ServletContext context) {
		this.context=context;
	}
    
}