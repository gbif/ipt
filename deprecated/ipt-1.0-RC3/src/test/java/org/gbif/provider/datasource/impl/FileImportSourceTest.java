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
package org.gbif.provider.datasource.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class FileImportSourceTest {

  @Test
  public void testEscapes() {
    String input = " hallo Bernd. / you öüΩ∂6782  gf ";
    assertEquals(escapeRawValue(input), input);
    input += " fhdf    f f  f f  ";
    assertEquals(escapeRawValue(input), input);
    String input2 = input + "tab  tab";
    assertFalse(input2.equals(escapeRawValue(input2)));
    input2 = input + "br\nbr";
    assertFalse(input2.equals(escapeRawValue(input2)));
  }

  private String escapeRawValue(String val) {
    return FileImportSource.ESCAPE_PATTERN.matcher(val).replaceAll(" ");
  }

}
