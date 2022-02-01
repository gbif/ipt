/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
