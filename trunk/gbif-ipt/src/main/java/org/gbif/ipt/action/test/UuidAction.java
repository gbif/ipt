package org.gbif.ipt.action.test;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.registration.BriefOrganisation;

public class UuidAction extends BaseAction {
  private BriefOrganisation org = new BriefOrganisation();

  @Override
  public String execute() {
    return SUCCESS;
  }

  public BriefOrganisation getOrg() {
    return org;
  }

  public void setOrg(BriefOrganisation org) {
    this.org = org;
  }

}
