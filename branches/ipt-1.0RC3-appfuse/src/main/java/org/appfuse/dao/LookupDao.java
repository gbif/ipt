package org.appfuse.dao;

import java.util.List;

import org.gbif.provider.model.Role;

/**
 * Lookup Data Access Object (GenericDao) interface.  This is used to lookup values in
 * the database (i.e. for drop-downs).
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public interface LookupDao extends UniversalDao {
    //~ Methods ================================================================

    /**
     * Returns all Roles ordered by name
     * @return populated list of roles
     */
    List<Role> getRoles();
}
