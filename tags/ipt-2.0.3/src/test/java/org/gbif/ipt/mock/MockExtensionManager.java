package org.gbif.ipt.mock;

import org.gbif.ipt.service.admin.ExtensionManager;

import static org.mockito.Mockito.mock;

public class MockExtensionManager {

  private static ExtensionManager extensionManager = mock(ExtensionManager.class);

  public static ExtensionManager buildMock() {
    setupMock();
    return extensionManager;
  }

  /** Stubbing some methods and assigning some default configurations. */
  private static void setupMock() {

    // TODO All general stubbing implementations for methods, properties, etc., should be here.

  }

}
