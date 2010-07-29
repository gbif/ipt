/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.admin.GBIFRegistryManager;
import org.gbif.ipt.service.admin.OrganisationsManager;
import org.gbif.ipt.validation.OrganisationSupport;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.List;

/**
 * The Action responsible for all user input relating to the organisations allowed in the IPT
 * 
 * @author tim
 */
public class OrganisationsAction extends POSTAction {

  private static final long serialVersionUID = 7297470324204084809L;

  private GBIFRegistryManager registryManager;
  private OrganisationsManager organisationsManager;
  private OrganisationSupport organisationValidation;

  private List<Organisation> organisations;
  private Organisation organisation;
  private List<Organisation> linkedOrganisations;

  @Inject
  public OrganisationsAction(GBIFRegistryManager registryManager, OrganisationsManager organisationsManager,
      OrganisationSupport organisationValidation) {
    this.registryManager = registryManager;
    this.organisationsManager = organisationsManager;
    this.organisationValidation = organisationValidation;
  }

  /**
   * @return the linkedOrganisations
   */
  public List<Organisation> getLinkedOrganisations() {
    return linkedOrganisations;
  }

  /**
   * @return the organisation
   */
  public Organisation getOrganisation() {
    return organisation;
  }

  /**
   * @return the organisations
   */
  public List<Organisation> getOrganisations() {
    return organisations;
  }

  public String list() {
    organisations = registryManager.listAllOrganisations();
    return SUCCESS;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    log.debug("getting list of organisations");
    organisations = registryManager.listAllOrganisations();
    log.debug("organisations returned: " + organisations.size());
    linkedOrganisations = organisationsManager.list();
    if (id != null) {
      // modify existing user
      organisation = organisationsManager.get(id);
    }
    // if no id was submitted we wanted to create a new account
    // if an invalid email was entered, it gets stored in the id field and obviously userManager above cant find a
    // matching user.
    // in that case again provide a new, empty user instance
    if (organisation == null) {
      // reset id
      id = null;
      // create new user
      organisation = new Organisation();
    }
  }

  @Override
  public String save() {

    try {
      addActionMessage("The organisation has been associated to this IPT");
      organisationsManager.add(organisation);
      organisationsManager.save();
      return SUCCESS;
    } catch (IOException e) {
      log.error("The organisation association couldnt be saved: " + e.getMessage(), e);
      addActionError(getText("admin.organisation.saveError"));
      addActionError(e.getMessage());
      return INPUT;
    } catch (AlreadyExistingException e) {
      addActionError(getText("admin.organisation.exists", new String[]{id,}));
      return INPUT;
    }

  }

  /**
   * @param linkedOrganisations the linkedOrganisations to set
   */
  public void setLinkedOrganisations(List<Organisation> linkedOrganisations) {
    this.linkedOrganisations = linkedOrganisations;
  }

  /**
   * @param organisation the organisation to set
   */
  public void setOrganisation(Organisation organisation) {
    this.organisation = organisation;
  }

  /**
   * @param organisations the organisations to set
   */
  public void setOrganisations(List<Organisation> organisations) {
    this.organisations = organisations;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      organisationValidation.validate(this, organisation);
    }
  }

}
