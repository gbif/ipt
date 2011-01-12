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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * TODO: Documentation.
 * 
 */
public class TabFileReaderTest extends ContextAwareTestBase {
  @Autowired
  private AppConfig cfg;

  @Test
  public void testTabFileReader() throws IOException, MalformedTabFileException {
    File f = cfg.getResourceSourceFile(Constants.TEST_OCC_RESOURCE_ID,
        "pontaurus.txt");
    TabFileReader reader = new TabFileReader(f, false);
    System.out.println(Arrays.asList(reader.getHeader()));
    System.out.println();
    for (int i = 0; i < 25; i++) {
      System.out.println(Arrays.asList(reader.next()));
    }
    reader.close();
  }

}
