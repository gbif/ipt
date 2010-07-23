/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.model.registration.Organisation;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.admin.OrganisationManager;
import org.gbif.ipt.utils.FileUtils;

import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * @author tim
 * @author josecuadra
 */
@Singleton
public class OrganisationManagerImpl extends BaseManager implements OrganisationManager {
  public static final String PERSISTENCE_FILE = "organisations.xml";
  private SortedMap<String, Organisation> organisations = new TreeMap<String, Organisation>();
  private final XStream xstream = new XStream();

  public OrganisationManagerImpl() {
    super();
    defineXstreamMapping();
  }

  public void add(Organisation organisation) {
    if (organisation != null) {
      addOrganisation(organisation);
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
