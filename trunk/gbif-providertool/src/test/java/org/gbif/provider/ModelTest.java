package org.gbif.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.CoreViewMapping;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.Constants;
import org.junit.Test;

public class ModelTest extends BaseDaoTestCase{
    private GenericManager<Extension, Long> extensionManager;
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    private ResourceFactory resourceFactory;
	

	public void setResourceFactory(ResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

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
		for (Long extId : Arrays.asList(ChecklistResource.EXTENSION_ID, OccurrenceResource.EXTENSION_ID, extension.getId())){
			Extension ext = extensionManager.get(extId);
			List<ExtensionProperty> props = ext.getProperties(); 
			assertFalse(props.isEmpty());
		}
	}

	@Test
	public void testExtensionMap(){
		OccurrenceResource occRes = resourceFactory.newOccurrenceResourceInstance();

		Extension ext1 = new Extension();
		ext1.setName("test extension 1");
		ext1 = extensionManager.save(ext1);
		ViewMapping map1 = new ViewMapping();
		map1.setExtension(ext1);
		occRes.addExtensionMapping(map1);

		Extension ext2 = new Extension();
		ext2.setName("test extension 2");
		ext2 = extensionManager.save(ext2);
		ViewMapping map2 = new ViewMapping();
		map2.setExtension(ext2);
		occRes.addExtensionMapping(map2);
		
		Long occId = occResourceManager.save(occRes).getId();
		this.flush();
		
		// check retrieved data. what about the hibernate cache?
		DatasourceBasedResource res = occResourceManager.get(occId);		
		Collection<ViewMapping> allMappings = res.getAllMappings();

		assertTrue(res.getAllMappings().size()==3);
		assertTrue(res.getExtensionMappings().size()==2);
		assertTrue(res.getCoreMapping().getExtension().getId().equals(OccurrenceResource.EXTENSION_ID));
		// the core mapping should not be in the extension mappings map
		assertFalse(res.getExtensionMappings().containsValue(res.getCoreMapping()));
		// but in all mappings it should:
		assertTrue(res.getAllMappings().contains(res.getCoreMapping()));

		for (Long i : res.getExtensionMappings().keySet()){
			Extension e = res.getExtensionMappings().get(i).getExtension();
			Long i2 = e.getId();
			assertTrue(i.equals(i2));
		}
	}

}
