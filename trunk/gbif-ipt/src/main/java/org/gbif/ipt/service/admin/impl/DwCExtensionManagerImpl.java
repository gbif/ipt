/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.admin.DwCExtensionManager;

import com.google.inject.Singleton;

/**
 * @author tim
 */
@Singleton
public class DwCExtensionManagerImpl extends BaseManager implements DwCExtensionManager {
	private Map<String, Extension> installedExtensions = new HashMap<String, Extension>();
	
	public void delete(String rowType) {
		// TODO Auto-generated method stub
		
	}

	public void install(URL url) {
		// TODO Auto-generated method stub
		
	}

	public List<Extension> list() {
		List<Extension> exts = new ArrayList<Extension>();
		
		return exts;
	}

	public Extension get(URL url) {
		// TODO Auto-generated method stub
		return null;
	}

	public Extension get(String rowType) {
		// TODO Auto-generated method stub
		return null;
	}

	public int load() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
