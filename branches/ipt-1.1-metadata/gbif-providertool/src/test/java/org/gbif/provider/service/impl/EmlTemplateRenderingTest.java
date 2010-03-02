/**
 * 
 */
package org.gbif.provider.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.gbif.provider.model.Address;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;


/**
 * The IPT has an EML model that is rendered to XML using the freemarker template
 * located in 
 *	/src/main/webapp/WEB-INF/pages/eml.ftl
 *
 * This JUnit Test is provided to test the rendering of the model model using the freemarker
 * against the sample XML files supplied with the original requirements.  The sample is
 * located in 
 *  /src/test/resources/eml/sample.xml
 * 
 * @author timrobertson
 */
public class EmlTemplateRenderingTest extends TestCase {
	private Template emlTemplate;
	private String sampleXML;
	
	/**
	 * Sets up the Freemarker templating and loads in the sample XML
	 * Throws an error if the template is not found
	 */
	@Override
	protected void setUp() throws Exception {
		Configuration cfg = new Configuration();
		// TODO probably not the best way to load these, but time pressures...
		emlTemplate = cfg.getTemplate("../../src/main/webapp/WEB-INF/pages/eml.ftl");
		sampleXML = readFileAsString("./src/test/resources/eml/sample.xml");
		assertNotNull(emlTemplate);
		assertNotNull(sampleXML);
	}

	/**
	 * Builds a sample model in the POJOs, and then uses freemarker to render the model to XML,
	 * and test the output for XML equivalence against a known sample
	 */
	@Test
	public void testModelRendering() {
		try {
			Eml eml = setupSample1();
			
	    	// run the rendering to a String to compare later
	    	StringWriter output = new StringWriter();
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("eml", eml);
			emlTemplate.process(data, output);
			String emlAsXML = output.toString();
			System.out.println(emlAsXML);
			
			// run XML comparison against the rendered output and the sample
			Diff xmlDiff = new Diff(sampleXML, emlAsXML);
			DetailedDiff detailedDiff = new DetailedDiff(xmlDiff);
			if (!detailedDiff.similar()) {
				System.out.println(detailedDiff.toString());	
			}
			assertTrue(detailedDiff.similar());
			assertTrue(detailedDiff.identical());
			
		} catch (Exception e) {
			fail("Error building and merging: " + e.getMessage());
		}
	}

	/**
	 * @return The EML model populated for the sample 1 test
	 * @throws ParseException On bad dates (never should happen)
	 */
	private Eml setupSample1() throws ParseException {
		Eml eml = new Eml();
		Resource resource = new Resource();
		eml.setResource(resource);
		
		eml.setEmlVersion(1);
		eml.getResource().setGuid("619a4b95-1a82-4006-be6a-7dbe3c9b33c5");
		eml.setTitle("Tanzanian Entomological Collection");
		eml.getResourceCreator().setFirstName("David");
		eml.getResourceCreator().setLastName("Remsen");
		eml.getResourceCreator().setOrganisation("GBIF");
		eml.getResourceCreator().setPosition("ECAT Programme Officer");
		Address a = new Address();
		a.setCity("Copenhagen");
		a.setAddress("Universitetsparken 15");
		a.setPostalCode("2100");
		a.setProvince("Sjaelland");
		a.setCountry("Denmark");
		eml.getResourceCreator().setAddress(a);
		eml.getResourceCreator().setPhone("+4528261487");
		eml.getResourceCreator().setEmail("dremsen@gbif.org");

		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		eml.setPubDate(sdf.parse("2010-02-02"));
		
		eml.setLanguage("en_US");
		eml.setAbstract("This is an abstract for a an Example EML");
		
		
		return eml;
	}
	
	/**
	 * @param filePath To read from
	 * @return The file read into a String
	 * @throws java.io.IOException On file read error - e.g. the file is not found
	 */
    private static String readFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1) {
        	String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }	
}