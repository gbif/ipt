package org.gbif.provider.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.hibernate.validator.NotNull;

@Entity
public class Annotation implements ResourceRelatedObject{
	private Long id;
	private String guid;
	private String sourceId;
	@NotNull
	private Resource resource;
	@NotNull
	private String type;
	private String note;
	private Integer probability;
	private Map<ExtensionProperty, String> proposal = new HashMap<ExtensionProperty, String>();
	private boolean removeDuringImport = false;
	private String creator;
	private Date created = new Date();
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	/**The GUID of the annotated record
	 * @return
	 */
	@Column(length=128)
	@org.hibernate.annotations.Index(name="annotation_guid")
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	@Column(length=128)
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
	@ManyToOne
	public Resource getResource() {
		return resource;
	}
	@Transient
	public Long getResourceId() {
		return resource.getId();
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	@Column(length=32)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getProbability() {
		return probability;
	}
	public void setProbability(Integer probability) {
		this.probability = probability;
	}
	@Lob
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}

//	@MapKey(columns = @Column(name = "property_fk"))
	@CollectionOfElements
	public Map<ExtensionProperty, String> getProposal() {
		return proposal;
	}
	public void setProposal(Map<ExtensionProperty, String> proposal) {
		this.proposal = proposal;
	}
	
	public boolean isRemoveDuringImport() {
		return removeDuringImport;
	}
	public void setRemoveDuringImport(boolean removeDuringImport) {
		this.removeDuringImport = removeDuringImport;
	}
	
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	@Column(length=64)
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String toString(){
		return String.format("%s [%s] %s - %s", this.note, this.type, this.creator, this.created);
	}
	
}
