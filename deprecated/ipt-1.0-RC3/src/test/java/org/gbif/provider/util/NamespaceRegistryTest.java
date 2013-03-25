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

/**
 * TODO: Documentation.
 * 
 */
public class NamespaceRegistryTest extends ResourceTestBase {

  @Test
  public void testAddExtensionProperty() {
    NamespaceRegistry nsr = new NamespaceRegistry();
    nsr.add("http://ipt.gbif.org");
    nsr.add("http://rs.tdwg.org/dwc/dwcore/");
    nsr.add("http://rs.tdwg.org/dwc/dwcore");
    nsr.add("http://ipt.gbif.org/multimedia");
    nsr.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    nsr.add("http://purl.org/dc/elements/1.1/");
    nsr.add("http://www.w3.org/2001/XMLSchema");
    nsr.add("http://www.w3.org/2000/01/rdf-schema#");
    nsr.add("http://www.w3.org/2002/07/owl#");
    nsr.add("http://www.tdwg.org/schemas/abcd/2.06");
    System.out.println(nsr);
  }

  @Test
  public void testAddResource() {
    setupOccResource();
    NamespaceRegistry nsr = new NamespaceRegistry(resource);
    System.out.println(nsr.xmlnsDef());
  }
}
