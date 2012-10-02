package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Registration;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.FileUtils;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

@Singleton
public class RegistrationManagerImpl extends BaseManager implements RegistrationManager {

  public static final String PERSISTENCE_FILE = "registration.xml";
  private Registration registration = new Registration();
  private final XStream xstream = new XStream();
  private ResourceManager resourceManager;
  private RegistryManager registryManager;

  @Inject
  public RegistrationManagerImpl(AppConfig cfg, DataDir dataDir, ResourceManager resourceManager,
    RegistryManager registryManager) {
    super(cfg, dataDir);
    this.resourceManager = resourceManager;
    defineXstreamMapping();
    this.registryManager = registryManager;
  }

  public Organisation addAssociatedOrganisation(Organisation organisation) throws AlreadyExistingException {
    if (organisation != null) {
      log.debug("Adding associated organisation " + organisation.getKey() + " - " + organisation.getName());
      registration.getAssociatedOrganisations().put(organisation.getKey().toString(), organisation);
    }
    return organisation;
  }

  public Organisation addHostingOrganisation(Organisation organisation) {
    if (organisation != null) {
      log.debug("Adding hosting organisation " + organisation.getKey() + " - " + organisation.getName());
      registration.setHostingOrganisation(organisation);
    }
    return organisation;
  }

  public void addIptInstance(Ipt ipt) {
    if (ipt != null) {
      if (ipt.getCreated() == null) {
        ipt.setCreated(new Date());
      }
      registration.setIpt(ipt);
    }
  }

  private void defineXstreamMapping() {
    xstream.omitField(Registration.class, "associatedOrganisations");
    xstream.alias("organisation", Organisation.class);
    xstream.alias("registry", Registration.class);
  }

  public Organisation delete(String key) throws DeletionNotAllowedException {
    Organisation org = get(key);
    if (org != null) {
      // Check whether the organisation does not have any resources associated
      for (Resource resource : resourceManager.list()) {
        if (resource.getOrganisation() != null && resource.getOrganisation().equals(org)) {
          throw new DeletionNotAllowedException(Reason.RESOURCE_REGISTERED_WITH_ORGANISATION,
            "Resource " + resource.getShortname() + " associated with organisation");
        }
      }
      // Check that the organization is not the hosting organization (IPT is not registered against the organization)
      Organisation host = registration.getHostingOrganisation();
      if (host != null && host.getKey() != null && host.getKey().toString().equals(key)) {
        throw new DeletionNotAllowedException(Reason.IPT_REGISTERED_WITH_ORGANISATION,
          "The IPT instance is associated with this organisation");
      }
      registration.getAssociatedOrganisations().remove(key);
    }
    return org;
  }

  public Organisation get(String key) {
    if (key == null) {
      return null;
    }
    return registration.getAssociatedOrganisations().get(key);
  }

  public Organisation get(UUID key) {
    if (key == null) {
      return null;
    }
    return registration.getAssociatedOrganisations().get(key.toString());
  }

  public Organisation getHostingOrganisation() {
    return registration.getHostingOrganisation();
  }

  public Ipt getIpt() {
    return registration.getIpt();
  }

  public List<Organisation> list() {
    List<Organisation> organisationList = new ArrayList<Organisation>();
    for (Organisation organisation : registration.getAssociatedOrganisations().values()) {
      if (organisation.isCanHost()) {
        organisationList.add(organisation);
      }
    }
    return organisationList;
  }

  public List<Organisation> listAll() {
    return new ArrayList<Organisation>(registration.getAssociatedOrganisations().values());
  }

  public void load() throws InvalidConfigException {
    Reader registrationReader;
    ObjectInputStream in = null;
    try {
      registrationReader = FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE));
      in = xstream.createObjectInputStream(registrationReader);
      registration.getAssociatedOrganisations().clear();

