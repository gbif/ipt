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
import java.util.List;

import com.google.common.base.Objects;

/**
 * Encapsulates all the information for an Organisation
 */
public class Registry implements Serializable {

	private List<Organisation> iptOrganisations;
	private String iptKey;
	private String iptPassword;

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Registry)) {
			return false;
		}
		Registry o = (Registry) other;
		return equal(iptKey, o.iptKey) && equal(iptPassword, o.iptPassword) && equal(iptOrganisations, o.iptOrganisations);
	}

	/**
	 * @return the iptOrganisations
	 */
	public List<Organisation> getIptOrganisations() {
		if(iptOrganisations == null || iptOrganisations.size() == 0)
			return null;
		return iptOrganisations;
	}

	/**
	 * @param iptOrganisations the iptOrganisations to set
	 */
	public void setIptOrganisations(List<Organisation> iptOrganisations) {
		this.iptOrganisations = iptOrganisations;
	}

	/**
	 * @return the iptKey
	 */
	public String getIptKey() {
		if(iptKey == null || iptKey.length() == 0)
			return null;		
		return iptKey;
	}

	/**
	 * @param iptKey the iptKey to set
	 */
	public void setIptKey(String iptKey) {
		this.iptKey = iptKey;
	}

	/**
	 * @return the iptPassword
	 */
	public String getIptPassword() {
		if(iptPassword == null || iptPassword.length() == 0)
			return null;				
		return iptPassword;
	}

	/**
	 * @param iptPassword the iptPassword to set
	 */
	public void setIptPassword(String iptPassword) {
		this.iptPassword = iptPassword;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(iptOrganisations, iptKey, iptPassword);
	}

	@Override
	public String toString() {
		return String.format("iptKey=%s, iptPassword=%s", iptKey, iptPassword);
	}

}