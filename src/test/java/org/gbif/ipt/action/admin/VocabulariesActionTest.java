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
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("HttpUrlsUsage")
public class VocabulariesActionTest {

  private static final String URI = "http://rs.gbif.org/vocabulary/dwc/establishment_means/";
  private static final Date MARCH = new Date(1_773_273_600_000L);
  private static final Date JULY = new Date(1_784_592_000_000L);

  private Vocabulary vocabulary;
  private VocabulariesManager vocabManager;
  private ExtensionManager extensionManager;
  private RegistryManager registryManager;

  @BeforeEach
  public void setup() {
    vocabulary = new Vocabulary();
    vocabulary.setUriString(URI);
    vocabulary.setTitle("Establishment Means");
    vocabulary.setIssued(MARCH);
    // English, Spanish and Czech labels, a blank language that doesn't count, and an upper case code counted with es
    vocabulary.addConcept(concept("native", "A taxon occurring within its natural range.",
        List.of(term("native (indigenous)", "en"), term("nativa", "es"), term("nativní", "cs")),
        List.of(term("Native", "en"), term("autóctona", "es"), term("indigenous", null))));
    vocabulary.addConcept(concept("vagrant", null,
        List.of(term("vagrant (casual)", "en"), term("vagante", "ES"), term("x", " ")),
        List.of()));
    // only English: shown in English in a Spanish interface
    vocabulary.addConcept(concept("uncertain", null, List.of(term("uncertain", "en")), List.of()));
    // no labels at all
    vocabulary.addConcept(concept("unknown", null, List.of(), List.of()));

    vocabManager = mock(VocabulariesManager.class);
    when(vocabManager.get(URI)).thenReturn(vocabulary);
    when(vocabManager.isDefault(URI)).thenReturn(false);

    // two extensions use the vocabulary (listed out of title order), one uses another vocabulary
    Vocabulary otherVocabulary = new Vocabulary();
    otherVocabulary.setUriString("http://rs.gbif.org/vocabulary/gbif/sex");
    Extension occurrence = extension("Occurrence", "http://rs.tdwg.org/dwc/terms/Occurrence",
        property("establishmentMeans", vocabulary), property("sex", otherVocabulary), property("degreeOfEstablishment", null));
    Extension distribution = extension("distribution", "http://rs.gbif.org/terms/1.0/Distribution",
        property("establishmentMeans", vocabulary), property("pathway", vocabulary));
    Extension unrelated = extension("Multimedia", "http://rs.gbif.org/terms/1.0/Multimedia", property("type", otherVocabulary));
    extensionManager = mock(ExtensionManager.class);
    when(extensionManager.list()).thenReturn(List.of(occurrence, unrelated, distribution));

    registryManager = mock(RegistryManager.class);
  }

  @Test
  public void testUnknownVocabulary() throws Exception {
    VocabulariesAction action = action("http://example.org/nope");
    assertEquals(VocabulariesAction.NOT_FOUND, action.execute());
    assertEquals(VocabulariesAction.NOT_FOUND, action.admin());
    verify(registryManager, never()).getVocabularies();
  }

  @Test
  public void testNoId() throws Exception {
    // vocabulary.do without an id: not found, not an error page
    VocabulariesAction action = action(null);
    assertEquals(VocabulariesAction.NOT_FOUND, action.execute());
    assertEquals(VocabulariesAction.NOT_FOUND, action.admin());
    assertNull(action.getVocabulary());
    verify(registryManager, never()).getVocabularies();
  }

  @Test
  public void testUsedBy() throws Exception {
    VocabulariesAction action = action(URI);
    assertEquals("success", action.execute());

    // by title, ignoring case; only the terms using this vocabulary; the extension not using it left out
    List<String> usedBy = action.getUsedBy().stream()
        .map(u -> u.getExtension().getTitle() + ":"
            + String.join(",", u.getTerms().stream().map(ExtensionProperty::getName).toList()))
        .toList();
    assertEquals(List.of("distribution:establishmentMeans,pathway", "Occurrence:establishmentMeans"), usedBy);
  }

  @Test
  public void testLabelLanguages() throws Exception {
    VocabulariesAction action = action(URI);
    action.execute();

    // by language name in the interface language (English here): Czech, English, Spanish; "ES" counted with "es",
    // blank languages ignored
    assertEquals(List.of("cs", "en", "es"), new ArrayList<>(action.getLabelLanguages().keySet()));
    assertEquals(1, action.getLabelLanguages().get("cs"));
    assertEquals(3, action.getLabelLanguages().get("en"));
    assertEquals(2, action.getLabelLanguages().get("es"));
  }

  @Test
  public void testConceptTextInInterfaceLanguage() throws Exception {
    VocabulariesAction action = action(URI, "es");
    action.execute();
    VocabularyConcept nativeConcept = vocabulary.getConcepts().get(0);
    VocabularyConcept vagrant = vocabulary.getConcepts().get(1);
    VocabularyConcept uncertain = vocabulary.getConcepts().get(2);
    VocabularyConcept unknown = vocabulary.getConcepts().get(3);

    // the Spanish label, matched ignoring case; else English, flagged as a fallback; else none
    assertEquals("nativa", action.labelOf(nativeConcept));
    assertEquals("vagante", action.labelOf(vagrant));
    assertFalse(action.isEnglishFallback(vagrant));
    assertEquals("uncertain", action.labelOf(uncertain));
    assertTrue(action.isEnglishFallback(uncertain));
    assertNull(action.labelOf(unknown));
    assertFalse(action.isEnglishFallback(unknown));

    // Spanish and language-less alternatives, sorted
    assertEquals(List.of("autóctona", "indigenous"), action.alternativesOf(nativeConcept));

    // no language first, then by language and title
    assertEquals(List.of("indigenous", "Native", "autóctona"),
        action.byLanguage(nativeConcept.getAlternativeTerms()).stream().map(VocabularyTerm::getTitle).toList());

    // value, description and every label, lower case
    String search = action.searchTextOf(nativeConcept);
    assertTrue(search.startsWith("native a taxon occurring"));
    assertTrue(search.contains("nativní"));
    assertTrue(search.contains("autóctona"));
    assertFalse(search.contains("null"));
  }

