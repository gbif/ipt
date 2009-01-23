package org.gbif.provider.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.gbif.provider.tapir.TemplateFactoryTest;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertTrue;


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
