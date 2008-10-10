package org.gbif.provider.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.appfuse.model.User;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Agent;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.GeoKeyword;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class EmlManagerImpl implements EmlManager{
	@Autowired
	public AppConfig cfg;
	@Autowired
	@Qualifier("resourceManager")
	public GenericResourceManager<Resource> resourceManager;

	public Eml load(Resource resource) {
		if (resource==null || resource.getId()==null){
			throw new IllegalArgumentException("Needs persistent resource with ID");
		}
		return Eml.loadFile(resource, cfg.getEmlFile(resource.getId()));
	}
	
	
	/**
	 * serialises the EML document into some file. Whatever the easiest way is. XMLStream?
	 */
	public void save(Eml eml){
		// first persist EML file
	     FileOutputStream fos = null;
	     ObjectOutputStream out = null;
	     try
	     {
	       fos = new FileOutputStream(eml.getFile());
	       out = new ObjectOutputStream(fos);
	       out.writeObject(eml);
	       out.close();
	     }catch(IOException ex){
	       ex.printStackTrace();
	     }
		// then update persistent EML properties on resource
		Resource res = eml.getResource();
		res.updateWithEml(eml);
		resourceManager.save(res);		
	}
	
}
