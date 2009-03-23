package org.gbif.provider.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


public class ExtensionTest extends BaseDaoTestCase{
    @Autowired
    @Qualifier("extensionManager")
    private GenericManager<Extension> extensionManager;
    @Autowired
    private OccResourceManager occResourceManager;
    @Autowired
    private ResourceFactory resourceFactory;
	

	public void setResourceFactory(ResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}


	@Test
	public void testExtensionPropertyList() throws Exception{
		Extension extension = new Extension();
		extension.setName("testExtensionPropertyList");
		ExtensionProperty propMap = new ExtensionProperty();
		extension.addProperty(propMap);
		extension = extensionManager.save(extension);
		// check dwc, checklist and inserted extensions
		for (Long extId : Arrays.asList(Constants.DARWIN_CORE_EXTENSION_ID, extension.getId())){
			Extension ext = extensionManager.get(extId);
			List<ExtensionProperty> props = ext.getProperties(); 
			assertFalse(props.isEmpty());
		}
	}

	@Test
	public void testExtensionMap(){
		OccurrenceResource occRes = resourceFactory.newOccurrenceResourceInstance();

		Extension ext1 = new Extension();
		ext1.setName("testExtensionMap 1");
		ext1 = extensionManager.save(ext1);
		ExtensionMapping map1 = new ExtensionMapping();
		map1.setExtension(ext1);
		occRes.addExtensionMapping(map1);

		Extension ext2 = new Extension();
		ext2.setName("testExtensionMap 2");
		ext2 = extensionManager.save(ext2);
		ExtensionMapping map2 = new ExtensionMapping();
		map2.setExtension(ext2);
		occRes.addExtensionMapping(map2);
		
		Long occId = occResourceManager.save(occRes).getId();
		
		// check retrieved data. what about the hibernate cache?
		DataResource res = occResourceManager.get(occId);		
		Collection<ExtensionMapping> allMappings = res.getAllMappings();

		assertTrue(res.getAllMappings().size()==3);
		assertTrue(res.getExtensionMappings().size()==2);
		assertTrue(res.getCoreMapping().getExtension().getId().equals(Constants.DARWIN_CORE_EXTENSION_ID));
		// the core mapping should not be in the extension mappings map
		assertFalse(res.getExtensionMappings().contains(res.getCoreMapping()));
		// but in all mappings it should:
		assertTrue(res.getAllMappings().contains(res.getCoreMapping()));

		for (Long i : res.getExtensionMappingsMap().keySet()){
			Extension e = res.getExtensionMappingsMap().get(i).getExtension();
			Long i2 = e.getId();
			assertTrue(i.equals(i2));
		}
	}

}
