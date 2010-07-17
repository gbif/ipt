/**
 * 
 */
package org.gbif.ipt.service.admin;

import java.net.URL;
import java.util.List;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.DwCExtensionManagerImpl;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the DwC extensions.
 *  
 * @author tim
 */
/**
 * @author markus
 *
 */
@ImplementedBy(DwCExtensionManagerImpl.class)
public interface DwCExtensionManager {

	/** Load all installed extensions from the data dir
	 * @return number of extensions that have been loaded successfully
	 */
	public int load();
		
	/**List all installed extensions
	 * @return list of installed IPT extensions
	 */
	public List<Extension> list();
	
	/** Install an extension based on its xml definition url
	 * @param url the url that returns the xml based etension definition
	 */
	public void install(URL url);
	
	public Extension get(URL url);
	public Extension get(String rowType);

	/** Remove an installed extension by its unique rowType
	 * @param rowType
	 */
	public void delete(String rowType);

}
