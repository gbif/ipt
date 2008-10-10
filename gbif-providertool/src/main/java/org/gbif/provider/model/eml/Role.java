package org.gbif.provider.model.eml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Role implements Serializable{
	ORIGINATOR,
	AUTHOR,
	CONTENT_PROVIDER,
	CUSTODIAN_STEWARD,
	DISTRIBUTOR,
	EDITOR,
	METADATA_PROVIDER,
	OWNER,
	POINT_OF_CONTACT,
	PRINCIPAL_INVESTIGATOR,
	PROCESSOR,
	PUBLISHER,
	USER,
	FIELD_STATION_MANAGER,
	INFORMATION_MANAGER;
	
	public static final List<Role> ALL;
	  static  
	  {  
	    List<Role> hosts = new ArrayList<Role>();  
	    hosts.add(ORIGINATOR);
	    hosts.add(AUTHOR);
	    hosts.add(CONTENT_PROVIDER);
	    hosts.add(CUSTODIAN_STEWARD);
	    hosts.add(DISTRIBUTOR);
	    hosts.add(EDITOR);
	    hosts.add(METADATA_PROVIDER);
	    hosts.add(OWNER);
	    hosts.add(POINT_OF_CONTACT);
	    hosts.add(PRINCIPAL_INVESTIGATOR);
	    hosts.add(PROCESSOR);
	    hosts.add(PUBLISHER);
	    hosts.add(USER);
	    hosts.add(FIELD_STATION_MANAGER);
	    hosts.add(INFORMATION_MANAGER); 
	    ALL = Collections.unmodifiableList(hosts);  
	  };
	  
}
