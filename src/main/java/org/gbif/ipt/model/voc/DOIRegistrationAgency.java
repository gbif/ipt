package org.gbif.ipt.model.voc;

/**
 * Enumeration that describes the different DOI Registration Agencies the IPT supports.
 */
public enum DOIRegistrationAgency {

  /**
   * DataCite (DataCite REST API).
   *
   * @see <a href="http://www.datacite.org/">http://www.datacite.org/</a>
   */
  DATACITE,

  /**
   * EZID.
   *
   * @see <a href="http://ezid.cdlib.org/">http://ezid.cdlib.org/</a>
   */
  @Deprecated
  EZID
}
