package org.gbif.provider.model;


public interface TreeNode<T extends TreeNode> {
	public Long getId();
	public String getLabel();
	public T getParent();
	public Enum getType();
	public Long getLft();
	public Long getRgt();
	
	public void setLabel(String label);
	public void setParent(T parent);
	public void setType(Enum t);
	public void setLft(Long lft);
	public void setRgt(Long rgt);
}
