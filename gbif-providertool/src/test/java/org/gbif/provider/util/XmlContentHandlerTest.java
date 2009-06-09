package org.gbif.provider.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.SAXException;


public class XmlContentHandlerTest {
	@Test
	public void testHandler() throws ParserConfigurationException, SAXException, IOException{
		 SAXParserFactory factory = SAXParserFactory.newInstance();
	      SAXParser saxParser = factory.newSAXParser();
	      XmlContentHandler handler = new XmlContentHandler();
	      File eml = new File(XmlContentHandlerTest.class.getResource("/eml.xml").getFile());
	      saxParser.parse(eml,  handler);
	      assertTrue(handler.getContent().length()>100);
	}
}
