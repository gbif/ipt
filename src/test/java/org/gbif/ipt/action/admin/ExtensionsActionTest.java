/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.service.registry.impl.RegistryManagerImplTest;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("HttpUrlsUsage")
public class ExtensionsActionTest {

  ExtensionsAction action;
  List<Extension> extensions;

  @BeforeEach
  public void setup() throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
    HttpClient mockHttpClient = mock(HttpClient.class);
    HttpResponse mockResponse = mock(HttpResponse.class);
    ExtendedResponse extResponse = new ExtendedResponse(mockResponse);

    // mock response from Registry listing all registered extensions
    extResponse.setContent(
      IOUtils.toString(
          Objects.requireNonNull(RegistryManagerImplTest.class.getResourceAsStream("/responses/extensions_sandbox.json")),
          StandardCharsets.UTF_8));
    when(mockHttpClient.get(anyString())).thenReturn(extResponse);

    // create instance of RegistryManager
    RegistryManager registryManager =
      new RegistryManagerImpl(mock(AppConfig.class), mock(DataDir.class), mockHttpClient, mock(SAXParserFactory.class),
        mock(ConfigWarnings.class), mock(SimpleTextProvider.class), mock(RegistrationManager.class), mock(
        ResourceManager.class));

    // load list of all registered extensions
    extensions = registryManager.getExtensions();

    // create instance of action
    action = new ExtensionsAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
      mock(RegistrationManager.class), mock(ExtensionManager.class), mock(VocabulariesManager.class),
      registryManager, mock(ConfigWarnings.class));
  }

  @Test
  public void testGetLatestVersions() {
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
    // make sure that Occurrence extension is the right one
    Extension latestOccurrence = getFirstOccurrenceExtension(filtered);
    assertNotNull(latestOccurrence);
    assertTrue(latestOccurrence.isLatest());
    assertEquals("http://rs.gbif.org/sandbox/core/dwc_occurrence_2015-04-24.xml", latestOccurrence.getUrl().toString());
  }

  @Test
  public void testUpdateIsLatest() throws MalformedURLException, ParseException {
    // mock returning list of installed extensions, having Occurrence extension that is NOT latest
    List<Extension> installed = new ArrayList<>();
    Extension occurrenceCore = new Extension();
    occurrenceCore.setUrl(new URL("http://rs.gbif.org/core/dwc_occurrence.xml"));
    occurrenceCore.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);
    installed.add(occurrenceCore);
    assertNull(occurrenceCore.getIssued());

    // do the check
    action.updateIsLatest(installed);
    assertFalse(installed.get(0).isLatest()); // Indicates it is NOT the latest version!

    // mock returning list of installed extensions, having Occurrence extension that IS latest
    installed.clear();
    String dateStr = "2015-04-24";
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date issuedDate = dateFormat.parse(dateStr);
    occurrenceCore.setIssued(issuedDate);
    occurrenceCore.setUrl(new URL("http://rs.gbif.org/sandbox/core/dwc_occurrence_2015-04-24.xml"));
    occurrenceCore.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);
    assertNotNull(occurrenceCore.getIssued());
    installed.add(occurrenceCore);

    action.updateIsLatest(installed);
    assertTrue(installed.get(0).isLatest()); // Now indicates it is the latest version!
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

  /**
   * Method that retrieves the first extensions with Occurrence rowType.
   *
   * @param extensions list of extensions
   *
   * @return first extensions with Occurrence rowType encountered in list
   */
  private Extension getFirstOccurrenceExtension(List<Extension> extensions) {
    for (Extension extension: extensions) {
      if (extension.getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
        return extension;
      }
    }
    return null;
  }
}
