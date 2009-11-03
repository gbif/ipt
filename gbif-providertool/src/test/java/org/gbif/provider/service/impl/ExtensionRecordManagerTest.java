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
package org.gbif.provider.service.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class ExtensionRecordManagerTest /* extends ResourceTestBase */{
  @Autowired
  private ExtensionRecordManager extensionRecordManager;
  @Autowired
  protected DarwinCoreManager darwinCoreManager;
  @Autowired
  protected TaxonManager taxonManager;

  @Test
  public void testExtendedRecord() {
    // this.setupTaxResource();
    List<Taxon> taxa = taxonManager.latest(
        Constants.TEST_CHECKLIST_RESOURCE_ID, 1, 10);
    assertTrue(taxa.size() == 10);
    List<ExtendedRecord> records = null;
    // extensionRecordManager.extendCoreRecords(
    // resource, taxa.toArray(new CoreRecord[taxa.size()]));
    assertTrue(records.size() == 10);
    List<Extension> extensions = records.get(6).getExtensions();
    assertTrue(records.get(6).getExtensionRecords(extensions.get(0)).size() > 0);
  }

}
