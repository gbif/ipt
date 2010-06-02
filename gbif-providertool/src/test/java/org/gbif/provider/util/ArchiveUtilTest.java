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
import org.gbif.provider.service.SourceInspectionManager.HeaderSpec;
import org.gbif.provider.util.ArchiveUtil.ArchiveRequest;
import org.gbif.provider.util.ArchiveUtil.ArchiveResponse;
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
    ArchiveRequest<OccurrenceResource> request = ArchiveRequest.with(archive,
        resource);
    ArchiveResponse<OccurrenceResource> response = archiveUtil.init(request).process();
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
    // final File file;
    final Charset encoding;
    ImmutableList<String> expectedHeader;
    ImmutableList<String> header;
    String path = "dwc-archives/unit-testing/headers/";
    expectedHeader = ImmutableList.of("MyLocalID", "Kingdom", "Name",
        "Latitude", "Longitude");
    encoding = Charsets.UTF_8;

    for (String name : Lists.newArrayList("header-tab.txt",
        "header-tab-quotes.txt")) {
      final File file = FileUtils.getClasspathFile(path + name);
      final char separator = '\t';
      HeaderSpec spec = new HeaderSpec() {
        public char getFieldSeparatorChar() {
          return separator;
        }

        public File getFile() {
          return file;
        }

        public Charset getFileEncoding() {
          return encoding;
        }

        public int getNumberOfLinesToSkip() {
          return 1;
        }

        public boolean headerExists() {
          return true;
        }
      };

      header = sourceInspector.getHeader(spec);
      assertEquals(expectedHeader, header);
    }

    for (String name : Lists.newArrayList("occurrencesNoHeader.txt")) {
      final File file = FileUtils.getClasspathFile(path + name);
      final char separator = '\t';
      HeaderSpec spec = new HeaderSpec() {
        public char getFieldSeparatorChar() {
          return separator;
        }

        public File getFile() {
          return file;
        }

        public Charset getFileEncoding() {
          return encoding;
        }

        public int getNumberOfLinesToSkip() {
          return 0;
        }

        public boolean headerExists() {
          return false;
        }
      };

      header = sourceInspector.getHeader(spec);
      assertEquals(ImmutableList.of("Column-000 (Example value: 1)",
          "Column-001 (Example value: Plantae)",
          "Column-002 (Example value: Abies alba)",
          "Column-003 (Example value: 44.5)",
          "Column-004 (Example value: 13.3)"), header);
    }

    for (String name : Lists.newArrayList("occurrencesNoHeader.csv")) {
      final File file = FileUtils.getClasspathFile(path + name);
      final char separator = ',';
      HeaderSpec spec = new HeaderSpec() {
        public char getFieldSeparatorChar() {
          return separator;
        }

        public File getFile() {
          return file;
        }

        public Charset getFileEncoding() {
          return encoding;
        }

        public int getNumberOfLinesToSkip() {
          return 0;
        }

        public boolean headerExists() {
          return false;
        }
      };

      header = sourceInspector.getHeader(spec);
      assertEquals(ImmutableList.of("Column-000 (Example value: 1)",
          "Column-001 (Example value: Plantae)",
          "Column-002 (Example value: Abies alba)",
          "Column-003 (Example value: 44.5)",
          "Column-004 (Example value: 13.3)"), header);
    }

    for (String name : Lists.newArrayList("header-comma.csv",
        "header-comma-quotes.csv")) {
      final File file = FileUtils.getClasspathFile(path + name);
      final char separator = ',';
      HeaderSpec spec = new HeaderSpec() {
        public char getFieldSeparatorChar() {
          return separator;
        }

        public File getFile() {
          return file;
        }

        public Charset getFileEncoding() {
          return encoding;
        }

        public int getNumberOfLinesToSkip() {
          return 1;
        }

        public boolean headerExists() {
          return true;
        }
      };

      header = sourceInspector.getHeader(spec);
      assertEquals(expectedHeader, header);
    }
  }

  @Test
  public void testHeaderSpec() throws IOException {
    final File file;
    String path = "dwc-archives/unit-testing/headers/header-tab-skip-first-9-lines.txt";
    file = FileUtils.getClasspathFile(path);
    final Charset encoding = Charsets.UTF_8;
    final char separator = '\t';
    ImmutableList<String> expectedHeader = ImmutableList.of("MyLocalID",
        "Kingdom", "Name", "Latitude", "Longitude");;
    ImmutableList<String> header;

    HeaderSpec spec = new HeaderSpec() {
      public char getFieldSeparatorChar() {
        return separator;
      }

      public File getFile() {
        return file;
      }

      public Charset getFileEncoding() {
        return encoding;
      }

      public int getNumberOfLinesToSkip() {
        return 2;
      }

      public boolean headerExists() {
        return true;
      }
    };

    assertEquals(expectedHeader, sourceInspector.getHeader(spec));
    // assertEquals(
    // ImmutableList.of("Column-000 (Example value: 1)",
    // "Column-001 (Example value: Plantae)",
    // "Column-002 (Example value: Abies alba)",
    // "Column-003 (Example value: 44.5)",
    // "Column-004 (Example value: 13.3)"),
    // sourceInspector.getHeader(spec));
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
    ArchiveRequest<OccurrenceResource> request = ArchiveRequest.with(archive,
        resource);
    ArchiveResponse<OccurrenceResource> response = archiveUtil.init(request).process();
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

  @Test
  public void testSource() {
    sourceManager.getSourceByFilename(13L, "DarwinCore.txt");
  }

  @Test
  public void testWorm() throws IOException {
    File archive = FileUtils.getClasspathFile("dwc-archives/unit-testing/worms.zip");
    OccurrenceResource resource = new OccurrenceResource();
    occResourceManager.save(resource);
    resource.getExtensionMappingsMap().clear();
    ArchiveRequest<OccurrenceResource> request = ArchiveRequest.with(archive,
        resource);
    ArchiveResponse<OccurrenceResource> response = archiveUtil.init(request).process();
    resource = response.getResource();

    assertNotNull(resource);
  }
}
