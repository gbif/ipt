/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.validation;

import java.util.Date;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseValidator {

  private static final Logger LOG = LogManager.getLogger(BaseValidator.class);

  protected boolean exists(String x) {
    return exists(x, 2);
  }

  protected boolean exists(Integer i) {
    return i != null;
  }

  protected boolean exists(Date d) {
    return d != null;
  }

  protected boolean exists(String x, int minLength) {
    return x != null && x.trim().length() >= minLength;
  }

  /**
   * Checks if string has a length in a certain range.
   *
   * @param x         incoming string
   * @param minLength min length inclusive
   * @param maxLength max length inclusive
   *
   * @return true if string
   */
  protected boolean existsInRange(String x, int minLength, int maxLength) {
    x = StringUtils.trimToNull(x);
    return x != null && x.length() >= minLength && x.length() <= maxLength;
  }

  protected boolean isValidEmail(String email) {
    if (email != null) {
      try {
        InternetAddress internetAddress = new InternetAddress(email);
        internetAddress.validate();
        return true;
      } catch (javax.mail.internet.AddressException e) {
        LOG.debug("Email address was invalid: " + StringUtils.trimToEmpty(email));
      }
    }
    return false;
  }
}
