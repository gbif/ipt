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

import org.gbif.provider.webapp.action.BaseMetadataResourceAction;

import com.opensymphony.xwork2.Preparable;

/**
 * TODO: Documentation.
 * 
 */
public class NotFoundAction extends BaseMetadataResourceAction implements
    Preparable {

  @Override
  public String execute() {
    if (resource == null) {
      return this.RESOURCE404;
    }
    return RECORD404;
  }

}
