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
package org.gbif.provider.util;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.OccResourceManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation
 * 
 */
public abstract class ResourceTestBase extends TransactionalTestBase {
  @Autowired
  protected ResourceFactory resourceFactory;
  @Autowired
  protected OccResourceManager occResourceManager;
  @Autowired
  protected ChecklistResourceManager checklistResourceManager;
  protected DataResource resource;

  protected OccurrenceResource getResourceMock() {
    OccurrenceResource res = resourceFactory.newOccurrenceResourceInstance();
    res.setTitle("FooBar");
    res.setId(1973L);
    return res;
  }

  protected void setupOccResource() {
    resource = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
  }

  protected void setupTaxResource() {
    resource = checklistResourceManager.get(Constants.TEST_CHECKLIST_RESOURCE_ID);
  }
}
