package org.gbif.provider.model;

import java.util.List;
import java.util.Queue;


public interface TreeNode<T extends TreeNode, E extends Enum> extends ResourceRelatedObject {
	public String getLabel();
	public T getParent();
	/**
	 * Retrieves the list of all parental nodes from the top root node down to the direct parent.
	 * @return list of all parental nodes from the top root node down to the immediate parent nodes. First list element is the root node.
	 */
	public Queue<T> getParents();
	public Boolean isLeafNode();
	public E getType();
	public Long getLft();
	public Long getRgt();
	public String getMpath();
	
	public void setLabel(String label);
	public void setParent(T parent);
	public void setType(E t);
	public void setLft(Long lft);
	public void setRgt(Long rgt);
	public void setMpath(String mpath);
}
