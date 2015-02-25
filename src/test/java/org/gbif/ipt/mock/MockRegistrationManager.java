package org.gbif.ipt.mock;

import org.gbif.ipt.service.admin.RegistrationManager;

import static org.mockito.Mockito.mock;

/**
 * @author julieth lopez
 */
public class MockRegistrationManager {

  private static RegistrationManager registrationManager = mock(RegistrationManager.class);

  public static RegistrationManager buildMock() {
    return registrationManager;
  }


}
