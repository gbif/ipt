package org.gbif.ipt.model.legacy;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Encapsulates all the information for legacy registration, using LegacyOrganisation and LegacyIpt objects meaning
 * that the passwords for these objects hasn't been encrypted when persisted to registration configuration.
 */
public class LegacyRegistration implements Serializable {

  private static final long serialVersionUID = 899864527700L;

  private SortedMap<String, LegacyOrganisation> associatedOrganisations = new TreeMap<String, LegacyOrganisation>();
  private LegacyOrganisation hostingOrganisation;
  private LegacyIpt ipt;

  /**
   * @return the associatedOrganisations
   */
  public SortedMap<String, LegacyOrganisation> getAssociatedOrganisations() {
    return associatedOrganisations;
  }

  /**
   * @return the hostingOrganisation
   */
  public LegacyOrganisation getHostingOrganisation() {
    return hostingOrganisation;
  }

  /**
   * @return the ipt
   */
  public LegacyIpt getIpt() {
    return ipt;
  }

  /**
   * @return the iptPassword
   */
  public String getIptPassword() {
    return ipt.getWsPassword();
  }

  /**
   * @param associatedOrganisations the associatedOrganisations to set
   */
  public void setAssociatedOrganisations(SortedMap<String, LegacyOrganisation> associatedOrganisations) {
    this.associatedOrganisations = associatedOrganisations;
  }

  /**
   * @param hostingOrganisation the hostingOrganisation to set
   */
  public void setHostingOrganisation(LegacyOrganisation hostingOrganisation) {
    this.hostingOrganisation = hostingOrganisation;
  }

  /**
   * @param ipt the ipt to set
   */
  public void setIpt(LegacyIpt ipt) {
    this.ipt = ipt;
  }

  /**
   * @param iptPassword the iptPassword to set
   */
  public void setIptPassword(String iptPassword) {
    this.ipt.setWsPassword(iptPassword);
  }

}
