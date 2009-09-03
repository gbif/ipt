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
package org.gbif.provider.model.factory;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.voc.RegionType;

/**
 * TODO: Documentation.
 * 
 */
public class RegionFactory extends ModelBaseFactory<Region> {

  public Region build(DarwinCore dwc) {
    return build(dwc, RegionType.Locality);
  }

  public Region build(DarwinCore dwc, RegionType regionType) {
    if (dwc == null) {
      return null;
    }
    Region region = Region.newInstance(dwc.getResource());
    region.setMpath(dwc.getGeographyPath(regionType));
    region.setLabel(dwc.getHigherGeographyName(regionType));
    region.setType(regionType);
    // currently regions dont have a GUID. Otherwise use
    // dwc.getSamplingLocationID() for localities
    return region;
  }
}
