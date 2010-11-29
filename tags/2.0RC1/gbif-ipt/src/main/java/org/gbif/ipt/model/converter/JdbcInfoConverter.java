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

import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.config.JdbcSupport.JdbcInfo;

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
public class JdbcInfoConverter implements Converter {
  private JdbcSupport jdbcs;

  @Inject
  public JdbcInfoConverter(JdbcSupport jdbcs) {
    super();
    this.jdbcs = jdbcs;
  }

  public boolean canConvert(Class clazz) {
    return clazz.equals(JdbcInfo.class);
  }

  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    JdbcInfo info = (JdbcInfo) value;
    writer.setValue(info.getName());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    JdbcInfo info = jdbcs.get(reader.getValue());
    return info;
  }

}