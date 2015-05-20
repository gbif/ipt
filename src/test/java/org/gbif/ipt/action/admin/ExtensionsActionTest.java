package org.gbif.ipt.action.admin;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.service.registry.impl.RegistryManagerImplTest;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.text.html.parser.DTD;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtensionsActionTest {

  ExtensionsAction action;
  List<Extension> extensions;

  @Before
  public void setup() throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
    HttpUtil mockHttpUtil = mock(HttpUtil.class);
    HttpUtil.Response mockResponse = mock(HttpUtil.Response.class);

    // mock response from Registry listing all registered extensions
    mockResponse.content =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/extensions_sandbox.json"), "UTF-8");
    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mock(AppConfig.class), mock(DataDir.class), mockHttpUtil, mock(SAXParserFactory.class),
        mock(ConfigWarnings.class), mock(SimpleTextProvider.class), mock(RegistrationManager.class));

    // load list of all registered extensions
    extensions = manager.getExtensions();

    // create instance of action
    action = new ExtensionsAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
      mock(RegistrationManager.class), mock(ExtensionManager.class), mock(VocabulariesManager.class),
      manager, mock(ConfigWarnings.class));
  }

  @Test
  public void testGetLatestVersions() throws Exception {
    // start with 52 extensions
    assertEquals(52, extensions.size());
    // start with 3 extensions with Occurrence rowType
    assertEquals(3, countOccurrenceExtensions(extensions));

    // filter extensions list so that it only includes the latest version of each extension
    List<Extension> filtered = action.getLatestVersions(extensions);

    // end with 44 extensions
    assertEquals(44, filtered.size());
    // end with 1 extension with Occurrence rowType
    assertEquals(1, countOccurrenceExtensions(filtered));
  }

  /**
   * Method that counts the number of extensions with Occurrence rowType.
   *
   * @param extensions list of extensions
   *
   * @return number of extensions in list with Occurrence rowType
   */
  private int countOccurrenceExtensions(List<Extension> extensions) {
    int count = 0;
    for (Extension extension: extensions) {
      if (extension.getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
        count++;
      }
    }
    return count;
  }
}
