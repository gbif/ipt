/*
 * Copyright 2010 Global Biodiversity Informatics Facility.
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

import com.google.common.collect.ImmutableList;

import junit.framework.Assert;

import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class SourceInspectorManagerTest extends ResourceTestBase {
  @Autowired
  private SourceInspectionManager sourceInspector;

  @Test
  public void testCsvHeader() {
    String input = "This,is,\"a test\",of,the,\"csv parser\",using,\"comma separated fields\",\"and this,is,one,token\"";
    ImmutableList<String> output = sourceInspector.splitLine(input, ',');
    Assert.assertEquals(9, output.size());

    input = ",,,";
    output = sourceInspector.splitLine(input, ',');
    Assert.assertEquals(4, output.size());

    input = "";
    output = sourceInspector.splitLine(input, ',');
    Assert.assertEquals(1, output.size());

    input = ",\"0,1,2\",3,4,5,";
    output = sourceInspector.splitLine(input, ',');
    Assert.assertEquals(6, output.size());

  }
}
