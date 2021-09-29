package org.gbif.ipt.mock;

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.impl.UserAccountManagerImpl;

import java.io.File;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class simulates a DataDir object and must only be used for Unit Tests purposes.
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
    when(dataDir.configFile(UserAccountManagerImpl.PERSISTENCE_FILE))
      .thenReturn(new File(tempDir + File.separatorChar + UserAccountManagerImpl.PERSISTENCE_FILE));

    // resource.xml is going to be located in temp directory: tmpDir/shortName/resource.xml
    when(dataDir.resourceFile(any(Resource.class), anyString())).thenAnswer(new Answer<File>() {

      @Override
      public File answer(InvocationOnMock invocation) throws Throwable {
        // create a file in OS temp directory named as shortName-resource.xml
        Resource resource = (Resource) invocation.getArguments()[0];
        String xmlName = (String) invocation.getArguments()[1];
        if (resource != null && resource.getShortname() != null && !(xmlName.length() == 0)) {

          // create tmpDir/shortName folder
          File dir = new File(tempDir, resource.getShortname());
          if (!dir.exists()) {
            dir.mkdir();
          }

          // create tmpDir/shortName/resource.xml folder
          File f = new File(dir, xmlName);
          if (!f.exists()) {
            f.createNewFile();
          }

          return f;
        } else {
          return null;
        }
      }
    });

    // retrieve existing resource.xml file located inside tmpDir/shortName
    when(dataDir.resourceFile(anyString(), anyString())).thenAnswer(new Answer<File>() {

      @Override
      public File answer(InvocationOnMock invocation) throws Throwable {
        String shortname = (String) invocation.getArguments()[0];
        String xmlName = (String) invocation.getArguments()[1];

        // retrieve resource folder
        File dir = new File(tempDir, shortname);
        if (dir.exists()) {
          File f = new File(dir, xmlName);
            if (f.exists()) {
              return f;
            }
          }
        return null;
        }
    });
  }
}
