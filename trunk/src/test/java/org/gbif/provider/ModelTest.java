package org.gbif.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.DwcExtension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewMapping;
import org.junit.Test;

public class ModelTest extends BaseDaoTestCase{
    private GenericManager<DwcExtension, Long> dwcExtensionManager;
    private GenericManager<OccurrenceResource, Long> occResourceManager;
	

	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	public void setDwcExtensionManager(
			GenericManager<DwcExtension, Long> dwcExtensionManager) {
		this.dwcExtensionManager = dwcExtensionManager;
	}


	@Test
	public void testExtensionPropertyList(){
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

	@Test
	public void testDwcExtensionMap(){
		OccurrenceResource occRes = new OccurrenceResource();

		DwcExtension ext1 = new DwcExtension();
		ext1.setName("test extension 1");
		ext1 = dwcExtensionManager.save(ext1);
		ViewMapping map1 = new ViewMapping();
		map1.setExtension(ext1);
		occRes.addMapping(map1);

		DwcExtension ext2 = new DwcExtension();
		ext2.setName("test extension 2");
		ext2 = dwcExtensionManager.save(ext2);
		ViewMapping map2 = new ViewMapping();
		map2.setExtension(ext2);
		occRes.addMapping(map2);
		
		occResourceManager.save(occRes);
		this.flush();
		
		List<OccurrenceResource> resources = occResourceManager.getAll();
		for (DatasourceBasedResource res : resources){
			Map<Long, ViewMapping> mapHM = res.getMappings(); 
			Set<Long> keys = mapHM.keySet();
			Collection<ViewMapping> mappings = mapHM.values();
			assertFalse(mapHM.isEmpty());
			assertFalse(keys.isEmpty());
			assertFalse(mappings.isEmpty());
			for (Long i : mapHM.keySet()){
				DwcExtension e = mapHM.get(i).getExtension();
				Long i2 = e.getId();
				assertTrue(i.equals(i2));
			}
		}
	}

}
