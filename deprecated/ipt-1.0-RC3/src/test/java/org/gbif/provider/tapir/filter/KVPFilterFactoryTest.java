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
package org.gbif.provider.tapir.filter;

import static org.junit.Assert.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

/**
 * TODO: Documentation.
 * 
 */
public class KVPFilterFactoryTest {
  Log log = LogFactory.getLog(this.getClass());

  @Test
  public void testFilters() throws Exception {
    File testDir = new File(
        this.getClass().getResource("/tapir/filter").getFile());

    File[] input = testDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("kvp")) {
          return true;
        } else {
          return false;
        }
      }
    });

    KVPFilterFactory fh = new KVPFilterFactory();
    try {
      log.debug("Parse empty string and NULL");
      Filter f = fh.parse("");
      log.debug(f.toHQL());

      f = fh.parse(null);
      log.debug(f.toHQL());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      fail(e.getMessage());
    }
    for (int i = 0; i < input.length; i++) {
      try {
        log.debug("\n\nStarting parsing of filter " + input[i].getName());

        FileReader fr = new FileReader(input[i]);
        BufferedReader br = new BufferedReader(fr);
        String filterString = br.readLine();
        log.debug(filterString);
        Filter f = fh.parse(filterString);

        // assertEquals(expected.trim(), filter.toString());
        log.debug("Input[" + i + "] parsed successfully");
        log.debug("HQL: " + f.toHQL());
        for (BooleanOperator op : f) {
          System.out.println(op);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        fail(e.getMessage());
      }
    }
  }
}
