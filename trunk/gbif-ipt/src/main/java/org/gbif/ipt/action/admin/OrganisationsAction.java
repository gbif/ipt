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
import com.google.inject.servlet.SessionScoped;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Action responsible for all user input relating to the organisations allowed in the IPT
 * 
 * @author tim
 */
public class OrganisationsAction extends POSTAction {
  @SessionScoped
  public static class RegisteredOrganisations {
    private List<Organisation> organisations = new ArrayList<Organisation>();
    private GBIFRegistryManager registryManager;

    @Inject
    public RegisteredOrganisations(GBIFRegistryManager registryManager) {
      super();
      this.registryManager = registryManager;
    }

    public boolean isLoaded() {
      if (organisations.size() > 0) {
        return true;
      }
      return false;
    }

    public void load() throws RuntimeException {
      log.debug("getting list of organisations from registry");
      organisations = registryManager.listAllOrganisations();
      log.debug("organisations returned: " + organisations.size());
    }

  }

  private static final long serialVersionUID = 7297470324204084809L;

  private GBIFRegistryManager registryManager;
  private OrganisationsManager organisationsManager;
  private OrganisationSupport organisationValidation;

  private Organisation organisation;
  private List<Organisation> linkedOrganisations;
  private RegisteredOrganisations orgSession;

  @Inject
  public OrganisationsAction(GBIFRegistryManager registryManager, OrganisationsManager organisationsManager,
      OrganisationSupport organisationValidation, RegisteredOrganisations orgSession) {
    this.registryManager = registryManager;
    this.organisationsManager = organisationsManager;
    this.organisationValidation = organisationValidation;
    this.orgSession = orgSession;
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
    return orgSession.organisations;
  }

  public String list() {
    return SUCCESS;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    // load orgs from registry if not done yet
    if (!orgSession.isLoaded()) {
      orgSession.load();
    }
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

  @Override
  public void validate() {
    if (isHttpPost()) {
      organisationValidation.validate(this, organisation);
    }
  }

}
