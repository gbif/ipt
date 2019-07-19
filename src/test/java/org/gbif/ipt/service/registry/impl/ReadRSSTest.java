package org.gbif.ipt.service.registry.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.google.inject.Singleton;
import org.apache.commons.digester.Digester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore("These rely on external IPTs")
public class ReadRSSTest {

  // logging
  private static final Logger LOG = LogManager.getLogger(ReadRSSTest.class);
  // timeout in milliseconds for both the connection timeout and the response read timeout
  private static final int TIMEOUT_MILLIS = 2000;
  private static final SAXParserFactory saxParserFactory = provideNsAwareSaxParserFactory();
  private static final String IPT_RSS_NAMESPACE = "http://ipt.gbif.org/";

  /**
   * Requests a RSS feed with GET request and parses it into RSS object.
   *
   * @param url     The RSS HTTP URL to be pinged
   *
   * @return Populated RSS object or null if URL was offline or version couldn't be determined for any other reason
   */
  public RSS pingURL(URL url) {
    RSS rss = null;
    try {
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(TIMEOUT_MILLIS);
      connection.setReadTimeout(TIMEOUT_MILLIS);
      connection.setRequestMethod("GET");
      try {
        rss = parse(connection.getInputStream());
      } catch (Exception e) {
        LOG.error("Failed to parse RSS feed: " + rss, e);
      }
      connection.disconnect();
    } catch (IOException exception) {
      return null;
    }
    return rss;
  }

  @Test
  public void testPingUrl() throws MalformedURLException {
    RSS rss = pingURL(new URL("http://ipt.ala.org.au/rss.do"));
    assertNotNull(rss);
    assertEquals("1322b7b1-6b85-499f-964e-5e8599c73e6e", rss.getIdentifier());
    assertEquals("GBIF IPT 2.3.4-r68469e8", rss.getVersion());
  }

  @Test
  public void testRSSParsing() throws IOException, SAXException, ParserConfigurationException {
    InputStream rssIs = ReadRSSTest.class.getResourceAsStream("/responses/rss.xml");
    RSS rss = parse(rssIs);
    assertNotNull(rss);
    assertEquals("1322b7b1-6b85-499f-964e-5e8599c73e6e", rss.getIdentifier());
    assertEquals("GBIF IPT 2.3.4-r68469e8", rss.getVersion());
  }

  /**
   * Parses a RSS response as input stream.
   *
   * @param is For the XML of the RSS feed
   *
   * @return Populated RSS object or null if the RSS response could not be extracted for any reason
   */
  private RSS parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
    // in order to deal with arbitrary namespace prefixes we need to parse namespace aware!
    Digester digester = new Digester(saxParserFactory.newSAXParser());
    digester.setRuleNamespaceURI(IPT_RSS_NAMESPACE);
    digester.setNamespaceAware(true);

    RSS rss = new RSS();
    digester.push(rss);
    digester.addBeanPropertySetter("*/identifier", "identifier");
    digester.addBeanPropertySetter("*/generator", "version");
    digester.parse(is);
    return rss;
  }

  @Singleton
  private static SAXParserFactory provideNsAwareSaxParserFactory() {
    SAXParserFactory saxf = null;
    try {
      saxf = SAXParserFactory.newInstance();
      saxf.setValidating(false);
      saxf.setNamespaceAware(true);
    } catch (Exception e) {
      LOG.error("Failed to create SAX Parser Factory: " + e.getMessage(), e);
    }
    return saxf;
  }

  /**
   * Class representing RSS feed with select properties of interest.
   */
  public class RSS {
    private String identifier;
    private String version;

    public RSS() {
    }

    public String getIdentifier() {
      return identifier;
    }

    public void setIdentifier(String identifier) {
      this.identifier = identifier;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }
  }
}
