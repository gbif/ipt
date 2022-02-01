/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

public class User implements Serializable, Cloneable {

  public enum Role {
    User, Manager, Publisher, Admin
  }

  private static final long serialVersionUID = 3832626162173359411L;

  private String email; // unique
  private String password;
  private String firstname;
  private String lastname;
  private Role role = Role.User;
  private Date lastLogin;

  @Override
  public Object clone() throws CloneNotSupportedException {
    User clone = (User) super.clone();
    if (clone != null && getLastLogin() != null) {
      clone.setLastLogin((Date) getLastLogin().clone());
    }
    return clone;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof User)) {
      return false;
    }
    User o = (User) other;
    return Objects.equals(email, o.email);
  }

  @NotNull
  public String getEmail() {
    return email;
  }

  public String getFirstname() {
    return firstname;
  }

  public Date getLastLogin() {
    return lastLogin;
  }

  public String getLastname() {
    return lastname;
  }

  public String getName() {
    return StringUtils.trimToNull(StringUtils.trimToEmpty(firstname) + " " + StringUtils.trimToEmpty(lastname));
  }

  /**
   * Called in login page, redirected to on failed logins.
   */
  public String getNameWithEmail() {
    return StringUtils.trimToNull(getName() + " <" + email + ">");
  }

  public String getPassword() {
    return password;
  }

  public Role getRole() {
    return role;
  }

  /**
   * @return true if user has admin rights
   */
  public boolean hasAdminRights() {
    return Role.Admin == this.role;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(email);
  }

  /**
   * @return true if user has manager rights, ie is a manager or admin
   */
  public boolean hasManagerRights() {
    return !(Role.User == this.role);
  }

  /**
   * @return true if a user has rights to register resources with gbif
   */
  public boolean hasRegistrationRights() {
    return Role.Publisher == this.role || Role.Admin == this.role;
  }

  public void setEmail(String email) {
    if (email != null) {
      email = email.toLowerCase().trim();
    }
    this.email = email;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public void setLastLogin(Date lastLogin) {
    this.lastLogin = lastLogin;
  }

  public void setLastLoginToNow() {
    this.lastLogin = new Date();
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setRole(Role role) {
    this.role = role == null ? Role.User : role;
  }

  @Override
  public String toString() {
    return "User " + email;
  }
}
