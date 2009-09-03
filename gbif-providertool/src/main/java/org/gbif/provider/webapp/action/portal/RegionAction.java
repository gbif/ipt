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
package org.gbif.provider.webapp.action.portal;

import org.gbif.provider.model.Region;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.RegionManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class RegionAction extends BaseTreeNodeAction<Region, RegionType> {
  @Autowired
  public RegionAction(RegionManager regionManager) {
    super(regionManager);
  }

  public Region getRegion() {
    return node;
  }

  @Override
  public Long getRegionId() {
    return id;
  }

  @Override
  public String occurrences() {
    String result = super.occurrences();
    if (node != null) {
      occurrences = darwinCoreManager.getByRegion(node.getId(), resourceId,
          true);
    }
    return result;
  }
}