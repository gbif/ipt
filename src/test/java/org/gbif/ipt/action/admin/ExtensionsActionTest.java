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

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.ExtensionPropertyTranslation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
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

import jakarta.servlet.http.HttpServletRequest;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("HttpUrlsUsage")
public class ExtensionsActionTest extends IptBaseTest {

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
        mock(ConfigWarnings.class), mock(SimpleTextProvider.class));

    // load list of all registered extensions
    extensions = registryManager.getExtensions();

    // create instance of action
    action = new ExtensionsAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
      mock(RegistrationManager.class), mock(ExtensionManager.class), mock(VocabulariesManager.class),
      registryManager, mock(ConfigWarnings.class), mock(ResourceManager.class));
  }

  @Test
  public void testGetLatestVersions() {
    // start with 53 extensions
    assertEquals(53, extensions.size());
    // start with 3 extensions with Occurrence rowType
    assertEquals(3, countOccurrenceExtensions(extensions));

    // filter extensions list so that it only includes the latest version of each extension
    List<Extension> filtered = action.getLatestVersions(extensions);

    // end with 45 extensions
    assertEquals(45, filtered.size());
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

  /**
   * The synchronisation changes the installed extensions and vocabularies, so a link (a GET request, which isn't
   * CSRF-checked) must not trigger it.
   */
  @Test
  public void testSynchroniseOnlyWithPost() throws Exception {
    for (String method : List.of("GET", "POST")) {
      VocabulariesManager vocabManager = mock(VocabulariesManager.class);
      ExtensionManager extensionManager = mock(ExtensionManager.class);
      ExtensionsAction listAction = new ExtensionsAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
          mock(RegistrationManager.class), extensionManager, vocabManager, mock(RegistryManager.class),
          mock(ConfigWarnings.class), mock(ResourceManager.class));
      HttpServletRequest req = mock(HttpServletRequest.class);
      when(req.getMethod()).thenReturn(method);
      listAction.withServletRequest(req);
      listAction.setSynchronise(true);
      // set by prepare()
      listAction.setLatestExtensionVersions(new ArrayList<>());

      assertEquals(ExtensionsAction.SUCCESS, listAction.list());
      int expected = "POST".equals(method) ? 1 : 0;
      verify(vocabManager, times(expected)).installOrUpdateDefaults();
      verify(vocabManager, times(expected)).updateIfChanged();
      verify(extensionManager, times(expected)).updateIfChanged();
    }
  }

  /**
   * The extension page (a GET of extension.do): groups in the definition's order, the resources mapping the extension
   * with how many of its terms each maps, the resources mapping each term, and the languages of the term labels.
   * A term counts as mapped with a source column or a default value only, and only if the extension has it.
   */
  @Test
  public void testExtensionPage() throws Exception {
    String rowType = "http://rs.tdwg.org/dwc/terms/ResourceRelationship";
    Extension ext = new Extension();
    ext.setRowType(rowType);
    ext.setTitle("Resource Relationship");
    // groups deliberately not in alphabetical order
    ext.addProperty(property(DwcTerm.resourceID, "Relationship", "Resource ID",
        new ExtensionPropertyTranslation("fr", "Identifiant de la ressource", null, null, null),
        new ExtensionPropertyTranslation("zh", "資源ID", null, null, null),
        new ExtensionPropertyTranslation("es", " ", null, null, null)));
    ext.addProperty(property(DwcTerm.relatedResourceID, "Relationship", "Related Resource ID",
        new ExtensionPropertyTranslation("fr", "Identifiant de la ressource liée", null, null, null)));
    ext.addProperty(property(DwcTerm.relationshipRemarks, "Remarks", "Relationship Remarks"));
    ext.addProperty(property(DwcTerm.relationshipAccordingTo, "Agent", "Relationship According To"));

    // "Beta" maps the extension twice: a column in one mapping, a default value in the other; a field with neither
    // and a term the extension doesn't have don't count
    Resource beta = resource("beta", "Beta",
        mapping(ext, field(DwcTerm.resourceID, 0, null), field(DwcTerm.relationshipRemarks, null, null),
            field(DwcTerm.occurrenceID, 1, null)),
        mapping(ext, field(DwcTerm.relatedResourceID, null, "urn:x")));
    Resource alpha = resource("alpha", "alpha", mapping(ext, field(DwcTerm.resourceID, 3, null)));
    Extension other = new Extension();
    other.setRowType("http://rs.tdwg.org/dwc/terms/Occurrence");
    Resource unrelated = resource("other", "Another", mapping(other, field(DwcTerm.occurrenceID, 0, null)));

    ExtensionManager extensionManager = mock(ExtensionManager.class);
    when(extensionManager.get(rowType)).thenReturn(ext);
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.list()).thenReturn(List.of(beta, unrelated, alpha));
    RegistryManager registryManager = mock(RegistryManager.class);
    when(registryManager.getExtensions()).thenReturn(new ArrayList<>());

    ExtensionsAction pageAction = new ExtensionsAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), extensionManager, mock(VocabulariesManager.class), registryManager,
        mock(ConfigWarnings.class), resourceManager);
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getMethod()).thenReturn("GET");
    // prepare() reads the id from the request
    when(req.getParameter("id")).thenReturn(rowType);
    pageAction.withServletRequest(req);
    pageAction.prepare();

    assertEquals("input", pageAction.execute());

    assertEquals(List.of("Relationship", "Remarks", "Agent"), new ArrayList<>(pageAction.getPropertiesByGroup().keySet()));

    // sorted by title, ignoring case; the resource not mapping the extension is left out
    List<String> users = pageAction.getExtensionUsage().stream()
        .map(u -> u.getResource().getShortname() + ":" + u.getTermsMapped())
        .collect(Collectors.toList());
    assertEquals(List.of("alpha:1", "beta:2"), users);

    assertEquals(List.of("alpha", "beta"), shortnames(pageAction.getTermUsedBy().get(DwcTerm.resourceID.qualifiedName())));
    assertEquals(List.of("beta"), shortnames(pageAction.getTermUsedBy().get(DwcTerm.relatedResourceID.qualifiedName())));
    assertFalse(pageAction.getTermUsedBy().containsKey(DwcTerm.relationshipRemarks.qualifiedName()));
    assertFalse(pageAction.getTermUsedBy().containsKey(DwcTerm.occurrenceID.qualifiedName()));

    // by language name (Chinese, French); the blank Spanish label doesn't count
    assertEquals(List.of("zh", "fr"), new ArrayList<>(pageAction.getLabelTranslations().keySet()));
    assertEquals(1, pageAction.getLabelTranslations().get("zh"));
    assertEquals(2, pageAction.getLabelTranslations().get("fr"));
    assertEquals("French", pageAction.languageName("fr"));
    assertEquals("xx-unknown", pageAction.languageName("xx-unknown"));
    assertEquals("xx", pageAction.languageName("xx"));
  }

  private static ExtensionProperty property(Term term, String group, String label, ExtensionPropertyTranslation... translations) {
    ExtensionProperty p = new ExtensionProperty(term.qualifiedName());
    p.setGroup(group);
    p.setLabel(label);
    for (ExtensionPropertyTranslation t : translations) {
      p.addTranslation(t);
    }
    return p;
  }

  private static PropertyMapping field(Term term, Integer index, String defaultValue) {
    PropertyMapping pm = new PropertyMapping();
    pm.setTerm(term);
    pm.setIndex(index);
    pm.setDefaultValue(defaultValue);
    return pm;
  }

  private static ExtensionMapping mapping(Extension ext, PropertyMapping... fields) {
    ExtensionMapping m = new ExtensionMapping();
    m.setExtension(ext);
    m.setFields(new LinkedHashSet<>(List.of(fields)));
    return m;
  }

  private static Resource resource(String shortname, String title, ExtensionMapping... mappings) {
    Resource r = new Resource();
    r.setShortname(shortname);
    r.setTitle(title);
    r.setMappings(new ArrayList<>(List.of(mappings)));
    return r;
  }

  private static List<String> shortnames(List<Resource> resources) {
    return resources == null ? null : resources.stream().map(Resource::getShortname).collect(Collectors.toList());
  }
}
