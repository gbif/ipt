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

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.util.RecursiveIterator;

import java.util.Iterator;

/**
 * TODO: Documentation.
 * 
 */
public abstract class ComparisonOperator extends BooleanOperator {
  protected ExtensionProperty property;

  public ExtensionProperty getProperty() {
    return property;
  }

  public Iterator<BooleanOperator> iterator() {
    return new RecursiveIterator<BooleanOperator>(this);
  }

  public void setProperty(ExtensionProperty property) {
    log.debug("Setting property to: " + property.getQualName());
    this.property = property;
  }

  public void setProperty(String propertyAsString) {
    setProperty(new ExtensionProperty(propertyAsString));
  }

  protected abstract String getOperatorSymbol();
}
