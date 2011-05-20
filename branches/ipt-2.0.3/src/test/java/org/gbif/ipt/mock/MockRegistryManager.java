package org.gbif.ipt.mock;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.registry.RegistryManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

/**
 * @author htobon
 */
public class MockRegistryManager {

  private static RegistryManager registryManager = mock(RegistryManager.class);

  public static RegistryManager buildMock() {
    setupMock();
    return registryManager;
  }

  /**
   * Method stub which simulate the original one: org.gbif.ipt.service.registry.impl.RegistryManager.getVocabularies()
   * 
   * @return A simulated vocabulary list.
   */
  private static List<Vocabulary> getVocabularies() {
    ArrayList<Vocabulary> vocabs = new ArrayList<Vocabulary>();

    return vocabs;
  }

  /** Stubbing some methods and assigning some default configurations. */
  private static void setupMock() {
    // TODO All general stubbing implementations for methods, properties, etc., should be here.
    when(registryManager.getVocabularies()).thenReturn(getVocabularies());

  }
}
