package org.gbif.ipt.mock;

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.impl.UserAccountManagerImpl;

import java.io.File;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
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
  private static String tempDir = System.getProperty("java.io.tmpdir");

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
      new File(tempDir + File.separatorChar + UserAccountManagerImpl.PERSISTENCE_FILE));

    // resource.xml is going to be located in temp directory.
    when(dataDir.resourceFile(any(Resource.class), anyString())).thenAnswer(new Answer<File>() {

      public File answer(InvocationOnMock invocation) throws Throwable {
        // create a file in OS temp directory named as shortName-resource.xml
        Resource resource = (Resource) invocation.getArguments()[0];
        String xmlName = (String) invocation.getArguments()[1];
        if (resource != null && !xmlName.equals("")) {
          return new File(tempDir + File.separatorChar + resource.getShortname() + "-" + xmlName);
        } else {
          return null;
        }
      }
    });

    when(dataDir.resourceFile(anyString(), anyString())).thenAnswer(new Answer<File>() {

      public File answer(InvocationOnMock invocation) throws Throwable {
        // create a file in OS temp directory named as shortName-resource.xml
        String shortname = (String) invocation.getArguments()[0];
        String xmlName = (String) invocation.getArguments()[1];
        return new File(tempDir + File.separatorChar + shortname + "-" + xmlName);
      }
    });
  }
}
