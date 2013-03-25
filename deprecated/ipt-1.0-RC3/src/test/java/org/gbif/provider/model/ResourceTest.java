/*
 * Copyright 2009 GBIF.
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
 */
package org.gbif.provider.model;

import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.ImportSourceFactory;
import org.gbif.provider.model.factory.DarwinCoreFactory;
import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResourceTestBase;

import junit.framework.Assert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class ResourceTest extends ResourceTestBase {

  @Autowired
  private ResourceFactory resourceFactory;

  @Autowired
  private DarwinCoreManager entityManager;

  @Autowired
  private OccResourceManager resourceManager;

  @Autowired
  private TaxonManager taxonManager;

  @Autowired
  private ImportSourceFactory importSourceFactory;

  @Autowired
  private RegionManager regionManager;

  @Autowired
  private DarwinCoreFactory darwinCoreFactory;

  @Test
  public void deleteExistingResource() throws ImportSourceException {
    OccurrenceResource resource = resourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
    resourceManager.remove(resource.getId());
    resourceManager.flush();
  }

  @Test
  public void deleteNewOccurrenceResource() {
    OccurrenceResource r = resourceFactory.newOccurrenceResourceInstance();
    String guid = "test-resource-guid";
    r.setGuid(guid);
    r = resourceManager.save(r);
    resourceManager.flush();
    assertTrue(resourceManager.exists(r.getId()));
    assertNotNull(resourceManager.get(guid));
    resourceManager.remove(r.getId());
    Assert.assertFalse(resourceManager.exists(r.getId()));
  }

  @Test
  public void isPublished() {
    Resource resource = resourceFactory.newMetadataResourceInstance();
    assertFalse(resource.isPublic());
    resource = resourceFactory.newChecklistResourceInstance();
    resource.setStatus(PublicationStatus.unpublished);
    assertFalse(resource.isPublic());

    resource.setStatus(PublicationStatus.modified);
    assertTrue(resource.isPublic());

    resource.setStatus(PublicationStatus.published);
    assertTrue(resource.isPublic());
  }
}
