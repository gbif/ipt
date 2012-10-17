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

import com.google.common.base.Strings;
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

    // it could be organisations have changed their name or node in the Registry, so update all organisation metadata
    updateAssociatedOrganisationsMetadata();
  }

  /**
   * Update the metadata of each organization that has been added to the IPT with the latest version coming from the
   * Registry.
   */
  private void updateAssociatedOrganisationsMetadata() {
    try {
      // 1. check associated Organisations
      for (Map.Entry<String, Organisation> entry : registration.getAssociatedOrganisations().entrySet()) {
        // search for that particular organisation, has the name changed?
        updateOrganisationMetadata(entry.getValue());
      }

      // 2. check hosting Organisation - IPT Organisation
      Organisation hostingOrganisation = registration.getHostingOrganisation();
      if (hostingOrganisation != null) {
        updateOrganisationMetadata(hostingOrganisation);
      }

      // ensure changes are persisted to registration.xml
      save();
    } catch (IOException e) {
      log.error("A problem occurred saving ");
    }
  }

  /**
   * For a single organization, update its metadata. Only updates the metadata for an organisation coming from the
   * registry, not the metadata set by the IPT administrator like can host data, etc.
   *
   * @param organisation          Organisation
   */
  private void updateOrganisationMetadata(Organisation organisation) {
    if (organisation != null) {
      // the organization key
      String key = (organisation.getKey() == null) ? null : organisation.getKey().toString();

      // retrieve the latest copy of the organisation from the Registry
      Organisation o = registryManager.getRegisteredOrganisation(key);

      if (o != null) {

        String oKey = (o.getKey() == null) ? null : o.getKey().toString();
        String oName = (o.getName() == null) ? null : o.getName();

        // sanity check - only the key must be exactly the same, and at least the name must not be null
        if (oKey != null && key.equalsIgnoreCase(oKey) && !Strings.isNullOrEmpty(oName)) {
          // organisation
          organisation.setName(oName);
          organisation.setDescription((o.getDescription() == null) ? null : o.getDescription());
          organisation.setHomepageURL((o.getHomepageURL() == null) ? null : o.getHomepageURL());
          // organisation node
          organisation.setNodeKey((o.getNodeKey() == null) ? null : o.getNodeKey());
          organisation.setNodeName((o.getNodeName() == null) ? null : o.getNodeName());
          organisation.setNodeContactEmail((o.getNodeContactEmail() == null) ? null : o.getNodeContactEmail());
          // organisation primary contact
          organisation.setPrimaryContactName((o.getPrimaryContactName() == null) ? null : o.getPrimaryContactName());
          organisation.setPrimaryContactFirstName((o.getPrimaryContactFirstName() == null) ? null : o.getPrimaryContactFirstName());
          organisation.setPrimaryContactLastName((o.getPrimaryContactLastName() == null) ? null : o.getPrimaryContactLastName());
          organisation.setPrimaryContactAddress((o.getPrimaryContactAddress() == null) ? null : o.getPrimaryContactAddress());
          organisation.setPrimaryContactDescription((o.getPrimaryContactDescription() == null) ? null : o.getPrimaryContactDescription());
          organisation.setPrimaryContactEmail((o.getPrimaryContactEmail() == null) ? null : o.getPrimaryContactEmail());
          organisation.setPrimaryContactPhone((o.getPrimaryContactPhone() == null) ? null : o.getPrimaryContactPhone());
          organisation.setPrimaryContactType((o.getPrimaryContactType() == null) ? null : o.getPrimaryContactType());
          // replace organisation in list of associated organisations now
          registration.getAssociatedOrganisations().put(key, organisation);
          log.debug("Organisation (" + key + ") updated with latest metadata from Registry");
        } else {
          log.debug("Update of organisation failed: organisation retrieved from Registry was missing name");
        }
      } else {
        log.debug("Update of organisation failed: organisation retrieved from Registry was null");
      }
    } else {
      log.debug("Update of organisation failed: organisation was null");
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