      try {
        Registration reg = (Registration) in.readObject();
        // load the organisation this IPT is registered against
        addHostingOrganisation(reg.getHostingOrganisation());
        addIptInstance(reg.getIpt());

        // load the associated organisations
        while (true) {
          try {
            Organisation org = (Organisation) in.readObject();
            addAssociatedOrganisation(org);
          } catch (EOFException e) {
            // end of file, expected exception!
            break;
          } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
          }
        }
      } catch (EOFException e) {
        // end of file, expected exception!
      } catch (AlreadyExistingException e) {
        log.error(e);
      }

    } catch (FileNotFoundException e) {
      log.warn("Registration information not existing, " + PERSISTENCE_FILE
        + " file missing  (This is normal when IPT is not registered yet)");
    } catch (ClassNotFoundException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new InvalidConfigException(TYPE.REGISTRATION_CONFIG,
        "Couldnt read the registration information: " + e.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          log.warn(e.getMessage());
        }
      }
    }

    // it could be organisations have changed their name in the Registry, so update all organisation names
    updateOrganisationNames();
  }

  /**
   * Update the name of each organization that has been added to the IPT with the latest version coming from the
   * Registry.
   */
  private void updateOrganisationNames() {
    try {
      // retrieve complete list of Organisations from Registry
      List<Organisation> registryOrganisations = registryManager.getOrganisations();

      // 1. check associated Organisations
      for (Map.Entry<String, Organisation> entry : registration.getAssociatedOrganisations().entrySet()) {
        // search for that particular organisation, has the name changed?
        updateOrganizationName(entry.getValue(), registryOrganisations);
      }

      // 2. check hosting Organisation - IPT Organisation
      Organisation hostingOrganisation = registration.getHostingOrganisation();
      if (hostingOrganisation != null) {
        updateOrganizationName(hostingOrganisation, registryOrganisations);
      }

      // ensure changes are persisted to registration.xml
      save();
    } catch (IOException e) {
      log.error("A problem occurred saving ");
    }
  }

  /**
   * For a single organization, update its name if it has changed. Then, update each resource it is associated with
   * ensuring that the updated name is used.
   *
   * @param organisation          Organisation
   * @param registryOrganisations list of the latest registered organisations from the Registry
   */
  private void updateOrganizationName(Organisation organisation,
    List<Organisation> registryOrganisations) {
    if (organisation != null && registryOrganisations != null) {
      // the organization key
      String key = (organisation.getKey() == null) ? null : organisation.getKey().toString();
      // the old organization name
      String oldName = organisation.getName();
      // the updated organization name
      String newName;

      for (Organisation registered : registryOrganisations) {
        if (key != null && key.equalsIgnoreCase(registered.getKey().toString())) {
          // compare names, if different, perform update
          if (!oldName.equals(registered.getName())) {
            newName = registered.getName();
            organisation.setName(newName);
            registration.getAssociatedOrganisations().put(key, organisation);
            log.debug("Organisation (" + key + ") name updated from " + oldName + " -> " + newName);
            // now, since the name is different, update all resources associated with it
            List<Resource> resources = resourceManager.list();
            for (Resource resource : resources) {
              Organisation resourcesOrganisation = resource.getOrganisation();
              // compare keys, if matched, perform update
              UUID resourcesOrganisationKey = resourcesOrganisation.getKey();
              if (resourcesOrganisationKey != null) {
                if (resourcesOrganisationKey.toString().equalsIgnoreCase(key)) {
                  resourcesOrganisation.setName(newName);
                  resource.setOrganisation(resourcesOrganisation);
                  // ensure the change to the resource is persisted
                  resourceManager.save(resource);
                  log.debug(
                    "Resource (" + resource.getShortname() + ") updated: Organisation (" + key + ") name updated from "
                    + oldName + " -> " + newName);
                }
              }
            }
          }
        }
      }
    } else {
      log.debug("Update of organisation name failed: organisation or list of registered organisations was null");
    }
  }

  public synchronized void save() throws IOException {
    log.debug("Saving all user organisations associated to this IPT...");
    Writer organisationWriter = FileUtils.startNewUtf8File(dataDir.configFile(PERSISTENCE_FILE));
    // registration.setAssociatedOrganisations(new ArrayList<Organisation>(organisations.values()));
    ObjectOutputStream out = xstream.createObjectOutputStream(organisationWriter, "registration");
    // out.writeObject(registration.getAssociatedOrganisations());
    out.writeObject(registration);
    for (Organisation organisation : registration.getAssociatedOrganisations().values()) {
      out.writeObject(organisation);
    }
    out.close();
  }

  public void setIptPassword(String password) {
    registration.setIptPassword(password);
  }
}