  @Test
  public void testEnglishInterfaceHasNoFallback() throws Exception {
    VocabulariesAction action = action(URI);
    action.execute();
    VocabularyConcept uncertain = vocabulary.getConcepts().get(2);
    assertEquals("uncertain", action.labelOf(uncertain));
    assertFalse(action.isEnglishFallback(uncertain));
    // in English only English and language-less alternatives
    assertEquals(List.of("indigenous", "Native"), action.alternativesOf(vocabulary.getConcepts().get(0)));
  }

  @Test
  public void testManagePageDoesNotCheckVersion() throws Exception {
    VocabulariesAction action = action(URI);
    assertEquals("success", action.execute());
    verify(registryManager, never()).getVocabularies();
    assertFalse(action.isVersionChecked());
    assertFalse(action.isRegistryUnreachable());
  }

  @Test
  public void testAdminPageNewerVersion() throws Exception {
    vocabulary.setLatest(true);
    when(registryManager.getVocabularies()).thenReturn(List.of(registered(URI, MARCH, false), registered(URI, JULY, true)));
    VocabulariesAction action = action(URI);
    assertEquals("success", action.admin());
    assertTrue(action.isVersionChecked());
    assertFalse(vocabulary.isLatest());
    assertEquals(JULY, action.getNewerIssued());
  }

  @Test
  public void testAdminPageLatestVersion() throws Exception {
    // an installed version with no issued date isn't the latest when the registered one has one
    when(registryManager.getVocabularies()).thenReturn(List.of(registered(URI, MARCH, true)));
    VocabulariesAction action = action(URI);
    action.admin();
    assertTrue(vocabulary.isLatest());
    assertNull(action.getNewerIssued());

    vocabulary.setIssued(null);
    action = action(URI);
    action.admin();
    assertFalse(vocabulary.isLatest());
    assertEquals(MARCH, action.getNewerIssued());
  }

  @Test
  public void testAdminPageNotRegistered() throws Exception {
    // a vocabulary the Registry doesn't list counts as the latest
    vocabulary.setLatest(false);
    when(registryManager.getVocabularies()).thenReturn(List.of(registered("http://example.org/other", JULY, true)));
    VocabulariesAction action = action(URI);
    action.admin();
    assertTrue(action.isVersionChecked());
    assertTrue(vocabulary.isLatest());
    assertNull(action.getNewerIssued());
  }

  @Test
  public void testAdminPageRegistryUnreachable() throws Exception {
    vocabulary.setLatest(false);
    when(registryManager.getVocabularies())
        .thenThrow(new RegistryException(RegistryException.Type.IO_ERROR, "http://registry", "down"));
    VocabulariesAction action = action(URI);
    // the page still shows
    assertEquals("success", action.admin());
    assertTrue(action.isRegistryUnreachable());
    assertFalse(action.isVersionChecked());
    // the shared vocabulary keeps its state from the last successful check
    assertFalse(vocabulary.isLatest());
    assertNull(action.getNewerIssued());
  }

  private VocabulariesAction action(String id) {
    return action(id, "en");
  }

  private VocabulariesAction action(String id, String language) {
    VocabulariesAction action = new VocabulariesAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), vocabManager, extensionManager, registryManager) {
      @Override
      public String getLocaleLanguage() {
        return language;
      }

      @Override
      public Locale getLocale() {
        return Locale.ENGLISH;
      }
    };
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("id")).thenReturn(id);
    action.withServletRequest(req);
    action.prepare();
    return action;
  }

  private static VocabularyConcept concept(String identifier, String description, List<VocabularyTerm> preferred,
                                           List<VocabularyTerm> alternative) {
    VocabularyConcept c = new VocabularyConcept();
    c.setIdentifier(identifier);
    c.setDescription(description);
    preferred.forEach(c::addPreferredTerm);
    alternative.forEach(c::addAlternativeTerm);
    return c;
  }

  private static VocabularyTerm term(String title, String lang) {
    VocabularyTerm t = new VocabularyTerm();
    t.setTitle(title);
    t.setLang(lang);
    return t;
  }

  private static Extension extension(String title, String rowType, ExtensionProperty... properties) {
    Extension e = new Extension();
    e.setTitle(title);
    e.setRowType(rowType);
    for (ExtensionProperty p : properties) {
      e.addProperty(p);
    }
    return e;
  }

  private static ExtensionProperty property(String name, Vocabulary vocabulary) {
    ExtensionProperty p = new ExtensionProperty("http://rs.tdwg.org/dwc/terms/" + name);
    // the constructor sets the qualified name only; the extension parser sets the name too
    p.setName(name);
    p.setVocabulary(vocabulary);
    return p;
  }

  private static Vocabulary registered(String uri, Date issued, boolean latest) {
    Vocabulary v = new Vocabulary();
    v.setUriString(uri);
    v.setIssued(issued);
    v.setLatest(latest);
    return v;
  }
}
