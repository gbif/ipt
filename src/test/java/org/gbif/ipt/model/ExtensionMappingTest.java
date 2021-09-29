package org.gbif.ipt.model;

import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtensionMappingTest {

  @Test
  public void testFieldsOrderedAlphabetically() {
    Set<PropertyMapping> fields = new TreeSet<PropertyMapping>();

    // initialize PropertyMapping for DwC month term
    PropertyMapping field0 = new PropertyMapping();
    field0.setTerm(DwcTerm.month);
    field0.setIndex(0);
    fields.add(field0);

    // initialize PropertyMapping for DwC BasisOfRecord term
    PropertyMapping field = new PropertyMapping();
    field.setTerm(DwcTerm.basisOfRecord);
    field.setIndex(1);
    fields.add(field);

    // initialize PropertyMapping for DwC CollectionCode term
    PropertyMapping field2 = new PropertyMapping();
    field2.setTerm(DwcTerm.collectionCode);
    field2.setIndex(2);
    fields.add(field2);

    // initialize PropertyMapping for DwC CatalogNumber term
    PropertyMapping field3 = new PropertyMapping();
    field3.setTerm(DwcTerm.catalogNumber);
    field3.setIndex(3);
    fields.add(field3);

    // initialize PropertyMapping for DwC CatalogNumber term
    PropertyMapping field4 = new PropertyMapping();
    field4.setTerm(DwcTerm.acceptedNameUsage);
    field4.setIndex(4);
    fields.add(field4);

    // initialize PropertyMapping for DC Modified term
    PropertyMapping field5 = new PropertyMapping();
    field5.setTerm(DcTerm.modified);
    field5.setIndex(5);
    fields.add(field5);

    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setFields(fields);

    assertEquals(6, mapping.getFields().size());
    // http://purl.org/dc/terms/modified comes before http://rs.tdwg.org/dwc/terms/...
    assertEquals(DcTerm.modified, ((PropertyMapping)mapping.getFields().toArray()[0]).getTerm());
    assertEquals(DwcTerm.acceptedNameUsage, ((PropertyMapping)mapping.getFields().toArray()[1]).getTerm());
    assertEquals(DwcTerm.basisOfRecord, ((PropertyMapping)mapping.getFields().toArray()[2]).getTerm());
    assertEquals(DwcTerm.catalogNumber, ((PropertyMapping)mapping.getFields().toArray()[3]).getTerm());
    assertEquals(DwcTerm.collectionCode, ((PropertyMapping)mapping.getFields().toArray()[4]).getTerm());
    assertEquals(DwcTerm.month, ((PropertyMapping)mapping.getFields().toArray()[5]).getTerm());
  }
}
