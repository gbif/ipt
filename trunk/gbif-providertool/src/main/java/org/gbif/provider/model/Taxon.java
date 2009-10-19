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
package org.gbif.provider.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.voc.Rank;
import org.hibernate.annotations.Index;
import org.hibernate.validator.NotNull;

/**
 * TODO: Documentation.
 * 
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "sourceId",
    "resource_fk" }) })
@org.hibernate.annotations.Table(appliesTo = "Taxon", indexes = {
    @Index(name = "tax_label", columnNames = { "label" }),
    @Index(name = "tax_lft", columnNames = { "lft" }),
    @Index(name = "tax_rgt", columnNames = { "rgt" }) })
public class Taxon extends TreeNodeBase<Taxon, Rank> implements CoreRecord {
  protected static final Log log = LogFactory.getLog(Taxon.class);

  public static Taxon newInstance(DataResource resource) {
    Taxon tax = new Taxon();
    tax.resource = resource;
    tax.modified = new Date();
    tax.isDeleted = false;
    tax.setGuid(UUID.randomUUID().toString());
    return tax;
  }

  // for core record
  private String sourceId;
  @NotNull
  private String guid;
  private String link;
  private boolean isDeleted;
  private Date modified = new Date();

  // taxon specific
  // private String taxonID; --> this.guid
  private String scientificNameID;
  private String nameAccordingToID;
  private String namePublishedInID;
  private String taxonConceptID;

  // private String scientificName; --> this.label

  private String nameAccordingTo;
  // deprecated private String taxonAccordingTo;
  private String namePublishedIn;

  // deprecated private String binomial;
  // private String kingdom;
  // private String phylum;
  // private String classs;
  // private String order;
  // private String family;
  // private String genus;
  // private String subgenus;
  private String specificEpithet;
  private String infraspecificEpithet;
  private String taxonRank;
  private String verbatimTaxonRank;
  private String scientificNameAuthorship;
  private String vernacularName;
  private String nomenclaturalCode;
  private String taxonomicStatus;
  private String nomenclaturalStatus;
  private String taxonRemarks;

  // deprecated private Taxon higherTaxon; --> this.parent
  // private Taxon parentNameUsage; -- this.parent
  // deprecated private String higherTaxonID;
  // private String parentNameUsageID;
  // depreacated private String higherTaxon;
  // private String higherClassification;

  private Taxon acc;
  // deprecated private String acceptedTaxonID;
  // private String acceptedNameUsageID;
  // deprecated private String acceptedTaxon;
  // private String acceptedNameUsage;

  private Taxon bas;

  // deprecated private String basionymID;
  // private String originalNameUsageID;
  // deprecated private String basionym;
  // private String originalNameUsage;

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Taxon)) {
      return false;
    }
    Taxon rhs = (Taxon) object;
    return new EqualsBuilder().append(this.getLabel(), rhs.getLabel()).append(
        this.taxonRank, rhs.taxonRank).append(this.nameAccordingTo,
        rhs.nameAccordingTo).append(this.getParent(), rhs.getParent()).append(
        this.getId(), rhs.getId()).isEquals();
  }

  @ManyToOne(optional = true)
  public Taxon getAcc() {
    return acc;
  }

  @Transient
  public String getAcceptedTaxon() {
    if (acc != null) {
      return acc.label;
    }
    return null;
  }

  @Transient
  public String getAcceptedTaxonID() {
    if (acc != null) {
      return acc.guid;
    }
    return null;
  }

  @ManyToOne(optional = true)
  public Taxon getBas() {
    return bas;
  }

  @Transient
  public String getBasionym() {
    if (bas != null) {
      return bas.label;
    }
    return null;
  }

  @Transient
  public String getBasionymID() {
    if (bas != null) {
      return bas.guid;
    }
    return null;
  }

  // deprecated
  // public String getBinomial() {
  // return binomial;
  // }

  @Transient
  public Long getCoreId() {
    return super.getId();
  }

  @Transient
  public Rank getDwcRank() {
    return super.getType();
  }

  @Column(length = 128, unique = true)
  @org.hibernate.annotations.Index(name = "tax_guid")
  public String getGuid() {
    return guid;
  }

  @Transient
  public String getHigherTaxon() {
    if (parent != null) {
      return parent.label;
    }
    return null;
  }

  @Transient
  public String getHigherTaxonID() {
    if (parent != null) {
      return parent.guid;
    }
    return null;
  }

  @Column(length = 128)
  public String getInfraspecificEpithet() {
    return infraspecificEpithet;
  }

  public String getLink() {
    return link;
  }

  public Date getModified() {
    return modified;
  }

  // deprecated in favor of nameAccordingTo
  // public String getTaxonAccordingTo() {
  // return taxonAccordingTo;
  // }
  public String getNameAccordingTo() {
    return nameAccordingTo;
  }

  @Column(length = 128)
  public String getNameAccordingToID() {
    return nameAccordingToID;
  }

  public String getNamePublishedIn() {
    return namePublishedIn;
  }

  @Column(length = 128)
  public String getNamePublishedInID() {
    return namePublishedInID;
  }

  @Column(length = 64)
  public String getNomenclaturalCode() {
    return nomenclaturalCode;
  }

  @Transient
  @Deprecated
  public String getNomenclaturalReference() {
    return namePublishedIn;
  }

  @Column(length = 128)
  public String getNomenclaturalStatus() {
    return nomenclaturalStatus;
  }

  @Transient
  public String getPropertyValue(ExtensionProperty property) {
    String propName = property.getName();
    if (propName.equals("Class")) {
      return null;
    }
    String getter = String.format("get%s", propName);
    String value = null;
    try {
      Method m = this.getClass().getMethod(getter);
      Object obj = m.invoke(this);
      if (obj != null) {
        value = obj.toString();
      }
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    } catch (IllegalArgumentException e) {
    } catch (IllegalAccessException e) {
    } catch (InvocationTargetException e) {
    }
    return value;
  }

  @Transient
  @Deprecated
  public String getRank() {
    return taxonRank;
  }

  @Transient
  public String getScientificName() {
    return getLabel();
  }

  public String getScientificNameAuthorship() {
    return scientificNameAuthorship;
  }

  @Column(length = 128)
  public String getScientificNameID() {
    return scientificNameID;
  }

  @Column(length = 64)
  @org.hibernate.annotations.Index(name = "tax_source_id")
  public String getSourceId() {
    return sourceId;
  }

  @Column(length = 128)
  public String getSpecificEpithet() {
    return specificEpithet;
  }

  @Column(length = 128)
  public String getTaxonConceptID() {
    return taxonConceptID;
  }

  @Transient
  public String getTaxonID() {
    return guid;
  }

  @Column(length = 128)
  public String getTaxonomicStatus() {
    return taxonomicStatus;
  }

  @Column(length = 128)
  public String getTaxonRank() {
    return taxonRank;
  }

  public String getTaxonRemarks() {
    return taxonRemarks;
  }

  public String getVerbatimTaxonRank() {
    return verbatimTaxonRank;
  }

  public String getVernacularName() {
    return vernacularName;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(2136008009, 497664597).append(this.taxonRank)
        .append(this.getLabel()).append(this.nameAccordingTo).append(
            this.getParent()).append(this.getId()).toHashCode();
  }

  @org.hibernate.annotations.Index(name = "tax_deleted")
  public boolean isDeleted() {
    return isDeleted;
  }

  public void setAcc(Taxon acceptedTaxon) {
    this.acc = acceptedTaxon;
  }

  public void setBas(Taxon basionym) {
    this.bas = basionym;
  }

  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public void setDwcRank(Rank dwcRank) {
    super.setType(dwcRank);
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setInfraspecificEpithet(String infraspecificEpithet) {
    this.infraspecificEpithet = infraspecificEpithet;
  }

  // deprecated
  // public void setBinomial(String binomial) {
  // this.binomial = binomial;
  // }

  public void setLink(String link) {
    this.link = link;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  // deprecated in favor of nameAccordingTo
  // public void setTaxonAccordingTo(String taxonAccordingTo) {
  // this.taxonAccordingTo = taxonAccordingTo;
  // }
  public void setNameAccordingTo(String nameAccordingTo) {
    this.nameAccordingTo = nameAccordingTo;
  }

  public void setNameAccordingToID(String nameAccordingToID) {
    this.nameAccordingToID = nameAccordingToID;
  }

  public void setNamePublishedIn(String namePublishedIn) {
    this.namePublishedIn = namePublishedIn;
  }

  public void setNamePublishedInID(String namePublishedInID) {
    this.namePublishedInID = namePublishedInID;
  }

  public void setNomenclaturalCode(String nomenclaturalCode) {
    this.nomenclaturalCode = nomenclaturalCode;
  }

  @Deprecated
  public void setNomenclaturalReference(String nomenclaturalReference) {
    this.namePublishedIn = nomenclaturalReference;
  }

  public void setNomenclaturalStatus(String nomStatus) {
    this.nomenclaturalStatus = nomStatus;
  }

  public boolean setPropertyValue(ExtensionProperty property, String value) {
    try {
      Method m = this.getClass().getMethod(
          String.format("set%s", property.getName()), String.class);
      m.invoke(this, value);
    } catch (SecurityException e) {
      e.printStackTrace();
      return false;
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      return false;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      return false;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      return false;
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Deprecated
  public void setRank(String rank) {
    this.taxonRank = rank;
  }

  @Override
  public void setResource(DataResource resource) {
    this.resource = resource;
  }

  public void setScientificName(String fullname) {
    setLabel(fullname);
  }

  public void setScientificNameAuthorship(String scientificNameAuthorship) {
    this.scientificNameAuthorship = scientificNameAuthorship;
  }

  public void setScientificNameID(String scientificNameID) {
    this.scientificNameID = scientificNameID;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public void setSpecificEpithet(String specificEpithet) {
    this.specificEpithet = specificEpithet;
  }

  public void setTaxonConceptID(String taxonConceptID) {
    this.taxonConceptID = taxonConceptID;
  }

  public void setTaxonID(String taxonID) {
    this.guid = taxonID;
  }

  public void setTaxonomicStatus(String taxStatus) {
    this.taxonomicStatus = taxStatus;
  }

  public void setTaxonRank(String taxonRank) {
    this.taxonRank = taxonRank;
  }

  public void setTaxonRemarks(String taxonRemarks) {
    this.taxonRemarks = taxonRemarks;
  }

  public void setVerbatimTaxonRank(String verbatimTaxonRank) {
    this.verbatimTaxonRank = verbatimTaxonRank;
  }

  public void setVernacularName(String vernacularName) {
    this.vernacularName = vernacularName;
  }

  @Override
  protected int compareWithoutHierarchy(Taxon first, Taxon second) {
    return new CompareToBuilder().append(first.resource, second.resource)
        .append(first.getLabel(), second.getLabel()).toComparison();
  }

}
