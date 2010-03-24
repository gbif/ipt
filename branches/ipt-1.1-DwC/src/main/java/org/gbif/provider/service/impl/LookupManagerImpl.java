package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.dao.LookupDao;
import org.gbif.provider.model.LabelValue;
import org.gbif.provider.model.Role;
import org.gbif.provider.service.LookupManager;


/**
 * Implementation of LookupManager interface to talk to the persistence layer.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class LookupManagerImpl extends UniversalManagerImpl implements LookupManager {
    private LookupDao dao;

    /**
     * Method that allows setting the DAO to talk to the data store with.
     * @param dao the dao implementation
     */
    public void setLookupDao(LookupDao dao) {
        super.dao = dao;
        this.dao = dao;
    }

    /**
     * {@inheritDoc}
     */
    public List<LabelValue> getAllRoles() {
        List<Role> roles = dao.getRoles();
        List<LabelValue> list = new ArrayList<LabelValue>();

        for (Role role1 : roles) {
            list.add(new LabelValue(role1.getName(), role1.getName()));
        }

        return list;
    }
}
