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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.metadata.eml.ipt.model.Eml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceLoaderImplTest {

  @Test
  public void testRestoreStrippedLicenseLink() {
    String ccBy = "This work is licensed under a <a href=\"http://creativecommons.org/licenses/by/4.0/legalcode\">"
        + "Creative Commons Attribution (CC-BY 4.0) License</a>.";

    // license anchor stripped by the sanitizer (IPT 3.3.5)
    Eml eml = new Eml();
    eml.setIntellectualRights("This work is licensed under a Creative Commons Attribution (CC-BY 4.0) License.");
    assertTrue(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals(ccBy, eml.getIntellectualRights());
    assertEquals("http://creativecommons.org/licenses/by/4.0/legalcode", eml.parseLicenseUrl());

    // old-fashioned license name, with the URL as text
    eml.setIntellectualRights("This work is licensed under a Creative Commons Attribution (CC-BY) 4.0 License: "
        + "http://creativecommons.org/licenses/by/4.0\".");
    assertTrue(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals(ccBy, eml.getIntellectualRights());

    // URL only
    eml.setIntellectualRights("Licensed under https://creativecommons.org/licenses/by-nc/4.0/legalcode");
    assertTrue(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals("http://creativecommons.org/licenses/by-nc/4.0/legalcode", eml.parseLicenseUrl());

    // CC0, name only
    eml.setIntellectualRights("To the extent possible under law, the publisher has waived all rights to these data "
        + "and has dedicated them to the Public Domain (CC0 1.0).");
    assertTrue(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals("http://creativecommons.org/publicdomain/zero/1.0/legalcode", eml.parseLicenseUrl());

    // intact license is left alone
    eml.setIntellectualRights(ccBy);
    assertFalse(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals(ccBy, eml.getIntellectualRights());

    // custom rights statement is left alone
    eml.setIntellectualRights("All rights reserved.");
    assertFalse(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals("All rights reserved.", eml.getIntellectualRights());

    // missing rights
    assertFalse(ResourceLoaderImpl.restoreStrippedLicenseLink(new Eml()));
  }

  /**
   * A resource with another form of the installed schema identifier gets the installed one, so its mappings, which refer
   * to the installed schema, are used.
   */
  @Test
  public void testNormalizeDataPackageIdentifier() {
    DataPackageSchemaManager schemaManager = mock(DataPackageSchemaManager.class);
    DataPackageSchema installed = DataPackageSchema.builder().identifier("http://rs.tdwg.org/dwc/dwc-dp").build();
    when(schemaManager.get("http://rs.gbif.org/data-packages/dwc-dp")).thenReturn(installed);
    when(schemaManager.get("http://rs.tdwg.org/dwc/dwc-dp")).thenReturn(installed);

    Resource alternative = resource("http://rs.gbif.org/data-packages/dwc-dp");
    assertTrue(ResourceLoaderImpl.normalizeDataPackageIdentifier(alternative, schemaManager));
    assertEquals("http://rs.tdwg.org/dwc/dwc-dp", alternative.getDataPackageIdentifier());

    Resource same = resource("http://rs.tdwg.org/dwc/dwc-dp");
    assertFalse(ResourceLoaderImpl.normalizeDataPackageIdentifier(same, schemaManager));

    Resource notInstalled = resource("http://example.org/unknown");
    assertFalse(ResourceLoaderImpl.normalizeDataPackageIdentifier(notInstalled, schemaManager));
    assertEquals("http://example.org/unknown", notInstalled.getDataPackageIdentifier());
  }

  private static Resource resource(String dataPackageIdentifier) {
    Resource resource = new Resource();
    resource.setShortname("res");
    resource.setDataPackageIdentifier(dataPackageIdentifier);
    return resource;
  }
}
