/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.model.registration.Organisation;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.OrganisationsManager;
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
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author tim
 * @author josecuadra
 */
@Singleton
public class OrganisationsManagerImpl extends BaseManager implements OrganisationsManager {
  public static final String PERSISTENCE_FILE = "organisations.xml";
  private SortedMap<String, Organisation> organisations = new TreeMap<String, Organisation>();
  private final XStream xstream = new XStream();

  public OrganisationsManagerImpl() {
    super();
    defineXstreamMapping();
  }

  public void add(Organisation organisation) {
    if (organisation != null) {
      addOrganisation(organisation);
    }
  }

  public List<Organisation> list() {
    return new ArrayList<Organisation>(organisations.values());
  }

  public void load() throws InvalidConfigException {
    Reader userReader;
    ObjectInputStream in = null;
    try {
      userReader = FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE));
      in = xstream.createObjectInputStream(userReader);
      organisations.clear();
      while (true) {
        try {
          Organisation o = (Organisation) in.readObject();
          addOrganisation(o);
        } catch (EOFException e) {
          // end of file, expected exception!
          break;
        } catch (ClassNotFoundException e) {
          log.error(e.getMessage(), e);
        }
      }
    } catch (FileNotFoundException e) {
      log.debug(e);
      throw new InvalidConfigException(TYPE.ORGANISATION_CONFIG,
          "Couldnt read list of organisations associated to this IPT: " + e.getMessage());
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new InvalidConfigException(TYPE.ORGANISATION_CONFIG,
          "Couldnt read list of organisations associated to this IPT: " + e.getMessage());
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
   * 
   * @see org.gbif.ipt.service.admin.OrganisationManager#save()
   */
  public void save() throws IOException {
    log.debug("Saving all user organisations associated to this IPT...");
    Writer organisationWriter = FileUtils.startNewUtf8File(dataDir.configFile(PERSISTENCE_FILE));
    ObjectOutputStream out = xstream.createObjectOutputStream(organisationWriter, "organisations");
    for (Entry<String, Organisation> entry : organisations.entrySet()) {
      out.writeObject(entry.getValue());
    }
    out.close();
  }

  private Organisation addOrganisation(Organisation organisation) {
    if (organisation != null) {
      log.debug("Adding organisation " + organisation.getKey() + " - " + organisation.getName());
      organisations.put(organisation.getKey(), organisation);
    }
    return organisation;
  }

  private void defineXstreamMapping() {
    xstream.alias("organisation", Organisation.class);
  }
}
