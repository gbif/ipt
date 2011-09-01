package org.gbif.ipt.mock;

import org.gbif.ipt.config.DataDir;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
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

    try {
      when(dataDir.configFile(anyString())).thenReturn(File.createTempFile("dataDir-", "temp"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
