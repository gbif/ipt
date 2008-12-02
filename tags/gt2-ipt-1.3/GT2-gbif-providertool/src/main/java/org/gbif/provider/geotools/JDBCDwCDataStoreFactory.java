package org.gbif.provider.geotools;


import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

/**
 * The simple factory that specifies the connection parameters for the DB
 * @author tim
 */
public class JDBCDwCDataStoreFactory implements DataStoreFactorySpi {

	/**
	 * Returns true 
	 * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
	 */
	public boolean canProcess(Map params) {
		if (params != null 
				&& params.containsKey("datadir"))
			return true;
		else
			return false;
	}

	/**
	 * Always returns a new one
	 * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
	 */
	public DataStore createDataStore(Map params) throws IOException {
		return createNewDataStore(params);
	}

	/**
	 * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public DataStore createNewDataStore(Map params) throws IOException {
		return new JDBCDwCDatastore(params);
	}

	/**
	 * @see org.geotools.data.DataStoreFactorySpi#getDescription()
	 */
	public String getDescription() {
		return "GBIF Integrated Publishing Toolkit H2 Datastore.";
	}

	/**
	 * @see org.geotools.data.DataStoreFactorySpi#getDisplayName()
	 */
	public String getDisplayName() {
		return "GBIF Integrated Publishing Toolkit H2 Datastore";
	}

	/**
	 * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
	 */
	public Param[] getParametersInfo() {
		Param[] p = {
				new Param("datadir", String.class, "IPT Data Directory", true, "tcp:localhost")
//				new Param("user", String.class, "Database user", false, "sa"),
//				new Param("password", String.class, "Database password", false, "")
		};
		return p;
	}

	/**
	 * Always available ja ;o)
	 * TODO: see if it is available perhaps?
	 * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
	 */
	public boolean isAvailable() {
		return true;
	}

	/**
	 * Hints? Nah mate
	 * @see org.geotools.factory.Factory#getImplementationHints()
	 */
	public Map getImplementationHints() {
		return null;
	}
}
