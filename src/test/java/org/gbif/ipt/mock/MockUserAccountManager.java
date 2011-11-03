package org.gbif.ipt.mock;

import org.gbif.ipt.service.admin.UserAccountManager;

import static org.mockito.Mockito.mock;

/**
 * @author julieth lopez
 */
public class MockUserAccountManager {

  private static UserAccountManager userAccountManager = mock(UserAccountManager.class);

  public static UserAccountManager buildMock() {
    return userAccountManager;
  }


}
