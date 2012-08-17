package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ExtensionFactoryTest;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
    ExtensionManager mockExtensionManager = mock(ExtensionManager.class);
    SourceManager mockSourceManager = mock(SourceManager.class);
    VocabulariesManager mockVocabManager = mock(VocabulariesManager.class);
    TranslationAction.Translation translation = new TranslationAction.Translation();

    // mock getting list of values back for BasisOfRecord field/column in source
    Set<String> values = new LinkedHashSet<String>();
    values.add("spe");
    values.add("obs");
    values.add("fos");
    when(mockSourceManager.inspectColumn(any(Source.class), anyInt(), anyInt(), anyInt())).thenReturn(values);

    // create mock Action
    action = new TranslationAction(mockTextProvider, mockCfg, mockResourceManager, mockSourceManager, mockVocabManager,
      translation);

    // initialize new Resource
    Resource resource = new Resource();

    // initialize new ExtensionMapping
    ExtensionMapping mapping = new ExtensionMapping();
    // add source to mapping
    mapping.setSource(new Source.FileSource());

    ExtensionFactory factory = ExtensionFactoryTest.getFactory();
    Extension e = factory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc-core-occurrence.xml"));
    // ensure rowType for Extension is set
    if (e.getRowType() == null) {
      e.setRowType(Constants.DWC_ROWTYPE_TAXON);
    }
    // add extension to ExtensionMapping
    mapping.setExtension(e);

    // create set of translations
    TreeMap<String, String> translations = new TreeMap<String, String>();
    translations.put("spe", "specimen");
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
    Set<PropertyMapping> fields = new HashSet<PropertyMapping>();
    fields.add(field);
    mapping.setFields(fields);

    // add ExtensionMapping to resource, with mapping ID 0
    List<ExtensionMapping> mappings = new LinkedList<ExtensionMapping>();
    mappings.add(mapping);
    resource.setMappings(mappings);

    // initialize ExtensionProperty representing BasisOfRecord field on Occurrence core Extension
    ExtensionProperty property = mapping.getExtension().getProperty(field.getTerm());

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
    action.setServletRequest(mockRequest);

    // ensure the resource is set
    action.setResource(resource);
  }

  @Test
  public void testDelete() throws SAXException, ParserConfigurationException, IOException {
    // check there are 2 sessionScoped translations
    assertEquals(2, action.getTrans().getTmap().size());
    assertEquals("observation", action.getTrans().getTmap().get("obs"));
    // check the PropertyMapping (field) translations are not empty
    assertFalse(action.getField().getTranslation().isEmpty());
    // perform deletion
    action.delete();
    // check 1. there are 3 key-only sessionScoped translations (represent values read from source with no translations
    assertEquals(3, action.getTrans().getTmap().size());
    assertEquals("fos", action.getTrans().getTmap().firstKey());
    // check 2. the PropertyMapping (field) translations are empty
    assertTrue(action.getField().getTranslation().isEmpty());
  }

}
