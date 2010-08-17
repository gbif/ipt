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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author markus
 * 
 */
public class ResourceConfiguration {
  private Resource resource;
//  private Map<String, Source> sources = new TreeMap<String, Source>();
  private Set<Source> sources = new HashSet<Source>();
  private ExtensionMapping core;
  private Set<ExtensionMapping> extensions = new HashSet<ExtensionMapping>();

  public void addSource(Source src, boolean allowOverwrite) throws AlreadyExistingException {
	  // make sure we talk about the same resource
	  src.setResource(resource);
    if (!allowOverwrite && sources.contains(src)) {
      throw new AlreadyExistingException();
    }
    sources.add(src);
  }

  public boolean deleteSource(Source src) {
    if (src != null) {
      return sources.remove(src);
    }
    return false;
  }

  public ExtensionMapping getCore() {
    return core;
  }

  public String getCoreRowType() {
	  if (core!=null && core.getExtension()!=null){
		 return core.getExtension().getRowType();
	  }
	  return null;
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
    name=Source.normaliseName(name);
    for (Source s : sources){
    	if (s.getName().equals(name)){
    		return s;
    	}
    }
    return null;
  }

  public List<Source> getSources() {
	  List<Source> srcs = new ArrayList<Source>(sources);
	  Collections.sort(srcs);
    return srcs;
  }

  public void setCore(ExtensionMapping core) {
    this.core = core;
  }

  public void setExtensions(Set<ExtensionMapping> extensions) {
    this.extensions = extensions;
  }
  public void addExtension(ExtensionMapping extension) {
	  if (extension!=null){
		  this.extensions.add(extension);		  
	  }
	}

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public String toString() {
    return "MappingConfiguration for " + resource;
  }
}
