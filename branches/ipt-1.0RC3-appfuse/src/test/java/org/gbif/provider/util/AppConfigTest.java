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

import org.gbif.provider.model.factory.DarwinCoreFactory;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class AppConfigTest extends ContextAwareTestBase {
  @Autowired
  public AppConfig cfg;
  @Autowired
  private DarwinCoreFactory dwcFactory;

  @Test
  public void testPropertiesLoaded() {
    assertEquals("http://localhost:8080/ipt", cfg.getBaseUrl());
  }

  @Test
  public void testSetAppBaseUrl() {
    cfg.setBaseUrl("http://localhost:8080/ipt/");
    assertEquals("http://localhost:8080/ipt", cfg.getBaseUrl());

    cfg.setBaseUrl(" http://localhost:8080/ipt  ");
    assertEquals("http://localhost:8080/ipt", cfg.getBaseUrl());
  }

}
