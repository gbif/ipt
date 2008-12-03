package org.gbif.provider.datasource.impl;

import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.TabFileReader;

public class ImportSourceFactory {
    public static ImportSource newInstance(DataResource resource, ViewMappingBase view) throws ImportSourceException{
    	if (resource == null || resource.getId()==null || view == null || view.getCoreIdColumn()==null){
    		throw new IllegalArgumentException();
    	}
    	
    	if (view.isMappedToFile()){
        	return FileImportSource.newInstance(resource, view);
    	}else{
			DatasourceContextHolder.setResourceId(resource.getId());
        	return RdbmsImportSource.newInstance(resource, view);
    	}

    }
}
