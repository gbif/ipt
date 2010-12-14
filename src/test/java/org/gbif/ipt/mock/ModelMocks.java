/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.mock;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author markus
 * 
 */
public class ModelMocks {
	public Source src1;
  public Source src2;
  public Source src3;
  public Set<Source> sources;
  public Resource r1;
  public Resource r2;

public ModelMocks() {
    // resources
    r1 = new Resource();
    r1.setTitle("Peterchen");
    r1.setKey(UUID.randomUUID());
    r2 = new Resource();
    r2.setTitle("Peterchen");
    r2.setKey(UUID.randomUUID());

    src1 = new FileSource();
    src1.setName("Peter");
    src1.setResource(r1);
    src2 = new SqlSource();
    src2.setName(" FranKY");
    src2.setResource(r1);
    src3 = new FileSource();
    src3.setName("karl");
    src3.setResource(r2);
    
    assertFalse(src3.equals(src1));
    assertFalse(src3.equals(src2));
    assertFalse(src1.equals(src2));
    assertFalse(src2.equals(src1));
    assertTrue(Source.class.isInstance(src1));
    assertTrue(Source.class.isInstance(src2));
    assertTrue(Source.class.isInstance(src3));
    assertFalse(FileSource.class.isInstance(src2));

    sources = new HashSet<Source>();
    sources.add(src1);
    sources.add(src2);
    sources.add(src3);

  }
}
