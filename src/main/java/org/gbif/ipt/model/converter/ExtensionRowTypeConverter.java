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

import org.gbif.ipt.model.Extension;
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
public class ExtensionRowTypeConverter implements Converter {
  private ExtensionManager extManager;
  private Extension lastExtensionConverted;

  @Inject
  public ExtensionRowTypeConverter(ExtensionManager extManager) {
    super();
    this.extManager = extManager;
  }

  public boolean canConvert(Class clazz) {
    return clazz.equals(Extension.class);
  }

  public Extension getLastExtensionConverted() {
    return lastExtensionConverted;
  }

  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    Extension e = (Extension) value;
    writer.setValue(e.getRowType());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Extension e = extManager.get(reader.getValue());
    lastExtensionConverted = e;
    return e;
  }

}