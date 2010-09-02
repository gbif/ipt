/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.GBIFRegistryManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.validation.OrganisationSupport;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * The Action responsible for all user input relating to the organisations allowed in the IPT
 * 
 * @author tim
 */
public class OrganisationsAction extends POSTAction {
  @SessionScoped
  public static class RegisteredOrganisations {
    private List<Organisation> organisations = new ArrayList<Organisation>();
    private RegistryManager registryManager;

    @Inject
    public RegisteredOrganisations(RegistryManager registryManager) {
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

      List<Organisation> tempOrganisations = registryManager.getOrganisations();
      organisations.addAll(tempOrganisations);

      Collections.sort(organisations, new Comparator<Organisation>() {
        public int compare(Organisation org1, Organisation org2) {
          if (org1 == null || org1.getName() == null) {
            return 1;
          }
          if (org2 == null || org2.getName() == null) {
            return -1;
          }
          return org1.getName().compareToIgnoreCase(org2.getName());
        }
      });

      log.debug("organisations returned: " + organisations.size());
    }

  }

  private static final long serialVersionUID = 7297470324204084809L;

  private RegistrationManager registrationManager;
  private ResourceManager resourceManager;
  private OrganisationSupport organisationValidation;

  private Organisation organisation;
  private List<Organisation> linkedOrganisations;
  private RegisteredOrganisations orgSession;

  /**
   * @param registryManager
   * @param registrationManager
   * @param organisationValidation
   * @param orgSession
   */
  @Inject
  public OrganisationsAction(GBIFRegistryManager registryManager, RegistrationManager registrationManager,
      OrganisationSupport organisationValidation, RegisteredOrganisations orgSession, ResourceManager resourceManager) {
    this.registrationManager = registrationManager;
    this.organisationValidation = organisationValidation;
    this.orgSession = orgSession;
    this.resourceManager = resourceManager;
  }

  @Override
  public String delete() {
    boolean canDelete = true;
    if (resourceManager.getResources() != null) {
      for (Resource resource : resourceManager.getResources().values()) {
        if (resource.getOrganisation() != null
            && resource.getOrganisation().getKey().compareTo(UUID.fromString(id)) == 0) {
          canDelete = false;
        }
      }
    }
    try {
      if (canDelete) {
        Organisation removedOrganisation = registrationManager.delete(id);
        if (removedOrganisation == null) {
          return NOT_FOUND;
        }
        registrationManager.save();
        addActionMessage(getText("admin.organisation.deleted"));
        return SUCCESS;
      } else {
        addActionError(getText("admin.organisation.cantdelete1"));
        addActionError(getText("admin.organisation.cantdelete2"));
      }
    } catch (DeletionNotAllowedException e) {
      addActionError(getText("admin.organisation.deleted.notempty"));
    } catch (IOException e) {
      addActionError("cant save organisation file: " + e.getMessage());
    }
    return INPUT;
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

  public String getRegistryURL() {
    return cfg.getRegistryUrl();
  }

  /**
   * @return the resourceManager
   */
  public ResourceManager getResourceManager() {
    return resourceManager;
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
    linkedOrganisations = registrationManager.listAll();
    if (id != null) {
      // modify existing organisation
      organisation = registrationManager.get(id);
    }
    // if no id was submitted we wanted to create a new organisation
    if (organisation == null) {
      // reset id
      id = null;
      // create new organisation
      organisation = new Organisation();
    }
  }

  @Override
  public String save() {

    try {
      if (id == null) {
        if (registrationManager.get(organisation.getKey()) != null) {
          log.error("The organisation association already exists");
          addActionError(getText("admin.organisation.error.existing"));
          return INPUT;
        }
        registrationManager.addAssociatedOrganisation(organisation);
      }
      addActionMessage(getText("admin.organisation.associated.ipt"));
      registrationManager.save();
      return SUCCESS;
    } catch (IOException e) {
      log.error("The organisation association couldnt be saved: " + e.getMessage(), e);
      addActionError(getText("admin.organisation.error.save"));
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
   * @param resourceManager the resourceManager to set
   */
  public void setResourceManager(ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      organisationValidation.validate(this, organisation);
    }
  }

}
