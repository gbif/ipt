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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.opensymphony.xwork2.DefaultLocaleProviderFactory;
import com.opensymphony.xwork2.LocaleProviderFactory;
import com.opensymphony.xwork2.inject.Container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslationActionTest {

  TranslationAction action;

  @BeforeEach
  public void setup() throws Exception {
    // mock needed managers
    SimpleTextProvider mockTextProvider = mock(SimpleTextProvider.class);
    LocaleProviderFactory localeProviderFactory = new DefaultLocaleProviderFactory();
    AppConfig mockCfg = mock(AppConfig.class);
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    SourceManager mockSourceManager = mock(SourceManager.class);
    VocabulariesManager mockVocabManager = mock(VocabulariesManager.class);
    TranslationAction.Translation translation = new TranslationAction.Translation();
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    Container container = mock(Container.class);

    // mock getting list of values back for BasisOfRecord field/column in source
    Set<String> values = new LinkedHashSet<>();
    values.add("spe");
    values.add("obs");
    values.add("fos");
    when(mockSourceManager.inspectColumn(any(SourceBase.class), anyInt(), anyInt(), anyInt())).thenReturn(values);

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

    // create map of source value
    TreeMap<String, String> sourceValues = new TreeMap<>();
    sourceValues.put("k1", "spe");
    sourceValues.put("k2", "obs");

    // create map of translation values
    TreeMap<String, String> translatedValues = new TreeMap<>();
    translatedValues.put("k1", "Preserved Specimen");
    translatedValues.put("k2", "observation");

    // create map of translations that get persisted
    Map<String, String> persistedTranslations = new HashMap<>();
    persistedTranslations.put("spe", "Preserved Specimen");
    persistedTranslations.put("obs", "observation");

    // initialize PropertyMapping for BasisOfRecord term
    PropertyMapping field = new PropertyMapping();
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

    // create mock Action
    action =
      new TranslationAction(mockTextProvider, mockCfg, mockRegistrationManager, mockResourceManager, mockSourceManager,
        mockVocabManager, translation);
    action.setContainer(container);

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

    // create sessionScoped Translation
    // populate sessionScoped Translation with translations
    action.getTrans().setTmap(mapping.getExtension().getRowType(), property, sourceValues, translatedValues);

    // set various properties on Action
    action.setField(field);
    action.setExtensionMapping(mapping);
    action.setProperty(property);

    // mock servlet request
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    // the mapping id is 0 - relates to resource's List<ExtensionMapping> mappings
    when(mockRequest.getParameter(TranslationAction.REQ_PARAM_MAPPINGID)).thenReturn("0");
    when(mockRequest.getParameter(TranslationAction.REQ_PARAM_ROWTYPE)).thenReturn(Constants.DWC_ROWTYPE_OCCURRENCE);
    when(mockRequest.getParameter(TranslationAction.REQ_PARAM_TERM)).thenReturn(DwcTerm.basisOfRecord.qualifiedName());
    when(mockRequest.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(resourceShortName);
    action.setServletRequest(mockRequest);

    // ensure the resource is set
    action.setResource(resource);
  }

  @Test
  public void testDelete() {
    // check there are 2 sessionScoped source values, 2 sessionScoped translated values, and the
    // 2 PropertyMapping (field) translations
    assertEquals(2, action.getTrans().getSourceValues().size());
    assertEquals(2, action.getTrans().getTranslatedValues().size());
    assertEquals(2, action.getField().getTranslation().size());

    // perform deletion
    action.delete();

    // check 1. there are 0 sessionScoped source values or translations - both get cleared
    assertEquals(0, action.getTrans().getTranslatedValues().size());

    // check 2. the PropertyMapping (field) translations are empty
    assertTrue(action.getField().getTranslation().isEmpty());

    // check 3. the 3 source values got reloaded
    assertEquals(3, action.getTrans().getSourceValues().size());
  }

  @Test
  public void testReload() {
    // check there are 2 sessionScoped and PropertyMapping (field) translations
    assertEquals(2, action.getTrans().getSourceValues().size());
    assertEquals(2, action.getField().getTranslation().size());

    // perform reload
    action.reload();

    // check an additional sessionScoped translation was read from source (added to source values)
    assertEquals(3, action.getTrans().getSourceValues().size());

    // reloading source doesn't change the number of translated values, or persisted translations on the field itself
    assertEquals(2, action.getTrans().getTranslatedValues().size());
    assertEquals(2, action.getField().getTranslation().size());
  }

  @Test
  public void testSaveTranslations() {
    // create new set of 5 translations
    TreeMap<String, String> sourceValues = new TreeMap<>();
    sourceValues.put("k1", "spe");
    sourceValues.put("k2", "obs");
    sourceValues.put("k3", "liv");
    sourceValues.put("k4", "mac");
    sourceValues.put("k5", "zoo");

    TreeMap<String, String> translatedValues = new TreeMap<>();
    translatedValues.put("k1", "specimen");
    translatedValues.put("k2", "observation");
    translatedValues.put("k3", "livingSpecimen");
    translatedValues.put("k4", "machineObservation");
    translatedValues.put("k5", "");

    // pretend this is the list of translations coming in from the UI
    action.getTrans().setTmap(Constants.DWC_ROWTYPE_TAXON, DwcTerm.basisOfRecord, sourceValues, translatedValues);
    // perform the save
    action.save();
    // ensure it has been saved to the PropertyMapping (field)
    // only 4 should be present, since those translations with empty string values get removed
    assertEquals(4, action.getField().getTranslation().entrySet().size());
  }

  @Test
  public void testAutoMap() {
    // create new map of source values that haven't been mapped yet
    TreeMap<String, String> sourceValues = new TreeMap<>();
    // will match vocab on concept
    sourceValues.put("k1", "PreservedSpecimen");
    // will match vocab on preferred title
    sourceValues.put("k2", "Preserved Specimen");
    // will match vocab on alternative title
    sourceValues.put("k3", "Conserved Specimen");
    // will not match vocab on anything
    sourceValues.put("k4", "Unknown");

    // create an empty map of translted values
    TreeMap<String, String> translatedValues = new TreeMap<>();

    action.getTrans().setTmap(Constants.DWC_ROWTYPE_TAXON, DwcTerm.basisOfRecord, sourceValues, translatedValues);

    // perform auto mapping
    action.automap();
    // assert there are 4 source values still in total
    assertEquals(4, action.getTrans().getSourceValues().size());
    // assert there are 3 auto-mapped translated values now in total, and they all auto-mapped to PreservedSpecimen
    assertEquals(3, action.getTrans().getTranslatedValues().size());
    for (String val : action.getTmap().values()) {
      assertEquals("PreservedSpecimen", val);
    }
  }

  @Test
  public void testPrepare() {
    action.prepare();
    assertEquals("0", String.valueOf(action.getMid()));
    assertEquals(DwcTerm.basisOfRecord.qualifiedName(), action.getProperty().getQualname());
    assertEquals(2, action.getVocabTerms().size());
    assertEquals(3, action.getTrans().getSourceValues().size());
    assertEquals(2, action.getTrans().getTranslatedValues().size());
  }
}
