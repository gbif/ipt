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

import java.io.IOException;
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
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslationActionTest {

  TranslationAction action;

  @Before
  public void setup() throws SAXException, ParserConfigurationException, IOException {
    // mock needed managers
    SimpleTextProvider mockTextProvider = mock(SimpleTextProvider.class);
    AppConfig mockCfg = mock(AppConfig.class);
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    SourceManager mockSourceManager = mock(SourceManager.class);
    VocabulariesManager mockVocabManager = mock(VocabulariesManager.class);
    TranslationAction.Translation translation = new TranslationAction.Translation();
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);

    // mock getting list of values back for BasisOfRecord field/column in source
    Set<String> values = new LinkedHashSet<String>();
    values.add("spe");
    values.add("obs");
    values.add("fos");
    when(mockSourceManager.inspectColumn(any(SourceBase.class), anyInt(), anyInt(), anyInt())).thenReturn(values);

    // mock getI18nVocab - only called in prepare()
    Map<String, String> mockVocab = new HashMap<String, String>();
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

    // create set of translations
    TreeMap<String, String> translations = new TreeMap<String, String>();
    translations.put("spe", "Preserved Specimen");
    translations.put("obs", "observation");

    // initialize PropertyMapping for BasisOfRecord term
    PropertyMapping field = new PropertyMapping();
    // set ConceptTerm
    field.setTerm(DwcTerm.basisOfRecord);
    // set index
    field.setIndex(1);
    // add translations to field
    field.setTranslation(translations);
    // add set of PropertyMapping, including field, to ExtensionMapping
    Set<PropertyMapping> fields = new TreeSet<PropertyMapping>();
    fields.add(field);
    mapping.setFields(fields);

    // add ExtensionMapping to resource, with mapping ID 0
    List<ExtensionMapping> mappings = new LinkedList<ExtensionMapping>();
    mappings.add(mapping);
    resource.setMappings(mappings);

    // mock resourceManager.get - called only in ManagerBaseAction.prepare()
    when(mockResourceManager.get(anyString())).thenReturn(resource);

    // create mock Action
    action =
      new TranslationAction(mockTextProvider, mockCfg, mockRegistrationManager, mockResourceManager, mockSourceManager,
        mockVocabManager, translation);

    // initialize ExtensionProperty representing BasisOfRecord field on Occurrence core Extension
    ExtensionProperty property = mapping.getExtension().getProperty(field.getTerm());

    // set a vocabulary for the BasisOfRecord field
    // mock creation of BasisOfRecord vocabulary
    VocabularyConcept concept = new VocabularyConcept();
    concept.setIdentifier("PreservedSpecimen");
    concept.setUri("http://rs.tdwg.org/dwc/dwctype/PreservedSpecimen");

    // preferred titles
    Set<VocabularyTerm> preferredTerms = new HashSet<VocabularyTerm>();
    VocabularyTerm term = new VocabularyTerm();
    term.setLang("en");
    term.setTitle("Preserved Specimen");
    preferredTerms.add(term);

    concept.setPreferredTerms(preferredTerms);

    // alternative titles
    Set<VocabularyTerm> alternateTerms = new HashSet<VocabularyTerm>();
    term = new VocabularyTerm();
    term.setLang("en");
    term.setTitle("Conserved Specimen");
    alternateTerms.add(term);

    concept.setAlternativeTerms(alternateTerms);

    Vocabulary vocab = new Vocabulary();
    List<VocabularyConcept> concepts = new ArrayList<VocabularyConcept>();
    concepts.add(concept);

    vocab.setConcepts(concepts);
    vocab.setUriString("http://rs.gbif.org/vocabulary/dwc/basis_of_record");
    property.setVocabulary(vocab);

    // create sessionScoped Translation
    // populate sessionScoped Translation with translations
    action.getTrans().setTmap(mapping.getExtension().getRowType(), property, translations);

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
  public void testDelete() throws SAXException, ParserConfigurationException, IOException {
    // check there are 2 sessionScoped and PropertyMapping (field) translations
    assertEquals(2, action.getTrans().getTmap().size());
    assertEquals(2, action.getField().getTranslation().size());

    // perform deletion
    action.delete();

    // check 1. there are 3 key-only sessionScoped translations (represent values read from source with no translations
    assertEquals(3, action.getTrans().getTmap().size());
    for (String val : action.getTrans().getTmap().values()) {
      assertNull(val);
    }

    // check 2. the PropertyMapping (field) translations are empty
    assertTrue(action.getField().getTranslation().isEmpty());
  }

  @Test
  public void testReload() throws SAXException, ParserConfigurationException, IOException {
    // check there are 2 sessionScoped and PropertyMapping (field) translations
    assertEquals(2, action.getTrans().getTmap().size());
    assertEquals(2, action.getField().getTranslation().size());

    // perform reload
    action.reload();

    // check an additional sessionScoped translation was read from source (added to tmap)
    assertEquals(3, action.getTrans().getTmap().size());

    // reloading source doesn't change the number of translations on the field itself
    assertEquals(2, action.getField().getTranslation().size());
  }

  @Test
  public void testSave() throws IOException {
    // create new set of 5 translations
    TreeMap<String, String> translations = new TreeMap<String, String>();
    translations.put("spe", "specimen");
    translations.put("obs", "observation");
    translations.put("liv", "livingSpecimen");
    translations.put("mac", "machineObservation");
    translations.put("zoo", "");
    // pretend this is the list of translations coming in from the UI
    action.getTrans().setTmap(Constants.DWC_ROWTYPE_TAXON, DwcTerm.basisOfRecord, translations);
    // perform the save
    action.save();
    // ensure it has been saved to the PropertyMapping (field)
    // only 4 should be present, since those translations with empty string values get removed
    assertEquals(4, action.getField().getTranslation().entrySet().size());
  }

  @Test
  public void testAutoMap() throws IOException {
    // create new set of translations that haven't been mapped yet
    TreeMap<String, String> translations = new TreeMap<String, String>();
    // will match vocab on concept
    translations.put("PreservedSpecimen", null);
    // will match vocab on preferred title
    translations.put("Preserved Specimen", null);
    // will match vocab on alternative title
    translations.put("Conserved Specimen", null);
    // will not match vocab on anything
    translations.put("Unknown", null);
    action.getTrans().setTmap(Constants.DWC_ROWTYPE_TAXON, DwcTerm.basisOfRecord, translations);
    // perform auto mapping
    action.automap();
    // assert there are 4 translations still in total
    assertEquals(4, action.getTrans().getTmap().keySet().size());
    // assert 3 have been auto-mapped (removing any null mapping values) and they are all equal to the vocab identifier
    action.getTrans().getTmap().values().remove(null);
    assertEquals(3, action.getTrans().getTmap().keySet().size());
    for (String val : action.getTrans().getTmap().values()) {
      assertEquals("PreservedSpecimen", val);
    }
  }

  @Test
  public void testPrepare() throws Exception {
    action.prepare();
    assertEquals("0", String.valueOf(action.getMid()));
    assertEquals(DwcTerm.basisOfRecord.qualifiedName(), action.getProperty().getQualname());
    assertEquals(2, action.getVocabTerms().size());
    assertEquals(3, action.getTrans().getTmap().size());
  }

  @Test
  public void testAcceptedParamNames() {
    // IPT accepted parameter names
    // can have alpha-numeric characters plus  ":", ";", ".", " ", "-"
    assertTrue(action.acceptableParameterName("tmap['Valid']"));
    assertTrue(action.acceptableParameterName("tmap['Vali:d']"));
    assertTrue(action.acceptableParameterName("tmap['Valid.']"));
    assertTrue(action.acceptableParameterName("tmap['within 6-20 km']"));
    assertTrue(action.acceptableParameterName("tmap['within 5 km;']"));
    assertTrue(action.acceptableParameterName("tmap['1969-02-22 00:00:00.0']"));

    // IPT non-accepted parameter names
    // cannot have malicious characters such as "<", ">", "&", """, "%", "'", "="
    assertFalse(action.acceptableParameterName("tmap['<Valid>']"));
    assertFalse(action.acceptableParameterName("tmap['Valid & valid']"));
    assertFalse(action.acceptableParameterName("tmap['Valid \"valid\" valid']"));
    assertFalse(action.acceptableParameterName("tmap['Valid%valid']"));
    assertFalse(action.acceptableParameterName("tmap['Valid=4']"));
  }

  @Test
  public void testStripBrackets() {
     assertEquals("Valid", action.stripBrackets("tmap['Valid']"));
  }
}
