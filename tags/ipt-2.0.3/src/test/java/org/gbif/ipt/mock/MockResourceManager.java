/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.mock;

import org.gbif.ipt.service.manage.ResourceManager;

import static org.mockito.Mockito.mock;

/**
 * This class simulates a ResourceManager object and must only be used for Unit Tests purposes.
 * 
 * @author hftobon
 */
public class MockResourceManager {

  private static ResourceManager resourceManager = mock(ResourceManager.class);

  public static ResourceManager buildMock() {
    setupMock();
    return resourceManager;
  }

  /**
   * All the ResourceManager methods behavior must be configured in this place.
   */
  private static void setupMock() {
    // TODO All general stubbing implementations for methods, properties, etc., should be here.
  }
}
