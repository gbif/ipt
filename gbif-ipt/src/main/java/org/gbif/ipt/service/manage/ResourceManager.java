/**
 * 
 */
package org.gbif.ipt.service.manage;

import java.io.IOException;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the main resource entity.
 *  
 * @author tim
 */
@ImplementedBy(ResourceManagerImpl.class)
public interface ResourceManager {
	
	public void load() throws InvalidConfigException;

	public void save(Resource resource) throws IOException;

	public void delete(Resource resource) throws IOException;
}
