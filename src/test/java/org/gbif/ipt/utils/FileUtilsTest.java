package org.gbif.ipt.utils;

import org.gbif.ipt.service.admin.impl.ExtensionManagerImpl;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileUtilsTest {

  @Test
  public void testGetSuffixedFileName() {
    assertEquals("http_rs_gbif_org_vocabulary_gbif_rank_xml.vocab", FileUtils
      .getSuffixedFileName("http://rs.gbif.org/vocabulary/gbif/rank.xml", VocabulariesManagerImpl.VOCAB_FILE_SUFFIX));
    assertEquals("http_rs_gbif_org_extension_gbif_1_0_multimedia_xml.xml", FileUtils
      .getSuffixedFileName("http://rs.gbif.org/extension/gbif/1.0/multimedia.xml",
        ExtensionManagerImpl.EXTENSION_FILE_SUFFIX));
  }
}
