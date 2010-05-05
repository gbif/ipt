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

import static com.google.common.base.Objects.equal;

import org.gbif.provider.model.Address;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * Encapsulates all the information for an Agent
 */
public class Agent implements Serializable {
  private static final long serialVersionUID = 7028536657833651816L;

  private String firstName;
  private String lastName;
  private String organisation;
  private String position;
  private Address address = new Address();
  private String phone;
  private String email;
  private Role role;
  private String homepage;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Agent)) {
      return false;
    }
    Agent o = (Agent) other;
    return equal(firstName, o.firstName) && equal(lastName, o.lastName)
        && equal(organisation, o.organisation) && equal(position, o.position)
        && equal(address, o.address) && equal(phone, o.phone)
        && equal(email, o.email) && equal(role, o.role)
        && equal(homepage, o.homepage);
  }

  public Address getAddress() {
    return address;
  }

  public String getEmail() {
    if(email == null || email.length()==0) return null;
    return email;
  }

  public String getFirstName() {
    if(firstName == null || firstName.length()==0) return null;
    return firstName;
  }

  public String getHomepage() {
    if(homepage == null || homepage.length()==0) return null;
    return homepage;
  }

  public String getLastName() {
    if(lastName == null || lastName.length()==0) return null;
    return lastName;
  }

  public String getOrganisation() {
    if(organisation == null || organisation.length()==0) return null;
    return organisation;
  }

  public String getPhone() {
    if(phone == null || phone.length()==0) return null;
    return phone;
  }

  public String getPosition() {
    if(position == null || position.length()==0) return null;
    return position;
  }

  public Role getRole() {
    return role;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(firstName, lastName, organisation, position,
        address, phone, email, role);
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

  public void setRole(String roleStr) {
    role = Role.fromString(roleStr);
  }

  @Override
  public String toString() {
    return String.format("FirstName=%s, LastName=%s, Role=%s", firstName, lastName, role);
  }

}