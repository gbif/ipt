package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.TreeNode;

public interface TreeNodeManager<T extends TreeNode> extends GenericManager<T> {
	public List<T> getRoots(Long resourceId);
	public List<T> getParents(Long resourceId, T node);
	public List<T> getChildren(Long resourceId, T node);
	public List<T> getDescendants(Long resourceId, T node);
}
