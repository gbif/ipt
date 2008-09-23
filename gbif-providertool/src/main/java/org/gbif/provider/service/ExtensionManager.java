package org.gbif.provider.service;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.springframework.beans.factory.annotation.Autowired;

public interface ExtensionManager extends GenericManager<Extension>{
	/**Install a new extension, i.e. saving the extension entity and creating a matching, new and empty extension table
	 * @param extension
	 */
	public void installExtension(Extension extension);
	/**Remove an extension, i.e. deleteing the extension instance and dropping the matching extension table with all its records
	 * @param extension
	 */
	public void removeExtension(Extension extension);
}
