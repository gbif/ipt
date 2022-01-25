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
package org.gbif.ipt.service;

import org.gbif.ipt.action.BaseAction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistryExceptionTest {

  @Test
  public void testLogRegistryException() {
    String answer = "An unknown error occurred!";
    BaseAction mockAction = mock(BaseAction.class);
    when(mockAction.getText("admin.registration.error.unknown")).thenReturn(answer);
    String msg = RegistryException.logRegistryException(new RegistryException(RegistryException.Type.UNKNOWN, "url", null, null), mockAction);
    assertEquals(answer+" [url]", msg);

    answer = "Response was empty! [url]";
    when(mockAction.getText("admin.registration.error.badResponse")).thenReturn(answer);
    msg = RegistryException.logRegistryException(new RegistryException(RegistryException.Type.BAD_RESPONSE, "url", null, null), mockAction);
    assertEquals(answer+" [url]", msg);
  }
}
