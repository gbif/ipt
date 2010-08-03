/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Encapsulates all the information for an Organisation
 */
public class Registration {

  // private List<Organisation> associatedOrganisations;
  private SortedMap<String, Organisation> associatedOrganisations = new TreeMap<String, Organisation>();
  private Organisation hostingOrganisation;
  private String iptPassword;

  /**
   * @return the associatedOrganisations
   */
  public SortedMap<String, Organisation> getAssociatedOrganisations() {
    return associatedOrganisations;
  }

  /**
   * @return the hostingOrganisation
   */
  public Organisation getHostingOrganisation() {
    return hostingOrganisation;
  }

  /**
   * @return the iptPassword
   */
  public String getIptPassword() {
    return iptPassword;
  }

  /**
   * @param associatedOrganisations the associatedOrganisations to set
   */
  public void setAssociatedOrganisations(SortedMap<String, Organisation> associatedOrganisations) {
    this.associatedOrganisations = associatedOrganisations;
  }

  /**
   * @param hostingOrganisation the hostingOrganisation to set
   */
  public void setHostingOrganisation(Organisation hostingOrganisation) {
    this.hostingOrganisation = hostingOrganisation;
  }

  /**
   * @param iptPassword the iptPassword to set
   */
  public void setIptPassword(String iptPassword) {
    this.iptPassword = iptPassword;
  }

}