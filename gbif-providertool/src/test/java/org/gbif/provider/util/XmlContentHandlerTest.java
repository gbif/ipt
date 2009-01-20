package org.gbif.provider.util;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import static org.junit.Assert.assertTrue;


public class XmlContentHandlerTest {
	@Test
	public void testHandler(){
		 SAXParserFactory factory = SAXParserFactory.newInstance();
		  try {
		      SAXParser saxParser = factory.newSAXParser();
		      XmlContentHandler handler = new XmlContentHandler();
		      saxParser.parse( new File("/Users/markus/Desktop/eml.xml"),  handler);
		      assertTrue(handler.getContent().length()>100);
		  } catch (Throwable err) {
		        err.printStackTrace ();
		  }		
	}
}
