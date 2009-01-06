package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.TreeNode;

public interface TreeNodeManager<T extends TreeNode<T,E>, E extends Enum> extends GenericResourceRelatedManager<T> {
	public List<T> getRoots(Long resourceId);
	public List<Long> getParentIds(Long resourceId, Long nodeId);
	public List<T> getParents(Long resourceId, Long nodeId);
	public List<T> getChildren(Long resourceId, Long parentId);
	public int countTerminalNodes(Long resourceId);
	public int countTreeNodes(Long resourceId);
	public int countByType(Long resourceId, E type);
	void buildNestedSet(Long resourceId);
	T getByMaterializedPath(Long resourceId, String mpath);
}
