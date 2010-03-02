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
package org.gbif.provider.model.hibernate;

import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class GbifNamingStrategyTest extends ContextAwareTestBase {
  private static String[] props = {
      "resource", "propertyName", "order", "resource.bbox.latitude"};
  private static String[] extensions = {"Paleaontology", "Multi Identification"};
  @Autowired
  private IptNamingStrategy namingStrategy;

  @Test
  public void testForeignKeyColumnName() {
    System.out.println(namingStrategy.foreignKeyColumnName(
        "resource.bbox.latitude", "DarwinCore", "Darwin_Core", "id"));
  }

  @Test
  public void testLogicalColumnName() {
    for (String p : props) {
      System.out.println(namingStrategy.logicalColumnName(p, p));
    }
  }

  @Test
  public void testPropertyToColumnName() {
    for (String p : props) {
      System.out.println(namingStrategy.propertyToColumnName(p));
    }
  }

}
