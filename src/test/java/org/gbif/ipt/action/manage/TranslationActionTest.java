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
package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.SourceBase;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ExtensionFactoryTest;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.struts2.inject.Container;
import org.apache.struts2.locale.DefaultLocaleProviderFactory;
import org.apache.struts2.locale.LocaleProviderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslationActionTest extends IptBaseTest {

  TranslationAction action;
  SourceManager mockSourceManager;
  HttpServletRequest mockRequest;
  PropertyMapping field;

  @BeforeEach
  public void setup() throws Exception {
    // mock needed managers
    SimpleTextProvider mockTextProvider = mock(SimpleTextProvider.class);
    LocaleProviderFactory localeProviderFactory = new DefaultLocaleProviderFactory();
    AppConfig mockCfg = mock(AppConfig.class);
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    mockSourceManager = mock(SourceManager.class);
    VocabulariesManager mockVocabManager = mock(VocabulariesManager.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    Container container = mock(Container.class);

    // mock getting list of values back for BasisOfRecord field/column in source
    mockSourceValues("spe", "obs", "fos");

    // mock getI18nVocab - only called in prepare()
    Map<String, String> mockVocab = new HashMap<>();
    mockVocab.put("NomenclaturalChecklist", "Nomenclatural Checklist");
    mockVocab.put("MachineObservation", "Machine Observation");
    when(mockVocabManager.getI18nVocab(anyString(), anyString(), anyBoolean())).thenReturn(mockVocab);

    // initialize new Resource
    Resource resource = new Resource();
    String resourceShortName = "TestResource";
    resource.setShortname(resourceShortName);

    // initialize new ExtensionMapping
    ExtensionMapping mapping = new ExtensionMapping();
    // add source to mapping
    mapping.setSource(new TextFileSource());

    ExtensionFactory factory = ExtensionFactoryTest.getFactory();
    Extension e = factory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc_occurrence.xml"));
    // ensure rowType for Extension is set
    if (e.getRowType() == null) {
      e.setRowType(Constants.DWC_ROWTYPE_TAXON);
    }
    // add extension to ExtensionMapping
    mapping.setExtension(e);

    // create map of translations that get persisted
    Map<String, String> persistedTranslations = new HashMap<>();
    persistedTranslations.put("spe", "Preserved Specimen");
    persistedTranslations.put("obs", "observation");

    // initialize PropertyMapping for BasisOfRecord term
    field = new PropertyMapping();
    // set ConceptTerm
    field.setTerm(DwcTerm.basisOfRecord);
    // set index
    field.setIndex(1);
    // add translations to field
    field.setTranslation(persistedTranslations);
    // add set of PropertyMapping, including field, to ExtensionMapping
    Set<PropertyMapping> fields = new TreeSet<>();
    fields.add(field);
    mapping.setFields(fields);

    // add ExtensionMapping to resource, with mapping ID 0
    List<ExtensionMapping> mappings = new LinkedList<>();
    mappings.add(mapping);
    resource.setMappings(mappings);

    // mock resourceManager.get - called only in ManagerBaseAction.prepare()
    when(mockResourceManager.get(anyString())).thenReturn(resource);

    // mock a locale provider
    when(container.getInstance(LocaleProviderFactory.class)).thenReturn(localeProviderFactory);

    // mock textProvider getTexts
    ResourceBundle mockResourceBundle = mock(ResourceBundle.class);
    when(mockTextProvider.getTexts(any())).thenReturn(mockResourceBundle);
    when(mockResourceBundle.getLocale()).thenReturn(Locale.ENGLISH);

    // create mock Action
    action =
      new TranslationAction(mockTextProvider, mockCfg, mockRegistrationManager, mockResourceManager, mockSourceManager,
        mockVocabManager);
    action.setContainer(container);

    // empty session params
    action.withSession(new HashMap<>());

    // initialize ExtensionProperty representing BasisOfRecord field on Occurrence core Extension
    ExtensionProperty property = mapping.getExtension().getProperty(field.getTerm());

    // set a vocabulary for the BasisOfRecord field
    // mock creation of BasisOfRecord vocabulary
    VocabularyConcept concept = new VocabularyConcept();
    concept.setIdentifier("PreservedSpecimen");
    concept.setUri("http://rs.tdwg.org/dwc/dwctype/PreservedSpecimen");

    // preferred titles
    Set<VocabularyTerm> preferredTerms = new HashSet<>();
    VocabularyTerm term = new VocabularyTerm();
    term.setLang("en");
    term.setTitle("Preserved Specimen");
    preferredTerms.add(term);

    concept.setPreferredTerms(preferredTerms);

    // alternative titles
    Set<VocabularyTerm> alternateTerms = new HashSet<>();
    term = new VocabularyTerm();
    term.setLang("en");
    term.setTitle("Conserved Specimen");
    alternateTerms.add(term);

    concept.setAlternativeTerms(alternateTerms);

    Vocabulary vocab = new Vocabulary();
    List<VocabularyConcept> concepts = new ArrayList<>();
    concepts.add(concept);

    vocab.setConcepts(concepts);
    vocab.setUriString("http://rs.gbif.org/vocabulary/dwc/basis_of_record");
    property.setVocabulary(vocab);

    // mock servlet request
    mockRequest = mock(HttpServletRequest.class);
    // the mapping id is 0 - relates to resource's List<ExtensionMapping> mappings
    when(mockRequest.getParameter(TranslationAction.REQ_PARAM_MAPPINGID)).thenReturn("0");
    when(mockRequest.getParameter(TranslationAction.REQ_PARAM_ROWTYPE)).thenReturn(Constants.DWC_ROWTYPE_OCCURRENCE);
    when(mockRequest.getParameter(TranslationAction.REQ_PARAM_TERM)).thenReturn(DwcTerm.basisOfRecord.qualifiedName());
    when(mockRequest.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(resourceShortName);
    when(mockRequest.getMethod()).thenReturn("GET");
    action.withServletRequest(mockRequest);

    // ensure the resource is set
    action.setResource(resource);
  }

  private void mockSourceValues(String... values) throws Exception {
    Set<String> set = new LinkedHashSet<>(List.of(values));
    when(mockSourceManager.inspectColumn(any(SourceBase.class), anyInt(), anyInt(), anyInt())).thenReturn(set);
  }

  /**
   * @return key of the row with the source value
   */
  private String keyOf(String sourceValue) {
    return action.getSourceValuesMap().entrySet().stream()
        .filter(e -> e.getValue().equals(sourceValue))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElseThrow();
  }

  @Test
  public void testPrepare() {
    action.prepare();
    assertEquals("0", String.valueOf(action.getMid()));
    assertEquals(DwcTerm.basisOfRecord.qualifiedName(), action.getProperty().getQualname());
    assertEquals(2, action.getVocabTerms().size());
    // sorted
    assertEquals(List.of("fos", "obs", "spe"), new ArrayList<>(action.getSourceValuesMap().values()));
    assertEquals(2, action.getTmap().size());
    assertEquals("Preserved Specimen", action.getTmap().get(keyOf("spe")));
    assertEquals("observation", action.getTmap().get(keyOf("obs")));
    assertFalse(action.isNotInSource(keyOf("spe")));
    assertTrue(action.getActionMessages().isEmpty());
  }

  @Test
  public void testPrepareKeepsTranslationsNotInSource() throws Exception {
    mockSourceValues("spe");
    action.prepare();

    assertEquals(List.of("obs", "spe"), new ArrayList<>(action.getSourceValuesMap().values()));
    assertEquals(2, action.getTmap().size());
    assertTrue(action.isNotInSource(keyOf("obs")));
    assertFalse(action.isNotInSource(keyOf("spe")));
  }

  @Test
  public void testPrepareLimitsDistinctValues() throws Exception {
    String[] values = new String[ValueTranslation.MAX_DISTINCT_VALUES + 1];
    for (int i = 0; i < values.length; i++) {
      values[i] = "v" + i;
    }
    mockSourceValues(values);
    action.prepare();

    assertTrue(action.getTranslation().isTruncated());
    assertEquals(ValueTranslation.MAX_DISTINCT_VALUES, action.getTranslation().getSourceValueCount());
    // the 2 saved translations are kept
    assertEquals(ValueTranslation.MAX_DISTINCT_VALUES + 2, action.getSourceValuesMap().size());
    assertEquals(1, action.getActionWarnings().size());
  }

  @Test
  public void testReload() {
    action.prepare();
    action.reload();

    assertEquals(3, action.getSourceValuesMap().size());
    // reloading source doesn't change the translated values, or persisted translations on the field itself
    assertEquals(2, action.getTmap().size());
    assertEquals(2, action.getField().getTranslation().size());
    assertEquals(1, action.getActionMessages().size());
  }

  @Test
  public void testDelete() {
    when(mockRequest.getMethod()).thenReturn("POST");
    action.prepare();
    action.delete();

    assertTrue(action.getField().getTranslation().isEmpty());
  }

  @Test
  public void testSaveTranslations() {
    when(mockRequest.getMethod()).thenReturn("POST");
    action.prepare();
    // the source isn't read for a submitted form
    assertTrue(action.getSourceValuesMap().isEmpty());

    // pretend these are the rows coming in from the UI, in a different order than when the page was shown
    action.getSmap().put("k2", "spe");
    action.getSmap().put("k1", "obs");
    action.getSmap().put("n1", "liv");
    action.getSmap().put("k3", "zoo");
    action.getTmap().put("k2", "specimen");
    action.getTmap().put("k1", " observation ");
    action.getTmap().put("n1", "livingSpecimen");
    action.getTmap().put("k3", "");
    // translated value without source value is ignored
    action.getTmap().put("k9", "unknown");

    action.save();

    Map<String, String> saved = action.getField().getTranslation();
    assertEquals(4, saved.size());
    assertEquals("specimen", saved.get("spe"));
    assertEquals("observation", saved.get("obs"));
    assertEquals("livingSpecimen", saved.get("liv"));
    assertEquals("", saved.get("zoo"));
  }

  @Test
  public void testAutoMap() throws Exception {
    mockSourceValues("PreservedSpecimen", "Preserved Specimen", "Conserved Specimen", "preserved_specimen",
        "Preserved-Specimen", "Unknown", "obs", "spe");
    field.setTranslation(Map.of("obs", "HumanObservation", "spe", "PreservedSpecimen"));
    action.prepare();

    action.automap();

    // matches on concept, preferred title, alternative title, and ignoring underscores and hyphens
    assertEquals("PreservedSpecimen", action.getTmap().get(keyOf("PreservedSpecimen")));
    assertEquals("PreservedSpecimen", action.getTmap().get(keyOf("Preserved Specimen")));
    assertEquals("PreservedSpecimen", action.getTmap().get(keyOf("Conserved Specimen")));
    assertEquals("PreservedSpecimen", action.getTmap().get(keyOf("preserved_specimen")));
    assertEquals("PreservedSpecimen", action.getTmap().get(keyOf("Preserved-Specimen")));
    assertFalse(action.getTmap().containsKey(keyOf("Unknown")));
    // existing translations are kept
    assertEquals("HumanObservation", action.getTmap().get(keyOf("obs")));
    assertEquals(7, action.getTmap().size());
  }

  /**
   * A mapping id that isn't a number matches no mapping: reload and automap return a 404.
   */
  @Test
  public void testInvalidMappingId() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter(TranslationAction.REQ_PARAM_MAPPINGID)).thenReturn("abc");
    when(req.getParameter(TranslationAction.REQ_PARAM_ROWTYPE)).thenReturn(Constants.DWC_ROWTYPE_OCCURRENCE);
    when(req.getParameter(TranslationAction.REQ_PARAM_TERM)).thenReturn(DwcTerm.basisOfRecord.qualifiedName());
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn("TestResource");
    when(req.getMethod()).thenReturn("GET");
    action.withServletRequest(req);

    action.prepare();
    assertEquals(BaseAction.NOT_FOUND, action.reload());
    assertEquals(BaseAction.NOT_FOUND, action.automap());
  }
}
