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
package org.gbif.provider.geo;

import org.gbif.provider.geo.TransformationUtils.Wgs84Transformer;

import org.junit.Test;
import org.opengis.referencing.FactoryException;

/**
 * TODO: Documentation.
 * 
 */
public class TransformationUtilsTest {
  // @Autowired
  private final TransformationUtils wgs84Util = new TransformationUtils();

  @Test
  public void testTransformIntoWGS84() {
    try {
      Wgs84Transformer transformer = wgs84Util.getWgs84Transformer("WGS84");
    } catch (FactoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
