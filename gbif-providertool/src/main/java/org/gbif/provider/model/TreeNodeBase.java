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

import java.util.LinkedList;
import java.util.Queue;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 * @param <E>
 */
@MappedSuperclass
public abstract class TreeNodeBase<T extends TreeNodeBase, E extends Enum>
    implements BaseObject, TreeNode<T, E>, Comparable<T> {
  protected Long id;
  protected T parent;
  protected String label;
  protected DataResource resource;
  protected BBox bbox = new BBox();
  protected int occTotal;
  protected E type;
  private Long lft;
  private Long rgt;
  private String mpath;

  /**
   * Do a depth first comparison of the taxonomy, comparing 2 taxa on an equal
   * level in the hierarchy via compareWithoutHierarchy(). Natural sortng order
   * therefore is a depth first walk, useful to assign nested set indices in one
   * go
   * 
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(T that) {
    Queue<T> thisTaxonomy = this.getParents();
    Queue<T> thatTaxonomy = that.getParents();
    // compare entire taxonomy incl the leaf nodes
    thisTaxonomy.add((T) this);
    thatTaxonomy.add(that);
    while (true) {
      T thisT = thisTaxonomy.poll();
      T thatT = thatTaxonomy.poll();
      if (thisT != null && thatT != null && !thisT.equals(thatT)) {
        // some parent node in the hierarchy is already different. Compare
        // those!
        return compareWithoutHierarchy(thisT, thatT);
      } else if (thisT == null && thatT == null) {
        return 0;
      } else if (thisT == null) {
        // thisTaxonomy is entirely included in thatTaxonomy, so this should
        // come first, thus is smaller!
        return -1;
      } else if (thatT == null) {
        // thatTaxonomy is entirely included in thisTaxonomy, so this should
        // after that
        return 1;
      } else {
        // ignore equal taxa in the hierarchy.
      }
    }
  }

  /**
   * Count a single occurrence record
   * 
   * @param region
   */
  public void countOcc(DarwinCore dwc) {
    this.occTotal++;
    bbox.expandBox(dwc.getLocation());
  }

  public void expandBox(Point p) {
    getBbox().expandBox(p);
  }

  public BBox getBbox() {
    if (bbox == null) {
      bbox = new BBox();
    }
    return bbox;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public Long getLft() {
    return lft;
  }

  public String getMpath() {
    return mpath;
  }

  public int getOccTotal() {
    return occTotal;
  }

  @ManyToOne(optional = true)
  public T getParent() {
    return parent;
  }

  @Transient
  public Queue<T> getParents() {
    Queue<T> parents;
    if (parent != null) {
      parents = parent.getParents();
      parents.add(parent);
    } else {
      parents = new LinkedList<T>();
    }
    return parents;
  }

  @ManyToOne(optional = false)
  public DataResource getResource() {
    return resource;
  }

  @Transient
  public Long getResourceId() {
    return resource.getId();
  }

  public Long getRgt() {
    return rgt;
  }

  public E getType() {
    return type;
  }

  @Transient
  public Boolean isLeafNode() {
    return lft == null ? false : rgt == lft + 1;
  }

  public void setBbox(BBox bbox) {
    this.bbox = bbox;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setLft(Long lft) {
    this.lft = lft;
  }

  public void setMpath(String mpath) {
    this.mpath = mpath;
  }

  public void setOccTotal(int occTotal) {
    this.occTotal = occTotal;
  }

  public void setParent(T parent) {
    this.parent = parent;
  }

  public void setResource(DataResource resource) {
    this.resource = resource;
  }

  public void setRgt(Long rgt) {
    this.rgt = rgt;
  }

  public void setType(E t) {
    type = t;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String infos = "";
    if (this.type != null && this.getParent() != null) {
      infos = String.format("%s in %s", this.type.toString(),
          getParent().getLabel());
    } else if (this.type != null) {
      infos = String.format("%s", this.type.toString());
    } else if (this.getParent() != null) {
      infos = String.format("%s", getParent().getLabel());
    }
    return String.format("%s (%s)", this.getLabel(), infos);
  }

  abstract int compareWithoutHierarchy(T first, T second);
}
