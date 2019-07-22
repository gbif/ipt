package org.gbif.ipt.validation;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwca.io.ArchiveField;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ExtensionMappingValidatorTest {

  private List<String> columns;
  private ExtensionMapping extensionMapping;
  private Resource resource;
  private ExtensionMappingValidator validator;
  private List<String[]> peek = Lists.newArrayList();

  @Before
  public void setup() {
    // set small list of source column names representing a source file to be mapped
    columns = new ArrayList<String>();
    columns.add("identificationID");
    columns.add("identificationQualifier");
    columns.add("unknown");
    columns.add("occurrenceID");

    // create a new Extension, that represents the Darwin Core Occurrence Core
    Extension extension = new Extension();
    extension.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);
    List<ExtensionProperty> extensionProperties = new ArrayList<ExtensionProperty>();
    ExtensionProperty extensionProperty = new ExtensionProperty();
    extensionProperty.setQualname(DwcTerm.occurrenceID.qualifiedName());
    extensionProperties.add(extensionProperty);
    extension.setProperties(extensionProperties);

    // an ExtensionMapping to Extension Darwin Core Occurrence Core
    extensionMapping = new ExtensionMapping();
    extensionMapping.setExtension(extension);

    // 2 translated fields pointing at same source column
    Set<PropertyMapping> fields = Sets.newHashSet();

    PropertyMapping mappingCoreid = new PropertyMapping();
    mappingCoreid.setTerm(DwcTerm.occurrenceID);
    mappingCoreid.setIndex(0);
    fields.add(mappingCoreid);

    extensionMapping.setFields(fields);

    // Resource
    resource = new Resource();
    resource.setShortname("myResource");
    resource.addMapping(extensionMapping);

    validator = new ExtensionMappingValidator();
  }

  /**
   * A column cannot be translated multiple times.
   */
  @Test
  public void testValidateMultipleTranslationsForColumn() {
    // before updating fields to simulate multiple translations for same column, mapping validates
    ExtensionMappingValidator.ValidationStatus status = validator.validate(extensionMapping, resource, peek, columns);
    assertTrue(status.isValid());
    assertEquals(0, status.getMultipleTranslationsForSameColumn().size());

    // field translation
    Map<String, String> translation = Maps.newHashMap();
    translation.put("id1", "translated-id1");

    Set<PropertyMapping> fields = extensionMapping.getFields();

    PropertyMapping identificationId = new PropertyMapping();
    identificationId.setTerm(DwcTerm.identificationID);
    identificationId.setIndex(0);
    identificationId.setTranslation(translation);
    fields.add(identificationId);

    PropertyMapping identificationQualifier = new PropertyMapping();
    identificationQualifier.setTerm(DwcTerm.identificationQualifier);
    identificationQualifier.setIndex(0);
    identificationQualifier.setTranslation(translation);
    fields.add(identificationQualifier);

    status = validator.validate(extensionMapping, resource, peek, columns);
    assertFalse(status.isValid());
    assertEquals(1, status.getMultipleTranslationsForSameColumn().size());
  }

  /**
   * A non-core extension must have an ID column mapping.
   */
  @Test
  public void testMissingIDColumMappingForNonCoreExtension() {
    // Core Occurrence extension doesn't need id column mapped
    ExtensionMappingValidator.ValidationStatus status = validator.validate(extensionMapping, resource, peek, columns);
    assertTrue(status.isValid());
    assertNull(status.getIdProblem());

    // Distribution extension does need id column mapped
    Extension extension = extensionMapping.getExtension();
    extension.setRowType("http://rs.gbif.org/terms/1.0/Distribution");
    status = validator.validate(extensionMapping, resource, peek, columns);
    assertFalse(status.isValid());
    assertNotNull(status.getIdProblem());
  }

  /**
   * Required fields must be mapped.
   */
  @Test
  public void testRequiredFieldMapped() {
    // Core Occurrence extension doesn't need id column mapped
    ExtensionMappingValidator.ValidationStatus status = validator.validate(extensionMapping, resource, peek, columns);
    assertTrue(status.isValid());
    assertNull(status.getIdProblem());

    // add BasisOfRecord to Occurrence extension, and make it a required field
    List<ExtensionProperty> extensionProperties = extensionMapping.getExtension().getProperties();
    ExtensionProperty extensionProperty = new ExtensionProperty();
    extensionProperty.setQualname(DwcTerm.basisOfRecord.qualifiedName());
    extensionProperty.setRequired(true);
    extensionProperties.add(extensionProperty);

    status = validator.validate(extensionMapping, resource, peek, columns);
    assertFalse(status.isValid());
    assertEquals(1, status.getMissingRequiredFields().size());
  }

  /**
   * Fields that are non-string data types, must be mapped to value of correct type.
   */
  @Test
  public void testCorrectDataType() {
    // Core Occurrence extension doesn't need id column mapped
    ExtensionMappingValidator.ValidationStatus status = validator.validate(extensionMapping, resource, peek, columns);
    assertTrue(status.isValid());
    assertNull(status.getIdProblem());

    // add eventDate term to Occurrence extension, and add mapping to it with wrong data type
    List<ExtensionProperty> extensionProperties = extensionMapping.getExtension().getProperties();
    ExtensionProperty extensionProperty = new ExtensionProperty();
    extensionProperty.setQualname(DwcTerm.eventDate.qualifiedName());
    extensionProperty.setRequired(false);
    extensionProperty.setType(ArchiveField.DataType.date);
    extensionProperties.add(extensionProperty);

    Set<PropertyMapping> fields = extensionMapping.getFields();

    PropertyMapping eventDate = new PropertyMapping();
    eventDate.setTerm(DwcTerm.eventDate);
    eventDate.setIndex(1);
    fields.add(eventDate);

    peek.add(new String[] {"id1", "August 38, 1983"});

    status = validator.validate(extensionMapping, resource, peek, columns);
    assertFalse(status.isValid());
    assertEquals(1, status.getWrongDataTypeFields().size());

    // Test some additional valid and invalid dates.
    String[] goodDates = new String[]{"2019-07-22", "2019-07", "2019", "2019-07-21/2019-07-22", "2019-06/2019-07", "2018/2019"};

    for (String d : goodDates) {
      peek.clear();
      peek.add(new String[]{"id1", d});
      System.out.println(d);
      status = validator.validate(extensionMapping, resource, peek, columns);
      assertTrue(status.isValid());
    }

    String[] badDates = new String[]{"2019-17-22", "2019.07", "010203", "01/02/03", "2019-00-00"};

    for (String d : badDates) {
      peek.clear();
      peek.add(new String[]{"id1", d});
      status = validator.validate(extensionMapping, resource, peek, columns);
      assertFalse(status.isValid());
      assertEquals(1, status.getWrongDataTypeFields().size());
    }
  }
}
