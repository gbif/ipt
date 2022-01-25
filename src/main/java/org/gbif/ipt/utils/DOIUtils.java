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
package org.gbif.ipt.utils;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.apache.commons.text.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DOIUtils {

  protected static final Logger LOG = LogManager.getLogger(DOIUtils.class);

  private static final int DOI_SUFFIX_LENGTH = 6;
  private static final String DOI_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  private static final RandomStringGenerator DOI_SUFFIX_GENERATOR =
      new RandomStringGenerator.Builder()
          .selectFrom(DOI_ALLOWED_CHARS.toCharArray())
          .build();

  /*
   * Empty constructor.
   */
  private DOIUtils() {
  }

  /**
   * Mint DOI that is allowed in organisation's namespace.
   *
   * @param agency DOI registration agency
   * @param prefix DOI prefix
   *
   * @return DOI object constructed
   */
  @NotNull
  public static DOI mintDOI(DOIRegistrationAgency agency, String prefix) {
    Objects.requireNonNull(agency);
    Objects.requireNonNull(prefix);

    // generate random alphanumeric string 6 characters long, lower case
    String suffix = DOI_SUFFIX_GENERATOR.generate(DOI_SUFFIX_LENGTH).toLowerCase();

    return new DOI(prefix, suffix);
  }
}
