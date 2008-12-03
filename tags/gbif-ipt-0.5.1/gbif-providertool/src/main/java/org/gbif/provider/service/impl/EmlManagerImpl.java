package org.gbif.provider.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import com.thoughtworks.xstream.XStream;

public class EmlManagerImpl implements EmlManager{
    protected static final Log log = LogFactory.getLog(EmlManagerImpl.class);

    @Autowired
	public AppConfig cfg;
	@Autowired
	@Qualifier("resourceManager")
	public GenericResourceManager<Resource> resourceManager;
	private XStream xstream = new XStream();

	public Eml load(Resource resource) {
		if (resource==null){
			throw new NullPointerException("EML file requires resource");
		}
		Eml eml = null;
		if (resource.getId()!=null){
    		// load existing data. This is a persistent resource
    		File emlFile = cfg.getEmlFile(resource.getId());
			try {
				FileReader reader = new FileReader(emlFile);
				eml = (Eml) xstream.fromXML(reader);
			} catch (FileNotFoundException e) {
				log.error(String.format("EML Metadata file not found for resource %s", resource.getId()));
			}
		}
		if (eml == null){
			// file not found or new resource
			eml = new Eml();
			// copy some resource default data that is not delegated (title & description are)
			eml.getResourceCreator().setEmail(resource.getContactEmail());
			eml.getResourceCreator().setLastName(resource.getContactName());
		}

		eml.setResource(resource);
		return eml;
	}
	
	
	/**
	 * serialises the EML document into some file. Whatever the easiest way is. XMLStream?
	 */
	public void save(Eml eml){
		// update persistent EML properties on resource
		Resource res = eml.getResource();
		res.updateWithEml(eml);
		resourceManager.save(res);		
		// now persist EML file (resource must have ID now)
		try {
    		File emlFile = cfg.getEmlFile(res.getId());
	    	FileWriter writer = new FileWriter(emlFile);
	        xstream.toXML(eml, writer);
	    	writer.close();
	     }catch(IOException ex){
	        ex.printStackTrace();
	     }
	}
	
}
