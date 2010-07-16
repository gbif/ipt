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
package org.gbif.ipt.model.registry;

import static com.google.common.base.Objects.equal;
import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * Encapsulates all the information for an Organisation
 */
public class Organisation implements Serializable {

	private String key;
	private String name;
	private String password;

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Organisation)) {
			return false;
		}
		Organisation o = (Organisation) other;
		return equal(key, o.key) && equal(name, o.name) && equal(password, o.password);
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		if (key == null || key.length() == 0)
			return null;
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		if (name == null || name.length() == 0)
			return null;
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		if(password == null || password.length() == 0)
			return null;
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(key, name, password);
	}

	@Override
	public String toString() {
		return String.format("Key=%s, Name=%s, Password=%s", key, name, password);
	}

}