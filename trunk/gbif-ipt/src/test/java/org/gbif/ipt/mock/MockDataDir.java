package org.gbif.ipt.mock;

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.impl.UserAccountManagerImpl;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class simulates a DataDir object and must only be used for Unit Tests purposes.
 * 
 * @author hftobon
 */
public class MockDataDir {

  private static DataDir dataDir = mock(DataDir.class);

  public static DataDir buildMock() {
    setupMock();
    return dataDir;
  }

  /**
   * All the DataDir methods behavior must be configured in this place.
   */
  private static void setupMock() {
    // user.xml is going to be located in temp directory.
    when(dataDir.configFile(UserAccountManagerImpl.PERSISTENCE_FILE)).thenReturn(
      new File(System.getProperty("java.io.tmpdir") + File.separatorChar + UserAccountManagerImpl.PERSISTENCE_FILE));
  }

}
