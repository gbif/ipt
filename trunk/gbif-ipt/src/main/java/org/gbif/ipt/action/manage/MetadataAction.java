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

package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.validation.EmlSupport;
import org.gbif.ipt.validation.ResourceSupport;

import com.google.inject.Inject;

/**
 * @author markus
 * 
 */
public class MetadataAction extends POSTAction {
  @Inject
//the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;
  @Inject
  private ResourceManager resourceManager;
  private ResourceSupport validator1 = new ResourceSupport();
  private EmlSupport validator2 = new EmlSupport();

  @Override
  public String save() throws Exception {
    ms.saveResource();
    ms.saveEml();
    return SUCCESS;
  }

  @Override
  public void validateHttpPostOnly() {
    validator1.validate(this, ms.getResource());
    validator2.validate(this, ms.getEml());
  }

}
