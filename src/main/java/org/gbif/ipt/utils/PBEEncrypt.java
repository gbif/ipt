/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.utils;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class PBEEncrypt {

  public static class EncryptionException extends Exception {

    private static final long serialVersionUID = 4781222329218307597L;

    public EncryptionException(String text, Exception chain) {
      super(text, chain);
    }
  }

  private static final String ALGORITHM = "PBEWithSHA1AndDESede";

  private final String characterEncoding = "UTF-8";

  private Cipher encryptCipher;

  private Cipher decryptCipher;

  public PBEEncrypt(String passphrase, byte[] salt, int iterationCount) throws EncryptionException {
    assert passphrase != null;
    assert passphrase.length() >= 6;
    assert salt != null;
    assert salt.length == 8;
    assert iterationCount > 6 && iterationCount < 20;

    try {
      PBEParameterSpec params = new PBEParameterSpec(salt, iterationCount);

      KeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
      SecretKey key = SecretKeyFactory.getInstance(ALGORITHM, "SunJCE").generateSecret(keySpec);

      this.encryptCipher = Cipher.getInstance(ALGORITHM, "SunJCE");
      this.encryptCipher.init(Cipher.ENCRYPT_MODE, key, params);

      this.decryptCipher = Cipher.getInstance(ALGORITHM, "SunJCE");
      this.decryptCipher.init(Cipher.DECRYPT_MODE, key, params);
    } catch (Exception e) {
      throw new EncryptionException("Problem constructing " + this.getClass().getName(), e);
    }
  }

  public synchronized String decrypt(String encodedEncryptedDataString) throws EncryptionException {
    assert encodedEncryptedDataString != null;

    try {
      byte[] encryptedDataStringBytes = Base64Coder.decode(encodedEncryptedDataString);
      byte[] dataStringBytes = this.decryptCipher.doFinal(encryptedDataStringBytes);
      return new String(dataStringBytes, characterEncoding);
    } catch (Exception e) {
      throw new EncryptionException("Problem decrypting string", e);
    }
  }

  public synchronized String encrypt(String dataString) throws EncryptionException {
    assert dataString != null;

    try {
      byte[] dataStringBytes = dataString.getBytes(characterEncoding);
      byte[] encryptedDataStringBytes = this.encryptCipher.doFinal(dataStringBytes);
      return String.valueOf(Base64Coder.encode(encryptedDataStringBytes));
    } catch (Exception e) {
      throw new EncryptionException("Problem encrypting string", e);
    }
  }
}
