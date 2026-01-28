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

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

public class User implements Serializable, Cloneable {

  public enum Role {
    User, Manager, Publisher, Admin
  }

  @Serial
  private static final long serialVersionUID = 3832626162173359411L;

  private String email; // unique
  @Setter
  @Getter
  private String password;
  @Setter
  @Getter
  private String firstname;
  @Setter
  @Getter
  private String lastname;
  @Getter
  private Role role = Role.User;
  @Setter
  @Getter
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

  public String getName() {
    return StringUtils.trimToNull(StringUtils.trimToEmpty(firstname) + " " + StringUtils.trimToEmpty(lastname));
  }

  public String getInitials() {
    String initials = "A";
    if (StringUtils.isNotEmpty(firstname) && StringUtils.isNotEmpty(lastname)) {
      initials = "" + firstname.charAt(0) + lastname.charAt(0);
    }
    return initials.toUpperCase();
  }

  /**
   * Called in login page, redirected to on failed logins.
   */
  public String getNameWithEmail() {
    return StringUtils.trimToNull(getName() + " <" + email + ">");
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

  public void setLastLoginToNow() {
    this.lastLogin = new Date();
  }

  public void setRole(Role role) {
    this.role = role == null ? Role.User : role;
  }

  @Override
  public String toString() {
    return "User " + email;
  }
}
