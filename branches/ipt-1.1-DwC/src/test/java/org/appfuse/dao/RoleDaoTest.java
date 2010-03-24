package org.appfuse.dao;

import java.util.HashMap;
import java.util.List;

import org.gbif.provider.dao.RoleDao;
import org.gbif.provider.model.Role;
import org.gbif.provider.util.Constants;

public class RoleDaoTest extends BaseDaoTestCase {
  private RoleDao dao;

  public void setRoleDao(RoleDao dao) {
    this.dao = dao;
  }

  public void testAddAndRemoveRole() throws Exception {
    Role role = new Role("testrole");
    role.setDescription("new role descr");
    dao.save(role);
    flush();

    role = dao.getRoleByName("testrole");
    assertNotNull(role.getDescription());

    dao.removeRole("testrole");
    flush();

    role = dao.getRoleByName("testrole");
    assertNull(role);
  }

  /**
   * Tests the generic findByNamedQuery method
   * 
   * @throws Exception
   */
  public void testFindByNamedQuery() throws Exception {
    HashMap<String, Object> queryParams = new HashMap<String, Object>();
    queryParams.put("name", Constants.USER_ROLE);
    List<Role> roles = dao.findByNamedQuery("findRoleByName", queryParams);
    assertNotNull(roles);
    assertTrue(roles.size() > 0);
  }

  public void testGetRole() throws Exception {
    Role role = dao.getRoleByName(Constants.USER_ROLE);
    assertNotNull(role);
  }

  public void testGetRoleInvalid() throws Exception {
    Role role = dao.getRoleByName("badrolename");
    assertNull(role);
  }

  public void testUpdateRole() throws Exception {
    Role role = dao.getRoleByName("ROLE_USER");
    role.setDescription("test descr");
    dao.save(role);
    flush();

    role = dao.getRoleByName("ROLE_USER");
    assertEquals("test descr", role.getDescription());
  }
}
