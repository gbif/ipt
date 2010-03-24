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
package org.gbif.provider.model.factory;

import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.ImportSourceFactory;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.ResourceTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

public class DarwinCoreFactoryTest extends ResourceTestBase {

  private static final long TEST_RESOURCE_ID = 13;

  @Autowired
  private ImportSourceFactory importSourceFactory;

  @Autowired
  private OccResourceManager resourceManager;

  @Autowired
  private DarwinCoreFactory darwinCoreFactory;

  @Test
  public void loadSourceFile() throws ImportSourceException {
    OccurrenceResource resource = resourceManager.get(TEST_RESOURCE_ID);
    ImportSource source;
    source = importSourceFactory.newInstance(resource,
        resource.getCoreMapping());
    for (ImportRecord ir : source) {
      if (ir == null) {
        continue;
      }
      darwinCoreFactory.build(resource, ir, new HashSet<Annotation>());
    }
  }
}
