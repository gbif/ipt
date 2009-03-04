package org.gbif.provider.datasource.impl;

import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.ImportSourceFactory;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.TabFileReader;

public class ImportSourceFactoryImpl implements ImportSourceFactory {

	
	/* (non-Javadoc)
	 * @see org.gbif.provider.datasource.impl.ImportSourceFact#newInstance(org.gbif.provider.model.DataResource, org.gbif.provider.model.ViewMappingBase)
	 */
    public ImportSource newInstance(DataResource resource, ViewCoreMapping view) throws ImportSourceException{
    	ImportSource src = getSource(resource, view);
		// init with resource, view
		src.init(resource, view);
		return src;
    }
    public ImportSource newInstance(DataResource resource, ViewExtensionMapping view) throws ImportSourceException{
    	ImportSource src = getSource(resource, view);
		// init with resource, view
		src.init(resource, view);
		return src;
    }
    
    
    private ImportSource getSource(DataResource resource, ViewMappingBase view) throws ImportSourceException{
    	if (resource == null || resource.getId()==null || view == null || view.getCoreIdColumn()==null){
    		throw new IllegalArgumentException();
    	}
    	
    	if (view.isMappedToFile()){
    		FileImportSource src = newFileImportSource();
    		return src;
    	}else{
			DatasourceContextHolder.setResourceId(resource.getId());
			SqlImportSource src = newSqlImportSource();
    		return src;
    	}
    }
    
    
    /**overridden by spring to inject prototype beans
     * @return
     */
    protected FileImportSource newFileImportSource(){
    	return null;
    }
    /**overridden by spring to inject prototype beans
     * @return
     */
    protected SqlImportSource newSqlImportSource(){
    	return null;
    }
}
