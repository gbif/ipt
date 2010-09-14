/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Registration;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.utils.FileUtils;

import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

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
import java.util.UUID;

/**
 * @author tim
 * @author josecuadra
 */
@Singleton
public class RegistrationManagerImpl extends BaseManager implements RegistrationManager {
  public static final String PERSISTENCE_FILE = "registration.xml";
  private Registration registration = new Registration();
  private final XStream xstream = new XStream();

  public RegistrationManagerImpl() {
    super();
    defineXstreamMapping();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#addAssociatedOrganisation(org.gbif.ipt.model.Organisation)
   */
  public Organisation addAssociatedOrganisation(Organisation organisation) throws AlreadyExistingException {
    if (organisation != null) {
      log.debug("Adding associated organisation " + organisation.getKey() + " - " + organisation.getName());
      registration.getAssociatedOrganisations().put(organisation.getKey().toString(), organisation);
    }
    return organisation;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#addHostingOrganisation(org.gbif.ipt.model.Organisation)
   */
  public Organisation addHostingOrganisation(Organisation organisation) {
    if (organisation != null) {
      log.debug("Adding hosting organisation " + organisation.getKey() + " - " + organisation.getName());
      registration.setHostingOrganisation(organisation);
    }
    return organisation;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#addIptInstance(org.gbif.ipt.model.Ipt)
   */
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

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#delete(java.lang.String)
   */
  public Organisation delete(String key) throws DeletionNotAllowedException {
    if (key != null) {
      Organisation remOrganisation = registration.getAssociatedOrganisations().remove(key);
      // TODO: Check whether the organisation does not have any resources associated
      return remOrganisation;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.OrganisationsManager#get(java.lang.String)
   */
  public Organisation get(String key) {
    if (key == null) {
      return null;
    }
    return registration.getAssociatedOrganisations().get(key);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.OrganisationsManager#get(java.util.UUID)
   */
  public Organisation get(UUID key) {
    if (key == null) {
      return null;
    }
    return registration.getAssociatedOrganisations().get(key.toString());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.OrganisationsManager#getHostingOrganisation()
   */
  public Organisation getHostingOrganisation() {
    return registration.getHostingOrganisation();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#getIpt()
   */
  public Ipt getIpt() {
    return registration.getIpt();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#list()
   */
  public List<Organisation> list() {
    List<Organisation> organisationList = new ArrayList<Organisation>();
    for (Organisation organisation : registration.getAssociatedOrganisations().values()) {
      if (organisation.isCanHost()) {
        organisationList.add(organisation);
      }
    }
    return organisationList;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#listAll()
   */
  public List<Organisation> listAll() {
    return new ArrayList<Organisation>(registration.getAssociatedOrganisations().values());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#load()
   */
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
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    } catch (FileNotFoundException e) {
      log.warn("Registration information not existing, " + PERSISTENCE_FILE + " file missing");
    } catch (ClassNotFoundException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new InvalidConfigException(TYPE.REGISTRATION_CONFIG, "Couldnt read the registration information: "
          + e.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.OrganisationManager#save()
   */
  public void save() throws IOException {
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

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.RegistrationManager#setIptPassword(java.lang.String)
   */
  public void setIptPassword(String password) {
    registration.setIptPassword(password);
  }
}
