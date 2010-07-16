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
package org.gbif.ipt.model;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * TODO: Documentation.
 * 
 */
public class Vocabulary implements Comparable {
	private URL url;
	private String uri;
	private String title;
	private String description;
	private String subject;
	private URL link;
	private List<VocabularyConcept> concepts = new LinkedList<VocabularyConcept>();
	private Date lastUpdate = new Date();



	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public URL getLink() {
		return link;
	}

	public void setLink(URL link) {
		this.link = link;
	}
	public void setLink(String link) {
		URL url;
		try {
			url = new URL(link);
			this.link = url;
		} catch (MalformedURLException e) {
		}
	}
	public List<VocabularyConcept> getConcepts() {
		return concepts;
	}

	public void setConcepts(List<VocabularyConcept> concepts) {
		this.concepts = concepts;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void addConcept(VocabularyConcept concept) {
		if (concepts == null) {
			concepts = new LinkedList<VocabularyConcept>();
		}
		//    concept.setVocabulary(this);

		if (concept.getOrder() == -1) {
			// set the order to be the next one
			int maxOrder = 0;
			for (VocabularyConcept tc : concepts) {
				if (tc.getOrder() >= 0 && maxOrder < tc.getOrder()) {
					maxOrder = tc.getOrder();
				}
			}
			concept.setOrder(maxOrder + 1);
		}
		concepts.add(concept);
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		Vocabulary myClass = (Vocabulary) object;
		return new CompareToBuilder()
		.append(this.uri, myClass.uri)
		.append(this.url, myClass.url)
		.toComparison();
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Vocabulary)) {
			return false;
		}
		Vocabulary rhs = (Vocabulary) object;
		return new EqualsBuilder()
		.append(this.uri, rhs.uri)
		.append(this.url, rhs.url)
		.isEquals();
	}


	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(-1469035489, -1512452511)
		.append(this.uri)
		.append(this.url)
		.toHashCode();
	}

}
