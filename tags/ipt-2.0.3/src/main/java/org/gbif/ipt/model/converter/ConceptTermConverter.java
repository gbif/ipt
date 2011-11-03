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
import org.gbif.dwc.terms.UnknownTerm;
import org.gbif.ipt.model.Extension;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.apache.log4j.Logger;

/**
 * @author markus
 * 
 */
@Singleton
public class ConceptTermConverter implements Converter {
  private Logger log = Logger.getLogger(this.getClass());
  private ExtensionRowTypeConverter extConverter;

  @Inject
  public ConceptTermConverter(ExtensionRowTypeConverter extConverter) {
    super();
    this.extConverter = extConverter;
  }

  public boolean canConvert(Class clazz) {
    return ConceptTerm.class.isAssignableFrom(clazz);
  }

  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    ConceptTerm t = (ConceptTerm) value;
    writer.setValue(t.qualifiedName());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    // this is tricky. Relies on xstream implementation to convert the matching extension BEFORE we reach here
    // But just cant get the hierarchical reader to tell me the current extension name

    // other far more complex solution is to create an full blown ExtensionMapping converter
    // that could make use of existing local converters for composite objects parts
    Extension extension = extConverter.getLastExtensionConverted();
    ConceptTerm t = null;
    if (extension != null) {
      t = extension.getProperty(reader.getValue());
    }
    if (t == null) {
      log.warn("Cant unmarshall concept " + reader.getValue());
      t = new UnknownTerm(reader.getValue(), reader.getValue());
    }

    return t;
  }
}