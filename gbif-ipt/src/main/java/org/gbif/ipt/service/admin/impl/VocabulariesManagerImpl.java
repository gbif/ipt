/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.admin.VocabulariesManager;

import com.google.inject.Singleton;

/**
 * @author tim
 */
@Singleton
public class VocabulariesManagerImpl extends BaseManager implements VocabulariesManager {

	public void update(Vocabulary vocabulary) {
		// TODO Auto-generated method stub
	}

	public void delete(String uri) {
		// TODO Auto-generated method stub
	}

	public Vocabulary get(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vocabulary get(URL url) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Vocabulary> list() {
		List<Vocabulary> vocs = new ArrayList<Vocabulary>();
		return vocs;
	}
}
