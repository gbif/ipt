/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.model;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.metadata.eml.Agent;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ResourceTest {
  private static final Logger LOG = Logger.getLogger(ResourceTest.class);
  private final Extension OCC;
  private final Extension EXT;
  private final Extension TAX;
  private static User USER;

  public ResourceTest() {
    OCC = new Extension();
    OCC.setInstalled(true);
    OCC.setName("Occurrence Core");
    OCC.setTitle("Occurrence Core");
    OCC.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);

    TAX = new Extension();
    TAX.setInstalled(true);
    TAX.setName("Taxon Core");
    TAX.setTitle("Taxon Core");
    TAX.setRowType(Constants.DWC_ROWTYPE_TAXON);

    EXT = new Extension();
    EXT.setInstalled(true);
    EXT.setName("Occurrence Extension");
    EXT.setTitle("Occurrence Extension");
    EXT.setRowType("http://rs.gbif.org/my/extension/test");

    USER = new User();
    USER.setEmail("jc@gbif.org");
    USER.setLastname("Costa");
    USER.setFirstname("Jose");
  }

  private ExtensionMapping getExtExtensionMapping() {
    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(EXT);
    return mapping;
  }

  private ExtensionMapping getOccExtensionMapping() {
    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(OCC);
    return mapping;
  }

  private ExtensionMapping getTaxExtensionMapping() {
    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(TAX);
    return mapping;
  }

  private Resource getResource() {
    Resource res = new Resource();
    res.setTitle("Test Resource");
    res.setShortname("test");
    return res;
  }

  @Test
  public void testAddMapping() {
    Resource res = getResource();
    boolean failed = false;
    try {
      res.addMapping(getExtExtensionMapping());
    } catch (IllegalArgumentException e) {
      failed = true;
    }
    // cant add an extension without having a core
    assertTrue(failed);

    res.addMapping(getTaxExtensionMapping());
    res.addMapping(getExtExtensionMapping());
    res.addMapping(getTaxExtensionMapping());
    res.addMapping(getExtExtensionMapping());
    // Occurrence mapped as an extension to Taxon core, which is allowed
    res.addMapping(getOccExtensionMapping());

    assertEquals(5, res.getMappings().size());
    // there are 2 mappings to Taxon core, 1 extension mapping to the Occurrence core which is excluded
    assertEquals(2, res.getCoreMappings().size());
  }

  @Test
  public void testDeleteMapping() {
    Resource res = getResource();
    ExtensionMapping em1 = getOccExtensionMapping();
    ExtensionMapping em2 = getOccExtensionMapping();
    ExtensionMapping em3 = getExtExtensionMapping();
    ExtensionMapping em4 = getExtExtensionMapping();
    ExtensionMapping em5 = getExtExtensionMapping();

    res.addMapping(em1);
    res.addMapping(em2);
    res.addMapping(em3);
    res.addMapping(em4);
    res.addMapping(em5);

    assertEquals(5, res.getMappings().size());
    assertEquals(2, res.getCoreMappings().size());

    // delete first core
    assertTrue(res.deleteMapping(em1));
    assertEquals(4, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // try againt to remove the same ext - should not work
    assertFalse(res.deleteMapping(em1));
    assertEquals(4, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // remove an extension
    assertTrue(res.deleteMapping(em5));
    assertEquals(3, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // remove the last core, should remove all
    assertTrue(res.deleteMapping(em2));
    assertFalse(res.hasCore());
    assertEquals(0, res.getMappings().size());
    assertEquals(0, res.getCoreMappings().size());
  }

  @Test
  public void testDeleteSource() throws AlreadyExistingException {
    Resource res = getResource();

    Source src1 = new TextFileSource();
    src1.setName("Peter");
    res.addSource(src1, false);

    Source src2 = new TextFileSource();
    src2.setName("Carla");
    res.addSource(src2, false);

    // add 3 mappings
    ExtensionMapping emOcc = getOccExtensionMapping();
    emOcc.setSource(src1);
    ExtensionMapping emE1 = getExtExtensionMapping();
    emE1.setSource(src1);
    ExtensionMapping emE2 = getExtExtensionMapping();
    emE2.setSource(src2);

    res.addMapping(emOcc);
    res.addMapping(emE1);
    res.addMapping(emE2);

    assertEquals(3, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // delete source mapped only to 1 extension
    assertTrue(res.deleteSource(src2));
    assertEquals(2, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // delete other source
    assertTrue(res.deleteSource(src1));
    assertEquals(0, res.getMappings().size());
    assertEquals(0, res.getCoreMappings().size());
  }

  @Test
  public void testCoreRowTypeSet() {
    // create test resource
    Resource resource = new Resource();
    // add mapping to taxon core
    ExtensionMapping mapping = new ExtensionMapping();
    Extension ext = new Extension();
    ext.setRowType(Constants.DWC_ROWTYPE_TAXON);
    mapping.setExtension(ext);
    resource.addMapping(mapping);
    // assert correct core row type has been determined from core mapping
    assertEquals(Constants.DWC_ROWTYPE_TAXON, resource.getCoreRowType());
  }

  @Test
  public void testAddVersionHistory() {
    Resource resource = getResource();

    VersionHistory vh1 = new VersionHistory(BigDecimal.valueOf(1.4), new Date(), USER);
    VersionHistory vh2 = new VersionHistory(BigDecimal.valueOf(1.5), new Date(), USER);

    resource.addVersionHistory(vh1);
    resource.addVersionHistory(vh2);

    assertEquals(2, resource.getVersionHistory().size());

    // try and add a version history with same version number - isn't allowed!
    VersionHistory vh3 = new VersionHistory(BigDecimal.valueOf(1.5), new Date(), USER);

    resource.addVersionHistory(vh3);

    assertEquals(2, resource.getVersionHistory().size());
  }

  @Test
  public void testRemoveVersionHistory() {
    Resource resource = getResource();

    VersionHistory vh1 = new VersionHistory(BigDecimal.valueOf(1.4), new Date(), USER);
    VersionHistory vh2 = new VersionHistory(BigDecimal.valueOf(1.5), new Date(), USER);

    resource.addVersionHistory(vh1);
    resource.addVersionHistory(vh2);

    assertEquals(2, resource.getVersionHistory().size());

    // remove the last version (imagining the version had to be rolled back after failed publication)
    resource.removeVersionHistory(BigDecimal.valueOf(1.5));

    assertEquals(1, resource.getVersionHistory().size());
    assertEquals(BigDecimal.valueOf(1.4), resource.getVersionHistory().get(0).getVersion());
  }

  @Test
  public void testFindVersionHistory() {
    Resource resource = getResource();

    VersionHistory vh1 = new VersionHistory(BigDecimal.valueOf(1.4), new Date(), USER);
    VersionHistory vh2 = new VersionHistory(BigDecimal.valueOf(1.5), new Date(), USER);

    resource.addVersionHistory(vh1);
    resource.addVersionHistory(vh2);

    VersionHistory vh = resource.findVersionHistory(BigDecimal.valueOf(1.4));
    assertEquals(BigDecimal.valueOf(1.4), vh.getVersion());
    assertEquals("jc@gbif.org", vh.getModifiedBy().getEmail());
  }

  @Test
  public void testGetAuthorName() {
    Agent creator = new Agent();
    creator.setLastName("Williams");
    creator.setFirstName("Brian");
    assertEquals("Williams B", getResource().getAuthorName(creator));

    creator.setFirstName("Brian Gonzalez");
    assertEquals("Williams B G", getResource().getAuthorName(creator));
  }

  @Test
  public void testGetPublicationYear() {
    Date now = new Date();
    int year = getResource().getPublicationYear(now);
    assertNotNull(year);
    assertEquals(4, String.valueOf(year).length());
  }

  @Test
  public void testGenerateResourceCitation() {
    Resource resource = new Resource();
    resource.setTitle("Birds");
    resource.setEmlVersion(BigDecimal.valueOf(1.6));
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
    resource.setDoi("http://doi.org/10.5886/1bft7W5f");

    Calendar calendar = Calendar.getInstance();
    calendar.set(2014, Calendar.JANUARY, 29);
    resource.getEml().setPubDate(calendar.getTime());

    Agent creator1 = new Agent();
    creator1.setFirstName("John");
    creator1.setLastName("Smith");

    Agent creator2 = new Agent();
    creator2.setFirstName("Paul");
    creator2.setLastName("Weir");

    List<Agent> creators = Lists.newArrayList();
    creators.add(creator1);
    creators.add(creator2);

    resource.getEml().setCreators(creators);

    Organisation publisher = new Organisation();
    publisher.setName("NHM");
    resource.setOrganisation(publisher);

    String citation = resource.generateResourceCitation();

    LOG.info("Resource citation using next minor version: " + citation);
    assertEquals("Smith J, Weir P (2014): Birds. v1.7. NHM. Dataset. http://doi.org/10.5886/1bft7W5f", citation);

    citation = resource.generateResourceCitation(BigDecimal.valueOf(1.6));

    LOG.info("Resource citation with version specified: " + citation);
    assertEquals("Smith J, Weir P (2014): Birds. v1.6. NHM. Dataset. http://doi.org/10.5886/1bft7W5f", citation);
  }

}
