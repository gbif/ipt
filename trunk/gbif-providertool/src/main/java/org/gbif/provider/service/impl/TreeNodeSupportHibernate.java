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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.TreeNodeManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 * @param <E>
 */
public class TreeNodeSupportHibernate<T extends TreeNode<T, E>, E extends Enum> {
  protected final Log log = LogFactory.getLog(getClass());
  private final Class persistentClass;
  private final TreeNodeManager<T, E> treeNodeManager;

  public TreeNodeSupportHibernate(final Class<T> persistentClass,
      TreeNodeManager<T, E> treeNodeManager) {
    this.persistentClass = persistentClass;
    this.treeNodeManager = treeNodeManager;
  }

  public void buildNestedSet(Long resourceId, Session session) {
    List<T> rootNodes = this.getRoots(resourceId, session, null);
    log.info(String.format("Building nested set for %s isolated trees",
        rootNodes.size()));
    Long idx = 1L;
    for (T node : rootNodes) {
      idx = crawlChildren(resourceId, node, idx, session);
      log.debug(String.format("Nested set indices for tree %s are %s to %s",
          node.getLabel(), node.getLft(), node.getRgt()));
      treeNodeManager.flush();
    }
  }

  public int countByType(Long resourceId, E type, Session session) {
    return ((Long) session.createQuery(
        String.format(
            "select count(n) from %s n WHERE n.type = :type and n.resource.id = :resourceId",
            persistentClass.getName())).setLong("resourceId", resourceId).setParameter(
        "type", type).iterate().next()).intValue();
  }

  public int countTerminalNodes(Long resourceId, Session session, String filter) {
    return ((Long) session.createQuery(
        String.format(
            "select count(e) from %s e WHERE e.resource.id = :resourceId and e.lft+1=e.rgt and parent is not null %s",
            persistentClass.getSimpleName(), getWhereString(filter))).setLong(
        "resourceId", resourceId).iterate().next()).intValue();
  }

  public int countTreeNodes(Long resourceId, Session session) {
    return ((Long) session.createQuery(
        String.format(
            "select count(n) from %s n WHERE (n.parent is not null OR n.rgt-n.lft>1) AND n.resource.id = :resourceId",
            persistentClass.getName())).setLong("resourceId", resourceId).iterate().next()).intValue();
  }

  public T getByMaterializedPath(Long resourceId, String mpath, Session session) {
    return (T) session.createQuery(
        String.format(
            "select e from %s e WHERE e.resource.id = :resourceId and e.mpath=:mpath",
            persistentClass.getName())).setLong("resourceId", resourceId).setString(
        "mpath", mpath).uniqueResult();
  }

  public List<T> getChildren(Long resourceId, Long parentId, Session session) {
    return session.createQuery(
        String.format(
            "from %s node where node.parent.id = :parentId and node.resource.id = :resourceId order by label",
            persistentClass.getName())).setLong("resourceId", resourceId).setLong(
        "parentId", parentId).list();
  }

  public List<Long> getParentIds(Long resourceId, Long nodeId, Session session) {
    return session.createQuery(
        String.format(
            "SELECT parent.id FROM %s node, %s parent WHERE node.id = :nodeId and node.lft between parent.lft and parent.rgt   ORDER BY parent.lft",
            persistentClass.getName(), persistentClass.getName())).setLong(
        "nodeId", nodeId).list();
  }

  public List<T> getParents(Long resourceId, Long nodeId, Session session) {
    List<T> result = new ArrayList<T>();
    return result;
  }

  /**
   * @param resourceId
   * @param session
   * @param filter additional string to be appended to where clause to match
   *          only some root records
   * @return
   */
  public List<T> getRoots(Long resourceId, Session session, String filter) {
    return session.createQuery(
        String.format(
            "from %s n where n.parent is null and n.resource.id = :resourceId %s  order by n.lft",
            persistentClass.getName(), getWhereString(filter))).setLong(
        "resourceId", resourceId).list();
  }

  @Transactional(readOnly = false)
  public int removeAll(Resource resource, Session session) {
    // use DML-style HQL batch updates
    // http://www.hibernate.org/hib_docs/reference/en/html/batch.html

    // get property name for this entity
    String propName = persistentClass.getSimpleName().toLowerCase();

    // first remove treeNodes from dwcore records
    String hqlUpdate = String.format(
        "update DarwinCore dwc SET dwc.%s=null WHERE dwc.resource = :resource",
        propName);
    session.createQuery(hqlUpdate).setEntity("resource", resource).executeUpdate();

    // now delete region entities
    hqlUpdate = String.format("delete %s reg WHERE reg.resource = :resource",
        persistentClass.getSimpleName());
    int count = session.createQuery(hqlUpdate).setEntity("resource", resource).executeUpdate();
    log.info(String.format("Removed %s %ss bound to resource %s", count,
        propName, resource.getTitle()));
    return count;
  }

  private Long crawlChildren(Long resourceId, T node, Long idx, Session session) {
    node.setLft(idx);
    idx++;
    List<T> children = session.createQuery(
        String.format(
            "from %s node where node.parent.id = :parentId and node.resource.id = :resourceId order by id",
            persistentClass.getName())).setLong("resourceId", resourceId).setLong(
        "parentId", node.getId()).list();
    for (T child : children) {
      idx = crawlChildren(resourceId, child, idx, session);
    }
    node.setRgt(idx);
    idx++;
    treeNodeManager.save(node);
    return idx;
  }

  private String getWhereString(String filter) {
    if (filter == null) {
      filter = "";
    } else if (!filter.trim().toLowerCase().startsWith("and")) {
      filter = " and " + filter;
    }
    return filter;
  }

}
