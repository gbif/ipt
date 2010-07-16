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

import java.net.URL;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.ipt.utils.CompactHashSet;


/**
 * A single, identifiable concept in a vocabulary.
 * For example "DE" is an identifier for the concept of Germany, 
 * while "Germany" is the preferred term or representation used in the English language.  
 * "Deutschland" represents the preferred term in German, but both Germany and Deutschland are simply
 * terms representing a single concept in a vocabulary. 
 */
public class VocabularyConcept implements Comparable{
	private static Log log = LogFactory.getLog(VocabularyConcept.class);

	private Vocabulary vocabulary;
	private String identifier;  // usually short, e.g. DE
	private String uri; // a URI denoting the concept, mostly used in rdf
	private URL link;  // web link to some more human documentation
	private int order=-1; // to maintain any custom order not based on a natural concept property
	private Set<VocabularyTerm> terms = new CompactHashSet<VocabularyTerm>();
	
	public Vocabulary getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(Vocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public URL getLink() {
		return link;
	}

	public void setLink(URL link) {
		this.link = link;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Set<VocabularyTerm> getTerms() {
		return terms;
	}

	public void setTerms(Set<VocabularyTerm> terms) {
		this.terms = terms;
	}

	public void addTerm(VocabularyTerm term) {
		terms.add(term);
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		VocabularyConcept myClass = (VocabularyConcept) object;
		return new CompareToBuilder()
		.append(this.vocabulary, myClass.vocabulary)
		.append(this.order, myClass.order)
		.append(this.uri, myClass.uri)
		.toComparison();
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof VocabularyConcept)) {
			return false;
		}
		VocabularyConcept rhs = (VocabularyConcept) object;
		return new EqualsBuilder()
		.append(this.vocabulary, rhs.vocabulary)
		.append(this.order, rhs.order)
		.append(this.uri, rhs.uri)
		.isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(-1161377931, -1626651913)
		.append(this.vocabulary)
		.append(this.order)
		.append(this.uri)    
		.toHashCode();
	}


	@Override
	public String toString() {
		return identifier;
	}

}
