package org.gbif.provider.model;

import java.util.LinkedList;
import java.util.Queue;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class TreeNodeBase<T extends TreeNodeBase, E extends Enum> implements BaseObject, TreeNode<T, E>, Comparable<T>{
	private Long id;
	private T parent;
	private String label;
	private Long lft;
	private Long rgt;
	private E type;
	private String mpath;
	protected DataResource resource;
	private BBox bbox = new BBox();
	private int occTotal;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(optional = true)
	public T getParent() {
		return parent;
	}
	public void setParent(T parent) {
		this.parent = parent;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public E getType() {
		return type;
	}
	public void setType(E t) {
		type = t;
	}

	public Long getLft() {
		return lft;
	}
	public void setLft(Long lft) {
		this.lft = lft;
	}

	public Long getRgt() {
		return rgt;
	}
	public void setRgt(Long rgt) {
		this.rgt = rgt;
	}
	
	public String getMpath() {
		return mpath;
	}
	public void setMpath(String mpath) {
		this.mpath = mpath;
	}
	
	@Transient
	public Queue<T> getParents() {
		Queue<T> parents;
		if (parent!=null){
			parents = parent.getParents();
			parents.add(parent);
		}else{
			parents = new LinkedList<T>();
		}
		return parents;
	}
	
	@Transient
	public Boolean isLeafNode() {
		return lft==null ? false : rgt == lft + 1;
	}
	
	abstract int compareWithoutHierarchy(T first, T second);

	/**
	 * Do a depth first comparison of the taxonomy, comparing 2 taxa on an equal level in the hierarchy via compareWithoutHierarchy().
	 * Natural sortng order therefore is a depth first walk, useful to assign nested set indices in one go
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(T that) {
		Queue<T> thisTaxonomy = this.getParents();
		Queue<T> thatTaxonomy = that.getParents();
		// compare entire taxonomy incl the leaf nodes
		thisTaxonomy.add((T) this);
		thatTaxonomy.add(that);
		while(true){
			T thisT = thisTaxonomy.poll();
			T thatT = thatTaxonomy.poll();
			if (thisT != null && thatT != null && !thisT.equals(thatT)){
				// some parent node in the hierarchy is already different. Compare those!
				return compareWithoutHierarchy(thisT, thatT);
			}else if (thisT==null && thatT==null){
				return 0;
			}else if (thisT==null){
				// thisTaxonomy is entirely included in thatTaxonomy, so this should come first, thus is smaller!
				return -1; 						
			}else if (thatT==null){
				// thatTaxonomy is entirely included in thisTaxonomy, so this should after that
				return 1;
			}else{
				// ignore equal taxa in the hierarchy.
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String infos = "";
		if (this.type != null && this.getParent() != null){
			infos = String.format("%s in %s", this.type.toString(), getParent().getLabel());
		}else if (this.type != null){
			infos = String.format("%s", this.type.toString());
		}else if (this.getParent() != null){
			infos = String.format("%s", getParent().getLabel());
		}
		return String.format("%s (%s)", this.getLabel(), infos);
	}
	
	@ManyToOne(optional = false)
	public DataResource getResource() {
		return resource;
	}
	public void setResource(DataResource resource) {
		this.resource = resource;
	}
	@Transient
	public Long getResourceId() {
		return resource.getId();
	}
	public BBox getBbox() {
		if (bbox==null){
			bbox = new BBox();
		}
		return bbox;
	}
	public void setBbox(BBox bbox) {
		this.bbox = bbox;
	}
	public void expandBox(Point p) {
		getBbox().expandBox(p);
	}
	public int getOccTotal() {
		return occTotal;
	}
	public void setOccTotal(int occTotal) {
		this.occTotal = occTotal;
	}
	/**
	 * Count a single occurrence record
	 * @param region
	 */
	public void countOcc(DarwinCore dwc) {
		this.occTotal++;
		bbox.expandBox(dwc.getLocation());
	}
}
