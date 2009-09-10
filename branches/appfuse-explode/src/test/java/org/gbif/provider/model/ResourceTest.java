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

import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class ResourceTest extends ContextAwareTestBase {
  @Autowired
  private ResourceFactory factory;

  @Test
  public void testIsPublished() {
    Resource resource = factory.newMetadataResourceInstance();
    assertFalse(resource.isPublic());
    resource = factory.newChecklistResourceInstance();
    resource.setStatus(PublicationStatus.unpublished);
    assertFalse(resource.isPublic());

    resource.setStatus(PublicationStatus.modified);
    assertTrue(resource.isPublic());

    resource.setStatus(PublicationStatus.published);
    assertTrue(resource.isPublic());
  }
}
