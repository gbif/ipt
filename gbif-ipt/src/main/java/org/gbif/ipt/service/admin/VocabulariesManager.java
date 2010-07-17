/**
 * 
 */
package org.gbif.ipt.service.admin;

import java.net.URL;
import java.util.List;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the vocabularies within the IPT.
 *  
 * @author tim
 */
@ImplementedBy(VocabulariesManagerImpl.class)
public interface VocabulariesManager {
	
	/** Load all known vocabularies from the data dir
	 * @return number of vocabularies that have been loaded successfully
	 */
	public int load();
	
	/** Downloads the latest version from the vocabulary URL and udpates all related concepts & terms 
	 * @param vocabulary the vocabulary to update
	 */
	public void update(Vocabulary vocabulary);
	
	public Vocabulary get(String uri);
	public Vocabulary get(URL url);

	public List<Vocabulary> list();

	public void delete(String uri);

}
