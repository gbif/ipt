/*
 * Copyright 2010 GBIF.
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
package org.gbif.registry.api.client;

import junit.framework.Assert;

import org.gbif.registry.api.client.GbifRegistry.ListExtensionsRequest;
import org.gbif.registry.api.client.Gbrds.ExtensionApi;
import org.junit.Test;

import java.util.List;

/**
 * Unit testing coverage for {@link ExtensionApi}.
 */
public class ExtensionApiTest {

  private static Gbrds gbif = GbifRegistry.init("http://gbrds.gbif.org");
  private static ExtensionApi api = gbif.getExtensionApi();

  /**
   * Test method for {@link Gbrds.ExtensionApi#list()} .
   */
  @Test
  public final void testList() {
    ListExtensionsRequest request = api.list();
    List<GbifExtension> list = request.execute().getResult();
    Assert.assertNotNull(list);
    System.out.println(list);
  }
}
