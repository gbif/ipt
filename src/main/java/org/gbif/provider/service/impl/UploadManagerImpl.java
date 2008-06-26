package org.gbif.provider.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.SourceRow;
import org.gbif.provider.job.RdbmsUploadJob;
import org.gbif.provider.model.DwcExtension;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.UploadManager;

public class UploadManagerImpl implements UploadManager{
	protected static final Log log = LogFactory.getLog(UploadManagerImpl.class);

	public Map<String, Long> uploadCore(ImportSource source, Resource resource,
			DwcExtension core, UploadEvent event) {
		log.info("Uploading core "+core.getName()+" for resource "+resource.getTitle());
		Map<String, Long> idMap = new HashMap<String, Long>();
		for (SourceRow row : source){
			//TODO: insert row
		}
		return idMap;
	}

	public void uploadExtension(ImportSource source, Map<String, Long> idMap,
			Resource resource, DwcExtension extension) {
		// TODO Auto-generated method stub
		
	}

}
