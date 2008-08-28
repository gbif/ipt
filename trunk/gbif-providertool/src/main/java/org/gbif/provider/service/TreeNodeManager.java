package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.TreeNode;

public interface TreeNodeManager<T extends TreeNode> extends GenericManager<T> {
	public List<T> getRoots(Long resourceId);
	public List<T> getParents(Long resourceId, Long nodeId);
	public List<T> getChildren(Long resourceId, Long parentId);
	public List<T> getDescendants(Long resourceId, Long parentId);
}
