package org.gbif.ipt.utils;


import org.gbif.api.model.common.DOI;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;

import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

public class DOIUtils {

  protected static final Logger LOG = Logger.getLogger(DOIUtils.class);

  /*
   * Empty constructor.
   */
  private DOIUtils() {
  }

  /**
   * Mint DOI that is allowed in organisation's namespace. Construction varies between EZID and  DataCite, because
   * EZID prefixes include a shoulder (e.g. "/FK2").
   *
   * @param agency DOI registration agency
   * @param prefix DOI prefix
   *
   * @return DOI object constructed
   */
  @NotNull
  public static DOI mintDOI(DOIRegistrationAgency agency, String prefix) {
    Preconditions.checkNotNull(agency);
    Preconditions.checkNotNull(prefix);

    // generate random alphanumeric string 6 characters long, lower case
    String suffix = RandomStringUtils.randomAlphanumeric(6).toLowerCase();

    // EZID shoulder contains forward slash "/", so handle construction differently to DataCite construction
    return (agency.equals(DOIRegistrationAgency.EZID)) ? new DOI(prefix + suffix) : new DOI(prefix, suffix);
  }
}
