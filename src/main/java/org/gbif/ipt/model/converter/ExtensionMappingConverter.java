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

package org.gbif.ipt.model.converter;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.service.admin.ExtensionManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author markus
 * 
 */
@Singleton
public class ExtensionMappingConverter implements Converter {
  private ExtensionManager extManager;

  @Inject
  public ExtensionMappingConverter(ExtensionManager extManager) {
    super();
    this.extManager = extManager;
  }

  public boolean canConvert(Class clazz) {
	    return clazz.equals(ExtensionMapping.class);
  }

  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
	  ExtensionMapping e = (ExtensionMapping) value;
	  // serialise whole object
      writer.startNode("mapping");
      // ...
      writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
	  ExtensionMapping map = new ExtensionMapping();
	  // read whole object, looking up the extension and its properties
	  String term = reader.getValue();
    for (Extension e: extManager.list()){
    	ExtensionProperty prop = e.getProperty(term);
    }
    return map;
  }

}