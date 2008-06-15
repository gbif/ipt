package org.gbif.provider;

import java.util.List;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DwcExtension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewMapping;
import org.junit.Test;

public class ModelTest extends BaseDaoTestCase{
    private GenericManager<DwcExtension, Long> dwcExtensionManager;
	

	public void setDwcExtensionManager(
			GenericManager<DwcExtension, Long> dwcExtensionManager) {
		this.dwcExtensionManager = dwcExtensionManager;
	}


	@Test
	public void testColumnIndex(){
		DwcExtension extension = new DwcExtension();
		ExtensionProperty propMap = new ExtensionProperty();
		extension.addProperty(propMap);
		extension = dwcExtensionManager.save(extension);
		this.flush();
		List<DwcExtension> extensions = dwcExtensionManager.getAll();
		for (DwcExtension ext : extensions){
			List<ExtensionProperty> props = ext.getProperties(); 
			assertFalse(props.isEmpty());
		}
	}

}
