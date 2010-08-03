/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***************************************************************************/

package org.gbif.ipt.validation;

import org.gbif.ipt.action.BaseAction;
import org.gbif.metadata.eml.Eml;

import com.google.inject.internal.Nullable;

/**
 * @author markus
 * 
 */
public class EmlSupport {

  /**
   * Validate an EML document, optionally only a part of it matching the infividual forms on the metadata editor:
   * "basic","parties","geocoverage","taxcoverage","tempcoverage","project","methods","citations","collections",
   * "physical","keywords","additional"
   * 
   * @param action
   * @param eml
   * @param part
   */
  public void validate(BaseAction action, Eml eml, @Nullable String part) {
    if (eml != null) {
      if (part == null || part.equalsIgnoreCase("basic")) {
        // TODO: validate this part
      } else if (part == null || part.equalsIgnoreCase("parties")) {
      } else if (part == null || part.equalsIgnoreCase("geocoverage")) {
      } else if (part == null || part.equalsIgnoreCase("taxcoverage")) {
      } else if (part == null || part.equalsIgnoreCase("tempcoverage")) {
      } else if (part == null || part.equalsIgnoreCase("project")) {
      } else if (part == null || part.equalsIgnoreCase("methods")) {
      } else if (part == null || part.equalsIgnoreCase("citations")) {
      } else if (part == null || part.equalsIgnoreCase("collections")) {
      } else if (part == null || part.equalsIgnoreCase("physical")) {
      } else if (part == null || part.equalsIgnoreCase("keywords")) {
      } else if (part == null || part.equalsIgnoreCase("additional")) {
      }
    }
  }

}
