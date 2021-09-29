package org.gbif.ipt.utils;

import org.gbif.ipt.config.IPTModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PBEEncryptTest {

  /**
   * Since we're using symmetric encryption with a fixed salt (defined in IPTModule),
   * the hash below can be used to reset a forgotten admin password.
   */
  @Test
  public void encryptPassword() throws Exception {
    final PBEEncrypt encrypter = new IPTModule().providePasswordEncryption();

    String password = "Ga_1bxiedrvNHSyK";

    String encrypted = encrypter.encrypt(password);
    assertEquals("VRRUXOTCtdCkQr40SrHdrnUJurTOYMW9", encrypted);

    String decrypted = encrypter.decrypt(encrypted);
    assertEquals(password, decrypted);
  }
}
