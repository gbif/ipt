package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ExtensionFactoryTest;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TranslationActionTest {

  @Test
  public void testDelete() throws SAXException, ParserConfigurationException, IOException {
    ExtensionMapping mapping = new ExtensionMapping();

    ExtensionFactory factory = ExtensionFactoryTest.getFactory();
    Extension e = factory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc-core-extension.xml"));

    mapping.setExtension(e);

    PropertyMapping field = new PropertyMapping();
    field.setTerm(DwcTerm.parentNameUsage);
    Set<PropertyMapping> fields = new HashSet<PropertyMapping>();
    fields.add(field);
    mapping.setFields(fields);

    ExtensionProperty property = mapping.getExtension().getProperty(field.getTerm());

    TranslationAction.Translation trans = new TranslationAction.Translation();

    trans.setTmap(mapping.getExtension().getRowType(), property, new TreeMap<String, String>());

    trans.getTmap().put("spe", "specimen");
    trans.getTmap().put("obs", "observation");

    field.setTranslation(trans.getTmap());
    assertFalse(field.getTranslation().entrySet().isEmpty());

    field.getTranslation().clear();
    assertTrue(field.getTranslation().entrySet().isEmpty());
  }

}
