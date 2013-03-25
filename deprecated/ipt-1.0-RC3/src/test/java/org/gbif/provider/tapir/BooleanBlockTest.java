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
package org.gbif.provider.tapir;

import org.gbif.provider.tapir.filter.And;
import org.gbif.provider.tapir.filter.BooleanBlock;
import org.gbif.provider.tapir.filter.BooleanOperator;
import org.gbif.provider.tapir.filter.LessThan;
import org.gbif.provider.tapir.filter.Like;
import org.gbif.provider.tapir.filter.Not;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class BooleanBlockTest {

  @Test
  public void testIterator() throws Exception {
    BooleanBlock block = new BooleanBlock();
    block.addAtom(new Not());
    block.addAtom(new Like());
    block.addAtom(new And());
    block.addAtom(new LessThan());
    for (BooleanOperator op : block) {
      // System.out.println(block);
    }
    assertTrue(block.size() == 4);
  }

}
