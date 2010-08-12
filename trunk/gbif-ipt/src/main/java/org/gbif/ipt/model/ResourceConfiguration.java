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

package org.gbif.ipt.model;

import org.gbif.ipt.service.AlreadyExistingException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author markus
 * 
 */
public class ResourceConfiguration {
  private Resource resource;
  private Set<Source> sources = new HashSet<Source>();
  private ExtensionMapping core;
  private Set<ExtensionMapping> extensions = new HashSet<ExtensionMapping>();

  public void addSource(Source src) throws AlreadyExistingException {
    if (sources.contains(src)) {
      throw new AlreadyExistingException();
    }
    sources.add(src);
  }

  public ExtensionMapping getCore() {
    return core;
  }

  public String getCoreRowType() {
    return core.getExtension().getRowType();
  }

  public Set<ExtensionMapping> getExtensions() {
    return extensions;
  }

  public ExtensionMapping getMapping(String rowType) {
    if (rowType == null) {
      return null;
    }
    for (ExtensionMapping em : extensions) {
      if (rowType.equals(em.getExtension().getRowType())) {
        return em;
      }
    }
    return null;
  }

  public Resource getResource() {
    return resource;
  }

  public Source getSource(String name) {
    if (name == null) {
      return null;
    }
    for (Source src : sources) {
      if (name.equalsIgnoreCase(src.getTitle())) {
        return src;
      }
    }
    return null;
  }

  public Set<Source> getSources() {
    return sources;
  }

  public void setCore(ExtensionMapping core) {
    this.core = core;
  }

  public void setExtensions(Set<ExtensionMapping> extensions) {
    this.extensions = extensions;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setSources(Set<Source> sources) {
    this.sources = sources;
  }

  @Override
  public String toString() {
    return "MappingConfiguration for " + resource;
  }
}
