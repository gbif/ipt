package org.gbif.ipt.model;

import org.gbif.ipt.model.registration.Organisation;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User implements Serializable {
  public enum Role {
    User, Manager, Admin
  }

  private static final long serialVersionUID = 3832626162173359411L;;

  private String email; // unique
  private String password;
  private String firstname;
  private String lastname;
  private Role role = Role.User;
  private Date lastLogin;
  private Set<UUID> associatedOrganisations = new HashSet<UUID>();

  public void addAssociatedOrganisation(Organisation org) {
    if (org != null) {
      this.associatedOrganisations.add(UUID.fromString(org.getKey()));
    }
  }

  public void addAssociatedOrganisation(UUID key) {
    if (key != null) {
      this.associatedOrganisations.add(key);
    }
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
    return equal(email, o.email);
  }

  public Set<UUID> getAssociatedOrganisations() {
    return associatedOrganisations;
  }

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

  public void setAssociatedOrganisations(Set<UUID> associatedOrganisations) {
    this.associatedOrganisations = associatedOrganisations;
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
    if (role == null) {
      this.role = Role.User;
    } else {
      this.role = role;
    }
  }

  public void setRole(String role) {
    if (role != null && role.equalsIgnoreCase("manager")) {
      this.role = Role.Manager;
    } else if (role != null && role.equalsIgnoreCase("admin")) {
      this.role = Role.Admin;
    } else {
      this.role = Role.User;
    }
  }
}
