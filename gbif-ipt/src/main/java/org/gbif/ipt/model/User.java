package org.gbif.ipt.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;


public class User implements Serializable {
    private static final long serialVersionUID = 3832626162173359411L;
    public enum Role {User, Manager, Admin};
    
	private String email; // unique
	private String password;
	private String firstname;
	private String lastname;
	private Role role = Role.User;
	private Date lastLogin;
	
	public String getName() {
		return StringUtils.trimToNull(StringUtils.trimToEmpty(firstname) + " " + StringUtils.trimToEmpty(lastname));
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		if (email!=null){
			email=email.toLowerCase().trim();
		}
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	public void setLastLoginToNow() {
		this.lastLogin = new Date();
	}
	
	/**
	 * @return true if user has admin rights
	 */
	public boolean hasAdminRights() {
		return Role.Admin==this.role;
	}
	/**
	 * @return true if user has manager rights, ie is a manager or admin
	 */
	public boolean hasManagerRights() {
		return !(Role.User==this.role);
	}
	
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		if (role==null){
			this.role = Role.User;
		}else{
			this.role = role;
		}
	}
	public void setRole(String role) {
		if(role!=null && role.equalsIgnoreCase("manager")){
			this.role = Role.Manager;
		}else if(role!=null && role.equalsIgnoreCase("admin")){
			this.role = Role.Admin;
		}else{
			this.role = Role.User;
		}
	}

}
