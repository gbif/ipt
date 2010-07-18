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

import static org.junit.Assert.fail;

import org.gbif.registry.api.client.GbrdsImpl;
import org.junit.Test;

/**
 * Unit testing coverage for {@link GbrdsImpl}.
 */
public class GbifRegistryTest {

  @Test
  public final void testInit() {
    try {
      GbrdsImpl.init(null);
      fail();
    } catch (Exception e) {
    }

    try {
      GbrdsImpl.init("");
      fail();
    } catch (Exception e) {
    }

    try {
      GbrdsImpl.init("google.com");
      fail();
    } catch (Exception e) {
    }

    GbrdsImpl.init("http://gbrds.gbif.org");
  }
}
