package org.gbif.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.util.Constants;
import org.junit.Test;

public class ModelTest extends BaseDaoTestCase{
    private GenericManager<Extension, Long> extensionManager;
    private GenericManager<OccurrenceResource, Long> occResourceManager;
	

	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	public void setExtensionManager(
			GenericManager<Extension, Long> extensionManager) {
		this.extensionManager = extensionManager;
	}


	@Test
	public void testExtensionPropertyList() throws Exception{
		Extension extension = new Extension();
		ExtensionProperty propMap = new ExtensionProperty();
		extension.addProperty(propMap);
		extension = extensionManager.save(extension);
		this.flush();
		// check dwc, checklist and inserted extensions
		for (Long extId : Arrays.asList(Constants.CHECKLIST_EXTENSION_ID, Constants.DARWIN_CORE_EXTENSION_ID, extension.getId())){
			Extension ext = extensionManager.get(extId);
			List<ExtensionProperty> props = ext.getProperties(); 
			assertFalse(props.isEmpty());
		}
	}

	@Test
	public void testDwcExtensionMap(){
		OccurrenceResource occRes = new OccurrenceResource();

		Extension ext1 = new Extension();
		ext1.setName("test extension 1");
		ext1 = extensionManager.save(ext1);
		ViewMapping map1 = new ViewMapping();
		map1.setExtension(ext1);
		occRes.addMapping(map1);

		Extension ext2 = new Extension();
		ext2.setName("test extension 2");
		ext2 = extensionManager.save(ext2);
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
				Extension e = mapHM.get(i).getExtension();
				Long i2 = e.getId();
				assertTrue(i.equals(i2));
			}
		}
	}

}
