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
package org.gbif.provider.model.eml;

import org.appfuse.model.Address;

import java.io.Serializable;

/**
 * TODO: Documentation.
 * 
 */
public class Agent implements Serializable {
  private String firstName;
  private String lastName;
  private String organisation;
  private String position;
  private Address address = new Address();
  private String phone;
  private String email;
  private Role role;
  private String homepage;

  public Address getAddress() {
    return address;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getHomepage() {
    return homepage;
  }

  public String getLastName() {
    return lastName;
  }

  public String getOrganisation() {
    return organisation;
  }

  public String getPhone() {
    return phone;
  }

  public String getPosition() {
    return position;
  }

  public Role getRole() {
    return role;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setHomepage(String homepage) {
    this.homepage = homepage;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setOrganisation(String organisation) {
    this.organisation = organisation;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public void setRole(Role role) {
    this.role = role;
  }

}
