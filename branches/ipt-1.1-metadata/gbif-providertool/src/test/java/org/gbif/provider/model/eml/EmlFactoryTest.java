package org.gbif.provider.model.eml;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import org.junit.Test;

public class EmlFactoryTest extends TestCase {

	@Test
	public void testBuild() {
		try {
			Eml eml = EmlFactory.build(new FileInputStream("./src/test/resources/eml/sample.xml"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			assertNotNull(eml);
			assertEquals("619a4b95-1a82-4006-be6a-7dbe3c9b33c5", eml.getGuid());
			assertEquals("Tanzanian Entomological Collection",eml.getTitle());
			
			// this is a pretty complete test for agents so subsequent agent tests will not be so extensive
			assertNotNull(eml.getResourceCreator());
			assertEquals("David",eml.getResourceCreator().getFirstName());
			assertEquals("Remsen",eml.getResourceCreator().getLastName());
			assertEquals("GBIF",eml.getResourceCreator().getOrganisation());
			assertEquals("ECAT Programme Officer",eml.getResourceCreator().getPosition());

      // this is a pretty complete test for agents so subsequent address tests will not be so extensive
			assertNotNull(eml.getResourceCreator().getAddress());
			assertEquals("Copenhagen",eml.getResourceCreator().getAddress().getCity());
			assertEquals("Sjaelland",eml.getResourceCreator().getAddress().getProvince());
			assertEquals("2100",eml.getResourceCreator().getAddress().getPostalCode());
			assertEquals("dk",eml.getResourceCreator().getAddress().getCountry());
			assertEquals("+4528261487",eml.getResourceCreator().getPhone());
			assertEquals("dremsen@gbif.org",eml.getResourceCreator().getEmail());
			assertEquals("http://www.gbif.org",eml.getResourceCreator().getHomepage());
	        
			// limited agent test
			assertNotNull(eml.getMetadataProvider());
			assertEquals("Robertson",eml.getMetadataProvider().getLastName());
			
			// limited agent test
			// TODO add tests for the ROLEs
			assertNotNull(eml.getAssociatedParties());
			assertEquals(2, eml.getAssociatedParties().size());
			
      assertEquals(sdf.parse("2010-02-02"), eml.getPubDate());
      assertEquals("en_US", eml.getLanguage());
      assertEquals("Specimens in jars", eml.getAbstract());
	        
      assertNotNull(eml.getKeywords());
      assertEquals(2, eml.getKeywords().size());
      assertNotNull(eml.getKeywords().get(0).keywords);
      assertEquals(3, eml.getKeywords().get(0).keywords.size());
      assertEquals("Insect", eml.getKeywords().get(0).getKeywords().get(0));
      assertEquals("Fly", eml.getKeywords().get(0).getKeywords().get(1));
      assertEquals("Bee", eml.getKeywords().get(0).getKeywords().get(2));
      assertEquals("Zoology Vocabulary Version 1", eml.getKeywords().get(0).getKeywordThesaurus());
      assertEquals(1, eml.getKeywords().get(1).keywords.size());
      assertEquals("Spider", eml.getKeywords().get(1).getKeywords().get(0));
      assertEquals("Zoology Vocabulary Version 1", eml.getKeywords().get(1).getKeywordThesaurus());
      
      assertEquals("Where can the additional information possibly come from?!", eml.getAdditionalInfo());
      assertNotNull(eml.getIntellectualRights());
      assertTrue(eml.getIntellectualRights().startsWith("Owner grants"));
      assertTrue(eml.getIntellectualRights().endsWith("Site)."));
       
      assertNotNull(eml.getGeospatialCoverages());
      assertEquals(2, eml.getGeospatialCoverages().size());
      assertEquals("Bounding Box 1", eml.getGeospatialCoverages().get(0).getDescription());
	    assertEquals(new Double("23.975"), eml.getGeospatialCoverages().get(0).getBoundingCoordinates().getMax().getLatitude());
      // TODO add more spatial tests
      // TODO - this will be 4 when the associatedMetadata is handled
      //assertEquals(4, eml.getTemporalCoverages().size());
      assertEquals(2, eml.getTemporalCoverages().size());  // see above comment
      //assertEquals("Jurassic", eml.getTemporalCoverages().get(3).getLivingTimePeriod());
      //assertEquals("During the 70s", eml.getTemporalCoverages().get(4).getFormationPeriod());
      // TODO add more temporal tests
      assertEquals(2, eml.getTaxonomicCoverages().size());
      // TODO add more taxonomic tests
	        
      assertEquals("Provide data to the whole world.", eml.getPurpose());
	        
      assertNotNull(eml.getSamplingMethods());
      assertEquals(3, eml.getSamplingMethods().size());
      // TODO add tests
	        
      assertNotNull(eml.getProject());
      assertEquals("Documenting Some Asian Birds and Insects", eml.getProject().getTitle());
      assertNotNull(eml.getProject().getPersonnel());
      assertEquals("My Deep Pockets", eml.getProject().getFunding());
      assertEquals("Turkish Mountains", eml.getProject().getStudyAreaDescription());
      assertEquals("This was done in Avian Migration patterns", eml.getProject().getDesignDescription());
      assertNotNull(eml.getCitations());
      assertEquals(1, eml.getCitations().size());
	    assertEquals("Please site me as: Tim Robertson", eml.getCitations().get(0));
	    assertEquals("en_UK", eml.getMetadataLanguage());
	    assertEquals("dataset", eml.getHierarchyLevel());	        

	    assertNotNull(eml.getPhysicalData());
	    assertEquals(2, eml.getPhysicalData().size());
	    assertEquals("INV-GCEM-0305a1_1_1.shp", eml.getPhysicalData().get(0).getName());
	    assertEquals("ASCII", eml.getPhysicalData().get(0).getCharset());
	    assertEquals("shapefile", eml.getPhysicalData().get(0).getFormat());
	    assertEquals("2.0", eml.getPhysicalData().get(0).getFormatVersion());
	    assertEquals("http://metacat.lternet.edu/knb/dataAccessServlet?docid=knb-lter-gce.109.10&urlTail=accession=INV-GCEM-0305a1&filename=INV-GCEM-0305a1_1_1.TXT", 
	        		eml.getPhysicalData().get(0).getDistributionUrl());
      assertEquals("INV-GCEM-0305a1_1_2.shp", eml.getPhysicalData().get(1).getName());
      assertEquals("ASCII", eml.getPhysicalData().get(1).getCharset());
      assertEquals("shapefile", eml.getPhysicalData().get(1).getFormat());
      assertEquals("2.0", eml.getPhysicalData().get(1).getFormatVersion());
      assertEquals("http://metacat.lternet.edu/knb/dataAccessServlet?docid=knb-lter-gce.109.10&urlTail=accession=INV-GCEM-0305a1&filename=INV-GCEM-0305a1_1_2.TXT", 
	        		eml.getPhysicalData().get(1).getDistributionUrl());
	    
      assertNotNull(eml.getJgtiCuratorialUnit());
      assertEquals("500", eml.getJgtiCuratorialUnit().getRangeStart());
	    assertEquals("600", eml.getJgtiCuratorialUnit().getRangeEnd());
	        
	    assertEquals("alcohol", eml.getSpecimenPreservationMethod());
//	        
//	        assertEquals("urn:lsid:tim.org:12:1", eml.getParentCollectionId());
//	        assertEquals("urn:lsid:tim.org:12:2", eml.getCollectionId());
//	        assertEquals("Mammals", eml.getCollectionName());
	    
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
