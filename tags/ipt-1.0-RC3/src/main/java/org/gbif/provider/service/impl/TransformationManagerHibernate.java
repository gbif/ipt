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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Transformation;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.TransformationManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * TODO: Documentation.
 * 
 */
public class TransformationManagerHibernate extends
    GenericResourceRelatedManagerHibernate<Transformation> implements
    TransformationManager {
  @Autowired
  @Qualifier("propertyMappingManager")
  private GenericManager<PropertyMapping> propertyMappingManager;

  public TransformationManagerHibernate() {
    super(Transformation.class);
  }

  @Override
  public void remove(Transformation obj) {
    // make sure no propertyMapping references this transformation
    for (PropertyMapping pm : obj.getPropertyMappings()) {
      pm.setTermTransformation(null);
      propertyMappingManager.save(pm);
    }
    super.remove(obj);
  }

}
