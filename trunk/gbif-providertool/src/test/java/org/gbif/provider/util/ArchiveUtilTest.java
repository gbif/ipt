/**
 * 
 */
package org.gbif.provider.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.gbif.file.FileUtils;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.eml.Charsets;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ResourceArchiveManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.util.ArchiveUtil.Request;
import org.gbif.provider.util.ArchiveUtil.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author eighty
 * 
 */
public class ArchiveUtilTest extends ResourceTestBase {
  @Autowired
  private ExtensionManager extensionManager;

  @Autowired
  private ArchiveUtil archiveUtil;

  @Autowired
  private SourceManager sourceManager;

  @Autowired
  private ResourceArchiveManager ram;

  @Autowired
  private SourceInspectionManager sourceInspector;

  /**
   * Metafile:
   * 
   * <pre>
   * <?xml version='1.0' encoding='utf-8'?>
   * <archive xmlns="http://rs.tdwg.org/dwc/text/">
   *   <core>
   *     <files>
   *       <location>data.txt</location>
   *     </files>
   *     <id index="0" term="http://rs.tdwg.org/dwc/terms/taxonID"/>
   *     <field index="1" term="http://rs.tdwg.org/dwc/terms/scientificName" type="xs:integer"/>
   *   </core>
   * </archive>
   * </pre>
   * 
   * Data file:
   * 
   * <pre>
   * TaxonID, scientificName
   * 1, tuco
   * 2, bufo
   * </pre>
   * 
   * @throws IOException
   * 
   */
  @Test
  public void test2() throws IOException {
    File archive = FileUtils.getClasspathFile("dwc-archives/unit-testing/1-archive.zip");
    OccurrenceResource resource = new OccurrenceResource();
    occResourceManager.save(resource);
    resource.getExtensionMappingsMap().clear();
    Request<OccurrenceResource> request = Request.with(archive, resource);
    Response<OccurrenceResource> response = archiveUtil.init(request).process();
    resource = response.getResource();

    assertNotNull(resource);

    // The metafile defines no extensions.
    List<ExtensionMapping> mapping = resource.getExtensionMappings();
    assertEquals(mapping.size(), 0);

    // The metafile doesn't define a rowType for core. The default rowType is
    // http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord which
    // should correspond to the Darwin Core extension:
    ExtensionMapping coreMapping = resource.getCoreMapping();
    assertNotNull(coreMapping);
    assertNotNull(coreMapping.getExtension());
    Extension dwc = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
    assertEquals(coreMapping.getExtension(), dwc);

    // <id index="0" /> in the metafile <core> element should correspond to
    // TaxonId in the data file.
    assertEquals("TaxonID", coreMapping.getCoreIdColumn());

    // The datafile has two columns, so we expect two
    // extension properties corresponding to TaxonID and scientificName:
    List<ExtensionProperty> props = coreMapping.getMappedProperties();
    assertEquals(props.size(), 1);
    ExtensionProperty scientificNameProp = props.get(0);
    assertEquals("scientificName", scientificNameProp.getName());

    // Similarly, we expect only a single property mapping that maps the
    // scientificName column to the scientificName extension property:
    List<PropertyMapping> propMappings = coreMapping.getPropertyMappingsSorted();
    assertEquals(propMappings.size(), 1);
    PropertyMapping propMap = propMappings.get(0);
    assertEquals(propMap.getColumn(), "scientificName");
    assertEquals(scientificNameProp, propMap.getProperty());

    // There should be exactly one SourceFile asscociated with the resource:
    assertEquals(1, sourceManager.getAll(resource.getId()).size());

    ram.createArchive(resource);
  }

  @Test
  public void testHeader() throws IOException {
    File file;
    Charset encoding;
    char separator;
    ImmutableList<String> expectedHeader;
    ImmutableList<String> header;
    String path = "dwc-archives/unit-testing/headers/";
    expectedHeader = ImmutableList.of("MyLocalID", "Kingdom", "Name",
        "Latitude", "Longitude");
    encoding = Charsets.UTF_8;

    separator = '\t';
    for (String name : Lists.newArrayList("header-tab.txt",
        "header-tab-quotes.txt")) {
      file = FileUtils.getClasspathFile(path + name);
      header = sourceInspector.getHeader(file, encoding, separator);
      assertEquals(expectedHeader, header);
    }

    separator = ',';
    for (String name : Lists.newArrayList("header-comma.csv",
        "header-comma-quotes.csv")) {
      file = FileUtils.getClasspathFile(path + name);
      header = sourceInspector.getHeader(file, encoding, separator);
      assertEquals(expectedHeader, header);
    }
  }

  @Test
  public void testIt() {
  }

  /**
   * Metafile:
   * 
   * <pre>
   * <?xml version='1.0' encoding='utf-8'?>
   * <archive xmlns="http://rs.tdwg.org/dwc/text/">
   *   <core>
   *     <files>
   *       <location>data.txt</location>
   *     </files>
   *     <id index="0" />
   *   </core>
   * </archive>
   * </pre>
   * 
   * Data file:
   * 
   * <pre>
   * TaxonId, scientificName
   * 1, tuco
   * 2, bufo
   * </pre>
   * 
   * @throws IOException
   * 
   */
  @Test
  public void testProcess() throws IOException {
    File archive = FileUtils.getClasspathFile("dwc-archives/unit-testing/1.zip");
    OccurrenceResource resource = new OccurrenceResource();
    occResourceManager.save(resource);
    resource.getExtensionMappingsMap().clear();
    Request<OccurrenceResource> request = Request.with(archive, resource);
    Response<OccurrenceResource> response = archiveUtil.init(request).process();
    resource = response.getResource();

    assertNotNull(resource);

    // The metafile defines no extensions.
    List<ExtensionMapping> mapping = resource.getExtensionMappings();
    assertEquals(mapping.size(), 0);

    // The metafile doesn't define a rowType for core. The default rowType is
    // http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord which
    // should correspond to the Darwin Core extension:
    ExtensionMapping coreMapping = resource.getCoreMapping();
    assertNotNull(coreMapping);
    assertNotNull(coreMapping.getExtension());
    Extension dwc = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
    assertEquals(coreMapping.getExtension(), dwc);

    // <id index="0" /> in the metafile <core> element should correspond to
    // TaxonId in the data file.
    assertEquals("TaxonId", coreMapping.getCoreIdColumn());

    // The datafile has two columns, but TaxonId isn't in H2, so we expect a
    // single extension property corresponding to scientificName:
    List<ExtensionProperty> props = coreMapping.getMappedProperties();
    assertEquals(props.size(), 1);
    ExtensionProperty scientificNameProp = props.get(0);
    assertEquals("scientificName", scientificNameProp.getName());

    // Similarly, we expect only a single property mapping that maps the
    // scientificName column to the scientificName extension property:
    List<PropertyMapping> propMappings = coreMapping.getPropertyMappingsSorted();
    assertEquals(propMappings.size(), 1);
    PropertyMapping propMap = propMappings.get(0);
    assertEquals(propMap.getColumn(), "scientificName");
    assertEquals(scientificNameProp, propMap.getProperty());

    // There should be exactly one SourceFile asscociated with the resource:
    assertEquals(1, sourceManager.getAll(resource.getId()).size());

    ram.createArchive(resource);
  }
}
