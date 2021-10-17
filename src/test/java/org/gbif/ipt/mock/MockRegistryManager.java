/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.mock;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.registry.RegistryManager;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockRegistryManager {

  private static RegistryManager registryManager = mock(RegistryManager.class);

  public static RegistryManager buildMock() {
    setupMock();
    return registryManager;
  }

  /**
   * Method stub which simulate the original one: org.gbif.ipt.service.registry.impl.RegistryManager.getVocabularies().
   *
   * @return A simulated vocabulary list.
   */
  private static List<Vocabulary> getVocabularies() {
    List<Vocabulary> vocabs = new ArrayList<>();

    return vocabs;
  }

  /**
   * Stubbing some methods and assigning some default configurations.
   */
  private static void setupMock() {
    // TODO All general stubbing implementations for methods, properties, etc., should be here.
    when(registryManager.getVocabularies()).thenReturn(getVocabularies());

  }
}
