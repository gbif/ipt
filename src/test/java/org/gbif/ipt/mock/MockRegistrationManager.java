package org.gbif.ipt.mock;

import org.gbif.ipt.service.admin.RegistrationManager;

import static org.mockito.Mockito.mock;

public class MockRegistrationManager {

  private static RegistrationManager registrationManager = mock(RegistrationManager.class);

  public static RegistrationManager buildMock() {
    return registrationManager;
  }


}
